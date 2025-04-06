package com.example.location.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.location.R;
import com.example.location.model.Offre;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class Offres2 extends AppCompatActivity {

    private LinearLayout offresContainer;
    private FirebaseFirestore db;
    private Button buttonAddOffre;
    private String agentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offres);
        buttonAddOffre = findViewById(R.id.buttonAddOffre);
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get agent's email from Intent
        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");

        // Initialize UI components
       // offresContainer = findViewById(R.id.offresContainer);

        // Fetch offers from Firestore
        fetchOffres();

        // Handle "Add Offer" button click

        buttonAddOffre.setOnClickListener(v -> {
            Intent intent = new Intent(Offres2.this, CreateOffreActivity.class);
            intent.putExtra("AGENT_EMAIL", agentEmail);
            startActivity(intent);
        });
    }

    private void fetchOffres() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        offresContainer.removeAllViews();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Offre offre = document.toObject(Offre.class);
                            addOffreToView(offre, document.getId());
                        }
                    } else {
                        Log.e("OffresActivity", "Error fetching offres: ", task.getException());
                    }
                });
    }

    private void addOffreToView(Offre offre, String documentId) {

        TextView offreView = new TextView(this);
        offreView.setText(
                "Title: " + offre.getTitre() + "\n" +
                        "Description: " + offre.getDescription() + "\n" +
                        "Price: " + offre.getPrix() + "MAD"
        );
        offreView.setPadding(16, 16, 16, 16);
        offreView.setTextSize(16);


        offreView.setOnClickListener(v -> {
            Intent intent = new Intent(Offres2.this, EditOffre.class);
            intent.putExtra("AGENT_EMAIL", agentEmail);
            intent.putExtra("DOCUMENT_ID", documentId);
            startActivity(intent);
        });


        offresContainer.addView(offreView);


        View divider = new View(this);
        divider.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
        ));
        divider.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
        offresContainer.addView(divider);
    }
}