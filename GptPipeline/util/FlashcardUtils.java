package com.example.flashcardapp.util;

import com.example.flashcardapp.data.Flashcard;
import com.example.flashcardapp.data.Topic;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class FlashcardUtils {

    public static List<Flashcard> parseFlashcardsFromJson(String jsonString) throws Exception {
        List<Flashcard> flashcards = new ArrayList<>();

        try {
            JSONArray questionsArray = new JSONArray(jsonString);

            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionObj = questionsArray.getJSONObject(i);
                Flashcard flashcard = new Flashcard(
                        questionObj.getString("question"),
                        questionObj.getString("answer")
                );
                flashcard.setSearchTerm(questionObj.optString("searchTerm", ""));
                flashcard.setUserNote(questionObj.optString("userNote", ""));

                // Parse topics
                JSONArray topicsArray = questionObj.optJSONArray("topics");
                if (topicsArray != null) {
                    List<Topic> topics = new ArrayList<>();
                    for (int j = 0; j < topicsArray.length(); j++) {
                        String topicName = topicsArray.getString(j);
                        topics.add(new Topic(topicName)); // Create new Topic objects with names
                    }
                    flashcard.setTopics(topics);
                }

                flashcards.add(flashcard);
            }
        } catch (Exception e) {
            throw new Exception("Failed to parse JSON string into Flashcards", e);
        }

        return flashcards;
    }
}
