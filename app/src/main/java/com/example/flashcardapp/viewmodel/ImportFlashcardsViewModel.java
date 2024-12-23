package com.example.flashcardapp.viewmodel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.flashcardapp.FlashcardDAO;
import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportFlashcardsViewModel extends AndroidViewModel {

    private final FlashcardDAO flashcardDAO;
    private final Map<String, Topic> topicCache = new HashMap<>();

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

    public List<Flashcard> parseFlashcardsFromJson(String jsonInput) {
        List<Flashcard> flashcards = new ArrayList<>();

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
            Toast.makeText(getApplication(), "Error parsing JSON", Toast.LENGTH_LONG).show();
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

    public void saveFlashcards(List<Flashcard> flashcards) {
        for (Flashcard flashcard : flashcards) {
            if (flashcard.getQuestion() != null && flashcard.getAnswer() != null) {
                flashcardDAO.createFlashcard(flashcard);
                for (Topic topic : flashcard.getTopics()) {
                    flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId());
                }
            }
        }
    }

    @Override
    protected void onCleared() {
        flashcardDAO.close();
        super.onCleared();
    }
}
