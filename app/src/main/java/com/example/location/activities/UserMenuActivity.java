package com.example.location.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.location.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserMenuActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_menu);

        // Initialisation Firebase Auth
        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Lier les composants UI
        TextView textViewUserInfo = findViewById(R.id.textViewUserInfo);
        Button buttonGestionOffres = findViewById(R.id.buttonGestionOffres);
        Button buttonGestionProfil = findViewById(R.id.buttonGestionProfil);
        Button buttonConsultationDemandes = findViewById(R.id.buttonConsultationDemandes);
        Button buttonDeconnexion = findViewById(R.id.buttonDeconnexion);

        // Affichage de l'email de l'utilisateur connecté
        if (user != null) {
            textViewUserInfo.setText("Bienvenue, " + user.getEmail());
        } else {
            textViewUserInfo.setText("Utilisateur non connecté");
        }

        // Gestion des événements des boutons
        buttonGestionOffres.setOnClickListener(v -> {
            Intent intent = new Intent(UserMenuActivity.this, Offres.class);
            intent.putExtra("AGENT_EMAIL", user != null ? user.getEmail() : "");
            startActivity(intent);
        });

        buttonGestionProfil.setOnClickListener(v -> {
            Intent intent = new Intent(UserMenuActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        buttonConsultationDemandes.setOnClickListener(v -> {
            Intent intent = new Intent(UserMenuActivity.this, AgentDemandesActivity.class);
            startActivity(intent);
        });

        buttonDeconnexion.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(UserMenuActivity.this, Authentification.class);
            startActivity(intent);
            finish(); // Ferme cette activité après déconnexion
        });
    }
}
