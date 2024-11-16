package com.example.flashcardapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTActivity extends AppCompatActivity {

    private EditText etPrompt, etTopic, etInformativeText;
    private Button btnAccept, btnBack;
    private RecyclerView rvQuestions;

    private boolean buildMode = true; // Set to true for using local JSON, false for real GPT requests

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_gpt);

        // Initialize views
        etPrompt = findViewById(R.id.et_prompt);
        etTopic = findViewById(R.id.et_topic);
        etInformativeText = findViewById(R.id.et_informative_text);
        btnAccept = findViewById(R.id.btn_accept);
        btnBack = findViewById(R.id.btn_back);
        rvQuestions = findViewById(R.id.rv_questions);

        rvQuestions.setLayoutManager(new LinearLayoutManager(this));

        // Set initial view state
        btnBack.setVisibility(View.GONE);
        btnAccept.setText("Submit");
        btnAccept.setOnClickListener(v -> handleSubmit());

        // Back button returns to prompt entry
        btnBack.setOnClickListener(v -> resetToPromptView());
    }

    private void handleSubmit() {
        if (buildMode) {
            String localJson = loadLocalJson();
            if (localJson != null) {
                displayResponse(localJson); // Use local JSON for display
            } else {
                Toast.makeText(this, "Failed to load local JSON", Toast.LENGTH_SHORT).show();
            }
        } else {
            handleRealSubmit(); // Call GPT in real mode
        }
    }

    private void handleRealSubmit() {
        String userInput = etPrompt.getText().toString().trim();
        if (!userInput.isEmpty()) {
            ChatGPTHelper.getContextForQuestion(userInput, new ChatGPTHelper.OnChatGPTResponse() {
                @Override
                public void onSuccess(String response) {
                    displayResponse(response);
                }

                @Override
                public void onFailure(String error) {
                    runOnUiThread(() -> {
                        etInformativeText.setText("Failed to connect to GPT: " + error);
                        Toast.makeText(ChatGPTActivity.this, "Failed to connect to GPT", Toast.LENGTH_SHORT).show();
                    });
                }
            });
            etPrompt.setVisibility(View.GONE);
            btnAccept.setText("Accept");
            btnBack.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayResponse(String response) {
        try {
            JSONObject parsedContent = new JSONObject(response);

            etTopic.setText(parsedContent.optString("title", "No title"));
            etInformativeText.setText(parsedContent.optString("informative_text", "No informative text"));

            JSONArray questionsArray = parsedContent.optJSONArray("questions");
            if (questionsArray != null) {
                List<Flashcard> flashcards = new ArrayList<>();
                for (int i = 0; i < questionsArray.length(); i++) {
                    JSONObject questionObject = questionsArray.getJSONObject(i);
                    Flashcard flashcard = new Flashcard();
                    flashcard.setQuestion(questionObject.optString("question"));
                    flashcard.setAnswer(questionObject.optString("answer"));
                    flashcard.setSearchTerm(questionObject.optString("searchTerm"));
                    flashcard.setUserNote(questionObject.optString("userNote"));

                    flashcards.add(flashcard);
                }

                // Populate the RecyclerView
                ChatGPTQuestionAdapter adapter = new ChatGPTQuestionAdapter(flashcards, flashcard -> {
                    Toast.makeText(this, "Recreate clicked for: " + flashcard.getQuestion(), Toast.LENGTH_SHORT).show();
                });
                rvQuestions.setAdapter(adapter);
                rvQuestions.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            etInformativeText.setText("Error parsing response");
            Log.e("ChatGPTActivity", "Error parsing JSON response", e);
        }
    }

    private String loadLocalJson() {
        StringBuilder jsonBuilder = new StringBuilder();
        try (InputStream is = getResources().openRawResource(R.raw.sample_response);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }
            return jsonBuilder.toString();
        } catch (IOException e) {
            Log.e("ChatGPTActivity", "Error loading JSON from raw resource", e);
            return null;
        }
    }

    private void resetToPromptView() {
        etPrompt.setVisibility(View.VISIBLE);
        btnAccept.setText("Submit");
        btnBack.setVisibility(View.GONE);
    }
}
