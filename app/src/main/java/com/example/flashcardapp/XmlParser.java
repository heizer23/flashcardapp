// File: XmlParser.java
package com.example.flashcardapp;

import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class XmlParser {

    // Method to parse XML input and return a list of Flashcard objects
    public List<Flashcard> parse(String xmlInput) throws XmlPullParserException, Exception {
        List<Flashcard> flashcards = new ArrayList<>();
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(new StringReader(xmlInput));
        int eventType = parser.getEventType();
        Flashcard currentFlashcard = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name;
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("flashcard")) {
                        currentFlashcard = new Flashcard();
                    } else if (currentFlashcard != null) {
                        if (name.equalsIgnoreCase("question")) {
                            currentFlashcard.setQuestion(parser.nextText());
                        } else if (name.equalsIgnoreCase("answer")) {
                            currentFlashcard.setAnswer(parser.nextText());
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("flashcard") && currentFlashcard != null) {
                        flashcards.add(currentFlashcard);
                        currentFlashcard = null;
                    }
                    break;
            }
            eventType = parser.next();
        }
        return flashcards;
    }
}
