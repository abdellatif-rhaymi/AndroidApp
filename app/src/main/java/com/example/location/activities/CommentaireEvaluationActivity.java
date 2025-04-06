package com.example.location.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.adapters.CommentairesAdapter;
import com.example.location.model.Commentaire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentaireEvaluationActivity extends AppCompatActivity {

    private static final String TAG = "CommentaireEvaluation";

    // Vues
    private TextView textViewTitreOffre;
    private RatingBar ratingBarOffre;
    private EditText editTextCommentaire;
    private Button btnEnvoyerCommentaire;
    private RecyclerView recyclerViewCommentaires;
    private TextView textViewAucunCommentaire;

    // Firebase
    private FirebaseFirestore db;
    private String clientEmail;

    // Données
    private String offreId;
    private String agentEmail;
    private String titreOffre;
    private boolean aDejaEvalue = false;
    private boolean aDejaCommente = false;
    private String commentaireExistantId;
    private String evaluationExistanteId;

    // Adapter pour les commentaires
    private CommentairesAdapter commentairesAdapter;
    private List<Commentaire> commentairesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentaire_evaluation);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();
        clientEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // Récupérer les données de l'intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");
            titreOffre = extras.getString("TITRE_OFFRE", "Offre immobilière");
        } else {
            Toast.makeText(this, "Erreur: Informations manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialiser les vues
        initViews();

        // Vérifier si l'utilisateur a déjà évalué ou commenté
        verifierEvaluationExistante();
        verifierCommentaireExistant();

        // Charger les commentaires existants
        chargerCommentaires();
    }

    private void initViews() {
        textViewTitreOffre = findViewById(R.id.textViewTitreOffre);
        ratingBarOffre = findViewById(R.id.ratingBarOffre);
        editTextCommentaire = findViewById(R.id.editTextCommentaire);
        btnEnvoyerCommentaire = findViewById(R.id.btnEnvoyerCommentaire);
        recyclerViewCommentaires = findViewById(R.id.recyclerViewCommentaires);
        textViewAucunCommentaire = findViewById(R.id.textViewAucunCommentaire);

        // Définir le titre de l'offre
        textViewTitreOffre.setText("Commenter: " + titreOffre);

        // Configurer RecyclerView
        commentairesList = new ArrayList<>();
        commentairesAdapter = new CommentairesAdapter(this, commentairesList);
        recyclerViewCommentaires.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCommentaires.setAdapter(commentairesAdapter);

        // Définir le listener du bouton
        btnEnvoyerCommentaire.setOnClickListener(v -> soumettreEvaluationEtCommentaire());
    }

    private void verifierEvaluationExistante() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("evaluation")
                .whereEqualTo("client", clientEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Le client a déjà évalué cette offre
                        aDejaEvalue = true;
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        evaluationExistanteId = documentSnapshot.getId();

                        // Récupérer le score existant
                        Long scoreLong = documentSnapshot.getLong("score");
                        float score = scoreLong != null ? scoreLong.floatValue() : 0f;
                        ratingBarOffre.setRating(score);

                        Log.d(TAG, "Évaluation existante trouvée: " + evaluationExistanteId + ", score: " + score);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la vérification de l'évaluation", e);
                    Toast.makeText(this, "Erreur lors de la vérification de l'évaluation", Toast.LENGTH_SHORT).show();
                });
    }

    private void verifierCommentaireExistant() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("commentaires")
                .whereEqualTo("client", clientEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Le client a déjà commenté cette offre
                        aDejaCommente = true;
                        QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        commentaireExistantId = documentSnapshot.getId();

                        // Récupérer le commentaire existant
                        String commentaire = documentSnapshot.getString("commentaire");
                        if (commentaire != null) {
                            editTextCommentaire.setText(commentaire);
                        }

                        Log.d(TAG, "Commentaire existant trouvé: " + commentaireExistantId);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la vérification du commentaire", e);
                    Toast.makeText(this, "Erreur lors de la vérification du commentaire", Toast.LENGTH_SHORT).show();
                });
    }

    private void chargerCommentaires() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("commentaires")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentairesList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewAucunCommentaire.setVisibility(View.VISIBLE);
                        recyclerViewCommentaires.setVisibility(View.GONE);
                    } else {
                        textViewAucunCommentaire.setVisibility(View.GONE);
                        recyclerViewCommentaires.setVisibility(View.VISIBLE);

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String commentaire = document.getString("commentaire");
                            String client = document.getString("client");
                            Date date = document.getDate("date");

                            if (commentaire != null && client != null && date != null) {
                                Commentaire com = new Commentaire(client, commentaire, date);
                                commentairesList.add(com);
                            }
                        }

                        commentairesAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors du chargement des commentaires", e);
                    Toast.makeText(this, "Erreur lors du chargement des commentaires", Toast.LENGTH_SHORT).show();
                });
    }

    private void soumettreEvaluationEtCommentaire() {
        float score = ratingBarOffre.getRating();
        String commentaire = editTextCommentaire.getText().toString().trim();

        if (score == 0) {
            Toast.makeText(this, "Veuillez attribuer une note", Toast.LENGTH_SHORT).show();
            return;
        }

        if (commentaire.isEmpty()) {
            Toast.makeText(this, "Veuillez écrire un commentaire", Toast.LENGTH_SHORT).show();
            return;
        }

        // Désactiver le bouton pour éviter les soumissions multiples
        btnEnvoyerCommentaire.setEnabled(false);

        // Soumettre à la fois l'évaluation et le commentaire
        soumettreEvaluation((int) score, commentaire);
    }

    private void soumettreEvaluation(int score, String commentaire) {
        Map<String, Object> evaluation = new HashMap<>();
        evaluation.put("client", clientEmail);
        evaluation.put("score", score);
        evaluation.put("date", new Date());

        if (aDejaEvalue) {
            // Mettre à jour l'évaluation existante
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("evaluation")
                    .document(evaluationExistanteId)
                    .update(evaluation)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Évaluation mise à jour avec succès");
                        soumettreCommentaire(commentaire);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour de l'évaluation", e);
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Erreur lors de la mise à jour de l'évaluation", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);
                    });
        } else {
            // Créer une nouvelle évaluation
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("evaluation")
                    .add(evaluation)
                    .addOnSuccessListener(documentReference -> {
                        evaluationExistanteId = documentReference.getId();
                        aDejaEvalue = true;
                        Log.d(TAG, "Nouvelle évaluation créée avec ID: " + evaluationExistanteId);
                        soumettreCommentaire(commentaire);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la création de l'évaluation", e);
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Erreur lors de la création de l'évaluation", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);
                    });
        }
    }

    private void soumettreCommentaire(String texteCommentaire) {
        Map<String, Object> commentaireData = new HashMap<>();
        commentaireData.put("client", clientEmail);
        commentaireData.put("commentaire", texteCommentaire);
        commentaireData.put("date", new Date());

        if (aDejaCommente) {
            // Mettre à jour le commentaire existant
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("commentaires")
                    .document(commentaireExistantId)
                    .update(commentaireData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Commentaire mis à jour avec succès");
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Votre évaluation a été mise à jour", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);

                        // Recharger les commentaires
                        chargerCommentaires();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la mise à jour du commentaire", e);
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Erreur lors de la mise à jour du commentaire", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);
                    });
        } else {
            // Créer un nouveau commentaire
            db.collection("agents")
                    .document(agentEmail)
                    .collection("offres")
                    .document(offreId)
                    .collection("commentaires")
                    .add(commentaireData)
                    .addOnSuccessListener(documentReference -> {
                        commentaireExistantId = documentReference.getId();
                        aDejaCommente = true;
                        Log.d(TAG, "Nouveau commentaire créé avec ID: " + commentaireExistantId);
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Votre évaluation a été soumise", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);

                        // Recharger les commentaires
                        chargerCommentaires();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Erreur lors de la création du commentaire", e);
                        Toast.makeText(CommentaireEvaluationActivity.this,
                                "Erreur lors de la création du commentaire", Toast.LENGTH_SHORT).show();
                        btnEnvoyerCommentaire.setEnabled(true);
                    });
        }
    }
}