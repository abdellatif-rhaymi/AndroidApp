package com.example.location.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.location.R;
import com.example.location.activities.ClientOffreDetail;
import com.example.location.activities.Demande;
import com.example.location.activities.OffreDetailActivity;
import com.example.location.model.Offre;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OffreAdapter extends RecyclerView.Adapter<OffreAdapter.OffreViewHolder> {
    private List<Offre> offres = new ArrayList<>();
    private Context context;
    private boolean isClient;

    public OffreAdapter(List<Offre> offres, Context context, boolean isClient) {
        this.offres = offres;
        this.context = context;
        this.isClient = isClient; // On stocke si c'est un client
    }

    @NonNull
    @Override
    public OffreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.offre_item_layout, parent, false);
        return new OffreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OffreViewHolder holder, int position) {
        Offre offre = offres.get(position);

        Log.d("omar", "Titre: " + offre.getTitre() + ", Description: " + offre.getDescription() + ", Prix: " + offre.getPrix());

        holder.textTitre.setText(offre.getTitre());
        holder.textDescription.setText(offre.getDescription());
        holder.textPrix.setText(offre.getPrix() + " MAD");

        // Charger l'image avec Glide
        if (offre.getPhoto() != null && !offre.getPhoto().isEmpty()) {
            File imgFile = new File(context.getExternalFilesDir(null), "images/" + offre.getPhoto());
            if (imgFile.exists()) {
                // 🔹 Charger l'image depuis le stockage local
                Glide.with(context)
                        .load(imgFile)
                        .into(holder.imageOffre);
            } else {
                Log.e("LoadImage", "Fichier non trouvé : " + imgFile.getAbsolutePath());
            }
        }

        // Ajouter un listener de clic sur l'élément entier
        holder.itemView.setOnClickListener(view -> {
            if (isClient) {
                // Rediriger vers DemandeActivity pour lancer une demande
                Intent intent = new Intent(context, ClientOffreDetail.class);
                // Ajouter l'ID de l'offre et l'email de l'agent (si nécessaire)
                intent.putExtra("OFFRE_ID", offre.getId()); // Vous devez ajouter un getter pour l'ID dans la classe Offre
                intent.putExtra("AGENT_EMAIL", offre.getAgentEmail()); // Vous devez ajouter un getter pour l'email de l'agent dans la classe Offre

                // Passer toutes les données de l'offre à l'activité de détail
                intent.putExtra("OFFRE_TITRE", offre.getTitre());
                intent.putExtra("OFFRE_DESCRIPTION", offre.getDescription());
                intent.putExtra("OFFRE_PRIX", offre.getPrix());
                intent.putExtra("OFFRE_ETAGE", offre.getEtage());
                intent.putExtra("OFFRE_LOYER", offre.getLoyer());
                intent.putExtra("OFFRE_PHOTO", offre.getPhoto());
                intent.putExtra("OFFRE_PIECES", offre.getPieces());
                intent.putExtra("OFFRE_SDB", offre.getSdb());
                intent.putExtra("OFFRE_SUPERFICIE", offre.getSuperficie());
                context.startActivity(intent);
            } else {
                // Rediriger vers OffreDetailActivity (cas agent)
                Intent intent = new Intent(context, OffreDetailActivity.class);
                // Ajouter l'ID de l'offre et l'email de l'agent (si nécessaire)
                intent.putExtra("OFFRE_ID", offre.getId()); // Vous devez ajouter un getter pour l'ID dans la classe Offre
                intent.putExtra("AGENT_EMAIL", offre.getAgentEmail()); // Vous devez ajouter un getter pour l'email de l'agent dans la classe Offre

                // Passer toutes les données de l'offre à l'activité de détail
                intent.putExtra("OFFRE_TITRE", offre.getTitre());
                intent.putExtra("OFFRE_DESCRIPTION", offre.getDescription());
                intent.putExtra("OFFRE_PRIX", offre.getPrix());
                intent.putExtra("OFFRE_ETAGE", offre.getEtage());
                intent.putExtra("OFFRE_LOYER", offre.getLoyer());
                intent.putExtra("OFFRE_PHOTO", offre.getPhoto());
                intent.putExtra("OFFRE_PIECES", offre.getPieces());
                intent.putExtra("OFFRE_SDB", offre.getSdb());
                intent.putExtra("OFFRE_SUPERFICIE", offre.getSuperficie());
                context.startActivity(intent);
            }
        });
    }
    @Override
    public int getItemCount() {
        return (offres != null) ? offres.size() : 0;
    }

    public static class OffreViewHolder extends RecyclerView.ViewHolder {
        TextView textTitre, textDescription, textPrix;
        ImageView imageOffre;

        public OffreViewHolder(View itemView) {
            super(itemView);
            textTitre = itemView.findViewById(R.id.textTitre);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPrix = itemView.findViewById(R.id.textPrix);
            imageOffre = itemView.findViewById(R.id.imageOffre);
        }
    }
}

