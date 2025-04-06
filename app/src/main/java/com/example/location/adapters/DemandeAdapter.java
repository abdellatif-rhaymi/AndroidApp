package com.example.location.adapters;

import android.app.MediaRouteButton;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.location.R;
import com.example.location.activities.ClientDemandeDetailActivity;
import com.example.location.model.Demande;
import java.util.List;

public class DemandeAdapter extends RecyclerView.Adapter<DemandeAdapter.DemandeViewHolder> {
    private List<Demande> demandes;
    private Context context;

    public DemandeAdapter(List<Demande> demandes, Context context) {
        this.demandes = demandes;
        this.context = context;
    }

    @NonNull
    @Override
    public DemandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.demande_item_layout, parent, false);
        return new DemandeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DemandeViewHolder holder, int position) {
        Demande demande = demandes.get(position);

        // Mise à jour des champs en fonction de votre structure Firestore
        holder.textReference.setText("Référence: " + demande.getReference());
        holder.textOffreId.setText("Offre: " + demande.getOffreId());
        holder.textAgentEmail.setText("Agent: " + demande.getAgentEmail());

        // Affichage des informations supplémentaires
        if (demande.getDuree() != null && !demande.getDuree().isEmpty()) {
            holder.textDuree.setText("Durée: " + demande.getDuree());
            holder.textDuree.setVisibility(View.VISIBLE);
        } else {
            holder.textDuree.setVisibility(View.GONE);
        }

        if (demande.getLoyer() != null && !demande.getLoyer().isEmpty()) {
            holder.textLoyer.setText("Loyer max: " + demande.getLoyer());
            holder.textLoyer.setVisibility(View.VISIBLE);
        } else {
            holder.textLoyer.setVisibility(View.GONE);
        }

        if (demande.getEnfants() != null && !demande.getEnfants().isEmpty()) {
            holder.textEnfants.setText("Enfants: " + demande.getEnfants());
            holder.textEnfants.setVisibility(View.VISIBLE);
        } else {
            holder.textEnfants.setVisibility(View.GONE);
        }

        if (demande.getSituationP() != null && !demande.getSituationP().isEmpty()) {
            holder.textSituationP.setText("Situation pro.: " + demande.getSituationP());
            holder.textSituationP.setVisibility(View.VISIBLE);
        } else {
            holder.textSituationP.setVisibility(View.GONE);
        }

        if (demande.getSituationF() != null && !demande.getSituationF().isEmpty()) {
            holder.textSituationF.setText("Situation fin.: " + demande.getSituationF());
            holder.textSituationF.setVisibility(View.VISIBLE);
        } else {
            holder.textSituationF.setVisibility(View.GONE);
        }

        if (demande.getMessage() != null && !demande.getMessage().isEmpty()) {
            holder.textMessage.setText("Message: " + demande.getMessage());
            holder.textMessage.setVisibility(View.VISIBLE);
        } else {
            holder.textMessage.setVisibility(View.GONE);
        }
        // Affichage de l'état
        if (demande.getStatus() != null && !demande.getStatus().isEmpty()) {
            holder.textStatus.setVisibility(View.VISIBLE);
            holder.textStatus.setText(demande.getStatus());
            // Vous pouvez ajouter ici une logique pour changer la couleur en fonction de l'état
            if (demande.getStatus().equals("Acceptée")) {
                holder.textStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            } else if (demande.getStatus().equals("Refusée")) {
                holder.textStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            } else {
                holder.textStatus.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            }
        } else {
            holder.textStatus.setVisibility(View.GONE);
        }

        // Ajouter un écouteur de clic pour ouvrir les détails
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ClientDemandeDetailActivity.class);
            intent.putExtra("DEMANDE_ID", demande.getId());
            intent.putExtra("OFFRE_ID", demande.getOffreId());
            intent.putExtra("AGENT_EMAIL", demande.getAgentEmail());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return demandes.size();
    }

    public static class DemandeViewHolder extends RecyclerView.ViewHolder {
        TextView textReference, textOffreId, textAgentEmail, textDuree, textLoyer,
                textEnfants, textSituationP, textSituationF, textMessage , textStatus;

        public DemandeViewHolder(View itemView) {
            super(itemView);
            textReference = itemView.findViewById(R.id.textReference);
            textOffreId = itemView.findViewById(R.id.textOffreId);
            textAgentEmail = itemView.findViewById(R.id.textAgentEmail);
            textDuree = itemView.findViewById(R.id.textDuree);
            textLoyer = itemView.findViewById(R.id.textLoyer);
            textEnfants = itemView.findViewById(R.id.textEnfants);
            textSituationP = itemView.findViewById(R.id.textSituationP);
            textSituationF = itemView.findViewById(R.id.textSituationF);
            textMessage = itemView.findViewById(R.id.textMessage);
            textStatus = itemView.findViewById(R.id.textStatus); // Initialisation de textStatus

        }
    }
}