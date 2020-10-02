package com.notelint.android.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;

import com.notelint.android.Application;
import com.notelint.android.MainActivity;
import com.notelint.android.R;
import com.notelint.android.database.models.Note;
import com.notelint.android.utils.ThemeUtil;

import org.apache.commons.lang3.StringUtils;

import io.realm.Realm;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("themes").setTitle(String.format("Текущая тема: %s", ThemeUtil.getCurrentThemeTitle()));
        findPreference("themes").setOnPreferenceChangeListener((preference, newValue) -> {
            int i = ((ListPreference)preference).findIndexOfValue(newValue.toString());
            CharSequence[] entries = ((ListPreference)preference).getEntryValues();
            int theme = Integer.parseInt(entries[i].toString());
            switch (theme) {
                case 0:
                    ThemeUtil.setCurrentTheme(R.style.AppTheme_Light);
                    break;
                case 1:
                    ThemeUtil.setCurrentTheme(R.style.AppTheme_Dark);
                    break;
                case 2:
                    Toast.makeText(Application.getInstance(), "В разработке...", Toast.LENGTH_SHORT).show();
                    break;
            }
            restart();
            return false;
        });
        findPreference("clearArchive").setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(this)
                    .setTitle("Очистка архива")
                    .setMessage("Вы действительно хотите удалить все заметки в архиве?\nЭто действие нельзя будет отменить")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Note.clearArchive();
                        Toast.makeText(Application.getInstance(), "Архив очищен", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return false;
        });
        findPreference("clearAll").setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(this)
                    .setTitle("Очистка заметок")
                    .setMessage("Вы действительно хотите удалить все заметки?\nЭто действие нельзя будет отменить")
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        Note.clearAll();
                        Toast.makeText(Application.getInstance(), "Все заметки удалены", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
            return false;
        });
    }

    private void restart() {
        Intent intent = new Intent(this, MainActivity.class);
        this.startActivity(intent);
        this.finishAffinity();
    }
}
