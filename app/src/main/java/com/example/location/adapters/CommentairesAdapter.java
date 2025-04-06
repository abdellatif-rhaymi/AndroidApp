package com.example.location.adapters;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.model.Commentaire;

import java.util.List;

public class CommentairesAdapter extends RecyclerView.Adapter<CommentairesAdapter.CommentaireViewHolder> {

    private final Context context;
    private final List<Commentaire> commentaires;

    public CommentairesAdapter(Context context, List<Commentaire> commentaires) {
        this.context = context;
        this.commentaires = commentaires;
    }

    @NonNull
    @Override
    public CommentaireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_commentaire, parent, false);
        return new CommentaireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentaireViewHolder holder, int position) {
        Commentaire commentaire = commentaires.get(position);

        // Afficher l'email du client (optionnellement, masquer une partie pour la confidentialité)
        String email = commentaire.getClient();
        int atIndex = email.indexOf('@');
        if (atIndex > 1) {
            String maskedEmail = email.substring(0, Math.min(3, atIndex)) + "..." + email.substring(atIndex);
            holder.textViewClient.setText(maskedEmail);
        } else {
            holder.textViewClient.setText(email);
        }

        // Afficher le texte du commentaire
        holder.textViewCommentaire.setText(commentaire.getTexte());

        // Formater et afficher la date
        CharSequence timeAgo = "";
        if (commentaire.getDate() != null) {
            timeAgo = DateUtils.getRelativeTimeSpanString(
                    commentaire.getDate().getTime(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS);
        }
        holder.textViewDate.setText(timeAgo);
    }

    @Override
    public int getItemCount() {
        return commentaires.size();
    }

    static class CommentaireViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClient;
        TextView textViewCommentaire;
        TextView textViewDate;

        public CommentaireViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClient = itemView.findViewById(R.id.textViewClient);
            textViewCommentaire = itemView.findViewById(R.id.textViewCommentaire);
            textViewDate = itemView.findViewById(R.id.textViewDate);
        }
    }
}