package com.example.location.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.location.model.Offre;

import com.example.location.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditOffre extends AppCompatActivity {
    private EditText editTextTitre, editTextDescription, editTextPrix;
    private Button buttonModifier, buttonOk;
    private FirebaseFirestore db;
    private String agentEmail, documentId;
    private boolean isNewOffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_offre);


        db = FirebaseFirestore.getInstance();

        // Get data from Intent
        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");
        documentId = getIntent().getStringExtra("DOCUMENT_ID");



        editTextTitre = findViewById(R.id.editTextTitre);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrix = findViewById(R.id.editTextPrix);
        buttonModifier = findViewById(R.id.buttonModifier);
        buttonOk = findViewById(R.id.buttonOk);



        loadOffer();

        buttonModifier.setOnClickListener(v -> {
            setFormEnabled(true);
            buttonModifier.setVisibility(View.GONE);
            buttonOk.setVisibility(View.VISIBLE);
        });

        buttonOk.setOnClickListener(v -> saveOffer());
    }

    private void loadOffer() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(documentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Offre offre = documentSnapshot.toObject(Offre.class);
                    if (offre != null) {
                        editTextTitre.setText(offre.getTitre());
                        editTextDescription.setText(offre.getDescription());
                        editTextPrix.setText(String.valueOf(offre.getPrix()));
                    }
                });
    }

    private void saveOffer() {
        String titre = editTextTitre.getText().toString();
        String description = editTextDescription.getText().toString();
        double prix = Double.parseDouble(editTextPrix.getText().toString());

        Offre offre = new Offre();
        offre.setTitre(titre);
        offre.setDescription(description);
        offre.setPrix(prix);


        // Update existing offer
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(documentId)
                .set(offre)
                .addOnSuccessListener(aVoid -> finish());

    }

    private void setFormEnabled(boolean enabled) {
        editTextTitre.setEnabled(enabled);
        editTextDescription.setEnabled(enabled);
        editTextPrix.setEnabled(enabled);
    }
}
