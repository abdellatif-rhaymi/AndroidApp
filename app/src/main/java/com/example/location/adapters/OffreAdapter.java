package com.example.location.adapters;

import android.content.Context;
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
import com.example.location.activities.Offres;
import com.example.location.model.Offre;

import java.util.ArrayList;
import java.util.List;

public class OffreAdapter extends RecyclerView.Adapter<OffreAdapter.OffreViewHolder> {
    private List<Offre> offres = new ArrayList<>(); // ✅ Initialise pour éviter `null`

    private Context context;

    public OffreAdapter(List<Offre> offres, Context context) {
        this.offres = offres;
        this.context = context;
    }

    public OffreAdapter(List<com.example.location.activities.Offre> offresList, Offres context) {
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

    }



    @Override
    public int getItemCount() {
        return (offres != null) ? offres.size() : 0; // ✅ Évite le crash
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
