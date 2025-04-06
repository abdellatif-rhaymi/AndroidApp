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
import com.example.location.model.Offre;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AddOffreActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTitre, editDescription, editPrix, editEtage,
            editLoyer, editPieces, editSdb, editSuperficie;
    private ImageView imageOffre;
    private Button btnChoisirImage, btnAjouter;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private Uri imageUri;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_offre);

        // Initialiser Firebase
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Lier les vues
        initViews();

        // Configurer les listeners
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
        btnAjouter = findViewById(R.id.btnAjouter);
    }

    private void setupListeners() {
        // Choisir une image
        btnChoisirImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        // Ajouter l'offre
        btnAjouter.setOnClickListener(v -> {
            if (validateInputs()) {
                ajouterOffre(imageName);
            }
        });
    }

    private boolean validateInputs() {
        // Validation basique des champs
        if (editTitre.getText().toString().trim().isEmpty()) {
            editTitre.setError("Titre requis");
            return false;
        }

        try {
            // Vérifier que les champs numériques sont valides
            Double.parseDouble(editPrix.getText().toString());
            Integer.parseInt(editEtage.getText().toString());
            Double.parseDouble(editLoyer.getText().toString());
            Integer.parseInt(editPieces.getText().toString());
            Integer.parseInt(editSdb.getText().toString());
            Double.parseDouble(editSuperficie.getText().toString());
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez vérifier les valeurs numériques", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void saveImageToStorage() {
        try {
            // Générer un nom unique pour l'image
            imageName = "offre_" + UUID.randomUUID().toString() + ".jpg";

            // Convertir l'URI en Bitmap
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);

            // Créer un dossier dans le stockage externe privé de l'application
            File directory = new File(getExternalFilesDir(null), "images");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Chemin complet du fichier image
            File file = new File(directory, imageName);

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

      private void ajouterOffre(String photoName) {
        // Récupérer l'email de l'agent connecté
        String agentEmail = mAuth.getCurrentUser() != null ?
                mAuth.getCurrentUser().getEmail() :
                "agent@exemple.com"; // Fallback si pas d'authentification

        // Créer un nouvel objet Offre
        Offre nouvelleOffre = new Offre(
                null, // id sera généré par Firestore
                agentEmail,
                editTitre.getText().toString(),
                editDescription.getText().toString(),
                Integer.parseInt(editEtage.getText().toString()),
                Double.parseDouble(editLoyer.getText().toString()),
                photoName != null ? photoName : "", // Nom de l'image
                Integer.parseInt(editPieces.getText().toString()),
                Double.parseDouble(editPrix.getText().toString()),
                Integer.parseInt(editSdb.getText().toString()),
                Double.parseDouble(editSuperficie.getText().toString())
        );

        // Ajouter l'offre à Firestore
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .add(nouvelleOffre)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Offre ajoutée avec succès", Toast.LENGTH_SHORT).show();
                    finish(); // Fermer l'activité après ajout
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erreur lors de l'ajout de l'offre", Toast.LENGTH_SHORT).show();
                    Log.e("AjouterOffre", "Erreur", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                // Charger l'image dans l'ImageView
                Glide.with(this).load(imageUri).into(imageOffre);

                // Sauvegarder l'image localement
                saveImageToStorage();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}