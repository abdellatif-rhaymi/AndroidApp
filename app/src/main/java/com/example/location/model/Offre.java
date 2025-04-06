package com.example.location.model;

public class Offre {
    private String id; // New field to store the document ID
    private String agentEmail; // New field to store the agent's email

    private String titre;
    private String description;
    private int etage;
    private double loyer;
    private String photo;
    private int pieces;
    private double prix;
    private int sdb;
    private double superficie;

    public Offre() {} // 🔥 Obligatoire pour Firestore

    public Offre(String id, String agentEmail, String titre, String description, int etage,
                 double loyer, String photo, int pieces, double prix, int sdb, double superficie) {
        this.id = id;
        this.agentEmail = agentEmail;
        this.titre = titre;
        this.description = description;
        this.etage = etage;
        this.loyer = loyer;
        this.photo = photo;
        this.pieces = pieces;
        this.prix = prix;
        this.sdb = sdb;
        this.superficie = superficie;
    }
    public Offre( String titre, String description, int etage,
                 double loyer, String photo, int pieces, double prix, int sdb, double superficie) {
      
        this.titre = titre;
        this.description = description;
        this.etage = etage;
        this.loyer = loyer;
        this.photo = photo;
        this.pieces = pieces;
        this.prix = prix;
        this.sdb = sdb;
        this.superficie = superficie;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }

    public double getLoyer() { return loyer; }
    public void setLoyer(double loyer) { this.loyer = loyer; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public int getPieces() { return pieces; }
    public void setPieces(int pieces) { this.pieces = pieces; }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }
    public int getSdb() { return sdb; }
    public void setSdb(int sdb) { this.sdb = sdb; }

    public double getSuperficie() { return superficie; }
    public void setSuperficie(double superficie) { this.superficie = superficie; }


}
