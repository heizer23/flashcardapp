package com.example.flashcardapp.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;

import java.util.ArrayList;
import java.util.List;

public class ChatGPTQuestionAdapter extends RecyclerView.Adapter<ChatGPTQuestionAdapter.QuestionViewHolder> {

    private final List<Flashcard> flashcards;
    private final List<Boolean> selectionStates; // Tracks selection states for each flashcard
    private OnRecreateClickListener recreateClickListener;

    public ChatGPTQuestionAdapter(List<Flashcard> flashcards, OnRecreateClickListener recreateClickListener) {
        this.flashcards = flashcards != null ? flashcards : new ArrayList<>();
        this.selectionStates = new ArrayList<>();
        for (int i = 0; i < this.flashcards.size(); i++) {
            this.selectionStates.add(false); // Initialize all as unselected
        }
        this.recreateClickListener = recreateClickListener;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generated_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);

        holder.etQuestion.setText(flashcard.getQuestion());
        holder.etAnswer.setText(flashcard.getAnswer());

        // Prevent triggering the listener during binding
        holder.cbSelect.setOnCheckedChangeListener(null);

        // Set the current checkbox state
        holder.cbSelect.setChecked(selectionStates.get(position));

        // Add a listener to update the selection state
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            selectionStates.set(position, isChecked); // Update selectionStates
        });

        // Optional: Add a listener for recreating a flashcard if needed
        holder.itemView.setOnClickListener(v -> {
            if (recreateClickListener != null) {
                recreateClickListener.onRecreateClick(flashcard, position); // Pass both flashcard and position
            }
        });
    }



    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public interface OnRecreateClickListener {
        void onRecreateClick(Flashcard flashcard, int position);
    }

    // Method to retrieve only the selected flashcards
    public List<Flashcard> getSelectedQuestions() {
        List<Flashcard> selectedQuestions = new ArrayList<>();
        for (int i = 0; i < flashcards.size(); i++) {
            if (selectionStates.get(i)) { // Check if the flashcard is selected
                selectedQuestions.add(flashcards.get(i));
            }
        }
        return selectedQuestions;
    }

    // Method to update the adapter's data
    public void updateData(List<Flashcard> newFlashcards) {
        flashcards.clear();
        selectionStates.clear();
        if (newFlashcards != null) {
            flashcards.addAll(newFlashcards);
            for (int i = 0; i < newFlashcards.size(); i++) {
                selectionStates.add(false); // Reset selection states for new data
            }
        }
        notifyDataSetChanged();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        EditText etQuestion, etAnswer;
        CheckBox cbSelect;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            etQuestion = itemView.findViewById(R.id.et_generated_question);
            etAnswer = itemView.findViewById(R.id.et_generated_answer);
            cbSelect = itemView.findViewById(R.id.cb_select_question);
        }
    }
}
