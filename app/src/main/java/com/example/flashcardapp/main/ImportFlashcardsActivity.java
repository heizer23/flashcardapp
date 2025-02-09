package com.example.flashcardapp.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.util.FlashcardUtils;
import com.example.flashcardapp.viewmodel.ImportFlashcardsViewModel;

import java.util.List;

public class ImportFlashcardsActivity extends AppCompatActivity {

    private EditText etJsonInput;
    private Button btnImport, btnCopyText, btnClearText;
    private ImportFlashcardsViewModel viewModel; // ViewModel reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_flashcards);

        // Initialize views
        etJsonInput = findViewById(R.id.et_xml_input);
        btnImport = findViewById(R.id.btn_import);
        btnCopyText = findViewById(R.id.btn_copy_text);
        btnClearText = findViewById(R.id.btn_clear_text);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ImportFlashcardsViewModel.class);

        // Retrieve prompts from XML resources
        String promptGenerateQuestions = getString(R.string.prompt_generate_questions);
        String promptExampleExistingQuestions = getString(R.string.prompt_example_existing_questions);

        Log.d("Debug", "Prompt String: " + promptExampleExistingQuestions);
        // Replace &quot; with actual quotation marks
        promptExampleExistingQuestions = promptExampleExistingQuestions.replace("&quot;", "\"");

        // Concatenate and set text to EditText
        String prefilledText = promptGenerateQuestions + "\n\n" + promptExampleExistingQuestions;
        etJsonInput.setText(prefilledText);

        // Import button listener
        btnImport.setOnClickListener(v -> {
            String jsonInput = etJsonInput.getText().toString().trim();
            if (!jsonInput.isEmpty()) {
                try {
                    // Parse flashcards using the utility method
                    List<Flashcard> flashcards = FlashcardUtils.parseFlashcardsFromJson(jsonInput);

                    // Save flashcards using ViewModel
                    viewModel.saveFlashcards(flashcards);

                    // Show success message
                    String message = "Imported " + flashcards.size() + " flashcards!";
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    // Handle JSON parsing errors
                    Toast.makeText(this, "Invalid JSON input. Please check the format.", Toast.LENGTH_SHORT).show();
                    Log.e("FlashcardImport", "Error parsing JSON input", e);
                }
            } else {
                // Handle empty input
                Toast.makeText(this, "Please enter JSON input.", Toast.LENGTH_SHORT).show();
            }
        });

        // Copy text button listener
        btnCopyText.setOnClickListener(v -> {
            String textToCopy = etJsonInput.getText().toString().trim();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Flashcard JSON", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "Text copied to clipboard!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Nothing to copy!", Toast.LENGTH_SHORT).show();
            }
        });

        // Clear text button listener
        btnClearText.setOnClickListener(v -> etJsonInput.setText(""));
    }


}
