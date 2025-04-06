package com.example.location.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.location.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClientOffreDetail extends AppCompatActivity {

    private static final String TAG = "ClientOffreDetail";
    private ImageView detailImageOffre;
    private TextView detailTextTitre, detailTextPrix, detailTextDescription;
    private TextView detailTextEtage, detailTextLoyer, detailTextPieces, detailTextSdb, detailTextSuperficie;
    private Button btnModifierOffre, btnSupprimerOffre;
    private RatingBar ratingBarMoyenne;
    private TextView textEvaluationCount;

    private String offreId, agentEmail, photoNom;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_offre_detail);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String clientEmail;
        if (currentUser != null) {
            clientEmail = currentUser.getEmail();
        } else {
            clientEmail = null;
            // Gérer le cas où aucun utilisateur n'est connecté
            Toast.makeText(this, "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            return; // Ou rediriger vers l'écran de connexion
        }

        // 🔥 Lier les composants UI
        detailImageOffre = findViewById(R.id.detailImageOffre);
        detailTextTitre = findViewById(R.id.detailTextTitre);
        detailTextDescription = findViewById(R.id.detailTextDescription);
        detailTextPrix = findViewById(R.id.detailTextPrix);
        detailTextEtage = findViewById(R.id.detailTextEtage);
        detailTextLoyer = findViewById(R.id.detailTextLoyer);
        detailTextPieces = findViewById(R.id.detailTextPieces);
        detailTextSdb = findViewById(R.id.detailTextSdb);
        detailTextSuperficie = findViewById(R.id.detailTextSuperficie);
        btnModifierOffre = findViewById(R.id.btnModifierOffre);
        btnSupprimerOffre = findViewById(R.id.btnSupprimerOffre);

        // 🔥 Nouveaux composants pour les évaluations
        ratingBarMoyenne = findViewById(R.id.ratingBarMoyenne);
        textEvaluationCount = findViewById(R.id.textEvaluationCount);

        // 🔥 Initialiser Firestore
        db = FirebaseFirestore.getInstance();

        // 🔥 Récupérer les données envoyées depuis `OffreAdapter`
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");
            photoNom = extras.getString("OFFRE_PHOTO", "");

            detailTextTitre.setText(extras.getString("OFFRE_TITRE", "N/A"));
            detailTextDescription.setText(extras.getString("OFFRE_DESCRIPTION", "N/A"));
            detailTextPrix.setText("Prix: " + extras.getDouble("OFFRE_PRIX", 0) + " MAD");
            detailTextEtage.setText("Étage: " + extras.getInt("OFFRE_ETAGE", 0));
            detailTextLoyer.setText("Loyer: " + extras.getDouble("OFFRE_LOYER", 0) + " MAD");
            detailTextPieces.setText("Pièces: " + extras.getInt("OFFRE_PIECES", 0));
            detailTextSdb.setText("Salles de bain: " + extras.getInt("OFFRE_SDB", 0));
            detailTextSuperficie.setText("Superficie: " + extras.getDouble("OFFRE_SUPERFICIE", 0) + " m²");

            // 🔥 Charger l'image selon son type (nom local ou URL)
            if (photoNom != null && !photoNom.isEmpty()) {
                if (photoNom.startsWith("http")) {
                    // 🔹 Chargement depuis une URL (si jamais on utilise Firebase Storage plus tard)
                    Glide.with(this).load(photoNom).into(detailImageOffre);
                } else {
                    // 🔹 Charger depuis le bon dossier de stockage interne
                    File imageFile = new File(getExternalFilesDir(null), "images/" + photoNom);

                    if (imageFile.exists()) {
                        Glide.with(this)
                                .load(imageFile)
                                .into(detailImageOffre);
                    } else {
                        // 🔹 Utiliser une image par défaut si le fichier n'existe pas
                        Log.e("LoadImage", "Fichier non trouvé : " + imageFile.getAbsolutePath());
                        detailImageOffre.setImageResource(R.drawable.placeholder_image);
                    }
                }
            }

            // 🔥 Charger les évaluations pour cette offre
            chargerEvaluations(agentEmail, offreId);

            // 🔥 Bouton pour faire une demande
            btnSupprimerOffre.setOnClickListener(v -> {
                Intent modifIntent = new Intent(ClientOffreDetail.this, Demande.class);
                modifIntent.putExtra("OFFRE_ID", offreId);
                modifIntent.putExtra("AGENT_EMAIL", agentEmail);
                modifIntent.putExtra("CLIENT_EMAIL", clientEmail); // Utilisez l'email récupéré

                Log.d("omar", "Titre: " + offreId + ", Description: " + agentEmail + clientEmail);

                startActivity(modifIntent);
            });

            // 🔥 Bouton pour commenter/évaluer
            btnModifierOffre.setOnClickListener(v -> ouvrirCommentaireActivity());
        }
    }

    private void chargerEvaluations(String agentEmail, String offreId) {
        // Afficher un indicateur de chargement
        if (textEvaluationCount != null) {
            textEvaluationCount.setText("Chargement des évaluations...");
        }

        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("evaluation")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Float> scores = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Récupérer le score de chaque évaluation
                            Object scoreObj = document.get("score");
                            if (scoreObj != null) {
                                try {
                                    // Convertir le score en float (peut être stocké comme Long, Double ou String)
                                    float score;
                                    if (scoreObj instanceof Long) {
                                        score = ((Long) scoreObj).floatValue();
                                    } else if (scoreObj instanceof Double) {
                                        score = ((Double) scoreObj).floatValue();
                                    } else if (scoreObj instanceof String) {
                                        score = Float.parseFloat((String) scoreObj);
                                    } else {
                                        Log.w(TAG, "Type de score inconnu: " + scoreObj.getClass().getName());
                                        continue;
                                    }

                                    // Vérifier que le score est dans la plage 1-5
                                    if (score >= 1 && score <= 5) {
                                        scores.add(score);
                                    } else {
                                        Log.w(TAG, "Score hors limite: " + score);
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e(TAG, "Erreur de conversion du score", e);
                                }
                            }
                        }

                        // Calculer et afficher la moyenne si des scores ont été trouvés
                        if (!scores.isEmpty()) {
                            float total = 0;
                            for (float score : scores) {
                                total += score;
                            }
                            float moyenne = total / scores.size();

                            // Mettre à jour l'UI sur le thread principal
                            runOnUiThread(() -> {
                                ratingBarMoyenne.setRating(moyenne);
                                textEvaluationCount.setText(String.format("%.1f/5 (%d évaluation%s)",
                                        moyenne, scores.size(), scores.size() > 1 ? "s" : ""));
                            });
                        } else {
                            // Aucune évaluation trouvée
                            runOnUiThread(() -> {
                                ratingBarMoyenne.setRating(0);
                                textEvaluationCount.setText("Aucune évaluation");
                            });
                        }
                    } else {
                        // Erreur lors de la récupération des évaluations
                        Log.e(TAG, "Erreur lors de la récupération des évaluations", task.getException());
                        runOnUiThread(() -> {
                            textEvaluationCount.setText("Erreur de chargement");
                        });
                    }
                });
    }

    private void ouvrirCommentaireActivity() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String clientEmail;
        if (currentUser != null) {
            clientEmail = currentUser.getEmail();
        } else {
            clientEmail = null;
            // Gérer le cas où aucun utilisateur n'est connecté
            Toast.makeText(this, "Aucun utilisateur connecté", Toast.LENGTH_SHORT).show();
            return; // Ou rediriger vers l'écran de connexion
        }
        Intent intent = new Intent(ClientOffreDetail.this, CommentaireEvaluationActivity.class);
        intent.putExtra("OFFRE_ID", offreId);
        intent.putExtra("AGENT_EMAIL", agentEmail);
        intent.putExtra("CLIENT_EMAIL", clientEmail);
        intent.putExtra("TITRE_OFFRE", detailTextTitre.getText().toString());

        startActivity(intent);
    }
}