package com.example.location.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.location.R;

public class OffreDetailActivity2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offre_detail);

        // Récupérer les références des vues
        ImageView imageOffre = findViewById(R.id.detailImageOffre);
        TextView textTitre = findViewById(R.id.detailTextTitre);
        TextView textDescription = findViewById(R.id.detailTextDescription);
        TextView textPrix = findViewById(R.id.detailTextPrix);
        TextView textEtage = findViewById(R.id.detailTextEtage);
        TextView textLoyer = findViewById(R.id.detailTextLoyer);
        TextView textPieces = findViewById(R.id.detailTextPieces);
        TextView textSdb = findViewById(R.id.detailTextSdb);
        TextView textSuperficie = findViewById(R.id.detailTextSuperficie);

        // Récupérer les données de l'intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String titre = extras.getString("OFFRE_TITRE", "");
            String description = extras.getString("OFFRE_DESCRIPTION", "");
            double prix = extras.getDouble("OFFRE_PRIX", 0);
            int etage = extras.getInt("OFFRE_ETAGE", 0);
            double loyer = extras.getDouble("OFFRE_LOYER", 0);
            String photo = extras.getString("OFFRE_PHOTO", "");
            int pieces = extras.getInt("OFFRE_PIECES", 0);
            int sdb = extras.getInt("OFFRE_SDB", 0);
            double superficie = extras.getDouble("OFFRE_SUPERFICIE", 0);

            // Définir les valeurs dans les vues
            textTitre.setText(titre);
            textDescription.setText(description);
            textPrix.setText(String.format("Prix: %.2f MAD", prix));
            textEtage.setText(String.format("Étage: %d", etage));
            textLoyer.setText(String.format("Loyer: %.2f MAD", loyer));
            textPieces.setText(String.format("Pièces: %d", pieces));
            textSdb.setText(String.format("Salles de bain: %d", sdb));
            textSuperficie.setText(String.format("Superficie: %.2f m²", superficie));

            // Charger l'image avec Glide
            if (photo != null && !photo.isEmpty()) {
                // Vérifier si c'est un nom de fichier local ou une URL
                if (photo.startsWith("http")) {
                    // C'est une URL, charger directement
                    Glide.with(this)
                            .load(photo)
                            .into(imageOffre);
                } else {
                    // C'est un nom de fichier local, obtenir l'ID de ressource
                    int resourceId = getResources().getIdentifier(
                            photo.replace(".jpg", "").replace(".png", ""),
                            "drawable",
                            getPackageName());

                    if (resourceId != 0) {
                        Glide.with(this)
                                .load(resourceId)
                                .into(imageOffre);
                    }
                }
            }
        }
    }
}