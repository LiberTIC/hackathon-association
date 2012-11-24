package controllers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import models.Association;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Finally;

public class Carte extends Controller {

    public static void carte() {
        List<Association> associations = Association.find("longitude IS NOT NULL").fetch();
        render(associations);
    }

    @Finally
    static void log() {
        try {
            Logger.info("Response contains : " + response.out);
            ByteArrayOutputStream resp = new ByteArrayOutputStream();
            resp.write("coucou".getBytes());
            response.out = resp;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
