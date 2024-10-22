// File: FlashcardAdapter.java
package com.example.flashcardapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Add other necessary imports
import android.widget.Button;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.ViewHolder> {

    private List<Flashcard> flashcards;
    private Context context;

    public FlashcardAdapter(Context context, List<Flashcard> flashcards) {
        this.context = context;
        this.flashcards = flashcards;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView tvQuestion;
        public Button btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }

    @Override
    public FlashcardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_flashcard, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FlashcardAdapter.ViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.tvQuestion.setText(flashcard.getQuestion());

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditFlashcardActivity.class);
            intent.putExtra("FLASHCARD_ID", flashcard.getId());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            FlashcardDAO dao = new FlashcardDAO(context);
            dao.open();
            dao.deleteFlashcard(flashcard.getId());
            dao.close();
            flashcards.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, flashcards.size());
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }
}
