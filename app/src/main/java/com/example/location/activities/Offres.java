package com.example.location.activities;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.location.R;
import com.example.location.adapters.OffreAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import com.example.location.model.Offre; // ✅ Met à jour l'import


public class Offres extends AppCompatActivity {
    private RecyclerView recyclerView;
    private OffreAdapter adapter;
    private List<Offre> offresList = new ArrayList<>();
    private FirebaseFirestore db;
    private String agentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offres);

        // Initialiser Firestore
        db = FirebaseFirestore.getInstance();
        agentEmail = getIntent().getStringExtra("AGENT_EMAIL");

        // Configurer RecyclerView
        recyclerView = findViewById(R.id.recyclerViewOffres);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OffreAdapter(offresList, this);
        recyclerView.setAdapter(adapter);

        // Charger les offres
        fetchOffres();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void fetchOffres() {
        db.collection("agents")
                .document(agentEmail)
                .collection("offres")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        offresList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Offre offre = document.toObject(Offre.class);
                            offresList.add(offre);
                        }
                        adapter.notifyDataSetChanged(); // ✅ Mise à jour du RecyclerView
                    } else {
                        Log.e("OffresActivity", "Erreur Firestore", task.getException());
                    }
                });
    }

}

