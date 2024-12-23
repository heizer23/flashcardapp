package com.example.flashcardapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GenerateQuestionsActivity extends AppCompatActivity {
    private FlashcardDAO flashcardDAO;
    private Button btnGenerate, btnSave;
    private RecyclerView rvGeneratedQuestions;
    private GeneratedQuestionAdapter adapter;
    private List<Flashcard> generatedQuestions;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_questions);

        btnGenerate = findViewById(R.id.btn_generate);
        btnSave = findViewById(R.id.btn_save);
        rvGeneratedQuestions = findViewById(R.id.rv_generated_questions);

        generatedQuestions = new ArrayList<>();
        adapter = new GeneratedQuestionAdapter(generatedQuestions);

        rvGeneratedQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvGeneratedQuestions.setAdapter(adapter);

        btnGenerate.setOnClickListener(v -> generateQuestions());
        btnSave.setOnClickListener(v -> saveQuestions());
    }

    private void generateQuestions() {
        List<Flashcard> existingQuestions = fetchExistingQuestions();

        // Build the prompt with explicit instructions
        StringBuilder promptBuilder = new StringBuilder(
                "Based on the following questions and answers, generate six new related questions. " +
                        "Provide the response as a valid JSON array without unnecessary formatting or newlines. " +
                        "Each object in the array should have the following structure: " +
                        "[" +
                        "  {" +
                        "    \"question\": \"<new question>\"," +
                        "    \"answer\": \"<corresponding answer>\"," +
                        "    \"searchTerm\": \"<related search term>\"," +
                        "    \"userNote\": \"<a note about the question>\"," +
                        "    \"topics\": [\"<topic1>\", \"<topic2>\"]" +
                        "  }" +
                        "] " +
                        "Here are the questions and answers to base the new ones on:"
        );


        // Add the existing flashcard questions and answers to the prompt
        for (Flashcard flashcard : existingQuestions) {
            promptBuilder.append("Q: ").append(flashcard.getQuestion()).append("\n");
            promptBuilder.append("A: ").append(flashcard.getAnswer()).append("\n");
        }

        String prompt = promptBuilder.toString();

        // Make the GPT request
        ChatGPTHelper.makeChatGPTRequest(prompt, new ChatGPTHelper.OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                Log.d("GPT Response", "Raw Response: " + response);
                runOnUiThread(() -> parseAndDisplayGeneratedQuestions(response));
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(GenerateQuestionsActivity.this, "Failed to generate questions: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }



    private void parseAndDisplayGeneratedQuestions(String response) {
        try {
            // Parse the root JSON object
            JSONObject rootObject = new JSONObject(response);
            JSONArray choicesArray = rootObject.getJSONArray("choices");

            if (choicesArray.length() > 0) {
                // Extract the "content" field from the first choice
                String content = choicesArray.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content");

                // Parse the content string as a JSON array
                JSONArray questionsArray = new JSONArray(content);
                generatedQuestions.clear();

                for (int i = 0; i < questionsArray.length(); i++) {
                    JSONObject questionObj = questionsArray.getJSONObject(i);
                    Flashcard flashcard = new Flashcard(
                            questionObj.getString("question"),
                            questionObj.getString("answer")
                    );
                    flashcard.setSearchTerm(questionObj.optString("searchTerm", ""));
                    flashcard.setUserNote(questionObj.optString("userNote", ""));
                    JSONArray topicsArray = questionObj.optJSONArray("topics");
                    List<String> topics = new ArrayList<>();
                    if (topicsArray != null) {
                        for (int j = 0; j < topicsArray.length(); j++) {
                            topics.add(topicsArray.getString(j));
                        }
                    }
                  //  flashcard.setTopics(topics); // Adjust Flashcard class to support this
                    generatedQuestions.add(flashcard);
                }

                adapter.notifyDataSetChanged();
                Toast.makeText(this, "Questions generated successfully!", Toast.LENGTH_SHORT).show();
            } else {
                throw new JSONException("No choices found in GPT response");
            }
        } catch (Exception e) {
            Log.e("ParseQuestions", "Error parsing generated questions", e);
            Toast.makeText(this, "Error parsing generated questions. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
         //   topicCache.put(topic.getName(), topic); // Preload the cache with all existing topics
        }
    }

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
                      //  topics.add(getOrInsertTopic(topicName));
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

    private List<Flashcard> fetchExistingQuestions() {
        // Replace with actual DAO calls to fetch flashcards
        FlashcardDAO dao = new FlashcardDAO(this);
        dao.open();
        List<Flashcard> questions = dao.getAllFlashcards();
        dao.close();
        return questions;
    }

    private void saveQuestions() {
        // Display selected questions for review (not saving to DB yet)
        List<Flashcard> selectedQuestions = adapter.getSelectedQuestions();
        for (Flashcard flashcard : selectedQuestions) {
            Log.d("SaveQuestions", "Selected Question: " + flashcard.getQuestion());
        }
        Toast.makeText(this, "Selected questions logged. Implement DB save next.", Toast.LENGTH_SHORT).show();
    }
}
