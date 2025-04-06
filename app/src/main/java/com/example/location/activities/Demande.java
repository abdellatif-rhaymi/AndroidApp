package com.example.location.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.location.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Demande extends AppCompatActivity {
    private EditText editDuree, editEnfants, editLoyer, editMessage, editSituationF, editSituationP;
    private Button btnSoumettreDemande;
    private FirebaseFirestore db;
    private String offreId, agentEmail, clientEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demande);

        db = FirebaseFirestore.getInstance();

        // Récupérer les données transmises par l'intent
        offreId = getIntent().getStringExtra("OFFRE_ID");
        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");
        clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Récupérer l'email du client

        // Initialiser les champs
        editDuree = findViewById(R.id.editDuree);
        editEnfants = findViewById(R.id.editEnfants);
        editLoyer = findViewById(R.id.editLoyer);
        editMessage = findViewById(R.id.editMessage);
        editSituationF = findViewById(R.id.editSituationF);
        editSituationP = findViewById(R.id.editSituationP);
        btnSoumettreDemande = findViewById(R.id.btnSoumettreDemande);

        // Gestion du clic sur le bouton "Soumettre"
        btnSoumettreDemande.setOnClickListener(v -> soumettreDemande());
    }

    private void soumettreDemande() {
        String duree = editDuree.getText().toString();
        String enfants = editEnfants.getText().toString();
        String loyer = editLoyer.getText().toString();
        String message = editMessage.getText().toString();
        String situationF = editSituationF.getText().toString();
        String situationP = editSituationP.getText().toString();

        if (duree.isEmpty() || enfants.isEmpty() || loyer.isEmpty() || message.isEmpty() || situationF.isEmpty() || situationP.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        // Création de la demande
        Map<String, Object> demande = new HashMap<>();
        demande.put("clientEmail", clientEmail);
        demande.put("duree", duree);
        demande.put("enfants", enfants);
        demande.put("loyer", loyer);
        demande.put("message", message);
        demande.put("situation_f", situationF);
        demande.put("situation_p", situationP);
        demande.put("timestamp", System.currentTimeMillis());

        // Ajouter la demande dans Firestore
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("demandes")
                .add(demande)
                .addOnSuccessListener(documentReference -> {
                    String demandeId = documentReference.getId(); // Récupérer l'ID du document

                    // Ajouter une référence côté client
                    Map<String, Object> refDemande = new HashMap<>();
                    refDemande.put("reference", demandeId);
                    refDemande.put("offreId", offreId);
                    refDemande.put("agentEmail", agentEmail);
                    refDemande.put("timestamp", System.currentTimeMillis());

                    db.collection("clients")
                            .document(clientEmail)
                            .collection("offres")
                            .document(demandeId)
                            .set(refDemande)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(Demande.this, "Demande envoyée avec succès", Toast.LENGTH_SHORT).show();
                                finish(); // Fermer l'activité
                            })
                            .addOnFailureListener(e -> Toast.makeText(Demande.this, "Erreur côté client", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(Demande.this, "Erreur d'envoi", Toast.LENGTH_SHORT).show());
    }
}
