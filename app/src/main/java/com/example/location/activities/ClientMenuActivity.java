package com.example.location.activities;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.location.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ClientMenuActivity extends AppCompatActivity {

    private static final String TAG = "ClientMenuActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView welcomeText;
    private Button btnConsulterOffres, btnGererDemandes, btnModifierProfil, btnDeconnexion;

    private String clientEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_menu);

        // Initialisation Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Récupération des éléments UI
        welcomeText = findViewById(R.id.welcomeText);
        btnConsulterOffres = findViewById(R.id.btnConsulterOffres);
        btnGererDemandes = findViewById(R.id.btnGererDemandes);
        btnModifierProfil = findViewById(R.id.btnModifierProfil);
        btnDeconnexion = findViewById(R.id.btnDeconnexion);

        // Récupération de l'email du client
        clientEmail = getIntent().getStringExtra("CLIENT_EMAIL");

        if (clientEmail != null) {
            chargerInformationsClient();
        } else {
            Toast.makeText(this, "Erreur : Email client introuvable", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Email client non reçu dans l'intent.");
        }

        // Gestion des clics des boutons
        btnConsulterOffres.setOnClickListener(v -> ouvrirConsulterOffres());
        btnGererDemandes.setOnClickListener(v -> ouvrirGererDemandes());
        btnModifierProfil.setOnClickListener(v -> ouvrirModifierProfil());
        btnDeconnexion.setOnClickListener(v -> deconnecterUtilisateur());
    }

    /**
     * Charge les informations du client depuis Firestore
     */
    private void chargerInformationsClient() {
        DocumentReference docRef = db.collection("clients").document(clientEmail);
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String nom = documentSnapshot.getString("nom");
                String prenom = documentSnapshot.getString("prenom");
                welcomeText.setText("Bienvenue, " + prenom + " " + nom + " !");
            } else {
                Toast.makeText(ClientMenuActivity.this, "Informations client introuvables.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la récupération des données client", e));
    }

    /**
     * Ouvre l'écran pour consulter les offres
     */
    private void ouvrirConsulterOffres() {
        Intent intent = new Intent(ClientMenuActivity.this, ConsulterOffresActivity.class);
        intent.putExtra("CLIENT_EMAIL", clientEmail);
        startActivity(intent);
    }

    /**
     * Ouvre l'écran pour gérer les demandes du client
     */
    private void ouvrirGererDemandes() {
        Intent intent = new Intent(ClientMenuActivity.this, ClientDemandesActivity.class);
        intent.putExtra("CLIENT_EMAIL", clientEmail);
        startActivity(intent);
    }

    /**
     * Ouvre l'écran pour modifier le profil du client
     */
    private void ouvrirModifierProfil() {
        Intent intent = new Intent(ClientMenuActivity.this, ProfileActivity.class);
        intent.putExtra("CLIENT_EMAIL", clientEmail);
        startActivity(intent);
    }

    /**
     * Déconnecte l'utilisateur et le redirige vers l'écran d'authentification
     */
    private void deconnecterUtilisateur() {
        mAuth.signOut();
        Toast.makeText(ClientMenuActivity.this, "Déconnexion réussie", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ClientMenuActivity.this, Authentification.class);
        startActivity(intent);
        finish();
    }
}
