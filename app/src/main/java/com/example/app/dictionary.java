package com.example.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Implementation of App Widget functionality.
 */
public class dictionary extends AppWidgetProvider {

    private static final String TAG = "DictionaryWidget";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, WordEntry entry) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dictionary);
        views.setTextViewText(R.id.dictWord, entry.word);
        views.setTextViewText(R.id.dictPartSpeech, entry.partOfSpeech);
        views.setTextViewText(R.id.dictDefinition, entry.definition);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    // The placeholder text from strings.xml, used when nothing else is available. Just
    // using a random word and definition. Might change later to actually placeholder text
    private static WordEntry placeholder(Context context) {
        return new WordEntry(
                context.getString(R.string.dict_word),
                context.getString(R.string.dict_part_of_speech),
                context.getString(R.string.dict_definition));
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "onUpdate Called");

        final PendingResult pendingResult = goAsync();

        executor.execute(() -> {
            try {
                WordEntry entry;
                WordEntry saved = WordCache.load(context);
                String savedDate = WordCache.getDate(context);
                String today = LocalDate.now().toString();

                if (saved != null && today.equals(savedDate)) {
                    Log.d(TAG, "Using cached word: " + saved.word);
                    entry = saved;
                } else {
                    try {
                        entry = new DictionaryAPI().getWordOfTheDay();
                        WordCache.save(context, entry, today);
                        Log.d(TAG, "Fetched and saved new word: " + entry.word);
                    } catch (IOException e) {
                        Log.e(TAG, "Couldn't fetch word of the day", e);
                        entry = (saved != null) ? saved : placeholder(context);
                    }
                }

                for (int appWidgetId : appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, entry);
                }
            } finally {
                pendingResult.finish();
            }
        });
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}