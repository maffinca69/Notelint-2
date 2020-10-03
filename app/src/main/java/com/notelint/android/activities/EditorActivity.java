package com.notelint.android.activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.notelint.android.Application;
import com.notelint.android.MainActivity;
import com.notelint.android.R;
import com.notelint.android.database.models.Note;
import com.notelint.android.helpers.MenuHelper;
import com.notelint.android.helpers.search.SearchTextHelper;
import com.notelint.android.receivers.Alarm;
import com.notelint.android.utils.PrefUtil;
import com.notelint.android.utils.ThemeUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Calendar;

public class EditorActivity extends AppCompatActivity {

    private EditText inputTitle;
    private EditText inputText;
    private long id = 0;

    private boolean isSaved = false;
    private boolean hasBeenCreated = false;
    private boolean isSearch = false;
    private Note note;

    private String updatedTitle = "";
    private String updatedText = "";

    Calendar dateAndTime = Calendar.getInstance();
    private SearchTextHelper searchTextHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ThemeUtil.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        this.inputTitle = findViewById(R.id.input_title);
        this.inputText = findViewById(R.id.input_text);

        this.getDataFromIntent();

        this.inputText.addTextChangedListener(editorTextFieldWatcher);
        this.inputTitle.addTextChangedListener(editorTextFieldWatcher);

        this.prepareToolbar();

        searchTextHelper = new SearchTextHelper(this, this.inputText);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.editor_menu, menu);
        MenuHelper.setColorIcons(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save: {
                this.createOrUpdate(true);
                this.isSaved = true;
                Toast.makeText(Application.getInstance(), "Заметка сохранена", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.notify: {
                new DatePickerDialog(this, d,
                        dateAndTime.get(Calendar.YEAR),
                        dateAndTime.get(Calendar.MONTH),
                        dateAndTime.get(Calendar.DAY_OF_MONTH))
                        .show();
                break;
            }
            case R.id.search_menu_btn: {
                isSearch = true;
                searchTextHelper.prepareSearch();
                break;
            }
        }
        return true;
    }

    TimePickerDialog.OnTimeSetListener t = (view, hourOfDay, minute) -> {
        dateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        dateAndTime.set(Calendar.MINUTE, minute);
        dateAndTime.set(dateAndTime.get(Calendar.YEAR), dateAndTime.get(Calendar.MONTH), dateAndTime.get(Calendar.DAY_OF_MONTH),
                hourOfDay, minute, 0);

        setAlarm(dateAndTime.getTimeInMillis(), StringUtils.trim(this.inputTitle.getText().toString()), StringUtils.trim(this.inputText.getText().toString()));
    };

    DatePickerDialog.OnDateSetListener d = (view, year, monthOfYear, dayOfMonth) -> {
        dateAndTime.set(Calendar.YEAR, year);
        dateAndTime.set(Calendar.MONTH, monthOfYear);
        dateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        (new TimePickerDialog(this, t, dateAndTime.get(Calendar.HOUR_OF_DAY), dateAndTime.get(Calendar.MINUTE), true)).show();
    };

    private void setAlarm(long time, String title, String description) {
        int notifyId = (int) (time / 1000);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Prepare data
        Intent i = new Intent(Application.getInstance(), Alarm.class);
        i.putExtra("notifyId", notifyId);
        i.putExtra("title", title);
        i.putExtra("text", description);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(Application.getInstance(), notifyId, i, 0);

        // Set alarm
        am.setRepeating(AlarmManager.RTC, time, PrefUtil.getNotifyTimeRepeater() * 60 * 1000, pendingIntent);
        // Save to db for delete after rebooting
        com.notelint.android.database.models.Alarm.create(id, title, description, notifyId, time);

        Toast.makeText(Application.getInstance(), "Напоминание установлено", Toast.LENGTH_SHORT).show();
    }

    private void getDataFromIntent() {
        Bundle intent = getIntent().getExtras();
        if (intent == null) {
            return;
        }

        this.note = (Note) getIntent().getParcelableExtra("note");

        assert this.note != null;
        this.inputText.setText(this.note.getText());
        this.inputTitle.setText(this.note.getTitle());

        this.updatedTitle = this.note.getTitle();
        this.updatedText = this.note.getText();

        this.id = this.note.getId();
    }

    @Override
    public void onBackPressed() {
        if (isSearch) {
            isSearch = false;
            searchTextHelper.cancelSearch();
            return;
        }
        exit();
    }

    private void exit() {
        Intent data = new Intent();
        data.putExtra("id", this.id);
        data.putExtra("hasBeenCreated", this.hasBeenCreated);
        setResult(MainActivity.STATUS_CREATED, data);
        finish();
    }

    protected TextWatcher editorTextFieldWatcher = new TextWatcher() {

        @Override
        public void afterTextChanged(Editable s) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            isSaved = false;
        }
    };

    private void prepareToolbar() {
        ((TextView) this.findViewById(R.id.toolbar_title)).setText(this.id == 0 ? "Новая заметка" : "Редактирование заметки");
        this.findViewById(R.id.search_btn).setVisibility(View.GONE);
        setSupportActionBar(this.findViewById(R.id.toolbar));
    }

    private void createOrUpdate(boolean visible) {
        String title = StringUtils.trim(this.inputTitle.getText().toString());
        String text = StringUtils.trim(this.inputText.getText().toString());

        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(text)) {
            return;
        }

        Note lastNote = Note.getLastNote();
        title = StringUtils.isEmpty(title) ? String.format("Новая заметка №%s", lastNote != null ? lastNote.getPosition() + 1 : 1) : title;

        if (this.id == 0) {
            this.hasBeenCreated = true;
            this.id = Note.create(title, text, visible);
        } else if (!StringUtils.equals(updatedText, text) || !StringUtils.equals(updatedTitle, title)) {
            Note.update(this.id, title, text, visible);
        }

        if (PrefUtil.isExitAfterSaving()) {
            exit();
        }
    }
}
