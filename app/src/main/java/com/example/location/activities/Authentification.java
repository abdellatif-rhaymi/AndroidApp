package com.example.location.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.location.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Authentification extends AppCompatActivity implements View.OnClickListener {

    LinearLayout layout1;
    LinearLayout layout2;
    LinearLayout layout3;
    TextView userinfo;
    Button bttauth;
    Button bttacc;
    Button bttconfAcc;
    Button bttAnnuler;
    Button bttDeconnect;
    Button btnListOffers; // New button for listing offers
    EditText login;
    EditText password;
    EditText nom;
    EditText prenom;
    EditText pays;
    EditText adresse;
    EditText phone;
    EditText email;
    EditText ville;
    RadioButton agentRadio;
    RadioButton clientRadio;
    RadioGroup radioGroup;
    boolean typeUser = true; // true for agent, false for client
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    String nomUser;
    String prenomUser;
    private String agentEmail;
    EditText createPassword;



    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_authentification);

        // Initialize UI components
        layout1 = findViewById(R.id.Layout1);
        layout2 = findViewById(R.id.Layout2);
        layout3 = findViewById(R.id.Layout3);

        userinfo = findViewById(R.id.userTextView);
        email = findViewById(R.id.emailEditText);
        nom = findViewById(R.id.nomEditText);
        prenom = findViewById(R.id.prenomEditText);
        adresse = findViewById(R.id.adresseEditText);
        ville = findViewById(R.id.villeEditText);
        pays = findViewById(R.id.paysEditText);
        phone = findViewById(R.id.phoneEditText);
        createPassword = findViewById(R.id.createPasswordEditText);

        bttauth = findViewById(R.id.authButton);
        bttacc = findViewById(R.id.acchButton);
        bttconfAcc = findViewById(R.id.confirmeButton);
        bttAnnuler = findViewById(R.id.annulerButton);
        bttDeconnect = findViewById(R.id.deconnexionButton);
        btnListOffers = findViewById(R.id.btnListOffers); // Initialize the new button

        // Set click listeners
        bttauth.setOnClickListener(this);
        bttacc.setOnClickListener(this);
        bttconfAcc.setOnClickListener(this);
        bttAnnuler.setOnClickListener(this);
        bttDeconnect.setOnClickListener(this);

        // Handle "Liste des offres" button click
        btnListOffers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    // Pass the authenticated agent's email to OffersActivity
                    Intent intent = new Intent(Authentification.this, Offres.class);
                    intent.putExtra("AGENT_EMAIL", currentUser.getEmail());
                    startActivity(intent);
                } else {
                    Log.e(TAG, "User not logged in");
                }
            }
        });

        login = findViewById(R.id.loginEditText);
        password = findViewById(R.id.passwordEditText);

        agentRadio = findViewById(R.id.agentRadio);
        agentRadio.setChecked(true);
        clientRadio = findViewById(R.id.clientRadio);
        radioGroup = findViewById(R.id.typeuserRadioGroup);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (clientRadio.isChecked()) {
                typeUser = false;
            } else if (agentRadio.isChecked()) {
                typeUser = true;
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == bttacc.getId()) {
            layout1.setVisibility(View.GONE);
            layout2.setVisibility(View.VISIBLE);
            layout3.setVisibility(View.GONE);
        } else if (view.getId() == bttauth.getId()) {
            mAuth.signInWithEmailAndPassword(login.getText().toString(), password.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                Log.w(TAG, "signInWithEmailAndPassword:failure", task.getException());
                                updateUI(null);
                            }
                        }
                    });
        } else if (view.getId() == bttconfAcc.getId()) {
            // Vérification que tous les champs sont renseignés
            if (email.getText().toString().isEmpty() || nom.getText().toString().isEmpty() ||
                    prenom.getText().toString().isEmpty() || createPassword.getText().toString().isEmpty()) {
                Toast.makeText(Authentification.this, "Veuillez remplir tous les champs obligatoires",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Création d'un utilisateur dans Firebase Authentication
            mAuth.createUserWithEmailAndPassword(email.getText().toString(), createPassword.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                // Seulement après réussite de l'authentification, ajout dans Firestore
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("nom", nom.getText().toString());
                                userData.put("prenom", prenom.getText().toString());
                                userData.put("adresse", adresse.getText().toString());
                                userData.put("ville", ville.getText().toString());
                                userData.put("pays", pays.getText().toString());
                                userData.put("email", email.getText().toString());
                                userData.put("telephone", phone.getText().toString());

                                String collectionName = typeUser ? "agents" : "clients";
                                db.collection(collectionName).document(email.getText().toString())
                                        .set(userData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d(TAG, "DocumentSnapshot successfully written!");
                                                Toast.makeText(Authentification.this, "Compte créé avec succès",
                                                        Toast.LENGTH_SHORT).show();
                                                layout1.setVisibility(View.VISIBLE);
                                                layout2.setVisibility(View.GONE);
                                                layout3.setVisibility(View.GONE);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "Error writing document", e);
                                                Toast.makeText(Authentification.this,
                                                        "Erreur lors de l'enregistrement des données utilisateur",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } else {
                                Log.w(TAG, "createUserWithEmailAndPassword:failure", task.getException());
                                Toast.makeText(Authentification.this,
                                        "Échec de création du compte: " + task.getException().getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                updateUI(null);
                            }
                        }
                    });
        } else if (view.getId() == bttAnnuler.getId()) {
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
            layout3.setVisibility(View.GONE);
        } else if (view.getId() == bttDeconnect.getId()) {
            mAuth.signOut();
            updateUI(null);
        }
    }

    void updateUI(FirebaseUser currentUser) {
        if (currentUser != null) {
            String email = currentUser.getEmail();
            if (email == null) {
                Log.e(TAG, "L'email de l'utilisateur est null !");
                return;
            }

            // Vérifier si l'utilisateur est un agent ou un client
            db.collection("agents").document(email).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().exists()) {
                    // L'utilisateur est un agent
                    DocumentSnapshot document = task.getResult();
                    nomUser = document.getString("nom");
                    prenomUser = document.getString("prenom");
                    userinfo.setText("L'agent " + nomUser + " " + prenomUser + " est authentifié correctement");

                    // Masquer les layouts inutiles
                    layout1.setVisibility(View.GONE);
                    layout2.setVisibility(View.GONE);
                    layout3.setVisibility(View.GONE);

                    // Rediriger vers l'interface Agent
                    Intent intent = new Intent(Authentification.this, UserMenuActivity.class);
                    intent.putExtra("AGENT_EMAIL", email);
                    startActivity(intent);
                    finish(); // Empêche de revenir en arrière avec le bouton retour

                } else {
                    // Vérifier si c'est un client
                    db.collection("clients").document(email).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful() && task2.getResult().exists()) {
                            // L'utilisateur est un client
                            DocumentSnapshot document = task2.getResult();
                            nomUser = document.getString("nom");
                            prenomUser = document.getString("prenom");
                            userinfo.setText("Le client " + nomUser + " " + prenomUser + " est authentifié correctement");

                            layout1.setVisibility(View.GONE);
                            layout2.setVisibility(View.GONE);
                            layout3.setVisibility(View.GONE);

                            // Rediriger vers l'interface Client
                            Intent intent = new Intent(Authentification.this, ClientMenuActivity.class);
                            intent.putExtra("CLIENT_EMAIL", email);
                            startActivity(intent);
                            finish();

                        } else {
                            // Aucun document trouvé ni dans "agents" ni dans "clients"
                            Log.d(TAG, "Utilisateur non trouvé dans la base de données.");
                            Toast.makeText(Authentification.this, "Compte introuvable.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } else {
            // Aucun utilisateur connecté
            layout1.setVisibility(View.VISIBLE);
            layout2.setVisibility(View.GONE);
            layout3.setVisibility(View.GONE);
        }
    }

}