package com.example.location.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.location.R;
import com.example.location.model.Commentaire;

import java.util.List;

public class CommentaireAdapter extends RecyclerView.Adapter<CommentaireAdapter.CommentaireViewHolder> {

    private List<Commentaire> commentaireList;

    public CommentaireAdapter(List<Commentaire> commentaireList) {
        this.commentaireList = commentaireList;
    }

    @NonNull
    @Override
    public CommentaireViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_commentaires, parent, false);
        return new CommentaireViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentaireViewHolder holder, int position) {
        Commentaire commentaire = commentaireList.get(position);
        holder.textViewClient.setText(commentaire.getClient());
        holder.textViewCommentaire.setText(commentaire.getCommentaire());
    }

    @Override
    public int getItemCount() {
        return commentaireList.size();
    }

    public static class CommentaireViewHolder extends RecyclerView.ViewHolder {
        TextView textViewClient, textViewCommentaire;

        public CommentaireViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewClient = itemView.findViewById(R.id.textViewClient);
            textViewCommentaire = itemView.findViewById(R.id.textViewCommentaire);
        }
    }
}