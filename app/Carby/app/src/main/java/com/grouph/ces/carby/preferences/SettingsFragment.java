package com.grouph.ces.carby.preferences;

import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;

import com.grouph.ces.carby.R;
import com.grouph.ces.carby.database.AppDatabase;

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListener();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.app_preferences);
    }

    private void setListener() {
        AppDatabase db = Room.databaseBuilder(getActivity() ,AppDatabase.class,"myDB").allowMainThreadQueries().build();
        Preference wipeConsume = findPreference(getResources().getString(R.string.key_wipe_consume));
        wipeConsume.setOnPreferenceClickListener((Preference preference) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.dialog_message_wipe_consume);
            builder.setTitle(R.string.title_wipe_consume);

            builder.setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
                db.consumptionDataDao().nukeTable();
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int id) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });

        Preference wipeAll = findPreference(getResources().getString(R.string.key_wipe_all));
        wipeAll.setOnPreferenceClickListener((Preference preference) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setMessage(R.string.dialog_message_wipe_all);
            builder.setTitle(R.string.title_wipe_all);

            builder.setPositiveButton(R.string.ok, (DialogInterface dialog, int id) -> {
                db.consumptionDataDao().nukeTable();
                db.nutritionDataDao().nukeTable();
                dialog.dismiss();
            });
            builder.setNegativeButton(R.string.cancel, (DialogInterface dialog, int id) -> {
                dialog.dismiss();
            });

            AlertDialog dialog = builder.create();
            dialog.show();
            return true;
        });
    }
}