package com.example.flashcardapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class ChatGPTActivity extends AppCompatActivity {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_KEY = "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"; // Replace with your actual API key

    private EditText etPrompt, etTopic, etInformativeText;
    private RecyclerView rvQuestions;
    private Button btnAccept, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_gpt);

        // Initialize views
        etPrompt = findViewById(R.id.et_prompt);
        etTopic = findViewById(R.id.et_topic);
        etInformativeText = findViewById(R.id.et_informative_text);
        rvQuestions = findViewById(R.id.rv_questions);
        btnAccept = findViewById(R.id.btn_accept);
        btnBack = findViewById(R.id.btn_back);

        // Set initial view state
        btnBack.setVisibility(View.GONE);

        // Set the initial button action for "Submit"
        btnAccept.setText("Submit");
        btnAccept.setOnClickListener(v -> handleSubmit());

        // Back button returns to prompt entry
        btnBack.setOnClickListener(v -> resetToPromptView());
    }

    private void handleSubmit() {
        String userInput = etPrompt.getText().toString().trim();
        if (!userInput.isEmpty()) {
            callChatGPT(userInput);
            // Hide prompt input and change button to "Accept"
            etPrompt.setVisibility(View.GONE);
            btnAccept.setText("Accept");
            btnBack.setVisibility(View.VISIBLE);
        } else {
            Toast.makeText(this, "Please enter a query", Toast.LENGTH_SHORT).show();
        }
    }

    private void callChatGPT(String userInput) {
        OkHttpClient client = new OkHttpClient();

        // Construct the JSON payload for the API request
        String json = "{"
                + "\"model\": \"gpt-3.5-turbo\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + userInput + "\"}]"
                + "}";

        RequestBody body = RequestBody.create(
                json, MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(ChatGPTActivity.this, "Failed to connect", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONObject jsonResponse = new JSONObject(responseBody);
                        String reply = jsonResponse
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content");

                        runOnUiThread(() -> displayResponse(reply));
                    } catch (Exception e) {
                        runOnUiThread(() -> Toast.makeText(ChatGPTActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ChatGPTActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void displayResponse(String response) {
        // Example: Parsing and displaying response text in TextViews and RecyclerView
        etTopic.setText("Generated Topic Name");  // Example; parse the actual topic from the response
        etInformativeText.setText("Generated Informative Text");  // Example; parse the actual text

        // Populate RecyclerView with questions (mock data here)
        // rvQuestions.setAdapter(new QuestionAdapter(parsedQuestions)); // Use parsed data here
    }

    private void resetToPromptView() {
        // Show prompt input, hide results, and reset button text
        etPrompt.setVisibility(View.VISIBLE);
        btnAccept.setText("Submit");
        btnBack.setVisibility(View.GONE);
    }
}
