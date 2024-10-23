// File: ImportFlashcardsActivity.java
package com.example.flashcardapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImportFlashcardsActivity extends AppCompatActivity {

    private EditText etXmlInput;
    private Button btnImport;
    private FlashcardDAO flashcardDAO;

    private Map<String, Topic> topicCache = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_flashcards);

        // Initialize views
        etXmlInput = findViewById(R.id.et_xml_input);
        btnImport = findViewById(R.id.btn_import);

        // Initialize DAO
        flashcardDAO = new FlashcardDAO(this);
        flashcardDAO.open();

        // Prefill the EditText with example XML flashcard entries
        etXmlInput.setText(
                "<flashcard>\n" +
                        "    <question>What year was New Orleans founded?</question>\n" +
                        "    <answer>1718</answer>\n" +
                        "    <searchTerm>New Orleans history</searchTerm>\n" +
                        "    <userNote>This is relevant for Louisiana history.</userNote>\n" +
                        "    <topic>History</topic>\n" +
                        "    <topic>Geography</topic>\n" +
                        "</flashcard>\n" +
                        "<flashcard>\n" +
                        "    <question>Which genre of music is New Orleans considered the birthplace of?</question>\n" +
                        "    <answer>Jazz</answer>\n" +
                        "    <searchTerm>New Orleans music</searchTerm>\n" +
                        "    <userNote>This ties into the evolution of Jazz.</userNote>\n" +
                        "    <topic>Music</topic>\n" +
                        "    <topic>History</topic>\n" +
                        "</flashcard>"
        );

        // Button click listener
        btnImport.setOnClickListener(v -> {
            String xmlInput = etXmlInput.getText().toString().trim();
            if (!xmlInput.isEmpty()) {
                List<Flashcard> importedFlashcards = parseFlashcardsFromXml(xmlInput);
                int skippedCount = 0;

                for (Flashcard flashcard : importedFlashcards) {
                    if (flashcard.getQuestion() == null || flashcard.getAnswer() == null) {
                        skippedCount++;
                    } else {
                        flashcardDAO.createFlashcard(flashcard); // Save flashcard first
                        // Associate the topics
                        for (Topic topic : flashcard.getTopics()) {
                            flashcardDAO.associateFlashcardWithTopic(flashcard.getId(), topic.getId()); // Associate the flashcard with the topic
                        }
                    }
                }

                String resultMessage = "Imported " + (importedFlashcards.size() - skippedCount) + " flashcards.";
                if (skippedCount > 0) {
                    resultMessage += " Skipped " + skippedCount + " flashcards due to missing question/answer.";
                }
                Toast.makeText(this, resultMessage, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Please enter XML input.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void preloadTopicCache() {
        List<Topic> existingTopics = flashcardDAO.getAllTopics();
        for (Topic topic : existingTopics) {
            topicCache.put(topic.getName(), topic); // Preload the cache with all existing topics
        }
    }

    // XML parsing method
    private List<Flashcard> parseFlashcardsFromXml(String xmlInput) {
        List<Flashcard> flashcards = new ArrayList<>();
        Flashcard currentFlashcard = null;
        String currentTag = null;

        // Preload the topic cache from the database
        preloadTopicCache();


        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xmlInput));

            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();

                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        if (tagName.equalsIgnoreCase("flashcard")) {
                            currentFlashcard = new Flashcard();
                            currentFlashcard.setTopics(new ArrayList<>()); // Reset topics for each flashcard
                        } else if (currentFlashcard != null) {
                            currentTag = tagName;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        String text = parser.getText().trim();
                        if (currentFlashcard != null && currentTag != null) {
                            switch (currentTag.toLowerCase()) {
                                case "question":
                                    currentFlashcard.setQuestion(text);
                                    break;
                                case "answer":
                                    currentFlashcard.setAnswer(text);
                                    break;
                                case "searchterm":
                                    currentFlashcard.setSearchTerm(text);
                                    break;
                                case "usernote":
                                    currentFlashcard.setUserNote(text);
                                    break;
                                case "topic":
                                    Topic topic = getOrInsertTopic(text); // Use buffered topic
                                    currentFlashcard.getTopics().add(topic); // Collect topics
                                    break;
                            }
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if (tagName.equalsIgnoreCase("flashcard") && currentFlashcard != null) {
                            flashcards.add(currentFlashcard); // Add to the list of parsed flashcards
                        }
                        currentTag = null;
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("ImportFlashcards", "Error parsing XML", e);
            Toast.makeText(this, "Error parsing XML", Toast.LENGTH_LONG).show();
        }
        return flashcards;
    }

    private Topic getOrInsertTopic(String topicName) {
        // Check if the topic is already in the cache
        if (topicCache.containsKey(topicName)) {
            return topicCache.get(topicName); // Return from cache if it exists
        }

        // If not in cache, check the database
        Topic topic = flashcardDAO.getTopicByName(topicName); // You need to have this method in your DAO
        if (topic == null) {
            // If not in database, insert it
            topic = flashcardDAO.insertTopic(topicName);
        }

        // Add the topic to the cache
        topicCache.put(topicName, topic);

        return topic;
    }

    @Override
    protected void onDestroy() {
        flashcardDAO.close();
        super.onDestroy();
    }
}
