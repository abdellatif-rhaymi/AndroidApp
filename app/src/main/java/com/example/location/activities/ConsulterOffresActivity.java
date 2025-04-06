package com.example.location.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.location.R;
import com.example.location.adapters.OffreAdapter;
import com.example.location.model.Offre;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ConsulterOffresActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OffreAdapter adapter;
    private List<Offre> offresList = new ArrayList<>();
    private List<Offre> filteredOffresList = new ArrayList<>();

    private FirebaseFirestore db;
    private EditText editTextFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulter_offres);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();

        // Configurer RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOffres);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Important: Use filteredOffresList with the adapter, not offresList
        adapter = new OffreAdapter(filteredOffresList, this, true); // 🔥 Mode client activé
        recyclerView.setAdapter(adapter);

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

        // Charger les offres de tous les agents
        fetchAllOffres();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when activity is resumed if needed
        // fetchAllOffres();
    }

    private void fetchAllOffres() {
        offresList.clear();
        db.collection("agents").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalAgents = task.getResult().size();
                final int[] agentsProcessed = {0}; // Compteur pour suivre le nombre d'agents traités

                for (QueryDocumentSnapshot agentDoc : task.getResult()) {
                    String agentEmail = agentDoc.getId();

                    db.collection("agents").document(agentEmail).collection("offres")
                            .get()
                            .addOnCompleteListener(offreTask -> {
                                if (offreTask.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : offreTask.getResult()) {
                                        Offre offre = document.toObject(Offre.class);
                                        offre.setId(document.getId());
                                        offre.setAgentEmail(agentEmail);
                                        offresList.add(offre);
                                    }
                                }

                                // Augmenter le compteur d'agents traités
                                agentsProcessed[0]++;

                                // Mettre à jour l'adaptateur seulement lorsque tous les agents ont été traités
                                if (agentsProcessed[0] == totalAgents) {
                                    // Apply any active filter once all data is loaded
                                    filterOffres(editTextFilter.getText().toString());
                                }
                            });
                }
            } else {
                Toast.makeText(ConsulterOffresActivity.this, "Erreur lors du chargement des agents", Toast.LENGTH_SHORT).show();
                Log.e("ConsulterOffres", "Erreur Firestore", task.getException());
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
                // Filtrer par titre, description, ville, prix, etc.
                if ((offre.getTitre() != null && offre.getTitre().toLowerCase().contains(filterLowerCase)) ||
                        (offre.getDescription() != null && offre.getDescription().toLowerCase().contains(filterLowerCase)))
                {
                    filteredOffresList.add(offre);
                }
            }
        }

        adapter.notifyDataSetChanged();
    }
}