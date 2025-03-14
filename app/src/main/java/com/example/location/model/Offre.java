package com.example.location.model;

public class Offre {
    private String titre;
    private String description;
    private double prix;

    // Empty constructor required for Firestore
    public Offre() {}

    public Offre(String titre, String description, double prix) {
    }

    // Getters and setters
    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
}