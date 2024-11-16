// File: FlashcardDAO.java
package com.example.flashcardapp;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FlashcardDAO {
    private SQLiteDatabase database;
    private FlashcardDatabaseHelper dbHelper;

    private String[] flashcardColumns = {
            FlashcardDatabaseHelper.COLUMN_ID,
            FlashcardDatabaseHelper.COLUMN_QUESTION,
            FlashcardDatabaseHelper.COLUMN_ANSWER,
            FlashcardDatabaseHelper.COLUMN_E_FACTOR,
            FlashcardDatabaseHelper.COLUMN_REPETITION,
            FlashcardDatabaseHelper.COLUMN_INTERVAL,
            FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW,
            FlashcardDatabaseHelper.COLUMN_SEARCH_TERM,  // Add search term
            FlashcardDatabaseHelper.COLUMN_USER_NOTE     // Add user note
    };

    private String[] topicColumns = {
            FlashcardDatabaseHelper.COLUMN_TOPIC_ID,
            FlashcardDatabaseHelper.COLUMN_TOPIC_NAME
    };

    public FlashcardDAO(Context context) {
        dbHelper = new FlashcardDatabaseHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Method to create a new flashcard
    public Flashcard createFlashcard(Flashcard flashcard) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.getQuestion());
        values.put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.getAnswer());
        values.put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.getEasinessFactor());
        values.put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.getRepetition());
        values.put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.getInterval());
        values.put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.getNextReview());
        values.put(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM, flashcard.getSearchTerm());
        values.put(FlashcardDatabaseHelper.COLUMN_USER_NOTE, flashcard.getUserNote());

        long insertId = database.insert(FlashcardDatabaseHelper.TABLE_FLASHCARDS, null, values);
        flashcard.setId((int) insertId);
        return flashcard;
    }

    // Method to retrieve a flashcard by its ID
    public Flashcard getFlashcard(int id) {
        Cursor cursor = database.query(
                FlashcardDatabaseHelper.TABLE_FLASHCARDS,
                flashcardColumns,
                FlashcardDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null
        );

        Flashcard flashcard = null;
        if (cursor != null && cursor.moveToFirst()) {
            flashcard = cursorToFlashcard(cursor);
            cursor.close();
        }

        return flashcard;
    }


    public Flashcard getNextDueFlashcard(long currentTime) {
        Flashcard flashcard = null;

        // Query to get the flashcard with the closest next_review time that is due
        String query = "SELECT * FROM view_filtered_flashcards " +
                " WHERE " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " <= ?" +
                " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " DESC " +
                " LIMIT 1"; // Limit to the first result

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(currentTime)});

        if (cursor.moveToFirst()) {
            flashcard = cursorToFlashcard(cursor); // Convert the cursor to a flashcard object
        }
        cursor.close();

        return flashcard;
    }


    // Method to update an existing flashcard
    public void updateFlashcard(Flashcard flashcard) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.getQuestion());
        values.put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.getAnswer());
        values.put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.getEasinessFactor());
        values.put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.getRepetition());
        values.put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.getInterval());
        values.put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.getNextReview());
        values.put(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM, flashcard.getSearchTerm());
        values.put(FlashcardDatabaseHelper.COLUMN_USER_NOTE, flashcard.getUserNote());

        database.update(
                FlashcardDatabaseHelper.TABLE_FLASHCARDS,
                values,
                FlashcardDatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(flashcard.getId())}
        );
    }

    // Method to clear all topic associations for a flashcard
    public void clearTopicsForFlashcard(int flashcardId) {
        int rowsDeleted = database.delete(
                FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF,
                FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID + " = ?",
                new String[]{String.valueOf(flashcardId)}
        );
        Log.d("Database", "Rows deleted: " + rowsDeleted);
    }

    // Method to insert a topic
    public Topic insertTopic(String topicName) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME, topicName);

        long topicId = database.insertWithOnConflict(
                FlashcardDatabaseHelper.TABLE_TOPICS, null, values, SQLiteDatabase.CONFLICT_IGNORE);

        Topic topic = new Topic(topicName);
        topic.setId((int) topicId);
        return topic;
    }

    public void updateTopicSelection(int topicId, boolean isSelected) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED, isSelected ? 1 : 0);
        database.update(
                FlashcardDatabaseHelper.TABLE_TOPICS,
                values,
                FlashcardDatabaseHelper.COLUMN_TOPIC_ID + " = ?",
                new String[]{String.valueOf(topicId)}
        );
    }

    // Method to associate a flashcard with a topic
    public void associateFlashcardWithTopic(int flashcardId, int topicId) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID, flashcardId);
        values.put(FlashcardDatabaseHelper.COLUMN_TOPIC_ID_REF, topicId);

        database.insert(FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF, null, values);
    }

    // Get all topics for a flashcard
    public List<Topic> getTopicsForFlashcard(int flashcardId) {
        List<Topic> topics = new ArrayList<>();
        Cursor cursor = database.rawQuery("SELECT * FROM " + FlashcardDatabaseHelper.TABLE_TOPICS + " t" +
                " INNER JOIN " + FlashcardDatabaseHelper.TABLE_FLASHCARD_TOPIC_CROSS_REF + " c" +
                " ON t." + FlashcardDatabaseHelper.COLUMN_TOPIC_ID + " = c." + FlashcardDatabaseHelper.COLUMN_TOPIC_ID_REF +
                " WHERE c." + FlashcardDatabaseHelper.COLUMN_FLASHCARD_ID + " = ?", new String[]{String.valueOf(flashcardId)});

        if (cursor.moveToFirst()) {
            do {
                Topic topic = new Topic();
                topic.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_TOPIC_ID)));
                topic.setName(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME)));
                topics.add(topic);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return topics;
    }

    // Method to retrieve all topics
    public List<Topic> getAllTopics() {
        List<Topic> topics = new ArrayList<>();
        Cursor cursor = database.query(
                FlashcardDatabaseHelper.TABLE_TOPICS,
                new String[]{FlashcardDatabaseHelper.COLUMN_TOPIC_ID, FlashcardDatabaseHelper.COLUMN_TOPIC_NAME,FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED},
                null, null, null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") int topicId = cursor.getInt(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_ID));
                @SuppressLint("Range") String topicName = cursor.getString(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME));
                @SuppressLint("Range") Boolean topicSelected = cursor.getInt(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED)) ==1;
                topics.add(new Topic(topicId, topicName, topicSelected));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return topics;
    }

    // Method to retrieve a topic by name
    public Topic getTopicByName(String topicName) {
        Topic topic = null;
        Cursor cursor = database.query(
                FlashcardDatabaseHelper.TABLE_TOPICS,
                new String[]{FlashcardDatabaseHelper.COLUMN_TOPIC_ID, FlashcardDatabaseHelper.COLUMN_TOPIC_NAME},
                FlashcardDatabaseHelper.COLUMN_TOPIC_NAME + " = ?",
                new String[]{topicName},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") int topicId = cursor.getInt(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_ID));
            @SuppressLint("Range") String retrievedTopicName = cursor.getString(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_NAME));
            @SuppressLint("Range") Boolean topicSelected = cursor.getInt(cursor.getColumnIndex(FlashcardDatabaseHelper.COLUMN_TOPIC_SELECTED)) == 1;
            topic = new Topic(topicId, retrievedTopicName, topicSelected);
            cursor.close();
        }

        return topic;
    }


    // Helper method to convert a cursor to a Flashcard object
    private Flashcard cursorToFlashcard(Cursor cursor) {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ID)));
        flashcard.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_QUESTION)));
        flashcard.setAnswer(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ANSWER)));
        flashcard.setEasinessFactor(cursor.getDouble(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_E_FACTOR)));
        flashcard.setRepetition(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_REPETITION)));
        flashcard.setInterval(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_INTERVAL)));
        flashcard.setNextReview(cursor.getLong(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW)));
        flashcard.setSearchTerm(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_SEARCH_TERM)));  // Get search term
        flashcard.setUserNote(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_USER_NOTE)));      // Get user note

        List<Topic> topics = getTopicsForFlashcard(flashcard.getId());
        flashcard.setTopics(topics);

        return flashcard;
    }

    // Get flashcards with next_review in the future (ascending)
    public List<Flashcard> getFutureFlashcards() {
        List<Flashcard> flashcards = new ArrayList<>();

        String query = "SELECT * FROM view_filtered_flashcards WHERE " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " > ?" +
                " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " ASC";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(System.currentTimeMillis())});

        if (cursor.moveToFirst()) {
            do {
                flashcards.add(cursorToFlashcard(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return flashcards;
    }

    public List<Flashcard> getPastFlashcards() {
        List<Flashcard> flashcards = new ArrayList<>();

        String query = "SELECT * FROM view_filtered_flashcards WHERE " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " <= ?" +
                " ORDER BY " + FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW + " DESC";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(System.currentTimeMillis())});

        if (cursor.moveToFirst()) {
            do {
                flashcards.add(cursorToFlashcard(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();

        return flashcards;
    }


    public int[] getPastAndFutureQuestionsCount() {

        long currentTime = System.currentTimeMillis();


        String query = "SELECT " +
                "SUM(CASE WHEN nextReview <= ? THEN 1 ELSE 0 END) AS pastCount, " +
                "SUM(CASE WHEN nextReview > ? THEN 1 ELSE 0 END) AS futureCount " +
                "FROM view_filtered_flashcards";

        Cursor cursor = database.rawQuery(query, new String[]{String.valueOf(currentTime), String.valueOf(currentTime)});
        cursor.moveToFirst();
        @SuppressLint("Range") int pastCount = cursor.getInt(cursor.getColumnIndex("pastCount"));
        @SuppressLint("Range") int futureCount = cursor.getInt(cursor.getColumnIndex("futureCount"));
        cursor.close();

        return new int[]{pastCount, futureCount}; // Return both counts as an array
    }

    // Get flashcards with next_review in the past (descending)

}
