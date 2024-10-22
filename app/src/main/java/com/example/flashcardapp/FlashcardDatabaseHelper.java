// File: FlashcardDatabaseHelper.java
package com.example.flashcardapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FlashcardDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "flashcards.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column names
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_E_FACTOR = "easinessFactor";
    public static final String COLUMN_REPETITION = "repetition";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_NEXT_REVIEW = "nextReview";

    // SQL statement to create the table
    private static final String TABLE_CREATE =
        "CREATE TABLE " + TABLE_FLASHCARDS + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_QUESTION + " TEXT, " +
        COLUMN_ANSWER + " TEXT, " +
        COLUMN_E_FACTOR + " REAL, " +
        COLUMN_REPETITION + " INTEGER, " +
        COLUMN_INTERVAL + " INTEGER, " +
        COLUMN_NEXT_REVIEW + " INTEGER" +
        ");";

    public FlashcardDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
       // Handle database upgrade as needed
    }
}
