// File: ImportFlashcardsActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class ImportFlashcardsActivity extends AppCompatActivity {

    private EditText etXmlInput;
    private Button btnImport;
    private FlashcardDAO flashcardDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_flashcards);

        // Initialize views
        etXmlInput = findViewById(R.id.et_xml_input);
        btnImport = findViewById(R.id.btn_import);

        // Prefill the EditText with two valid XML flashcard entries
        etXmlInput.setText(
                        "<flashcard>\n" +
                                "    <question>What year was New Orleans founded?</question>\n" +
                                "    <answer>1718</answer>\n" +
                                "    <topic>History</topic>\n" +
                                "    <topic>Music</topic>\n" +
                                "    <search_term>New Orleans History</search_term>\n" +
                                "    <user_note>This question is important because it's about a significant historical event.</user_note>\n" +
                                "</flashcard>\n" +
                                "<flashcard>\n" +
                                "    <question>Which genre of music is New Orleans considered the birthplace of?</question>\n" +
                                "    <answer>Jazz</answer>\n" +
                                "    <topic>History</topic>\n" +
                                "    <topic>Music</topic>\n" +
                                "    <search_term>Jazz History</search_term>\n" +
                                "    <user_note>This highlights the cultural significance of jazz music.</user_note>\n" +
                                "</flashcard>"

        );

        // Initialize DAO
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        // Set import button action
        btnImport.setOnClickListener(v -> {
            String xmlInput = etXmlInput.getText().toString();
            if (!xmlInput.isEmpty()) {
                List<Flashcard> flashcards = parseXmlToFlashcards(xmlInput);
                if (flashcards != null && !flashcards.isEmpty()) {
                    boolean allInserted = true;  // Track if all flashcards were inserted
                    for (Flashcard flashcard : flashcards) {
                        long result = flashcardDAO.insertFlashcardWithResult(flashcard);  // Updated to return result
                        if (result == -1) {
                            allInserted = false;  // If any flashcard fails to insert
                            Toast.makeText(this, "Failed to insert: " + flashcard.getQuestion(), Toast.LENGTH_LONG).show();
                        }
                    }
                    if (allInserted) {
                        Toast.makeText(this, flashcards.size() + " flashcards imported successfully.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid XML format or no flashcards found.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Please paste the XML formatted string.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Parse the XML input to Flashcards
    private List<Flashcard> parseXmlToFlashcards(String xmlInput) {
        try {
            // Parse the XML and return a list of Flashcard objects
            XmlParser parser = new XmlParser();  // Assume you have an XML parser
            return parser.parse(xmlInput);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing XML", Toast.LENGTH_LONG).show();
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
