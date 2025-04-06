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
import com.example.location.model.Demande;
import com.example.location.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;

public class ClientDemandeDetailActivity extends AppCompatActivity {

    private static final String TAG = "ClientDemandeDetail";
    public static final int REQUEST_CALL_PHONE = 1;

    // Vues pour l'offre
    private ImageView detailImageOffre;
    private TextView detailTextTitre, detailTextPrix, detailTextDescription;
    private TextView detailTextEtage, detailTextLoyer, detailTextPieces, detailTextSdb, detailTextSuperficie;

    // Vues pour la demande
    private TextView textDuree, textEnfants, textLoyerMax, textMessage, textSituationP, textSituationF;

    // Boutons
        private Button btnModifierDemande, btnSupprimerDemande , btnAppeler;

    // Données
    private String demandeId, offreId, agentEmail, photoNom, clientEmail , agentPhone;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_demande_detail);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();
        clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Lier les vues d'offre
        detailImageOffre = findViewById(R.id.detailImageOffre);
        detailTextTitre = findViewById(R.id.detailTextTitre);
        detailTextDescription = findViewById(R.id.detailTextDescription);
        detailTextPrix = findViewById(R.id.detailTextPrix);
        detailTextEtage = findViewById(R.id.detailTextEtage);
        detailTextLoyer = findViewById(R.id.detailTextLoyer);
        detailTextPieces = findViewById(R.id.detailTextPieces);
        detailTextSdb = findViewById(R.id.detailTextSdb);
        detailTextSuperficie = findViewById(R.id.detailTextSuperficie);

        // Lier les vues de demande
        textDuree = findViewById(R.id.textDuree);
        textEnfants = findViewById(R.id.textEnfants);
        textLoyerMax = findViewById(R.id.textLoyerMax);
        textMessage = findViewById(R.id.textMessage);
        textSituationP = findViewById(R.id.textSituationP);
        textSituationF = findViewById(R.id.textSituationF);

        // Lier les boutons
        btnModifierDemande = findViewById(R.id.btnModifierDemande);
        btnSupprimerDemande = findViewById(R.id.btnSupprimerDemande);
        btnAppeler = findViewById(R.id.btnAppeler);

        // Récupérer les données de l'intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            demandeId = extras.getString("DEMANDE_ID");
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");

            // Charger les détails de l'offre et de la demande
            loadClientDetails();
            loadOffreDetails();
            loadDemandeDetails();
        } else {
            Toast.makeText(this, "Erreur: Informations manquantes", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configuration des boutons
        btnModifierDemande.setOnClickListener(v -> modifierDemande());
        btnSupprimerDemande.setOnClickListener(v -> confirmerSuppression());
        btnAppeler.setOnClickListener(v -> appelerAgent());

    }

    private void loadClientDetails() {
        try {
            db.collection("agents")
                    .document(agentEmail)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            UserProfile agent = documentSnapshot.toObject(UserProfile.class);
                            if (agent != null) {
                                // Mise à jour des vues
                                String nomComplet = (agent.getNom() != null ? agent.getNom() : "") + " " +
                                        (agent.getPrenom() != null ? agent.getPrenom() : "");

                                agentPhone = agent.getTelephone();


                               }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors du chargement des infos agent", e);
                        Toast.makeText(this, "Impossible de charger les informations du agent", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans loadClientDetails", e);
        }
    }


    private void loadOffreDetails() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Récupérer les données de l'offre
                        String titre = documentSnapshot.getString("titre");
                        String description = documentSnapshot.getString("description");
                        Double prix = documentSnapshot.getDouble("prix");
                        Integer etage = documentSnapshot.getLong("etage") != null ? documentSnapshot.getLong("etage").intValue() : 0;
                        Double loyer = documentSnapshot.getDouble("loyer");
                        Integer pieces = documentSnapshot.getLong("pieces") != null ? documentSnapshot.getLong("pieces").intValue() : 0;
                        Integer sdb = documentSnapshot.getLong("sdb") != null ? documentSnapshot.getLong("sdb").intValue() : 0;
                        Double superficie = documentSnapshot.getDouble("superficie");
                        photoNom = documentSnapshot.getString("photo");

                        // Mettre à jour l'interface
                        detailTextTitre.setText(titre != null ? titre : "N/A");
                        detailTextDescription.setText(description != null ? description : "N/A");
                        detailTextPrix.setText("Prix: " + (prix != null ? prix : 0) + " MAD");
                        detailTextEtage.setText("Étage: " + etage);
                        detailTextLoyer.setText("Loyer: " + (loyer != null ? loyer : 0) + " MAD");
                        detailTextPieces.setText("Pièces: " + pieces);
                        detailTextSdb.setText("Salles de bain: " + sdb);
                        detailTextSuperficie.setText("Superficie: " + (superficie != null ? superficie : 0) + " m²");

                        // Charger l'image
                        loadImage(photoNom);
                    } else {
                        Toast.makeText(ClientDemandeDetailActivity.this, "Offre introuvable", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement de l'offre", e);
                    Toast.makeText(ClientDemandeDetailActivity.this, "Erreur lors du chargement de l'offre", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadDemandeDetails() {
        // Charger les détails de la demande du client
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("demandes")
                .document(demandeId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Récupérer les données de la demande
                        String duree = documentSnapshot.getString("duree");
                        String enfants = documentSnapshot.getString("enfants");
                        String loyer = documentSnapshot.getString("loyer");
                        String message = documentSnapshot.getString("message");
                        String situationF = documentSnapshot.getString("situation_f");
                        String situationP = documentSnapshot.getString("situation_p");
                        String status = documentSnapshot.getString("status"); // Get the status

                        // Mettre à jour l'interface
                        textDuree.setText("Durée souhaitée: " + (duree != null ? duree : "N/A"));
                        textEnfants.setText("Nombre d'enfants: " + (enfants != null ? enfants : "N/A"));
                        textLoyerMax.setText("Loyer max: " + (loyer != null ? loyer : "N/A") + " MAD");
                        textMessage.setText("Message: " + (message != null ? message : "N/A"));
                        textSituationP.setText("Situation professionnelle: " + (situationP != null ? situationP : "N/A"));
                        textSituationF.setText("Situation financière: " + (situationF != null ? situationF : "N/A"));

                        // Show the call button only if the status is "Accepté"
                        if (status != null && status.equals("Acceptée")) {
                            btnAppeler.setVisibility(View.VISIBLE);
                        } else {
                            btnAppeler.setVisibility(View.GONE);
                        }
                    } else {
                        Toast.makeText(ClientDemandeDetailActivity.this, "Demande introuvable", Toast.LENGTH_SHORT).show();
                        btnAppeler.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement de la demande", e);
                    Toast.makeText(ClientDemandeDetailActivity.this, "Erreur lors du chargement de la demande", Toast.LENGTH_SHORT).show();
                    btnAppeler.setVisibility(View.GONE);
                });
    }

    private void loadImage(String photoNom) {
        if (photoNom != null && !photoNom.isEmpty()) {
            if (photoNom.startsWith("http")) {
                // Chargement depuis une URL
                Glide.with(this).load(photoNom).into(detailImageOffre);
            } else {
                // Charger depuis le stockage interne
                File imageFile = new File(getExternalFilesDir(null), "images/" + photoNom);
                if (imageFile.exists()) {
                    Glide.with(this)
                            .load(imageFile)
                            .into(detailImageOffre);
                } else {
                    Log.e(TAG, "Fichier non trouvé : " + imageFile.getAbsolutePath());
                    detailImageOffre.setImageResource(R.drawable.placeholder_image);
                }
            }
        } else {
            detailImageOffre.setImageResource(R.drawable.placeholder_image);
        }
    }

    private void modifierDemande() {
        // Rediriger vers l'activité de modification de demande
        Intent intent = new Intent(ClientDemandeDetailActivity.this, ModifierDemandeActivity.class);
        intent.putExtra("DEMANDE_ID", demandeId);
        intent.putExtra("OFFRE_ID", offreId);
        intent.putExtra("AGENT_EMAIL", agentEmail);
        startActivity(intent);
    }

    private void confirmerSuppression() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmation")
                .setMessage("Voulez-vous vraiment supprimer cette demande ?")
                .setPositiveButton("Oui", (dialog, which) -> supprimerDemande())
                .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void supprimerDemande() {
        // Supprimer la demande des deux collections (agent et client)

        // 1. Supprimer de la collection du client
        db.collection("clients")
                .document(clientEmail)
                .collection("offres")
                .document(demandeId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Demande supprimée de la collection du client");

                    // 2. Supprimer de la collection de l'agent
                    db.collection("agents")
                            .document(agentEmail)
                            .collection("offres")
                            .document(offreId)
                            .collection("demandes")
                            .document(demandeId)
                            .delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(ClientDemandeDetailActivity.this, "Demande supprimée avec succès", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la suppression de la demande côté agent", e);
                                Toast.makeText(ClientDemandeDetailActivity.this, "La suppression a partiellement échoué", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la suppression de la demande côté client", e);
                    Toast.makeText(ClientDemandeDetailActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });
    }
    private void appelerAgent() {
        if (agentPhone != null && !agentEmail.isEmpty()) {
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
            callIntent.setData(Uri.parse("tel:" + agentPhone));
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
}