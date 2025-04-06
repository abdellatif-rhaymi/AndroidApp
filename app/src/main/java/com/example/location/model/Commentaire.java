package com.example.location.model;

import java.util.Date;

public class Commentaire {
    private String client;
    private String texte;
    private Date date;
    private String id;
    private String commentaire;

    // Constructeur vide nécessaire pour Firestore
    public Commentaire() {
    }

    public Commentaire(String client, String texte, Date date) {
        this.client = client;
        this.texte = texte;
        this.date = date;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getTexte() {
        return texte;
    }

    public void setTexte(String texte) {
        this.texte = texte;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }



    public Commentaire(String id, String client, String commentaire) {
        this.id = id;
        this.client = client;
        this.commentaire = commentaire;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }
}