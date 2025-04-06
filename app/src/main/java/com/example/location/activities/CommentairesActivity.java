package com.example.location.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.adapters.CommentaireAdapter;
import com.example.location.model.Commentaire;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CommentairesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCommentaires;
    private TextView textViewAucunCommentaire;
    private CommentaireAdapter adapter;
    private List<Commentaire> commentaireList;
    private FirebaseFirestore db;
    private String offreId, agentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commentaires);

        // 🔥 Initialiser les vues
        recyclerViewCommentaires = findViewById(R.id.recyclerViewCommentaires);
        textViewAucunCommentaire = findViewById(R.id.textViewAucunCommentaire);

        // 🔥 Configurer le RecyclerView
        recyclerViewCommentaires.setLayoutManager(new LinearLayoutManager(this));
        commentaireList = new ArrayList<>();
        adapter = new CommentaireAdapter(commentaireList);
        recyclerViewCommentaires.setAdapter(adapter);

        // 🔥 Initialiser Firestore
        db = FirebaseFirestore.getInstance();

        // 🔥 Récupérer les données de l'intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            offreId = extras.getString("OFFRE_ID");
            agentEmail = extras.getString("AGENT_EMAIL");

            // 🔥 Charger les commentaires
            chargerCommentaires();
        } else {
            Toast.makeText(this, "Erreur : données manquantes", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void chargerCommentaires() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .document(offreId)
                .collection("commentaires")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    commentaireList.clear();

                    if (queryDocumentSnapshots.isEmpty()) {
                        textViewAucunCommentaire.setVisibility(android.view.View.VISIBLE);
                        recyclerViewCommentaires.setVisibility(android.view.View.GONE);
                    } else {
                        textViewAucunCommentaire.setVisibility(android.view.View.GONE);
                        recyclerViewCommentaires.setVisibility(android.view.View.VISIBLE);

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String client = document.getString("client");
                            String commentaire = document.getString("commentaire");

                            commentaireList.add(new Commentaire(document.getId(), client, commentaire));
                        }

                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CommentairesActivity.this, "Erreur lors du chargement des commentaires", Toast.LENGTH_SHORT).show();
                });
    }
}