package controllers;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import models.Association;
import models.Subvention;

import org.apache.commons.io.IOUtils;

import play.Logger;
import play.data.validation.Required;
import play.mvc.Controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Import extends Controller {

    private static String GEO_REVERSE_URL = "http://maps.google.com/maps/geo?output=json";
    private static String GEO_REVERSE_KEY = "aaa";

    public static void page() {
        render();
    }

    public static void execute(@Required File csv, Integer year) {
        Integer nbImportAssoc = 0;
        Integer nbImportSub = 0;
        try {
            FileInputStream fstream = new FileInputStream(csv);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            // Read File Line By Line
            br.readLine();
            while ((strLine = br.readLine()) != null) {
                if (strLine.trim() != "") {
                    // Print the content on the console
                    String[] cells = strLine.split(";");
                    // try to find the association if t exist
                    if (cells[0] != null && !cells[0].isEmpty() && cells[0].trim().length() > 0) {
                        Association assoc = Association.find("siren=?", cells[0]).first();
                        if (assoc == null) {
                            assoc = new Association();
                            assoc.siren = cells[0];
                            assoc.nom = cells[1];
                            assoc.adresse = cells[2];
                            assoc.codePostal = cells[3].split(" ")[0];
                            assoc.ville = cells[3].substring(assoc.codePostal.length() + 1, cells[3].length());
                            assoc.save();
                            geolocalize(assoc);
                            nbImportAssoc++;
                            // Logger.debug("Create association :" + assoc.toString());
                        }
                        if (cells[4] != null && cells[4].trim().length() > 0) {
                            Subvention sb = new Subvention();
                            sb.year = year;
                            sb.montant = Double.parseDouble(cells[4].replaceAll(" ", "").replaceAll(",", "."));
                            sb.association = assoc;
                            sb.save();
                            nbImportSub++;
                            // Logger.debug("Create subvention " + sb.toString() + "for assoc :" + assoc.toString());
                            assoc.save();
                        }
                    }
                }
            }
            // Close the input stream
            in.close();
        } catch (Exception e) {// Catch exception if any
            Logger.error(e, e.getMessage());
        }
        flash.success("Importing " + nbImportAssoc + " associations et " + nbImportSub + " subventions");
        page();
    }

    private static Boolean geolocalize(Association assoc) {
        String adresse = assoc.adresse + "," + assoc.codePostal + " " + assoc.ville + ",FRANCE";
        Boolean isOk = Boolean.FALSE;
        try {
            while (!isOk) {
                isOk = Boolean.TRUE;
                URL url = new URL(GEO_REVERSE_URL + "&q=" + URLEncoder.encode(adresse, "UTF-8") + "&key="
                        + GEO_REVERSE_KEY);
                URLConnection conn = url.openConnection();
                ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
                IOUtils.copy(conn.getInputStream(), output);
                output.close();
                JsonParser parser = new JsonParser();
                JsonObject object = (JsonObject) parser.parse(output.toString());
                JsonElement placemark = object.get("Placemark");
                if (placemark != null) {
                    String text = placemark.toString();
                    String result[] = text.replaceAll("(.*)\"coordinates\":\\[(.*)\\]\\}\\}\\]", "$2").split(",");
                    if (result.length >= 2 & result[0] != null && result[1] != null) {
                        assoc.longitude = Double.valueOf(result[0]);
                        assoc.latitude = Double.valueOf(result[1]);
                        assoc.save();
                    }
                    else {
                        Logger.error("Error of geolocalisation  (" + url.toString() + "): " + output.toString());
                        if (output.toString().contains("\"code\": 620,")) {
                            isOk = Boolean.FALSE;
                            Thread.sleep(1000);
                        }
                    }
                }
                else {
                    Logger.error("Error of geolocalisation  (" + url.toString() + "): " + output.toString());
                    if (output.toString().contains("\"code\": 620,")) {
                        isOk = Boolean.FALSE;
                        Thread.sleep(1000);
                    }
                }
            }
        } catch (Exception e) {
            Logger.error(e, e.getMessage());
        }
        return null;

    }
}
