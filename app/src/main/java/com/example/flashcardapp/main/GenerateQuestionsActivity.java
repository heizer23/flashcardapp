package com.example.flashcardapp.main;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModel;

import java.util.ArrayList;
import java.util.List;

public class GenerateQuestionsActivity extends AppCompatActivity {

    private Button btnGenerate, btnSave;
    private RecyclerView rvGeneratedQuestions;
    private ChatGPTQuestionAdapter adapter;
    private ImportFlashcardsViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_questions);

        // Initialize UI components
        btnGenerate = findViewById(R.id.btn_generate);
        btnSave = findViewById(R.id.btn_save);
        rvGeneratedQuestions = findViewById(R.id.rv_generated_questions);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ImportFlashcardsViewModel.class);

        // Initialize DAO and pass it to ViewModel
        FlashcardDAO flashcardDAO = new FlashcardDAO(this); // Use Activity context
        flashcardDAO.open(); // Ensure the DAO is initialized
        viewModel.initialize(flashcardDAO); // Pass the DAO to the ViewModel

        // Setup RecyclerView
        adapter = new ChatGPTQuestionAdapter(new ArrayList<>(), null); // Corrected line
        rvGeneratedQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvGeneratedQuestions.setAdapter(adapter);

        // Observe LiveData for changes
        viewModel.getGeneratedQuestions().observe(this, generatedQuestions -> {
            adapter.updateData(generatedQuestions); // Update adapter data
        });

        // Button listeners
        btnGenerate.setOnClickListener(v -> generateQuestions());
        btnSave.setOnClickListener(v -> saveQuestions());
    }


    private void generateQuestions() {
        List<Flashcard> existingQuestions = viewModel.fetchExistingQuestions();
        String promptGenerateQuestions = getString(R.string.prompt_generate_questions_activity); // Retrieve prompt from strings.xml

        viewModel.generateQuestions(existingQuestions, promptGenerateQuestions, this,  new ImportFlashcardsViewModel.OnGenerateCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    adapter.notifyDataSetChanged();
                    Toast.makeText(GenerateQuestionsActivity.this, "Questions generated successfully!", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(GenerateQuestionsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }


    private void saveQuestions() {
        List<Flashcard> selectedQuestions = adapter.getSelectedQuestions();
        viewModel.saveFlashcards(selectedQuestions);
        Toast.makeText(this, "Selected questions saved!", Toast.LENGTH_SHORT).show();
    }
}
