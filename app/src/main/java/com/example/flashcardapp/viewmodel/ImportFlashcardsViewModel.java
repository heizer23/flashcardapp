package com.example.flashcardapp.viewmodel;


import android.app.Application;
import android.util.Log;

import com.example.flashcardapp.ChatGPTHelper;
import com.example.flashcardapp.FlashcardDAO;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ImportFlashcardsViewModel extends AndroidViewModel {

    private final FlashcardDAO flashcardDAO;
    private final Map<String, Topic> topicCache = new HashMap<>();
    private final MutableLiveData<List<Flashcard>> generatedQuestions = new MutableLiveData<>(new ArrayList<>());

    public ImportFlashcardsViewModel(@NonNull Application application) {
        super(application);
        flashcardDAO = new FlashcardDAO(application);
        flashcardDAO.open();
        preloadTopicCache();
    }

    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic); // Preload the cache with all existing topics
        }
    }

    public LiveData<List<Flashcard>> getGeneratedQuestions() {
        return generatedQuestions;
    }

    public void generateQuestions(List<Flashcard> existingQuestions, OnGenerateCallback callback) {
        StringBuilder promptBuilder = new StringBuilder(
                "Based on the following questions and answers, generate six new related questions. " +
                        "Provide the response as a valid JSON array. Each object should have: " +
                        "[{\"question\":\"<new question>\",\"answer\":\"<answer>\",\"searchTerm\":\"<term>\",\"userNote\":\"<note>\",\"topics\":[\"<topic>\"]}] " +
                        "Here are the existing questions:"
        );

        for (Flashcard flashcard : existingQuestions) {
            promptBuilder.append("Q: ").append(flashcard.getQuestion()).append("\n");
            promptBuilder.append("A: ").append(flashcard.getAnswer()).append("\n");
        }

        String prompt = promptBuilder.toString();

        ChatGPTHelper.makeChatGPTRequest(prompt, new ChatGPTHelper.OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject rootObject = new JSONObject(response);
                    String content = rootObject.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    JSONArray questionsArray = new JSONArray(content);
                    List<Flashcard> newQuestions = new ArrayList<>();

                    for (int i = 0; i < questionsArray.length(); i++) {
                        JSONObject questionObj = questionsArray.getJSONObject(i);
                        Flashcard flashcard = new Flashcard(
                                questionObj.getString("question"),
                                questionObj.getString("answer")
                        );
                        flashcard.setSearchTerm(questionObj.optString("searchTerm", ""));
                        flashcard.setUserNote(questionObj.optString("userNote", ""));
                        newQuestions.add(flashcard);
                    }

                    // Update LiveData on the main thread
                    generatedQuestions.postValue(newQuestions);
                    callback.onSuccess();

                } catch (Exception e) {
                    Log.e("GenerateQuestions", "Error parsing response", e);
                    callback.onFailure("Failed to parse response.");
                }
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    public List<Flashcard> fetchExistingQuestions() {
        return flashcardDAO.getAllFlashcards(); // Assumes getAllFlashcards() fetches all flashcards from the database
    }

    public void saveFlashcards(List<Flashcard> flashcards) {
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getQuestion() != null && flashcard.getAnswer() != null) {
                flashcardDAO.createFlashcard(flashcard); // Save the flashcard to the database
                for (Topic topic : flashcard.getTopics()) {
                    flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId()); // Associate flashcard with its topics
                }
            }
        }
    }

    @Override
    protected void onCleared() {
        flashcardDAO.close();
        super.onCleared();
    }

    public interface OnGenerateCallback {
        void onSuccess();
        void onFailure(String error);
    }
}
