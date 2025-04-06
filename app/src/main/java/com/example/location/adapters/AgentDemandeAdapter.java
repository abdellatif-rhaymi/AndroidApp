package com.example.location.adapters;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.activities.AgentDemandeDetailActivity;
import com.example.location.model.Demande;

import java.util.List;

public class AgentDemandeAdapter extends RecyclerView.Adapter<AgentDemandeAdapter.DemandeViewHolder> {
    private static final String TAG = "AgentDemandeAdapter";
    private List<Demande> demandes;
    private Context context;
    private DemandeActionListener actionListener;

    public interface DemandeActionListener {
        void onAcceptDemande(Demande demande);
        void onRejectDemande(Demande demande);
        void onDemandeDetails(Demande demande);
    }

    public AgentDemandeAdapter(List<Demande> demandes, Context context, DemandeActionListener listener) {
        this.demandes = demandes;
        this.context = context;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public DemandeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.agent_demande_item_layout, parent, false);
            return new DemandeViewHolder(view);
        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onCreateViewHolder", e);
            Toast.makeText(context, "Erreur d'affichage", Toast.LENGTH_SHORT).show();
            View fallbackView = new View(context);
            fallbackView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new DemandeViewHolder(fallbackView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull DemandeViewHolder holder, int position) {
        try {
            Demande demande = demandes.get(position);

            // Vérifier que les vues existent
            if (holder.itemView == null) {
                return;
            }

            // Informations sur le client
            String clientName = demande.getClientName();
            if (holder.textClientName != null) {
                holder.textClientName.setText(clientName != null ? clientName : "Client: " + demande.getClientEmail());
            }

            if (holder.textClientPhone != null) {
                if (demande.getClientPhone() != null && !demande.getClientPhone().isEmpty()) {
                    holder.textClientPhone.setText("Tél: " + demande.getClientPhone());
                    holder.textClientPhone.setVisibility(View.VISIBLE);
                } else {
                    holder.textClientPhone.setVisibility(View.GONE);
                }
            }

            if (holder.textClientVille != null) {
                if (demande.getClientVille() != null && !demande.getClientVille().isEmpty()) {
                    holder.textClientVille.setText(demande.getClientVille() + ", " + (demande.getClientPays() != null ? demande.getClientPays() : ""));
                    holder.textClientVille.setVisibility(View.VISIBLE);
                } else {
                    holder.textClientVille.setVisibility(View.GONE);
                }
            }

            // Informations sur l'offre
            if (holder.textPropertyTitle != null) {
                String offreTitle = demande.getOffreTitle();
                holder.textPropertyTitle.setText("Bien: " + (offreTitle != null ? offreTitle : "Offre " + demande.getOffreId()));
            }

            // Référence de la demande
            if (holder.textReference != null) {
                holder.textReference.setText("Réf: " + demande.getReference());
            }

            // Informations de la demande
            if (holder.textDuree != null) {
                if (demande.getDuree() != null && !demande.getDuree().isEmpty()) {
                    holder.textDuree.setText("Durée: " + demande.getDuree());
                    holder.textDuree.setVisibility(View.VISIBLE);
                } else {
                    holder.textDuree.setVisibility(View.GONE);
                }
            }

            if (holder.textLoyer != null) {
                if (demande.getLoyer() != null && !demande.getLoyer().isEmpty()) {
                    holder.textLoyer.setText("Budget: " + demande.getLoyer() + " MAD");
                    holder.textLoyer.setVisibility(View.VISIBLE);
                } else {
                    holder.textLoyer.setVisibility(View.GONE);
                }
            }

            if (holder.textEnfants != null) {
                if (demande.getEnfants() != null && !demande.getEnfants().isEmpty()) {
                    holder.textEnfants.setText("Enfants: " + demande.getEnfants());
                    holder.textEnfants.setVisibility(View.VISIBLE);
                } else {
                    holder.textEnfants.setVisibility(View.GONE);
                }
            }

            if (holder.textSituationP != null) {
                if (demande.getSituationP() != null && !demande.getSituationP().isEmpty()) {
                    holder.textSituationP.setText("Situation pro: " + demande.getSituationP());
                    holder.textSituationP.setVisibility(View.VISIBLE);
                } else {
                    holder.textSituationP.setVisibility(View.GONE);
                }
            }

            // Afficher le statut
            if (holder.textStatus != null) {
                String status = demande.getStatus();
                holder.textStatus.setText("Statut: " + (status != null ? status : "En attente"));

                // Définir la couleur en fonction du statut
                String statusLower = status != null ? status.toLowerCase() : "en attente";
                switch (statusLower) {
                    case "acceptée":
                        holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.green));
                        if (holder.cardView != null) {
                            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_green));
                        }
                        break;
                    case "refusée":
                        holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.red));
                        if (holder.cardView != null) {
                            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.light_red));
                        }
                        break;
                    default: // En attente
                        holder.textStatus.setTextColor(ContextCompat.getColor(context, R.color.blue));
                        if (holder.cardView != null) {
                            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
                        }
                }
            }

            // Gestion des boutons en fonction du statut
            boolean isInProgress = demande.getStatus() == null || demande.getStatus().equalsIgnoreCase("En attente");

            if (holder.layoutButtons != null) {
                holder.layoutButtons.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
            }

            if (holder.btnAccept != null) {
                holder.btnAccept.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
                holder.btnAccept.setOnClickListener(v -> {
                    try {
                        if (actionListener != null) {
                            Log.d(TAG, "Bouton Accepter cliqué pour: " + demande.getId());
                            actionListener.onAcceptDemande(demande);
                        } else {
                            Log.e(TAG, "actionListener est null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de l'acceptation", e);
                        Toast.makeText(context, "Erreur lors de l'acceptation", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            if (holder.btnReject != null) {
                holder.btnReject.setVisibility(isInProgress ? View.VISIBLE : View.GONE);
                holder.btnReject.setOnClickListener(v -> {
                    try {
                        if (actionListener != null) {
                            Log.d(TAG, "Bouton Refuser cliqué pour: " + demande.getId());
                            actionListener.onRejectDemande(demande);
                        } else {
                            Log.e(TAG, "actionListener est null");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors du refus", e);
                        Toast.makeText(context, "Erreur lors du refus", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Configurer le clic sur la carte
            holder.itemView.setOnClickListener(v -> {
                try {
                    Log.d(TAG, "Clic sur l'élément: " + demande.getId());
                    if (actionListener != null) {
                        actionListener.onDemandeDetails(demande);
                    } else {
                        // Fallback au cas où l'interface n'est pas implémentée
                        Intent intent = new Intent(context, AgentDemandeDetailActivity.class);
                        intent.putExtra("DEMANDE_ID", demande.getId());
                        intent.putExtra("OFFRE_ID", demande.getOffreId());
                        intent.putExtra("CLIENT_EMAIL", demande.getClientEmail());
                        context.startActivity(intent);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Erreur lors de l'ouverture des détails", e);
                    Toast.makeText(context, "Erreur lors de l'ouverture des détails", Toast.LENGTH_SHORT).show();
                }
            });

            if (holder.cardView != null) {
                holder.cardView.setOnClickListener(v -> {
                    try {
                        Log.d(TAG, "Clic sur la carte: " + demande.getId());
                        if (actionListener != null) {
                            actionListener.onDemandeDetails(demande);
                        } else {
                            // Fallback au cas où l'interface n'est pas implémentée
                            Intent intent = new Intent(context, AgentDemandeDetailActivity.class);
                            intent.putExtra("DEMANDE_ID", demande.getId());
                            intent.putExtra("OFFRE_ID", demande.getOffreId());
                            intent.putExtra("CLIENT_EMAIL", demande.getClientEmail());
                            context.startActivity(intent);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erreur lors de l'ouverture des détails via cardView", e);
                        Toast.makeText(context, "Erreur lors de l'ouverture des détails", Toast.LENGTH_SHORT).show();
                    }
                });
            }

        } catch (Exception e) {
            Log.e(TAG, "Erreur dans onBindViewHolder", e);
        }
    }

    @Override
    public int getItemCount() {
        return demandes != null ? demandes.size() : 0;
    }

    static class DemandeViewHolder extends RecyclerView.ViewHolder {
        TextView textClientName, textClientPhone, textClientVille;
        TextView textReference, textPropertyTitle;
        TextView textDuree, textLoyer, textEnfants, textSituationP, textStatus;
        Button btnAccept, btnReject;
        CardView cardView;
        View layoutButtons;

        DemandeViewHolder(View itemView) {
            super(itemView);
            try {
                cardView = itemView.findViewById(R.id.cardViewDemande);
                textClientName = itemView.findViewById(R.id.textClientName);
                textClientPhone = itemView.findViewById(R.id.textClientPhone);
                textClientVille = itemView.findViewById(R.id.textClientVille);
                textReference = itemView.findViewById(R.id.textReference);
                textPropertyTitle = itemView.findViewById(R.id.textPropertyTitle);
                textDuree = itemView.findViewById(R.id.textDuree);
                textLoyer = itemView.findViewById(R.id.textLoyer);
                textEnfants = itemView.findViewById(R.id.textEnfants);
                textSituationP = itemView.findViewById(R.id.textSituationP);
                textStatus = itemView.findViewById(R.id.textStatus);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnReject = itemView.findViewById(R.id.btnReject);
                layoutButtons = itemView.findViewById(R.id.layoutButtons);
            } catch (Exception e) {
                Log.e("DemandeViewHolder", "Erreur dans le constructeur", e);
            }
        }
    }
}