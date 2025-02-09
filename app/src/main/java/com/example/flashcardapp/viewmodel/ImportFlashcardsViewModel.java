package com.example.flashcardapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.util.Log;

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

    // Save flashcards into the database on a background thread
    public void saveFlashcards(final List<Flashcard> flashcards) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Flashcard flashcard : flashcards) {
                    if (flashcard.getQuestion() != null && flashcard.getAnswer() != null) {
                        flashcardDAO.createFlashcard(flashcard); // Save flashcard in database

                        // TODO: Handle topics association if needed
                        /*
                        for (Topic topic : flashcard.getTopics()) {
                            Topic existingTopic = topicCache.get(topic.getName());
                            if (existingTopic == null) {
                                // Add new topic if it doesn't exist
                                int topicId = flashcardDAO.insertTopic(topic.getName()).getId();
                                topic.setId(topicId);
                                topicCache.put(topic.getName(), topic);
                            } else {
                                topic.setId(existingTopic.getId());
                            }
                            flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId());
                        }
                        */
                    }
                }
            }
        }).start();
    }

    // Generate questions using ChatGPT based on existing questions and a given prompt
    public void generateQuestions(List<Flashcard> existingQuestions, String prompt, final android.content.Context context, final OnGenerateCallback callback) {
        StringBuilder promptBuilder = new StringBuilder(prompt + "\n\nExisting questions:");
        for (Flashcard flashcard : existingQuestions) {
            promptBuilder.append("\nQ: ").append(flashcard.getQuestion());
            promptBuilder.append("\nA: ").append(flashcard.getAnswer());
        }

        com.example.flashcardapp.main.ChatGPTHelper.makeChatGPTRequest(promptBuilder.toString(), new com.example.flashcardapp.main.ChatGPTHelper.OnChatGPTResponse() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject rootObject = new JSONObject(response);
                    String content = rootObject.getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");

                    List<Flashcard> newQuestions = FlashcardUtils.parseFlashcardsFromJson(content);
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
        }, context);
    }

    // Interface for handling callbacks during question generation
    public interface OnGenerateCallback {
        void onSuccess();
        void onFailure(String error);
    }

    @Override
    protected void onCleared() {
        if (flashcardDAO != null) {
            flashcardDAO.close();
        }
        super.onCleared();
    }
}
