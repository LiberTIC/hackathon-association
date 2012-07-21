package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import play.db.jpa.Model;

import com.google.gson.Gson;

@Entity
public class Subvention extends Model {

    public Integer     year;

    public Double      montant;

    @ManyToOne
    public Association association;

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
