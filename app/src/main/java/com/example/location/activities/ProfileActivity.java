package com.example.location.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.location.R;
import com.example.location.model.UserProfile;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private EditText editTextNom, editTextPrenom, editTextEmail, editTextAdresse,
            editTextVille, editTextPays, editTextTelephone;
    private Button buttonModifier, buttonSave;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;
    private String userType; // "agents" ou "clients"
    private boolean isEditMode = false;
    private static final String TAG = "ProfileActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialisation des vues
        editTextNom = findViewById(R.id.editTextNom);
        editTextPrenom = findViewById(R.id.editTextPrenom);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextAdresse = findViewById(R.id.editTextAdresse);
        editTextVille = findViewById(R.id.editTextVille);
        editTextPays = findViewById(R.id.editTextPays);
        editTextTelephone = findViewById(R.id.editTextTelephone);
        buttonModifier = findViewById(R.id.buttonModifier);
        buttonSave = findViewById(R.id.buttonSave);
        progressBar = findViewById(R.id.progressBar);

        // Au début, tous les champs sont désactivés et seul le bouton Modifier est visible
        setFieldsEditable(false);
        buttonSave.setVisibility(View.GONE);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // L'utilisateur n'est pas connecté, rediriger vers la page de connexion
            Toast.makeText(this, "Veuillez vous connecter pour accéder à votre profil", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Déterminer le type d'utilisateur
        checkUserType(currentUser.getEmail());

        // Bouton pour passer en mode édition
        buttonModifier.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditMode = true;
                setFieldsEditable(true);
                buttonModifier.setVisibility(View.GONE);
                buttonSave.setVisibility(View.VISIBLE);
            }
        });

        // Bouton pour enregistrer les modifications
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }
        });
    }

    private void setFieldsEditable(boolean editable) {
        editTextNom.setEnabled(editable);
        editTextPrenom.setEnabled(editable);
        // L'email reste toujours non modifiable
        editTextEmail.setEnabled(false);
        editTextAdresse.setEnabled(editable);
        editTextVille.setEnabled(editable);
        editTextPays.setEnabled(editable);
        editTextTelephone.setEnabled(editable);
    }

    private void checkUserType(String email) {
        progressBar.setVisibility(View.VISIBLE);

        // Vérifier d'abord dans la collection agents
        db.collection("agents").document(email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // L'utilisateur est un agent
                            userType = "agents";
                            userRef = db.collection("agents").document(email);
                            loadUserProfile(document);
                        } else {
                            // Vérifier dans la collection clients
                            db.collection("clients").document(email)
                                    .get()
                                    .addOnCompleteListener(clientTask -> {
                                        if (clientTask.isSuccessful()) {
                                            DocumentSnapshot clientDocument = clientTask.getResult();
                                            if (clientDocument.exists()) {
                                                // L'utilisateur est un client
                                                userType = "clients";
                                                userRef = db.collection("clients").document(email);
                                                loadUserProfile(clientDocument);
                                            } else {
                                                // Nouvel utilisateur, demander le type
                                                Toast.makeText(ProfileActivity.this,
                                                        "Impossible de déterminer le type d'utilisateur",
                                                        Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        } else {
                                            Log.e(TAG, "Erreur lors de la vérification client", clientTask.getException());
                                            Toast.makeText(ProfileActivity.this,
                                                    "Erreur: " + clientTask.getException().getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    } else {
                        Log.e(TAG, "Erreur lors de la vérification agent", task.getException());
                        Toast.makeText(ProfileActivity.this,
                                "Erreur: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }
                });
    }

    private void loadUserProfile(DocumentSnapshot document) {
        UserProfile userProfile = document.toObject(UserProfile.class);
        if (userProfile != null) {
            // Remplir les champs avec les données de l'utilisateur
            editTextNom.setText(userProfile.getNom());
            editTextPrenom.setText(userProfile.getPrenom());
            editTextEmail.setText(userProfile.getEmail());
            editTextAdresse.setText(userProfile.getAdresse());
            editTextVille.setText(userProfile.getVille());
            editTextPays.setText(userProfile.getPays());
            editTextTelephone.setText(userProfile.getTelephone());
        }
        progressBar.setVisibility(View.GONE);
    }

    private void saveUserProfile() {
        progressBar.setVisibility(View.VISIBLE);

        String nom = editTextNom.getText().toString().trim();
        String prenom = editTextPrenom.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String adresse = editTextAdresse.getText().toString().trim();
        String ville = editTextVille.getText().toString().trim();
        String pays = editTextPays.getText().toString().trim();
        String telephone = editTextTelephone.getText().toString().trim();

        // Validation des champs
        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs obligatoires", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Création de l'objet UserProfile
        UserProfile userProfile = new UserProfile(email, nom, prenom, adresse, ville, pays, telephone);

        // Sauvegarde dans Firestore
        userRef.set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ProfileActivity.this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                    // Revenir en mode affichage
                    isEditMode = false;
                    setFieldsEditable(false);
                    buttonModifier.setVisibility(View.VISIBLE);
                    buttonSave.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Erreur lors de la mise à jour du profil", e);
                    Toast.makeText(ProfileActivity.this,
                            "Erreur lors de la mise à jour du profil: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}