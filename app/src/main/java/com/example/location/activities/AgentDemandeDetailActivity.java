package com.example.location.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.location.R;
import com.example.location.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class AgentDemandeDetailActivity extends AppCompatActivity {

    private static final String TAG = "AgentDemandeDetail";
    public static final int REQUEST_CALL_PHONE = 1;

    // Vues pour le client
    private TextView textClientName, textClientEmail, textClientPhone;
    private TextView textClientAdresse, textClientVille, textClientPays;

    // Vues pour la demande
    private TextView textReference, textDuree, textEnfants;
    private TextView textLoyerMax, textMessage, textSituationP, textSituationF;
    private TextView textStatus;

    // Vues pour l'offre (propriété)
    private ImageView imageOffre;
    private TextView textTitre, textPrix, textDescription;
    private TextView textEtage, textLoyer, textPieces, textSdb, textSuperficie;

    // Boutons
    private Button btnAccepter, btnRefuser, btnAppeler;

    // Données
    private String demandeId, offreId, clientEmail, agentEmail, clientPhone;
    private FirebaseFirestore db;
    private String statusActuel = "En attente";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_agent_demande_detail);

            // Initialiser Firestore
            db = FirebaseFirestore.getInstance();
            agentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            // Récupérer les données de l'intent
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                demandeId = extras.getString("DEMANDE_ID");
                offreId = extras.getString("OFFRE_ID");
                clientEmail = extras.getString("CLIENT_EMAIL");

                if (demandeId == null || offreId == null || clientEmail == null) {
                    Toast.makeText(this, "Erreur: Informations manquantes", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Paramètres manquants: DEMANDE_ID=" + demandeId + ", OFFRE_ID=" + offreId + ", CLIENT_EMAIL=" + clientEmail);
                    finish();
                    return;
                }

                // Initialiser les vues
                initViews();

                // Charger les détails
                loadDemandeDetails();
                loadOffreDetails();
                loadClientDetails();
            } else {
                Toast.makeText(this, "Erreur: Informations manquantes", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate", e);
            Toast.makeText(this, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initViews() {
        try {
            // Vues du client
            textClientName = findViewById(R.id.textClientName);
            textClientEmail = findViewById(R.id.textClientEmail);
            textClientPhone = findViewById(R.id.textClientPhone);
            textClientAdresse = findViewById(R.id.textClientAdresse);
            textClientVille = findViewById(R.id.textClientVille);
            textClientPays = findViewById(R.id.textClientPays);

            // Vues de la demande
            textReference = findViewById(R.id.textReference);
            textDuree = findViewById(R.id.textDuree);
            textEnfants = findViewById(R.id.textEnfants);
            textLoyerMax = findViewById(R.id.textLoyerMax);
            textMessage = findViewById(R.id.textMessage);
            textSituationP = findViewById(R.id.textSituationP);
            textSituationF = findViewById(R.id.textSituationF);
            textStatus = findViewById(R.id.textStatus);

            // Vues de l'offre
            imageOffre = findViewById(R.id.imageOffre);
            textTitre = findViewById(R.id.textTitre);
            textPrix = findViewById(R.id.textPrix);
            textDescription = findViewById(R.id.textDescription);
            textEtage = findViewById(R.id.textEtage);
            textLoyer = findViewById(R.id.textLoyer);
            textPieces = findViewById(R.id.textPieces);
            textSdb = findViewById(R.id.textSdb);
            textSuperficie = findViewById(R.id.textSuperficie);

            // Boutons
            btnAccepter = findViewById(R.id.btnAccepter);
            btnRefuser = findViewById(R.id.btnRefuser);
            btnAppeler = findViewById(R.id.btnAppeler);

            // Configuration des listeners
            btnAccepter.setOnClickListener(v -> accepterDemande());
            btnRefuser.setOnClickListener(v -> refuserDemande());
            btnAppeler.setOnClickListener(v -> appelerClient());
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans initViews", e);
            Toast.makeText(this, "Erreur d'initialisation des vues", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadClientDetails() {
        try {
            db.collection("clients")
                    .document(clientEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile client = documentSnapshot.toObject(UserProfile.class);
                            if (client != null) {
                                // Mise à jour des vues
                                String nomComplet = (client.getNom() != null ? client.getNom() : "") + " " +
                                        (client.getPrenom() != null ? client.getPrenom() : "");
                                textClientName.setText(nomComplet.trim());
                                textClientEmail.setText(client.getEmail() != null ? client.getEmail() : clientEmail);

                                clientPhone = client.getTelephone();
                                if (clientPhone != null && !clientPhone.isEmpty()) {
                                    textClientPhone.setText(clientPhone);
                                    btnAppeler.setVisibility(View.VISIBLE);
                                } else {
                                    textClientPhone.setText("Non disponible");
                                    btnAppeler.setVisibility(View.GONE);
                                }

                                textClientAdresse.setText(client.getAdresse() != null ? client.getAdresse() : "Non spécifiée");
                                textClientVille.setText(client.getVille() != null ? client.getVille() : "Non spécifiée");
                                textClientPays.setText(client.getPays() != null ? client.getPays() : "Non spécifié");
                            }
                        } else {
                            textClientName.setText("Client inconnu");
                            textClientEmail.setText(clientEmail);
                            textClientPhone.setText("Non disponible");
                            textClientAdresse.setText("Non disponible");
                            textClientVille.setText("Non disponible");
                            textClientPays.setText("Non disponible");

                            btnAppeler.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du chargement des infos client", e);
                        Toast.makeText(this, "Impossible de charger les informations du client", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadClientDetails", e);
        }
    }

    private void loadDemandeDetails() {
        try {
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("demandes")
                    .document(demandeId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            // Mise à jour des vues avec les données
                            textReference.setText("Référence: " + document.getId());

                            String duree = document.getString("duree");
                            textDuree.setText("Durée souhaitée: " + (duree != null ? duree : "Non spécifiée"));

                            String enfants = document.getString("enfants");
                            textEnfants.setText("Nombre d'enfants: " + (enfants != null ? enfants : "0"));

                            String loyer = document.getString("loyer");
                            textLoyerMax.setText("Budget max: " + (loyer != null ? loyer : "Non spécifié") + " MAD");

                            String message = document.getString("message");
                            textMessage.setText("Message: " + (message != null ? message : "Aucun message"));

                            String situationP = document.getString("situation_p");
                            textSituationP.setText("Situation professionnelle: " + (situationP != null ? situationP : "Non spécifiée"));

                            String situationF = document.getString("situation_f");
                            textSituationF.setText("Situation financière: " + (situationF != null ? situationF : "Non spécifiée"));

                            String status = document.getString("status");
                            statusActuel = status != null ? status : "En attente";
                            textStatus.setText("Statut: " + statusActuel);

                            // Mettre à jour l'interface en fonction du statut
                            updateUIBasedOnStatus();
                        } else {
                            Toast.makeText(AgentDemandeDetailActivity.this, "Demande introuvable", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du chargement de la demande", e);
                        Toast.makeText(AgentDemandeDetailActivity.this, "Erreur lors du chargement de la demande", Toast.LENGTH_SHORT).show();
                        finish();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadDemandeDetails", e);
            Toast.makeText(this, "Erreur de chargement des détails", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadOffreDetails() {
        try {
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            String titre = document.getString("titre");
                            String description = document.getString("description");
                            Double prix = document.getDouble("prix");
                            Integer etage = document.getLong("etage") != null ? document.getLong("etage").intValue() : 0;
                            Double loyer = document.getDouble("loyer");
                            Integer pieces = document.getLong("pieces") != null ? document.getLong("pieces").intValue() : 0;
                            Integer sdb = document.getLong("sdb") != null ? document.getLong("sdb").intValue() : 0;
                            Double superficie = document.getDouble("superficie");
                            String photoNom = document.getString("photo");

                            // Mise à jour des vues
                            textTitre.setText(titre != null ? titre : "Sans titre");
                            textDescription.setText(description != null ? description : "Aucune description");
                            textPrix.setText("Prix: " + (prix != null ? prix : 0) + " MAD");
                            textEtage.setText("Étage: " + etage);
                            textLoyer.setText("Loyer: " + (loyer != null ? loyer : 0) + " MAD");
                            textPieces.setText("Pièces: " + pieces);
                            textSdb.setText("Salles de bain: " + sdb);
                            textSuperficie.setText("Superficie: " + (superficie != null ? superficie : 0) + " m²");

                            // Charger l'image
                            loadImage(photoNom);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du chargement de l'offre", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadOffreDetails", e);
        }
    }

    private void loadImage(String photoNom) {
        try {
            if (photoNom != null && !photoNom.isEmpty()) {
                if (photoNom.startsWith("http")) {
                    // Chargement depuis une URL
                    Glide.with(this).load(photoNom).into(imageOffre);
                } else {
                    // Charger depuis le stockage interne
                    File imageFile = new File(getExternalFilesDir(null), "images/" + photoNom);
                    if (imageFile.exists()) {
                        Glide.with(this)
                                .load(imageFile)
                                .into(imageOffre);
                    } else {
                        Log.e(TAG, "Fichier non trouvé : " + imageFile.getAbsolutePath());
                        imageOffre.setImageResource(R.drawable.placeholder_image);
                    }
                }
            } else {
                imageOffre.setImageResource(R.drawable.placeholder_image);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadImage", e);
            imageOffre.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void updateUIBasedOnStatus() {
        try {
            // Mettre à jour les boutons en fonction du statut actuel
            if (statusActuel.equalsIgnoreCase("En attente")) {
                btnAccepter.setVisibility(View.VISIBLE);
                btnRefuser.setVisibility(View.VISIBLE);
            } else {
                btnAccepter.setVisibility(View.GONE);
                btnRefuser.setVisibility(View.GONE);
            }

            // Mettre à jour la couleur du texte de statut
            if (statusActuel.equalsIgnoreCase("Acceptée")) {
                textStatus.setTextColor(ContextCompat.getColor(this, R.color.green));
            } else if (statusActuel.equalsIgnoreCase("Refusée")) {
                textStatus.setTextColor(ContextCompat.getColor(this, R.color.red));
            } else {
                textStatus.setTextColor(ContextCompat.getColor(this, R.color.blue));
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans updateUIBasedOnStatus", e);
        }
    }

    private void accepterDemande() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous accepter cette demande ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    updateDemandeStatus("Acceptée");
                    if (clientPhone != null && !clientPhone.isEmpty()) {
                        checkCallPermission();
                    }
                })
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void refuserDemande() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment refuser cette demande ?")
                .setPositiveButton("Oui", (dialog, which) -> updateDemandeStatus("Refusée"))
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateDemandeStatus(String newStatus) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);

            // Mettre à jour le statut dans la collection de l'agent
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("demandes")
                    .document(demandeId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AgentDemandeDetailActivity.this, "Statut mis à jour : " + newStatus, Toast.LENGTH_SHORT).show();
                        statusActuel = newStatus;
                        textStatus.setText("Statut: " + statusActuel);
                        updateUIBasedOnStatus();

                        // Mettre à jour la collection du client
                        updateClientDemande(newStatus);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour du statut", e);
                        Toast.makeText(AgentDemandeDetailActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans updateDemandeStatus", e);
            Toast.makeText(AgentDemandeDetailActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateClientDemande(String newStatus) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", newStatus);

            db.collection("clients")
                    .document(clientEmail)
                    .collection("offres")
                    .document(demandeId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Statut mis à jour pour le client");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour du statut pour le client", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans updateClientDemande", e);
        }
    }

    private void appelerClient() {
        if (clientPhone != null && !clientPhone.isEmpty()) {
            checkCallPermission();
        } else {
            Toast.makeText(this, "Numéro de téléphone non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkCallPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
        } else {
            makePhoneCall();
        }
    }

    private void makePhoneCall() {
        try {
            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + clientPhone));
            startActivity(callIntent);
        } catch (SecurityException se) {
            Log.e(TAG, "Permission d'appel manquante", se);
            Toast.makeText(this, "Permission d'appel manquante", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors de l'appel téléphonique", e);
            Toast.makeText(this, "Impossible de passer l'appel", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL_PHONE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Nettoyer les ressources si nécessaire
        super.onDestroy();
    }
}