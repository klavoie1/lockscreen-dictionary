package com.example.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DictionaryAPI {

    private static final String TAG = "DictionaryAPI";
    private static final String RANDOM_WORD_URL = "https://random-word-api.herokuapp.com/word";
    private static final String DICTIONARY_URL = "https://freedictionaryapi.com/api/v1/entries/en/";
    private static final int MAX_ATTEMPTS = 3;

    // Thrown when a request comes back with anything other than 200
    public static class HttpStatusException extends IOException {
        public final int statusCode;

        public HttpStatusException(int statusCode) {
            super("HTTP Response was: " + statusCode);
            this.statusCode = statusCode;
        }
    }

    // Thrown when the dictionary has no usable definition for a word
    public static class WordNotFoundException extends IOException {
        public WordNotFoundException(String message) {
            super(message);
        }
    }

    // Gets a random word with its definition, trying a new word if one isn't in the dictionary
    public WordEntry getWordOfTheDay() throws IOException {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String word = getRandomWord();
            try {
                return lookUpWord(word);
            } catch (WordNotFoundException e) {
                Log.d(TAG, "Attempt " + attempt + ": " + e.getMessage());
            }
        }
        throw new IOException("No definition found after " + MAX_ATTEMPTS + " attempts");
    }

    public String getRandomWord() throws IOException {
        String text = fetchText(RANDOM_WORD_URL);
        Log.d(TAG, "Raw random word response: " + text);

        try {
            JSONArray jsonArray = new JSONArray(text);
            return jsonArray.getString(0);
        } catch (JSONException e) {
            throw new IOException("Couldn't parse random word response", e);
        }
    }

    public WordEntry lookUpWord(String word) throws IOException {
        // Needed to make the word capable in the URL, errored elsewise
        String encoded = URLEncoder.encode(word, "UTF-8").replace("+", "%20");

        String text;
        try {
            text = fetchText(DICTIONARY_URL + encoded);
        } catch (HttpStatusException e) {
            if (e.statusCode == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new WordNotFoundException("No dictionary entry for " + word);
            }
            throw e;
        }

        try {
            JSONObject root = new JSONObject(text);
            JSONArray entries = root.optJSONArray("entries");

            if (entries != null) {
                // Find the first entry that has a non-empty definition
                for (int i = 0; i < entries.length(); i++) {
                    JSONObject entry = entries.getJSONObject(i);
                    String partOfSpeech = entry.optString("partOfSpeech", "");
                    JSONArray senses = entry.optJSONArray("senses");

                    if (senses == null) {
                        continue;
                    }

                    for (int j = 0; j < senses.length(); j++) {
                        String definition = senses.getJSONObject(j).optString("definition", "");
                        if (!definition.isEmpty()) {
                            return new WordEntry(word, partOfSpeech, definition);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new IOException("Couldn't parse dictionary response for " + word, e);
        }

        throw new WordNotFoundException("No definition in entries for " + word);
    }

    private String fetchText(String address) throws IOException {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        try {
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            int responseCode = con.getResponseCode();
            Log.d(TAG, "GET " + address + " :: " + responseCode);

            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new HttpStatusException(responseCode);
            }

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder builder = new StringBuilder();
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    builder.append(inputLine);
                }
                return builder.toString();
            }
        } finally {
            con.disconnect();
        }
    }
}