package controllers;

import java.util.List;

import models.Association;
import play.mvc.Controller;

public class Carte extends Controller {

    public static void carte() {
        List<Association> associations = Association.find("longitude IS NOT NULL").fetch();
        render(associations);
    }
}
