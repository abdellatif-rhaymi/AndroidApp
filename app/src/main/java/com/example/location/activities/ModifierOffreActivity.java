package com.example.location.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.location.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ModifierOffreActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitre, editDescription, editPrix, editEtage,
            editLoyer, editPieces, editSdb, editSuperficie;
    private ImageView imageOffre;
    private Button btnChoisirImage, btnModifier;

    private FirebaseFirestore db;
    private String offreId, agentEmail, imageFileName;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modifier_offre);

        db = FirebaseFirestore.getInstance();
        initViews();
        recupererDonneesOffre();
        setupListeners();
    }

    private void initViews() {
        editTitre = findViewById(R.id.editTitre);
        editDescription = findViewById(R.id.editDescription);
        editPrix = findViewById(R.id.editPrix);
        editEtage = findViewById(R.id.editEtage);
        editLoyer = findViewById(R.id.editLoyer);
        editPieces = findViewById(R.id.editPieces);
        editSdb = findViewById(R.id.editSdb);
        editSuperficie = findViewById(R.id.editSuperficie);

        imageOffre = findViewById(R.id.imageOffre);
        btnChoisirImage = findViewById(R.id.btnChoisirImage);
        btnModifier = findViewById(R.id.btnModifier);
    }

    private void recupererDonneesOffre() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");
            imageFileName = extras.getString("OFFRE_PHOTO", "");

            editTitre.setText(extras.getString("OFFRE_TITRE", ""));
            editDescription.setText(extras.getString("OFFRE_DESCRIPTION", ""));
            editPrix.setText(String.valueOf(extras.getDouble("OFFRE_PRIX", 0)));
            editEtage.setText(String.valueOf(extras.getInt("OFFRE_ETAGE", 0)));
            editLoyer.setText(String.valueOf(extras.getDouble("OFFRE_LOYER", 0)));
            editPieces.setText(String.valueOf(extras.getInt("OFFRE_PIECES", 0)));
            editSdb.setText(String.valueOf(extras.getInt("OFFRE_SDB", 0)));
            editSuperficie.setText(String.valueOf(extras.getDouble("OFFRE_SUPERFICIE", 0)));

            // Charger l'image locale
            chargerImageLocale(imageFileName);
        }
    }

    private void setupListeners() {
        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        btnModifier.setOnClickListener(v -> {
            if (validateInputs()) {
                if (imageUri != null) {
                    saveImageToStorage();
                    mettreAJourOffre(imageFileName);
                } else {
                    mettreAJourOffre(imageFileName);
                }
            }
        });
    }

    private boolean validateInputs() {
        if (editTitre.getText().toString().isEmpty()) {
            editTitre.setError("Titre requis");
            return false;
        }
        return true;
    }

    private void saveImageToStorage() {
        try {
            // Générer un nom unique pour l'image
            imageFileName = "offre_" + UUID.randomUUID().toString() + ".jpg";

            // Convertir l'URI en Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Créer un dossier dans le stockage externe privé de l'application
            File directory = new File(getExternalFilesDir(null), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Chemin complet du fichier image
            File file = new File(directory, imageFileName);

            // Sauvegarder l'image
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
            }

            Log.d("SaveImage", "Image sauvegardée : " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.e("SaveImage", "Erreur de sauvegarde", e);
            Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    private void mettreAJourOffre(String photoName) {
        Map<String, Object> offreData = new HashMap<>();
        offreData.put("titre", editTitre.getText().toString());
        offreData.put("description", editDescription.getText().toString());
        offreData.put("prix", Double.parseDouble(editPrix.getText().toString()));
        offreData.put("etage", Integer.parseInt(editEtage.getText().toString()));
        offreData.put("loyer", Double.parseDouble(editLoyer.getText().toString()));
        offreData.put("pieces", Integer.parseInt(editPieces.getText().toString()));
        offreData.put("sdb", Integer.parseInt(editSdb.getText().toString()));
        offreData.put("superficie", Double.parseDouble(editSuperficie.getText().toString()));
        offreData.put("photo", photoName); // On stocke juste le nom de l'image

        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .update(offreData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Offre mise à jour avec succès", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de la mise à jour", Toast.LENGTH_SHORT).show();
                    Log.e("ModifierOffre", "Erreur de mise à jour", e);
                });
    }

    private void chargerImageLocale(String imageName) {
        if (imageName != null && !imageName.isEmpty()) {
            File directory = new File(getExternalFilesDir(null), "images");
            File imageFile = new File(directory, imageName);
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(imageOffre);
            } else {
                imageOffre.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            imageOffre.setImageResource(R.drawable.placeholder_image);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Glide.with(this).load(imageUri).into(imageOffre);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
