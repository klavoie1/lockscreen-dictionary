package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Saves and loads today's word so the widget doesn't fetch a new one on every update.
 */
public class WordCache {

    private static final String PREFS_NAME = "com.example.app.WordCache";
    private static final String KEY_WORD = "word";
    private static final String KEY_PART_OF_SPEECH = "part_of_speech";
    private static final String KEY_DEFINITION = "definition";
    private static final String KEY_DATE = "date";

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static void save(Context context, WordEntry entry, String date) {
        prefs(context).edit()
                .putString(KEY_WORD, entry.word)
                .putString(KEY_PART_OF_SPEECH, entry.partOfSpeech)
                .putString(KEY_DEFINITION, entry.definition)
                .putString(KEY_DATE, date)
                .apply();
    }

    // Returns the saved entry, or null if nothing complete has been saved yet
    public static WordEntry load(Context context) {
        SharedPreferences p = prefs(context);
        String word = p.getString(KEY_WORD, null);
        String partOfSpeech = p.getString(KEY_PART_OF_SPEECH, null);
        String definition = p.getString(KEY_DEFINITION, null);

        if (word == null || partOfSpeech == null || definition == null) {
            return null;
        }
        return new WordEntry(word, partOfSpeech, definition);
    }

    public static String getDate(Context context) {
        return prefs(context).getString(KEY_DATE, null);
    }
}