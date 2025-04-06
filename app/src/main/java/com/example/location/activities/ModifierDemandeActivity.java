package com.example.location.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.location.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ModifierDemandeActivity extends AppCompatActivity {

    private static final String TAG = "ModifierDemande";

    private EditText editDuree, editEnfants, editLoyerMax, editMessage, editSituationP, editSituationF;
    private Button btnEnregistrer;
    private ProgressBar progressBar;

    private String demandeId, offreId, agentEmail, clientEmail;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_demande);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();
        clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Lier les vues
        editDuree = findViewById(R.id.editDuree);
        editEnfants = findViewById(R.id.editEnfants);
        editLoyerMax = findViewById(R.id.editLoyerMax);
        editMessage = findViewById(R.id.editMessage);
        editSituationP = findViewById(R.id.editSituationP);
        editSituationF = findViewById(R.id.editSituationF);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);
        progressBar = findViewById(R.id.progressBar);

        // Récupérer les données de l'intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            demandeId = extras.getString("DEMANDE_ID");
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");

            // Charger les données actuelles de la demande
            chargerDonneesDemande();
        } else {
            Toast.makeText(this, "Erreur: Informations manquantes", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configuration du bouton d'enregistrement
        btnEnregistrer.setOnClickListener(v -> enregistrerModifications());
    }

    private void chargerDonneesDemande() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("demandes")
                .document(demandeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        // Remplir les champs avec les données existantes
                        String duree = documentSnapshot.getString("duree");
                        String enfants = documentSnapshot.getString("enfants");
                        String loyer = documentSnapshot.getString("loyer");
                        String message = documentSnapshot.getString("message");
                        String situationF = documentSnapshot.getString("situation_f");
                        String situationP = documentSnapshot.getString("situation_p");

                        if (duree != null) editDuree.setText(duree);
                        if (enfants != null) editEnfants.setText(enfants);
                        if (loyer != null) editLoyerMax.setText(loyer);
                        if (message != null) editMessage.setText(message);
                        if (situationF != null) editSituationF.setText(situationF);
                        if (situationP != null) editSituationP.setText(situationP);
                    } else {
                        Toast.makeText(ModifierDemandeActivity.this, "Demande introuvable", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Erreur lors du chargement de la demande", e);
                    Toast.makeText(ModifierDemandeActivity.this, "Erreur lors du chargement de la demande", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void enregistrerModifications() {
        progressBar.setVisibility(View.VISIBLE);
        btnEnregistrer.setEnabled(false);

        // Récupérer les valeurs des champs
        String duree = editDuree.getText().toString().trim();
        String enfants = editEnfants.getText().toString().trim();
        String loyerMax = editLoyerMax.getText().toString().trim();
        String message = editMessage.getText().toString().trim();
        String situationP = editSituationP.getText().toString().trim();
        String situationF = editSituationF.getText().toString().trim();

        // Créer un Map pour les mises à jour
        Map<String, Object> updatesPourAgent = new HashMap<>();
        updatesPourAgent.put("duree", duree);
        updatesPourAgent.put("enfants", enfants);
        updatesPourAgent.put("loyer", loyerMax);
        updatesPourAgent.put("message", message);
        updatesPourAgent.put("situation_p", situationP);
        updatesPourAgent.put("situation_f", situationF);

        // Mettre à jour dans la collection de l'agent
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("demandes")
                .document(demandeId)
                .update(updatesPourAgent)
                .addOnSuccessListener(aVoid -> {
                    // Si nécessaire, mettre à jour des informations dans la collection du client
                    db.collection("clients")
                            .document(clientEmail)
                            .collection("offres")
                            .document(demandeId)
                            .update("modifieLe", System.currentTimeMillis())
                            .addOnSuccessListener(aVoid2 -> {
                                progressBar.setVisibility(View.GONE);
                                Toast.makeText(ModifierDemandeActivity.this, "Demande mise à jour avec succès", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                btnEnregistrer.setEnabled(true);
                                Log.e(TAG, "Erreur lors de la mise à jour côté client", e);
                                Toast.makeText(ModifierDemandeActivity.this, "Mise à jour partielle", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnEnregistrer.setEnabled(true);
                    Log.e(TAG, "Erreur lors de la mise à jour côté agent", e);
                    Toast.makeText(ModifierDemandeActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                });
    }
}