package models;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

import play.db.jpa.Model;

import com.google.gson.Gson;

@Entity
public class Association extends Model {

    public String           siren;
    public String           nom;
    public String           adresse;
    public String           ville;
    public String           codePostal;
    public Double           longitude;
    public Double           latitude;

    @OneToMany(mappedBy = "association", cascade = CascadeType.ALL)
    public List<Subvention> subventions = new ArrayList<Subvention>();

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
