package com.example.location.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.adapters.AgentDemandeAdapter;
import com.example.location.model.Demande;
import com.example.location.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class AgentDemandesActivity extends AppCompatActivity implements AgentDemandeAdapter.DemandeActionListener {
    private static final String TAG = "AgentDemandesActivity";
    private static final int REQUEST_CALL_PHONE = 1;

    private RecyclerView recyclerView;
    private AgentDemandeAdapter adapter;
    private List<Demande> demandeList = new ArrayList<>();
    private Set<String> demandesTraitees = new HashSet<>();
    private FirebaseFirestore db;
    private String agentEmail;
    private ProgressBar progressBar;
    private TextView emptyView;

    // Stocke temporairement le numéro à appeler lorsqu'on attend la permission
    private String pendingPhoneNumber;
    private String pendingDemandeId;
    private String pendingOffreId;

    // Pour éviter les rechargements trop fréquents
    private long dernierChargement = 0;
    private static final long DELAI_RECHARGEMENT = 1000; // 1 seconde


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_agent_demandes);

            // Initialiser Firebase
            db = FirebaseFirestore.getInstance();
            agentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

            Log.d(TAG, "Agent email: " + agentEmail);

            // Référencer les vues
            recyclerView = findViewById(R.id.recyclerViewAgentDemandes);
            progressBar = findViewById(R.id.progressBarAgent);
            emptyView = findViewById(R.id.emptyViewAgent);

            // Configurer RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            adapter = new AgentDemandeAdapter(demandeList, this, this);
            recyclerView.setAdapter(adapter);

            // Charger les demandes
            fetchAgentDemandes();

        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreate", e);
            Toast.makeText(this, "Une erreur est survenue lors du démarrage", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchAgentDemandes() {
        try {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);

            // Vider la liste actuelle et le set de suivi
            demandeList.clear();
            demandesTraitees.clear();
            adapter.notifyDataSetChanged();

            // Mettre à jour l'horodatage du dernier chargement
            dernierChargement = System.currentTimeMillis();

            // Liste pour stocker tous les IDs d'offres
            List<String> offreIds = new ArrayList<>();

            // D'abord, obtenir toutes les offres de l'agent
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .get()
                    .addOnCompleteListener(offreTask -> {
                        if (offreTask.isSuccessful()) {
                            Log.d(TAG, "Nombre d'offres trouvées: " + offreTask.getResult().size());

                            for (QueryDocumentSnapshot offreDoc : offreTask.getResult()) {
                                String offreId = offreDoc.getId();
                                offreIds.add(offreId);
                                Log.d(TAG, "Offre trouvée: " + offreId);
                            }

                            // Si aucune offre trouvée
                            if (offreIds.isEmpty()) {
                                progressBar.setVisibility(View.GONE);
                                emptyView.setVisibility(View.VISIBLE);
                                return;
                            }

                            // Compteur pour suivre le traitement des offres
                            AtomicInteger offresTraitees = new AtomicInteger(0);

                            // Maintenant chercher les demandes pour chaque offre
                            for (String offreId : offreIds) {
                                fetchDemandesForOffre(offreId, () -> {
                                    // Incrémenter le compteur après chaque offre traitée
                                    int completedOffers = offresTraitees.incrementAndGet();

                                    // Si toutes les offres ont été traitées, trier et afficher
                                    if (completedOffers == offreIds.size()) {
                                        // Trier les demandes par statut (En attente -> Acceptée -> Refusée)
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
                            Log.e(TAG, "Erreur lors de la récupération des offres", offreTask.getException());
                            progressBar.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            Toast.makeText(AgentDemandesActivity.this, "Erreur lors du chargement des offres", Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans fetchAgentDemandes", e);
            progressBar.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Erreur lors du chargement des données", Toast.LENGTH_SHORT).show();
        }
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

        // Notifier l'adapter des changements
        adapter.notifyDataSetChanged();
    }

    // Version modifiée de fetchDemandesForOffre avec callback
    private void fetchDemandesForOffre(String offreId, Runnable onComplete) {
        try {
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("demandes")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Demandes trouvées pour l'offre " + offreId + ": " + task.getResult().size());

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                try {
                                    String demandeId = document.getId();

                                    // Vérifier si cette demande a déjà été traitée
                                    if (demandesTraitees.contains(demandeId)) {
                                        Log.d(TAG, "Demande " + demandeId + " déjà traitée, ignorée");
                                        continue;
                                    }

                                    // Marquer comme traitée
                                    demandesTraitees.add(demandeId);

                                    Log.d(TAG, "Demande trouvée: " + demandeId);

                                    // Créer un objet Demande avec les données de base
                                    Demande demande = new Demande();
                                    demande.setId(demandeId);
                                    demande.setReference(demandeId);
                                    demande.setOffreId(offreId);
                                    demande.setAgentEmail(agentEmail);

                                    // Récupérer les champs spécifiques de la demande
                                    String clientEmail = document.getString("clientEmail");

                                    // Si clientEmail est null, essayer avec le champ "client"
                                    if (clientEmail == null || clientEmail.isEmpty()) {
                                        clientEmail = document.getString("client");
                                        Log.d(TAG, "Email client trouvé dans le champ 'client': " + clientEmail);
                                    }

                                    // Si toujours null, afficher les champs pour déboguer
                                    if (clientEmail == null || clientEmail.isEmpty()) {
                                        Log.w(TAG, "Email client manquant pour la demande " + demandeId + ", affichage des champs disponibles:");
                                        Map<String, Object> data = document.getData();
                                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                                            Log.d(TAG, "Champ: " + entry.getKey() + ", Valeur: " + entry.getValue());
                                        }
                                    }

                                    // Définir l'email client, avec valeur par défaut si nécessaire
                                    demande.setClientEmail(clientEmail != null ? clientEmail : "email_inconnu@example.com");

                                    // Récupérer les autres champs
                                    String duree = document.getString("duree");
                                    demande.setDuree(duree);

                                    String enfants = document.getString("enfants");
                                    demande.setEnfants(enfants);

                                    String loyer = document.getString("loyer");
                                    demande.setLoyer(loyer);

                                    String message = document.getString("message");
                                    demande.setMessage(message);

                                    String situationF = document.getString("situation_f");
                                    demande.setSituationF(situationF);

                                    String situationP = document.getString("situation_p");
                                    demande.setSituationP(situationP);

                                    String status = document.getString("status");
                                    demande.setStatus(status != null ? status : "En attente");

                                    // Récupérer les informations du client
                                    if (clientEmail != null && !clientEmail.isEmpty() && !clientEmail.equals("email_inconnu@example.com")) {
                                        fetchClientInfo(demande);
                                    }

                                    // Récupérer le titre de l'offre
                                    fetchOffreTitle(demande);

                                    demandeList.add(demande);
                                } catch (Exception e) {
                                    Log.e(TAG, "Erreur lors de la conversion d'une demande", e);
                                }
                            }
                        } else {
                            Log.e(TAG, "Erreur lors de la récupération des demandes", task.getException());
                        }

                        // Exécuter le callback pour indiquer que cette offre est terminée
                        onComplete.run();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans fetchDemandesForOffre", e);
            // En cas d'erreur, on appelle quand même le callback
            onComplete.run();
        }
    }
    private void fetchClientInfo(Demande demande) {
        try {
            if (demande.getClientEmail() == null || demande.getClientEmail().isEmpty()) {
                return;
            }

            Log.d(TAG, "Récupération des infos client: " + demande.getClientEmail());

            db.collection("clients")
                    .document(demande.getClientEmail())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Infos client trouvées pour: " + demande.getClientEmail());

                            UserProfile clientProfile = documentSnapshot.toObject(UserProfile.class);
                            if (clientProfile != null) {
                                String nom = clientProfile.getNom();
                                String prenom = clientProfile.getPrenom();
                                String nomComplet = (nom != null ? nom : "") + " " + (prenom != null ? prenom : "");
                                demande.setClientName(nomComplet.trim());
                                demande.setClientPhone(clientProfile.getTelephone());
                                demande.setClientAdresse(clientProfile.getAdresse());
                                demande.setClientVille(clientProfile.getVille());
                                demande.setClientPays(clientProfile.getPays());
                                adapter.notifyDataSetChanged();
                            }
                        } else {
                            Log.d(TAG, "Pas d'infos client trouvées pour: " + demande.getClientEmail());
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la récupération des infos client", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans fetchClientInfo", e);
        }
    }

    private void fetchOffreTitle(Demande demande) {
        try {
            Log.d(TAG, "Récupération du titre de l'offre: " + demande.getOffreId());

            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(demande.getOffreId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String titre = documentSnapshot.getString("titre");
                            demande.setOffreTitle(titre != null ? titre : "Offre " + demande.getOffreId());
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la récupération du titre de l'offre", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans fetchOffreTitle", e);
        }
    }

    @Override
    public void onAcceptDemande(Demande demande) {
        try {
            Log.d(TAG, "onAcceptDemande appelé pour: " + demande.getId());

            // Vérifier si on a le numéro de téléphone du client
            if (demande.getClientPhone() == null || demande.getClientPhone().isEmpty()) {
                // Si pas de numéro disponible, juste mettre à jour le statut
                Log.d(TAG, "Pas de numéro de téléphone disponible");
                updateDemandeStatus(demande.getId(), demande.getOffreId(), "Acceptée");
                return;
            }

            // Stocker les informations de la demande pour après la vérification des permissions
            pendingPhoneNumber = demande.getClientPhone();
            pendingDemandeId = demande.getId();
            pendingOffreId = demande.getOffreId();

            Log.d(TAG, "Numéro de téléphone à appeler: " + pendingPhoneNumber);

            // Vérifier et demander la permission d'appeler
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Demande de permission d'appel");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL_PHONE);
            } else {
                // Mettre à jour le statut et lancer l'appel
                Log.d(TAG, "Permission d'appel déjà accordée");
                updateDemandeStatus(pendingDemandeId, pendingOffreId, "Acceptée");
                makePhoneCall(pendingPhoneNumber);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onAcceptDemande", e);
            Toast.makeText(this, "Erreur lors de l'acceptation de la demande", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRejectDemande(Demande demande) {
        try {
            Log.d(TAG, "onRejectDemande appelé pour: " + demande.getId());
            updateDemandeStatus(demande.getId(), demande.getOffreId(), "Refusée");
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onRejectDemande", e);
            Toast.makeText(this, "Erreur lors du refus de la demande", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDemandeDetails(Demande demande) {
        try {
            Log.d(TAG, "onDemandeDetails appelé pour: " + demande.getId());

            Intent intent = new Intent(this, AgentDemandeDetailActivity.class);
            intent.putExtra("DEMANDE_ID", demande.getId());
            intent.putExtra("OFFRE_ID", demande.getOffreId());
            intent.putExtra("CLIENT_EMAIL", demande.getClientEmail());

            Log.d(TAG, "Ouverture des détails - DEMANDE_ID: " + demande.getId());
            Log.d(TAG, "Ouverture des détails - OFFRE_ID: " + demande.getOffreId());
            Log.d(TAG, "Ouverture des détails - CLIENT_EMAIL: " + demande.getClientEmail());

            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onDemandeDetails", e);
            Toast.makeText(this, "Erreur lors de l'ouverture des détails", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateDemandeStatus(String demandeId, String offreId, String status) {
        try {
            Log.d(TAG, "Mise à jour du statut de la demande " + demandeId + " à " + status);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", status);

            // Mettre à jour le statut dans la collection de l'agent
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("demandes")
                    .document(demandeId)
                    .update(updates)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Statut mis à jour avec succès");
                        Toast.makeText(AgentDemandesActivity.this, "Demande " + status.toLowerCase(), Toast.LENGTH_SHORT).show();

                        // Mettre à jour la liste
                        for (Demande demande : demandeList) {
                            if (demande.getId().equals(demandeId)) {
                                demande.setStatus(status);
                                adapter.notifyDataSetChanged();
                                break;
                            }
                        }

                        // Notifier également le client du changement de statut
                        updateClientDemande(demandeId, offreId, status);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour du statut", e);
                        Toast.makeText(AgentDemandesActivity.this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans updateDemandeStatus", e);
            Toast.makeText(this, "Erreur lors de la mise à jour du statut", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateClientDemande(String demandeId, String offreId, String status) {
        try {
            // D'abord récupérer l'email du client
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("demandes")
                    .document(demandeId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String clientEmail = documentSnapshot.getString("clientEmail");
                            if (clientEmail == null || clientEmail.isEmpty()) {
                                clientEmail = documentSnapshot.getString("client");
                            }

                            if (clientEmail != null && !clientEmail.isEmpty()) {
                                Log.d(TAG, "Mise à jour du statut pour le client: " + clientEmail);

                                // Mettre à jour le statut dans la collection du client
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("status", status);

                                db.collection("clients")
                                        .document(clientEmail)
                                        .collection("offres")
                                        .document(demandeId)
                                        .update(updates)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d(TAG, "Statut mis à jour pour le client avec succès");
                                        })
                                        .addOnFailureListener(e -> {
                                            Log.e(TAG, "Erreur lors de la mise à jour du statut pour le client", e);
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la récupération des infos client", e);
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans updateClientDemande", e);
        }
    }

    private void makePhoneCall(String phoneNumber) {
        try {
            Log.d(TAG, "Tentative d'appel au numéro: " + phoneNumber);

            Intent callIntent = new Intent(Intent.ACTION_CALL);
            callIntent.setData(Uri.parse("tel:" + phoneNumber));
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
                Log.d(TAG, "Permission d'appel accordée");
                if (pendingPhoneNumber != null && pendingDemandeId != null && pendingOffreId != null) {
                    updateDemandeStatus(pendingDemandeId, pendingOffreId, "Acceptée");
                    makePhoneCall(pendingPhoneNumber);
                }
            } else {
                Log.d(TAG, "Permission d'appel refusée");
                Toast.makeText(this, "Permission d'appel refusée", Toast.LENGTH_SHORT).show();
                // Mettre à jour le statut même si l'appel n'est pas possible
                if (pendingDemandeId != null && pendingOffreId != null) {
                    updateDemandeStatus(pendingDemandeId, pendingOffreId, "Acceptée");
                }
            }
            // Réinitialiser les variables temporaires
            pendingPhoneNumber = null;
            pendingDemandeId = null;
            pendingOffreId = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Ne recharger que si suffisamment de temps s'est écoulé depuis le dernier chargement
        long maintenant = System.currentTimeMillis();
        if (maintenant - dernierChargement > DELAI_RECHARGEMENT) {
            Log.d(TAG, "onResume appelé, rechargement des données");
            fetchAgentDemandes();
            dernierChargement = maintenant;
        } else {
            Log.d(TAG, "onResume appelé, mais trop tôt pour recharger");
        }
    }
}