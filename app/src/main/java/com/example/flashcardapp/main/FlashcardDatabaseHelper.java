// File: FlashcardDatabaseHelper.java
package com.example.flashcardapp.main;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class FlashcardDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "flashcards.db";
    private static final int DATABASE_VERSION = 12;  // Incremented for new tables


    // Table and column names
    public static final String TABLE_FLASHCARDS = "flashcards";
    public static final String TABLE_TOPICS = "topics";
    public static final String TABLE_FLASHCARD_TOPIC_CROSS_REF = "flashcard_topic_cross_ref";

    // Flashcard columns
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_QUESTION = "question";
    public static final String COLUMN_ANSWER = "answer";
    public static final String COLUMN_E_FACTOR = "easinessFactor";
    public static final String COLUMN_REPETITION = "repetition";
    public static final String COLUMN_INTERVAL = "interval";
    public static final String COLUMN_NEXT_REVIEW = "nextReview";
    public static final String COLUMN_SEARCH_TERM = "searchTerm";
    public static final String COLUMN_USER_NOTE = "userNote";

    // Topic columns
    public static final String COLUMN_TOPIC_ID = "id";
    public static final String COLUMN_TOPIC_NAME = "name";
    public static final String COLUMN_TOPIC_SELECTED = "selected";

    // CrossRef columns
    public static final String COLUMN_FLASHCARD_ID = "flashcard_id";
    public static final String COLUMN_TOPIC_ID_REF = "topic_id";

    // SQL statement to create the flashcards table
    private static final String TABLE_CREATE_FLASHCARDS =
            "CREATE TABLE " + TABLE_FLASHCARDS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_QUESTION + " TEXT, " +
                    COLUMN_ANSWER + " TEXT, " +
                    COLUMN_E_FACTOR + " REAL, " +
                    COLUMN_REPETITION + " INTEGER, " +
                    COLUMN_INTERVAL + " INTEGER, " +
                    COLUMN_NEXT_REVIEW + " INTEGER, " +
                    COLUMN_SEARCH_TERM + " TEXT, " +    // Add searchTerm column
                    COLUMN_USER_NOTE + " TEXT" +        // Add userNote column
                    ");";

    // SQL statement to create the topics table
    private static final String TABLE_CREATE_TOPICS =
            "CREATE TABLE " + TABLE_TOPICS + " (" +
                    COLUMN_TOPIC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TOPIC_NAME + " TEXT, " +
                    COLUMN_TOPIC_SELECTED + " INTEGER DEFAULT 0" + // Added the selected column
                    ");";


    // SQL statement to create the flashcard_topic_cross_ref table
    private static final String TABLE_CREATE_FLASHCARD_TOPIC_CROSS_REF =
            "CREATE TABLE " + TABLE_FLASHCARD_TOPIC_CROSS_REF + " (" +
                    COLUMN_FLASHCARD_ID + " INTEGER, " +
                    COLUMN_TOPIC_ID_REF + " INTEGER, " +
                    "PRIMARY KEY(" + COLUMN_FLASHCARD_ID + ", " + COLUMN_TOPIC_ID_REF + "), " +
                    "FOREIGN KEY(" + COLUMN_FLASHCARD_ID + ") REFERENCES " + TABLE_FLASHCARDS + "(" + COLUMN_ID + "), " +
                    "FOREIGN KEY(" + COLUMN_TOPIC_ID_REF + ") REFERENCES " + TABLE_TOPICS + "(" + COLUMN_TOPIC_ID + ")" +
                    ");";

    private static final String CREATE_VIEW_FILTERED_FLASHCARDS =
            "CREATE VIEW IF NOT EXISTS view_filtered_flashcards AS " +
                    "SELECT DISTINCT flashcards.* " +
                    "FROM flashcards " +
                    "JOIN flashcard_topic_cross_ref ON flashcards.id = flashcard_topic_cross_ref.flashcard_id " +
                    "JOIN topics ON flashcard_topic_cross_ref.topic_Id = topics.id " +
                    "WHERE topics.selected = 1;";


    // Review History Table Constants
    public static final String TABLE_REVIEW_HISTORY = "review_history";
    public static final String COLUMN_HISTORY_ID = "id";
    public static final String COLUMN_HISTORY_QUESTION_ID = "question_id";
    public static final String COLUMN_HISTORY_CONFIDENCE_LEVEL = "confidence_level";
    public static final String COLUMN_HISTORY_TIMESTAMP = "timestamp";
    public static final String COLUMN_HISTORY_TIME_SINCE_LAST_SEEN = "time_since_last_seen";
    public static final String COLUMN_HISTORY_INTERVAL = "interval";
    public static final String COLUMN_HISTORY_REVIEW_TYPE = "review_type";
    public static final String COLUMN_HISTORY_ANSWER_DURATION = "answer_duration";


    private static final String TABLE_CREATE_REVIEW_HISTORY = "CREATE TABLE " + TABLE_REVIEW_HISTORY + " (" +
            COLUMN_HISTORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_HISTORY_QUESTION_ID + " INTEGER, " +
            COLUMN_HISTORY_CONFIDENCE_LEVEL + " INTEGER, " +
            COLUMN_HISTORY_TIMESTAMP + " INTEGER, " +
            COLUMN_HISTORY_TIME_SINCE_LAST_SEEN + " INTEGER, " +
            COLUMN_HISTORY_INTERVAL + " INTEGER, " +
            COLUMN_HISTORY_REVIEW_TYPE + " TEXT, " + // Review type (e.g., "normal", "quiz")
            COLUMN_HISTORY_ANSWER_DURATION + " INTEGER, " + // Answer duration in milliseconds
            "FOREIGN KEY(" + COLUMN_HISTORY_QUESTION_ID + ") REFERENCES " + TABLE_FLASHCARDS + "(" + COLUMN_ID + ") " +
            "ON DELETE CASCADE);";

    public FlashcardDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_FLASHCARDS);
        db.execSQL(TABLE_CREATE_TOPICS);
        db.execSQL(TABLE_CREATE_FLASHCARD_TOPIC_CROSS_REF);
        db.execSQL(CREATE_VIEW_FILTERED_FLASHCARDS);
        db.execSQL(TABLE_CREATE_REVIEW_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrade
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_FLASHCARDS + " ADD COLUMN " + COLUMN_SEARCH_TERM + " TEXT;");
            db.execSQL("ALTER TABLE " + TABLE_FLASHCARDS + " ADD COLUMN " + COLUMN_USER_NOTE + " TEXT;");
            db.execSQL(TABLE_CREATE_TOPICS);
            db.execSQL(TABLE_CREATE_FLASHCARD_TOPIC_CROSS_REF);
        }
        if (oldVersion < 3){
            db.execSQL("ALTER TABLE topics ADD COLUMN selected INTEGER DEFAULT 0");
        }

        if (oldVersion < 11){
            db.execSQL(TABLE_CREATE_REVIEW_HISTORY);
        }

        db.execSQL("DROP VIEW IF EXISTS view_filtered_flashcards;");
        db.execSQL(CREATE_VIEW_FILTERED_FLASHCARDS);


    }





}
