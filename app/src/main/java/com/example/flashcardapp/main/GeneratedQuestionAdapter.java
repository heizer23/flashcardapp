package com.example.flashcardapp.main;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GeneratedQuestionAdapter extends RecyclerView.Adapter<GeneratedQuestionAdapter.QuestionViewHolder> {

    private final List<Flashcard> questions;
    private final List<Boolean> selected;

    public GeneratedQuestionAdapter(List<Flashcard> questions) {
        this.questions = questions;
        this.selected = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            selected.add(false); // Initially, no questions are selected
        }
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_generated_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        Flashcard question = questions.get(position);

        holder.etQuestion.setText(question.getQuestion());
        holder.etAnswer.setText(question.getAnswer());
        holder.cbSelect.setChecked(selected.get(position));

        // Handle question text change
        holder.etQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setQuestion(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle answer text change
        holder.etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setAnswer(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Handle selection state
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> selected.set(position, isChecked));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public List<Flashcard> getSelectedQuestions() {
        List<Flashcard> selectedQuestions = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            if (selected.get(i)) {
                selectedQuestions.add(questions.get(i));
            }
        }
        return selectedQuestions;
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {
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
