package com.smith.lotrdeckbuilder;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.smith.lotrdeckbuilder.game.Card;
import com.smith.lotrdeckbuilder.game.Deck;
import com.smith.lotrdeckbuilder.game.RingsDB;
import com.smith.lotrdeckbuilder.helper.AppManager;
import com.smith.lotrdeckbuilder.helper.CardImagesDownloader;
import com.smith.lotrdeckbuilder.helper.StringDownloader;
import com.smith.lotrdeckbuilder.prefs.SetNamesPreferenceMultiSelect;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

public class SettingsActivity extends PreferenceActivity
        implements OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_DISPLAY_ALL_DATA_PACKS = "pref_DataPacksShowAll";
    public static final String KEY_PREF_DATA_PACKS_TO_DISPLAY = "pref_DataPacks";
    public static final String KEY_PREF_AMOUNT_OF_CORE_DECKS = "pref_AmountOfCoreDecks";
    public static final String KEY_PREF_DISPLAY_SET_NAMES_WITH_CARDS = "pref_ShowSetNames";
    public static final String KEY_PREF_TAP_TO_CLOSE_CARD_PREVIEW = "pref_TapToCloseCardPreview";
    public static final String KEY_PREF_CLEAR_CACHE = "pref_ClearCache";
    public static final String KEY_PREF_DOWNLOAD_ALL_IMAGES = "pref_DownloadAllImages";
    public static final String KEY_PREF_LANGUAGE = "pref_Language";
    public static final String KEY_PREF_USE_MOST_WANTED_LIST = "pref_MostWantedList";
    public static final String KEY_PREF_EXPORT_ALL_DECKS = "pref_ExportAllDecks";

    private String mInitialPacksToDisplay;

    // Preferences
    Preference prefDataPacks;
    Preference prefAmountOfCoreDecks;
    Preference prefClearCache;
    Preference prefDownloadAllImages;
    Preference prefLanguage;
    Preference prefExportDecks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // Preferences
        prefDataPacks = findPreference(KEY_PREF_DATA_PACKS_TO_DISPLAY);
        prefAmountOfCoreDecks = findPreference(KEY_PREF_AMOUNT_OF_CORE_DECKS);
        prefClearCache = findPreference(KEY_PREF_CLEAR_CACHE);
        prefDownloadAllImages = findPreference(KEY_PREF_DOWNLOAD_ALL_IMAGES);
        prefLanguage = findPreference(KEY_PREF_LANGUAGE);
        prefExportDecks = findPreference(KEY_PREF_EXPORT_ALL_DECKS);

        // Listeners
        prefDataPacks.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (newValue.toString().isEmpty()) {
                    Toast.makeText(SettingsActivity.this, R.string.toast_require_one_datapack, Toast.LENGTH_LONG).show();
                    return false;
                } else {
                    return true;
                }
            }
        });
        prefClearCache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
                builder.setTitle(R.string.clear_cache);
                builder.setMessage(getString(R.string.message_clear_cache));
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {                    }
                });
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteCache(SettingsActivity.this);
                    }
                });
                builder.show();
                return false;
            }
        });
        prefDownloadAllImages.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                CardImagesDownloader cardDownloader = new CardImagesDownloader(SettingsActivity.this, new CardImagesDownloader.CardImagesDownloaderListener() {
                    @Override
                    public void onBeforeStartTask(Context context, int max) {
                    }

                    @Override
                    public void onTaskCompleted() {
                    }

                    @Override
                    public void onImageDownloaded(Card card, int count, int max) {
                    }
                });
                cardDownloader.execute();
                return false;
            }
        });
        prefExportDecks.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                JSONArray jsonArray = new JSONArray();
                for (Deck deck : AppManager.getInstance().getAllDecks()) {
                    JSONObject jsonDeck = deck.toJSON();
                    jsonArray.put(jsonDeck);
                }
                String filename = "lotr_decks.anrdecks";
                // Save the file as OCTGN format
                try {
                    FileOutputStream fileOut = openFileOutput(filename, Context.MODE_WORLD_READABLE);
                    fileOut.write(jsonArray.toString().getBytes());
                    fileOut.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.i("LOGCAT", String.valueOf(getFileStreamPath(filename)));

                // Create the send intent
                Intent intentEmail = new Intent(Intent.ACTION_SEND);
                intentEmail.setType("text/plain");
                intentEmail.putExtra(Intent.EXTRA_SUBJECT, "Lotr Deck - All decks");
                intentEmail.putExtra(Intent.EXTRA_TEXT, "\r\n\r\nDownload Android Lotr DeckBuilder for free at https://play.google.com/store/apps/details?id=com.smith.lotrdeckbuilder");
                intentEmail.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(getFileStreamPath(filename)));
                startActivityForResult(Intent.createChooser(intentEmail, getText(R.string.menu_share)), 0);

                return false;
            }
        });

        // Initial preferences
        mInitialPacksToDisplay = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_PREF_DATA_PACKS_TO_DISPLAY, "");

        // Display the summary for data packs to display
        refreshPrefsSummaries();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        if (key.equals(KEY_PREF_DATA_PACKS_TO_DISPLAY)) {
            // If zero is selected, cancel commit
            if (SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(key, "")) == null) {
                sharedPreferences.edit().putString(key, mInitialPacksToDisplay).apply();
            }
            // change the summary to display the datapacks to use
            mInitialPacksToDisplay = sharedPreferences.getString(key, "");
            refreshPrefsSummaries();
        } else if (key.equals(KEY_PREF_DISPLAY_ALL_DATA_PACKS)) {
            refreshPrefsSummaries();
        } else if (key.equals(KEY_PREF_AMOUNT_OF_CORE_DECKS)) {
            refreshPrefsSummaries();
        } else if (key.equals(KEY_PREF_LANGUAGE)) {
            StringDownloader sd = new StringDownloader(this, String.format(RingsDB.getAllCardsUrl()), AppManager.FILE_CARDS_JSON, new StringDownloader.FileDownloaderListener() {
                ProgressDialog mDialog;

                @Override
                public void onBeforeTask() {
                    // Display a progress dialog
                    mDialog = new ProgressDialog(SettingsActivity.this);
                    mDialog.setTitle(getString(R.string.downloading_cards));
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mDialog.setMessage(getString(R.string.downloading_cards_restart));
                    mDialog.show();
                }

                @Override
                public void onTaskComplete(String s) {
                    // Close the dialog
                    mDialog.dismiss();

                    Context context = SettingsActivity.this;
                    Intent mStartActivity = new Intent(context, MainActivity.class);
                    int mPendingIntentId = 123456;
                    PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
                    mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                    System.exit(0);
                }

                @Override
                public void onError(Exception e) {
                    // Display the error and cancel the ongoing dialog
                    mDialog.dismiss();

                    // If zero cards are available, exit the application
                    if (AppManager.getInstance().getAllCards().size() <= 0) {
                        Toast.makeText(SettingsActivity.this, R.string.error_downloading_cards_quit, Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(SettingsActivity.this, R.string.error_downloading_cards, Toast.LENGTH_LONG).show();
                    }

                    // Log
                    Log.e("LOG", e.getMessage());
                }
            });
            sd.execute();
        }

    }

    private void refreshPrefsSummaries() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Packs
        String packs[] = SetNamesPreferenceMultiSelect.parseStoredValue(sharedPreferences.getString(KEY_PREF_DATA_PACKS_TO_DISPLAY, ""));
        if (packs != null) {
            prefDataPacks.setSummary(TextUtils.join(", ", packs));
        }

        // Amount of core decks
        prefAmountOfCoreDecks.setSummary(sharedPreferences.getString(KEY_PREF_AMOUNT_OF_CORE_DECKS, "1"));




    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK);
        super.onBackPressed();
    }

    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
