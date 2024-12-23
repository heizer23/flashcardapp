package com.example.flashcardapp;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportFlashcardsActivity extends AppCompatActivity {

    private EditText etJsonInput;
    private Button btnImport, btnCopyText, btnClearText;
    private FlashcardDAO flashcardDAO;
    private Map<String, Topic> topicCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_flashcards);

        // Initialize views
        etJsonInput = findViewById(R.id.et_xml_input);  // Change ID if needed
        btnImport = findViewById(R.id.btn_import);
        btnCopyText = findViewById(R.id.btn_copy_text);
        btnClearText = findViewById(R.id.btn_clear_text);

        // Initialize DAO
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

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
                List<Flashcard> importedFlashcards = parseFlashcardsFromJson(jsonInput);
                int skippedCount = 0;

                for (Flashcard flashcard : importedFlashcards) {
                    if (flashcard.getQuestion() == null || flashcard.getAnswer() == null) {
                        skippedCount++;
                    } else {
                        flashcardDAO.createFlashcard(flashcard); // Save flashcard first
                        for (Topic topic : flashcard.getTopics()) {
                            flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId()); // Associate flashcard with topics
                        }
                    }
                }

                String resultMessage = "Imported " + (importedFlashcards.size() - skippedCount) + " flashcards.";
                if (skippedCount > 0) {
                    resultMessage += " Skipped " + skippedCount + " flashcards due to missing question/answer.";
                }
                Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please enter JSON input.", Toast.LENGTH_SHORT).show();
            }
        });

        // Copy text button listener
        btnCopyText.setOnClickListener(v -> {
            String textToCopy = etJsonInput.getText().toString().trim();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
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

    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic); // Preload the cache with all existing topics
        }
    }

    // JSON parsing method
    private List<Flashcard> parseFlashcardsFromJson(String jsonInput) {
        List<Flashcard> flashcards = new ArrayList<>();
        preloadTopicCache();  // Load topics from database

        try {
            JSONArray flashcardsArray = new JSONArray(jsonInput);

            for (int i = 0; i < flashcardsArray.length(); i++) {
                JSONObject flashcardJson = flashcardsArray.getJSONObject(i);
                Flashcard flashcard = new Flashcard();

                // Parse flashcard fields
                flashcard.setQuestion(flashcardJson.optString("question"));
                flashcard.setAnswer(flashcardJson.optString("answer"));
                flashcard.setSearchTerm(flashcardJson.optString("searchTerm"));
                flashcard.setUserNote(flashcardJson.optString("userNote"));

                // Parse and cache topics
                JSONArray topicsArray = flashcardJson.optJSONArray("topics");
                if (topicsArray != null) {
                    List<Topic> topics = new ArrayList<>();
                    for (int j = 0; j < topicsArray.length(); j++) {
                        String topicName = topicsArray.getString(j);
                        topics.add(getOrInsertTopic(topicName));
                    }
                    flashcard.setTopics(topics);
                }

                flashcards.add(flashcard);  // Add the parsed flashcard
            }

        } catch (Exception e) {
            Log.e("ImportFlashcards", "Error parsing JSON", e);
            Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_LONG).show();
        }
        return flashcards;
    }

    private Topic getOrInsertTopic(String topicName) {
        // Check if the topic is already in the cache
        if (topicCache.containsKey(topicName)) {
            return topicCache.get(topicName); // Return from cache if it exists
        }

        // If not in cache, check the database
        Topic topic = flashcardDAO.getTopicByName(topicName);
        if (topic == null) {
            // If not in database, insert it
            topic = flashcardDAO.insertTopic(topicName);
        }

        // Add the topic to the cache
        topicCache.put(topicName, topic);

        return topic;
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
