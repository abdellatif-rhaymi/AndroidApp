package com.example.location.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.location.R;
import com.example.location.adapters.OffreAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.location.model.Offre;

public class Offres extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OffreAdapter adapter;
    private List<Offre> offresList = new ArrayList<>();
    private List<Offre> filteredOffresList = new ArrayList<>();
    private FirebaseFirestore db;
    private String agentEmail;
    private FloatingActionButton fabAddOffre;
    private EditText editTextFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offres);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();
        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");

        // Configurer RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOffres);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OffreAdapter(filteredOffresList, this, false);
        recyclerView.setAdapter(adapter);

        // Configuration du bouton Add Offer
        fabAddOffre = findViewById(R.id.buttonAddOffre);
        fabAddOffre.setOnClickListener(view -> {
            // Lancer l'activité pour ajouter une nouvelle offre
            Intent intent = new Intent(Offres.this, AddOffreActivity.class);
            intent.putExtra("AGENT_EMAIL", agentEmail);
            startActivity(intent);
        });

        // Configuration du filtrage
        editTextFilter = findViewById(R.id.editTextFilterOffres);
        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Non utilisé
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filtrer les offres à chaque changement de texte
                filterOffres(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Non utilisé
            }
        });

        // Charger les offres
        fetchOffres();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les données à chaque fois que l'activité revient au premier plan
        fetchOffres();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchOffres() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        offresList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Offre offre = document.toObject(Offre.class);
                            offre.setId(document.getId()); // Set the Firestore document ID
                            offre.setAgentEmail(agentEmail); // Set the agent's email
                            offresList.add(offre);
                        }
                        // Mettre à jour la liste filtrée après avoir chargé les données
                        filterOffres(editTextFilter.getText().toString());
                    } else {
                        Log.e("OffresActivity", "Erreur Firestore", task.getException());
                        Toast.makeText(Offres.this, "Erreur lors du chargement des offres", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void filterOffres(String filterText) {
        filteredOffresList.clear();

        if (filterText.isEmpty()) {
            // Si le filtre est vide, afficher toutes les offres
            filteredOffresList.addAll(offresList);
        } else {
            // Sinon, filtrer les offres selon le texte entré
            String filterLowerCase = filterText.toLowerCase();
            for (Offre offre : offresList) {
                // Vous pouvez ajuster les critères de filtrage selon vos besoins
                // Par exemple, filtrer par titre, description, etc.
                if (offre.getTitre() != null && offre.getTitre().toLowerCase().contains(filterLowerCase) ||
                        offre.getDescription() != null && offre.getDescription().toLowerCase().contains(filterLowerCase)
                        ) {
                    filteredOffresList.add(offre);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}