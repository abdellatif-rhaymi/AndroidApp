package com.example.location.model;

public class Demande {
    private String id;
    private String reference;
    private String offreId;
    private String offreTitle;
    private String agentEmail;
    private String clientEmail;
    private String clientPhone;
    private String clientName;
    private String clientAdresse;
    private String clientVille;
    private String clientPays;
    private String duree;
    private String enfants;
    private String loyer;
    private String message;
    private String situationP;
    private String situationF;
    private String status;;

    // Constructeur vide requis pour Firestore
    public Demande() {
    }

    // Getters et Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getOffreId() {
        return offreId;
    }

    public void setOffreId(String offreId) {
        this.offreId = offreId;
    }

    public String getOffreTitle() {
        return offreTitle;
    }

    public void setOffreTitle(String offreTitle) {
        this.offreTitle = offreTitle;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public String getClientEmail() {
        return clientEmail;
    }

    public void setClientEmail(String clientEmail) {
        this.clientEmail = clientEmail;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientAdresse() {
        return clientAdresse;
    }

    public void setClientAdresse(String clientAdresse) {
        this.clientAdresse = clientAdresse;
    }

    public String getClientVille() {
        return clientVille;
    }

    public void setClientVille(String clientVille) {
        this.clientVille = clientVille;
    }

    public String getClientPays() {
        return clientPays;
    }

    public void setClientPays(String clientPays) {
        this.clientPays = clientPays;
    }

    public String getDuree() {
        return duree;
    }

    public void setDuree(String duree) {
        this.duree = duree;
    }

    public String getEnfants() {
        return enfants;
    }

    public void setEnfants(String enfants) {
        this.enfants = enfants;
    }

    public String getLoyer() {
        return loyer;
    }

    public void setLoyer(String loyer) {
        this.loyer = loyer;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSituationP() {
        return situationP;
    }

    public void setSituationP(String situationP) {
        this.situationP = situationP;
    }

    public String getSituationF() {
        return situationF;
    }

    public void setSituationF(String situationF) {
        this.situationF = situationF;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}