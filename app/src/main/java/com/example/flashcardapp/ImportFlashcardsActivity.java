package com.example.flashcardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.flashcardapp.data.Flashcard;
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

        // Prefill the EditText with example JSON flashcard entries
        etJsonInput.setText(
                "[{\"question\": \"What year was New Orleans founded?\"," +
                        " \"answer\": \"1718\"," +
                        " \"searchTerm\": \"New Orleans history\"," +
                        " \"userNote\": \"This is relevant for Louisiana history.\"," +
                        " \"topics\": [\"History\", \"Geography\"]}," +
                        "{\"question\": \"Which genre of music is New Orleans considered the birthplace of?\"," +
                        " \"answer\": \"Jazz\"," +
                        " \"searchTerm\": \"New Orleans music\"," +
                        " \"userNote\": \"This ties into the evolution of Jazz.\"," +
                        " \"topics\": [\"Music\", \"History\"]}]"
        );

        // Import button listener
        btnImport.setOnClickListener(v -> {
            String jsonInput = etJsonInput.getText().toString().trim();
            if (!jsonInput.isEmpty()) {
                // Use ViewModel to parse and save flashcards
                List<Flashcard> flashcards = viewModel.parseFlashcardsFromJson(jsonInput);
                viewModel.saveFlashcards(flashcards);

                String message = "Imported " + flashcards.size() + " flashcards!";
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
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
