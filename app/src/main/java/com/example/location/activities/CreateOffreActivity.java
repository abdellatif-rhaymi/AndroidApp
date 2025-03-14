package com.example.location.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.location.R;
import com.example.location.model.Offre;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateOffreActivity extends AppCompatActivity {

    private EditText editTextTitre, editTextDescription, editTextPrix;
    private Button buttonCreateOffre;
    private FirebaseFirestore db;
    private String agentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_offre);


        db = FirebaseFirestore.getInstance();


        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");


        editTextTitre = findViewById(R.id.editTextTitre);
        editTextDescription = findViewById(R.id.editTextDescription);
        editTextPrix = findViewById(R.id.editTextPrix);
        buttonCreateOffre = findViewById(R.id.buttonCreateOffre);


        buttonCreateOffre.setOnClickListener(v -> createOffre());
    }

    private void createOffre() {
        String titre = editTextTitre.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        double prix = Double.parseDouble(editTextPrix.getText().toString().trim());


        Offre offre = new Offre();
        //offre.setTitre(titre);
        //offre.setDescription(description);
        //offre.setPrix(prix);


        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .add(offre)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Offer created successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error creating offer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}