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
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ChatGPTActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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"; // Replace with your actual API key

    private EditText etPrompt, etTopic, etInformativeText;
    private Button btnAccept, btnBack;

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
            callChatGPT(userInput);
            etPrompt.setVisibility(View.GONE);
            btnAccept.setText("Accept");
            btnBack.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show();
        }
    }

    private void callChatGPT(String userInput) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("model", "gpt-3.5-turbo");

            JSONArray messagesArray = new JSONArray();
            JSONObject messageObject = new JSONObject();
            messageObject.put("role", "user");
            messageObject.put("content", userInput);
            messagesArray.put(messageObject);

            jsonObject.put("messages", messagesArray);

            String json = jsonObject.toString();
            RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));

            Request request = new Request.Builder()
                    .url(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        etInformativeText.setText("Failed to connect to GPT: " + e.getMessage());
                        Toast.makeText(ChatGPTActivity.this, "Failed to connect to GPT", Toast.LENGTH_SHORT).show();
                    });
                    Log.e("GPT Check", "Failed to connect", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        runOnUiThread(() -> displayResponse(responseBody));
                    } else {
                        runOnUiThread(() -> {
                            etInformativeText.setText("Error: " + response.code());
                            Toast.makeText(ChatGPTActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        });
                        Log.e("GPT Check", "Response error: " + response.code());
                    }
                    response.close();
                }
            });
        } catch (Exception e) {
            Log.e("GPT Check", "Error constructing JSON payload", e);
            Toast.makeText(this, "Error constructing JSON payload", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONObject messageObject = jsonResponse
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");
            String content = messageObject.getString("content");

            JSONObject parsedContent = new JSONObject(content);

            etTopic.setText(parsedContent.optString("title", "No title"));
            etInformativeText.setText(parsedContent.optString("informative_text", "No informative text"));

            // Parse questions into Flashcard list
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

                    JSONArray topicsArray = questionObject.optJSONArray("topics");
                    List<String> topics = new ArrayList<>();
                    if (topicsArray != null) {
                        for (int j = 0; j < topicsArray.length(); j++) {
                            topics.add(topicsArray.getString(j));
                        }
                    }
                  //  flashcard.setTopics(topics);

                    flashcards.add(flashcard);
                }

                // Set up RecyclerView with the populated adapter
                RecyclerView rvQuestions = findViewById(R.id.rv_questions);
                rvQuestions.setLayoutManager(new LinearLayoutManager(this));

                ChatGPTQuestionAdapter adapter = new ChatGPTQuestionAdapter(flashcards, flashcard -> {
                    // Handle "Recreate" button clicks here, if necessary
                    Toast.makeText(this, "Recreate clicked for: " + flashcard.getQuestion(), Toast.LENGTH_SHORT).show();
                });
                rvQuestions.setAdapter(adapter);
                rvQuestions.setVisibility(View.VISIBLE);

            }
        } catch (Exception e) {
            etInformativeText.setText("Error parsing response");
            Log.e("GPT Display", "Error parsing JSON response", e);
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
            Log.e("Load JSON", "Error loading JSON from raw resource", e);
            return null;
        }
    }

    private void resetToPromptView() {
        etPrompt.setVisibility(View.VISIBLE);
        btnAccept.setText("Submit");
        btnBack.setVisibility(View.GONE);
    }
}
