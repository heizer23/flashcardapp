package com.example.flashcardapp.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.flashcardapp.R;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import androidx.appcompat.app.AppCompatActivity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditFlashcardActivity extends AppCompatActivity {

    private EditText etQuestion, etAnswer, etSearchTerm, etUserNote, etTopics;
    private Button btnUpdate, btnContext, btnRelatedQuestion, btnCopy, btnDelete;
    private FlashcardDAO flashcardDAO;
    private Flashcard flashcard;
    private List<Topic> associatedTopics;

    // Cache for topics
    private Map<String, Topic> topicCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_flashcard);

        // Initialize views
        etQuestion = findViewById(R.id.et_question);
        etAnswer = findViewById(R.id.et_answer);
        etSearchTerm = findViewById(R.id.et_search_term);
        etUserNote = findViewById(R.id.et_user_note);
        etTopics = findViewById(R.id.et_topics);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);
        btnCopy = findViewById(R.id.btn_copy);
        btnContext = findViewById(R.id.btn_context);
        btnRelatedQuestion = findViewById(R.id.btn_generate_related_question);

        // Initialize DAO
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        // Preload the topic cache with all existing topics from the database
        preloadTopicCache();

        // Retrieve flashcard ID from Intent and load flashcard details if ID is valid
        int flashcardId = getIntent().getIntExtra("FLASHCARD_ID", -1);
        if (flashcardId != -1) {
            flashcard = flashcardDAO.getFlashcard(flashcardId);
            if (flashcard != null) {
                populateFields(flashcard);
            }
        }

        // Set up button listeners
        setupButtonListeners();
    }

    private void populateFields(Flashcard flashcard) {
        etQuestion.setText(flashcard.getQuestion());
        etAnswer.setText(flashcard.getAnswer());
        etSearchTerm.setText(flashcard.getSearchTerm());
        etUserNote.setText(flashcard.getUserNote());

        associatedTopics = flashcardDAO.getTopicsForFlashcard(flashcard.getId());
        StringBuilder topicList = new StringBuilder();
        for (Topic topic : associatedTopics) {
            topicList.append(topic.getName()).append(", ");
        }
        if (topicList.length() > 0) {
            topicList.setLength(topicList.length() - 2);
        }
        etTopics.setText(topicList.toString());
    }

    private void setupButtonListeners() {
        btnUpdate.setOnClickListener(v -> updateFlashcard());

        btnCopy.setOnClickListener(v -> copyUserNoteToClipboard());

        btnContext.setOnClickListener(v -> generateContextForQuestion());

        btnRelatedQuestion.setOnClickListener(v -> generateRelatedQuestion());

        btnDelete.setOnClickListener(v -> {
            if (flashcard != null) {
                flashcardDAO.deleteFlashcard(flashcard.getId()); // Call DAO method to delete
                Toast.makeText(this, "Flashcard deleted.", Toast.LENGTH_SHORT).show();
                finish(); // Close the activity and return to the previous screen
            } else {
                Toast.makeText(this, "No flashcard to delete.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateFlashcard() {
        String question = etQuestion.getText().toString().trim();
        String answer = etAnswer.getText().toString().trim();
        String searchTerm = etSearchTerm.getText().toString().trim();
        String userNote = etUserNote.getText().toString().trim();
        String topics = etTopics.getText().toString().trim();

        if (!question.isEmpty() && !answer.isEmpty()) {
            flashcard.setQuestion(question);
            flashcard.setAnswer(answer);
            flashcard.setSearchTerm(searchTerm);
            flashcard.setUserNote(userNote);
            flashcardDAO.updateFlashcard(flashcard);

            flashcardDAO.clearTopicsForFlashcard(flashcard.getId());
            if (!topics.trim().isEmpty()) {
                String[] topicArray = topics.split(",");
                for (String topicName : topicArray) {
                    topicName = topicName.trim();
                    if (!topicName.isEmpty()) {
                        Topic topic = getOrInsertTopicFromCache(topicName);
                        flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId());
                    }
                }
            }

            Toast.makeText(this, "Flashcard updated!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Please enter both question and answer.", Toast.LENGTH_SHORT).show();
        }
    }

    private void copyUserNoteToClipboard() {
        String userNoteContent = etUserNote.getText().toString();
        if (!userNoteContent.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("User Note", userNoteContent);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "User Note copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "User Note is empty", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateContextForQuestion() {
        String question = " \"" + etQuestion.getText().toString().trim() + "\" with answer: \"" +
                etAnswer.getText().toString().trim() + "\" and additional info: \"" +
                etSearchTerm.getText().toString().trim() + "\".";

        if (!question.isEmpty()) {
            ChatGPTHelper.getContextForQuestion(question, this , new ChatGPTHelper.OnChatGPTResponse() {
                @Override
                public void onSuccess(String response) {
                    runOnUiThread(() -> etUserNote.setText(response));
                }

                @Override
                public void onFailure(String error) {
                    Log.e("EditFlashcardActivity", "Failed to get context: " + error);
                    runOnUiThread(() -> Toast.makeText(EditFlashcardActivity.this, "Failed to get context", Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            Toast.makeText(this, "Please enter a question first", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateRelatedQuestion() {
        String question = etQuestion.getText().toString().trim();
        if (!question.isEmpty()) {
            ChatGPTHelper.generateRelatedQuestion(question, new ChatGPTHelper.OnChatGPTResponse() {
                @Override
                public void onSuccess(String response) {
                    Intent intent = new Intent(EditFlashcardActivity.this, EditFlashcardActivity.class);
                    intent.putExtra("relatedQuestion", response);
                    startActivity(intent);
                }

                @Override
                public void onFailure(String error) {
                    Log.e("EditFlashcardActivity", "Failed to generate related question: " + error);
                    runOnUiThread(() -> Toast.makeText(EditFlashcardActivity.this, "Failed to generate related question", Toast.LENGTH_SHORT).show());
                }
            }, EditFlashcardActivity.this);  // Add the context here
        } else {
            Toast.makeText(this, "Please enter a question first", Toast.LENGTH_SHORT).show();
        }
    }


    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic);
        }
    }

    private Topic getOrInsertTopicFromCache(String topicName) {
        if (topicCache.containsKey(topicName)) {
            return topicCache.get(topicName);
        }
        Topic topic = flashcardDAO.getTopicByName(topicName);
        if (topic == null) {
            topic = flashcardDAO.insertTopic(topicName);
        }
        topicCache.put(topicName, topic);
        return topic;
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
