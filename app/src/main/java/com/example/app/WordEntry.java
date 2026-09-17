package com.example.app;

/**
 * Holds one word with its part of speech and definition.
 */
public class WordEntry {

    public final String word;
    public final String partOfSpeech;
    public final String definition;

    public WordEntry(String word, String partOfSpeech, String definition) {
        this.word = word;
        this.partOfSpeech = partOfSpeech;
        this.definition = definition;
    }
}