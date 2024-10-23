// File: FlashcardDAO.java
package com.example.flashcardapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class FlashcardDAO {
    private SQLiteDatabase database;
    private FlashcardDatabaseHelper dbHelper;

    private String[] allColumns = {
        FlashcardDatabaseHelper.COLUMN_ID,
        FlashcardDatabaseHelper.COLUMN_QUESTION,
        FlashcardDatabaseHelper.COLUMN_ANSWER,
        FlashcardDatabaseHelper.COLUMN_E_FACTOR,
        FlashcardDatabaseHelper.COLUMN_REPETITION,
        FlashcardDatabaseHelper.COLUMN_INTERVAL,
        FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW
    };

    public FlashcardDAO(Context context) {
        dbHelper = new FlashcardDatabaseHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Create a new flashcard
    public Flashcard createFlashcard(Flashcard flashcard) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.getQuestion());
        values.put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.getAnswer());
        values.put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.getEasinessFactor());
        values.put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.getRepetition());
        values.put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.getInterval());
        values.put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.getNextReview());

        long insertId = database.insert(FlashcardDatabaseHelper.TABLE_FLASHCARDS, null, values);
        flashcard.setId((int) insertId);
        return flashcard;
    }

    // Read a flashcard by ID
    public Flashcard getFlashcard(int id) {
        Cursor cursor = database.query(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            allColumns,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            new String[]{String.valueOf(id)},
            null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            Flashcard flashcard = cursorToFlashcard(cursor);
            cursor.close();
            return flashcard;
        } else {
            return null;
        }
    }

    // Update a flashcard
    public void updateFlashcard(Flashcard flashcard) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.getQuestion());
        values.put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.getAnswer());
        values.put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.getEasinessFactor());
        values.put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.getRepetition());
        values.put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.getInterval());
        values.put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.getNextReview());

        database.update(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            values,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            new String[]{String.valueOf(flashcard.getId())}
        );
    }

    // Delete a flashcard
    public void deleteFlashcard(int id) {
        database.delete(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            FlashcardDatabaseHelper.COLUMN_ID + " = ?",
            new String[]{String.valueOf(id)}
        );
    }

    // Get all flashcards
    public List<Flashcard> getAllFlashcards() {
        List<Flashcard> flashcards = new ArrayList<>();

        Cursor cursor = database.query(
            FlashcardDatabaseHelper.TABLE_FLASHCARDS,
            allColumns, null, null, null, null, null
        );

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Flashcard flashcard = cursorToFlashcard(cursor);
            flashcards.add(flashcard);
            cursor.moveToNext();
        }
        cursor.close();
        return flashcards;
    }

    private Flashcard cursorToFlashcard(Cursor cursor) {
        Flashcard flashcard = new Flashcard();
        flashcard.setId(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ID)));
        flashcard.setQuestion(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_QUESTION)));
        flashcard.setAnswer(cursor.getString(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_ANSWER)));
        flashcard.setEasinessFactor(cursor.getDouble(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_E_FACTOR)));
        flashcard.setRepetition(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_REPETITION)));
        flashcard.setInterval(cursor.getInt(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_INTERVAL)));
        flashcard.setNextReview(cursor.getLong(cursor.getColumnIndexOrThrow(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW)));
        return flashcard;
    }

    public long insertFlashcardWithResult(Flashcard flashcard) {
        ContentValues values = new ContentValues();
        values.put(FlashcardDatabaseHelper.COLUMN_QUESTION, flashcard.getQuestion());
        values.put(FlashcardDatabaseHelper.COLUMN_ANSWER, flashcard.getAnswer());
        values.put(FlashcardDatabaseHelper.COLUMN_E_FACTOR, flashcard.getEasinessFactor());
        values.put(FlashcardDatabaseHelper.COLUMN_REPETITION, flashcard.getRepetition());
        values.put(FlashcardDatabaseHelper.COLUMN_INTERVAL, flashcard.getInterval());
        values.put(FlashcardDatabaseHelper.COLUMN_NEXT_REVIEW, flashcard.getNextReview());

        // Insert the flashcard into the flashcards table
        long result = database.insert(FlashcardDatabaseHelper.TABLE_FLASHCARDS, null, values);

        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert flashcard: " + flashcard.getQuestion());
        } else {
            Log.d("DB_SUCCESS", "Flashcard inserted successfully: " + flashcard.getQuestion());
        }

        return result;
    }



}
