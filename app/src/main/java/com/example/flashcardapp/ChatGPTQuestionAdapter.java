package com.example.flashcardapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ChatGPTQuestionAdapter extends RecyclerView.Adapter<ChatGPTQuestionAdapter.QuestionViewHolder> {

    private List<Flashcard> flashcards;
    private OnRecreateClickListener recreateClickListener;

    public ChatGPTQuestionAdapter(List<Flashcard> flashcards, OnRecreateClickListener recreateClickListener) {
        this.flashcards = flashcards;
        this.recreateClickListener = recreateClickListener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chatgpt_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.tvQuestion.setText(flashcard.getQuestion());
        holder.tvAnswer.setText(flashcard.getAnswer());
        holder.tvSearchTerm.setText(flashcard.getSearchTerm());
        holder.tvUserNote.setText(flashcard.getUserNote());

        // Display topics as a comma-separated string
        //String topics = flashcard.getTopics() != null ? String.join(", ", flashcard.getTopics()) : "No topics";
        holder.tvTopics.setText("placeholder");

        holder.btnRecreate.setOnClickListener(v -> {
            if (recreateClickListener != null) {
                recreateClickListener.onRecreateClick(flashcard);
            }
        });
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion, tvAnswer, tvSearchTerm, tvUserNote, tvTopics;
        Button btnRecreate;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tv_question);
            tvAnswer = itemView.findViewById(R.id.tv_answer);
            tvSearchTerm = itemView.findViewById(R.id.tv_search_term);  // Add this
            tvUserNote = itemView.findViewById(R.id.tv_user_note);      // And this
            tvTopics = itemView.findViewById(R.id.tv_topics);           // And topics
            btnRecreate = itemView.findViewById(R.id.btn_recreate);
        }
    }


    public interface OnRecreateClickListener {
        void onRecreateClick(Flashcard flashcard);
    }
}
