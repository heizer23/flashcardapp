package com.example.flashcardapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.util.Log;

import com.example.flashcardapp.main.ChatGPTHelper;
import com.example.flashcardapp.main.FlashcardDAO;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;
import com.example.flashcardapp.util.FlashcardUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportFlashcardsViewModel extends ViewModel {

    private final Map<String, Topic> topicCache = new HashMap<>();
    private final MutableLiveData<List<Flashcard>> generatedQuestions = new MutableLiveData<>(new ArrayList<>());
    private FlashcardDAO flashcardDAO;

    // Initialize FlashcardDAO in ViewModel
    public void initialize(FlashcardDAO dao) {
        this.flashcardDAO = dao;
        preloadTopicCache();
    }

    // Preload existing topics from the database into a cache
    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic); // Cache topics by name
        }
    }

    // Getter for generated questions as LiveData
    public LiveData<List<Flashcard>> getGeneratedQuestions() {
        return generatedQuestions;
    }

    // Fetch existing flashcards from the database
    public List<Flashcard> fetchExistingQuestions() {
        return flashcardDAO.getAllFlashcards();
    }

    // Save flashcards into the database

    public void saveFlashcards(List<Flashcard> flashcards) {
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getQuestion() != null && flashcard.getAnswer() != null) {
                flashcardDAO.createFlashcard(flashcard); // Save flashcard in database

                // Handle topics
                for (Topic topic : flashcard.getTopics()) {
                    Topic existingTopic = topicCache.get(topic.getName());
                    if (existingTopic == null) {
                        // Add new topic if it doesn't exist
                        int topicId = flashcardDAO.insertTopic(topic.getName()).getId();
                        topic.setId(topicId);
                        topicCache.put(topic.getName(), topic); // Update cache
                    } else {
                        topic.setId(existingTopic.getId()); // Use existing topic ID
                    }
                    // Associate flashcard with its topics
                    flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId());
                }
            }
        }
    }

    // Generate questions using ChatGPT based on existing questions and a given prompt
    public void generateQuestions(List<Flashcard> existingQuestions, String prompt, Context context, OnGenerateCallback callback) {
        StringBuilder promptBuilder = new StringBuilder(prompt + "\n\nExisting questions:");
        for (Flashcard flashcard : existingQuestions) {
            promptBuilder.append("\nQ: ").append(flashcard.getQuestion());
            promptBuilder.append("\nA: ").append(flashcard.getAnswer());
        }

        ChatGPTHelper.makeChatGPTRequest(promptBuilder.toString(), new ChatGPTHelper.OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject rootObject = new JSONObject(response);
                    String content = rootObject.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    List<Flashcard> newQuestions = FlashcardUtils.parseFlashcardsFromJson(content);
                    generatedQuestions.postValue(newQuestions); // Update LiveData
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
        }, context);
    }

    // Interface for handling callbacks during question generation
    public interface OnGenerateCallback {
        void onSuccess();
        void onFailure(String error);
    }

    // Cleanup resources when ViewModel is cleared
    @Override
    protected void onCleared() {
        if (flashcardDAO != null) {
            flashcardDAO.close();
        }
        super.onCleared();
    }
}
