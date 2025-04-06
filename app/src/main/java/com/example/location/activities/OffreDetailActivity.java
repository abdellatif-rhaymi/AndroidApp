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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.File;

public class OffreDetailActivity extends AppCompatActivity {

    private ImageView detailImageOffre;
    private TextView detailTextTitre, detailTextPrix, detailTextDescription;
    private TextView detailTextEtage, detailTextLoyer, detailTextPieces, detailTextSdb, detailTextSuperficie;
    private Button btnModifierOffre, btnSupprimerOffre, btnVoirCommentaires;
    private RatingBar ratingBarOffre;
    private TextView textViewNoRating;

    private String offreId, agentEmail, photoNom;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offre_detail);

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
        btnVoirCommentaires = findViewById(R.id.btnVoirCommentaires);
        ratingBarOffre = findViewById(R.id.ratingBarOffre);
        textViewNoRating = findViewById(R.id.textViewNoRating);

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

            // 🔥 Charger l'évaluation moyenne
            chargerEvaluationMoyenne();
        }

        // 🔥 Modifier l'offre
        btnModifierOffre.setOnClickListener(v -> {
            Intent modifIntent = new Intent(OffreDetailActivity.this, ModifierOffreActivity.class);
            modifIntent.putExtra("OFFRE_ID", offreId);
            modifIntent.putExtra("AGENT_EMAIL", agentEmail);
            modifIntent.putExtra("OFFRE_TITRE", detailTextTitre.getText().toString());
            modifIntent.putExtra("OFFRE_DESCRIPTION", detailTextDescription.getText().toString());
            modifIntent.putExtra("OFFRE_PRIX", extras.getDouble("OFFRE_PRIX", 0));
            modifIntent.putExtra("OFFRE_PHOTO", photoNom);
            modifIntent.putExtra("OFFRE_ETAGE", extras.getInt("OFFRE_ETAGE", 0));
            modifIntent.putExtra("OFFRE_LOYER", extras.getDouble("OFFRE_LOYER", 0));
            modifIntent.putExtra("OFFRE_PIECES", extras.getInt("OFFRE_PIECES", 0));
            modifIntent.putExtra("OFFRE_SDB", extras.getInt("OFFRE_SDB", 0));
            modifIntent.putExtra("OFFRE_SUPERFICIE", extras.getDouble("OFFRE_SUPERFICIE", 0));
            Log.d("omar", "Titre: " + offreId + ", Description: " + agentEmail + ", Prix: " + photoNom);

            startActivity(modifIntent);
        });

        // 🔥 Supprimer l'offre
        btnSupprimerOffre.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(OffreDetailActivity.this)
                    .setTitle("Confirmation")
                    .setMessage("Voulez-vous vraiment supprimer cette offre ?")
                    .setPositiveButton("Oui", (dialog, which) -> supprimerOffre())
                    .setNegativeButton("Non", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 🔥 Voir les commentaires
        btnVoirCommentaires.setOnClickListener(v -> {
            Intent commentairesIntent = new Intent(OffreDetailActivity.this, CommentairesActivity.class);
            commentairesIntent.putExtra("OFFRE_ID", offreId);
            commentairesIntent.putExtra("AGENT_EMAIL", agentEmail);
            startActivity(commentairesIntent);
        });
    }

    // 🔥 Méthode pour supprimer l'offre
    private void supprimerOffre() {
        if (offreId != null && agentEmail != null) {
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(OffreDetailActivity.this, "Offre supprimée !", Toast.LENGTH_SHORT).show();
                        finish(); // Fermer l'activité après suppression
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OffreDetailActivity.this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "Erreur : ID ou AgentEmail manquant", Toast.LENGTH_SHORT).show();
        }
    }

    // 🔥 Méthode pour charger l'évaluation moyenne
    private void chargerEvaluationMoyenne() {
        if (offreId != null && agentEmail != null) {
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("evaluation")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            float totalScore = 0;
                            int count = 0;

                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                if (document.contains("score")) {
                                    float score = document.getLong("score").floatValue();
                                    totalScore += score;
                                    count++;
                                }
                            }

                            if (count > 0) {
                                float moyenne = totalScore / count;
                                ratingBarOffre.setRating(moyenne);
                                ratingBarOffre.setVisibility(View.VISIBLE);
                                textViewNoRating.setVisibility(View.GONE);
                            } else {
                                ratingBarOffre.setVisibility(View.GONE);
                                textViewNoRating.setVisibility(View.VISIBLE);
                            }
                        } else {
                            // Aucune évaluation trouvée
                            ratingBarOffre.setVisibility(View.GONE);
                            textViewNoRating.setVisibility(View.VISIBLE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(OffreDetailActivity.this, "Erreur lors du chargement des évaluations", Toast.LENGTH_SHORT).show();
                        ratingBarOffre.setVisibility(View.GONE);
                        textViewNoRating.setVisibility(View.VISIBLE);
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recharger les évaluations lorsque l'utilisateur revient à cette activité
        if (offreId != null && agentEmail != null) {
            chargerEvaluationMoyenne();
        }
    }
}