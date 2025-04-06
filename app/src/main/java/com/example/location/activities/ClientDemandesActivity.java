package com.example.location.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.location.R;
import com.example.location.adapters.DemandeAdapter;
import com.example.location.model.Demande;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ClientDemandesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DemandeAdapter adapter;
    private List<Demande> demandeList = new ArrayList<>();
    private FirebaseFirestore db;
    private String clientEmail;
    private ProgressBar progressBar;
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_demandes);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Référencer les vues
        recyclerView = findViewById(R.id.recyclerViewDemandes);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);

        // Configurer RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DemandeAdapter(demandeList, this);
        recyclerView.setAdapter(adapter);

        // Charger les demandes
        fetchClientDemandes();
    }
    private void fetchClientDemandes() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        emptyView.setVisibility(View.GONE);

        db.collection("clients")
                .document(clientEmail)
                .collection("offres")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        demandeList.clear();

                        // Compteur pour suivre le nombre de demandes traitées
                        AtomicInteger demandesRestantes = new AtomicInteger(task.getResult().size());

                        if (task.getResult().size() == 0) {
                            // Aucune demande trouvée
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            return;
                        }

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Demande demande = document.toObject(Demande.class);
                            demande.setId(document.getId());

                            // Si la référence n'est pas correctement mappée, utiliser l'ID du document
                            if (demande.getReference() == null || demande.getReference().isEmpty()) {
                                demande.setReference(document.getId());
                            }

                            demandeList.add(demande);

                            // Pour chaque demande, récupérer les détails supplémentaires
                            fetchDemandeDetails(demande, () -> {
                                // Décrémenter le compteur de demandes restantes
                                int restantes = demandesRestantes.decrementAndGet();

                                // Si toutes les demandes sont traitées, trier et mettre à jour l'UI
                                if (restantes == 0) {
                                    // Trier les demandes par statut
                                    trierDemandesParStatut();

                                    // Mettre à jour l'interface
                                    progressBar.setVisibility(View.GONE);
                                    if (demandeList.isEmpty()) {
                                        emptyView.setVisibility(View.VISIBLE);
                                    } else {
                                        recyclerView.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        }
                    } else {
                        Log.e("ClientDemandesActivity", "Erreur Firestore", task.getException());
                        Toast.makeText(ClientDemandesActivity.this, "Erreur lors du chargement des demandes", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void fetchDemandeDetails(Demande demande, Runnable onComplete) {
        // Récupérer les détails de la demande depuis la collection de l'agent
        db.collection("agents")
                .document(demande.getAgentEmail())
                .collection("offres")
                .document(demande.getOffreId())
                .collection("demandes")
                .document(demande.getReference())
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // Mettre à jour les informations de la demande avec les informations complètes
                        String duree = document.getString("duree");
                        String enfants = document.getString("enfants");
                        String loyer = document.getString("loyer");
                        String message = document.getString("message");
                        String situationF = document.getString("situation_f");
                        String situationP = document.getString("situation_p");
                        // Récupérer l'état directement depuis la collection client
                        String status = document.getString("status");
                        if (status != null) {
                            demande.setStatus(status);
                        } else {
                            demande.setStatus("En attente"); // Ou une autre valeur par défaut
                        }

                        if (duree != null) demande.setDuree(duree);
                        if (enfants != null) demande.setEnfants(enfants);
                        if (loyer != null) demande.setLoyer(loyer);
                        if (message != null) demande.setMessage(message);
                        if (situationF != null) demande.setSituationF(situationF);
                        if (situationP != null) demande.setSituationP(situationP);
                    }

                    // Appeler le callback une fois terminé, que le document existe ou non
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("ClientDemandesActivity", "Erreur lors de la récupération des détails de la demande", e);
                    // En cas d'erreur, appeler également le callback
                    onComplete.run();
                });
    }

    // Méthode pour trier les demandes selon le statut
    private void trierDemandesParStatut() {
        Collections.sort(demandeList, (demande1, demande2) -> {
            // Fonction pour obtenir la priorité d'un statut
            Function<String, Integer> getPriorite = status -> {
                if (status == null) return 3; // Si null, priorité la plus basse
                switch (status) {
                    case "En attente": return 0; // Priorité la plus haute
                    case "Acceptée": return 1;
                    case "Refusée": return 2;
                    default: return 3; // Autres statuts
                }
            };

            int priorite1 = getPriorite.apply(demande1.getStatus());
            int priorite2 = getPriorite.apply(demande2.getStatus());

            return Integer.compare(priorite1, priorite2);
        });

        // Notifier l'adapter des changements après le tri
        adapter.notifyDataSetChanged();
    }
}