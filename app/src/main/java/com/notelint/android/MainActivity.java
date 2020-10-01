package com.notelint.android;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.notelint.android.activities.EditorActivity;
import com.notelint.android.activities.PreferencesActivity;
import com.notelint.android.adapters.MainAdapter;
import com.notelint.android.database.models.Alarm;
import com.notelint.android.database.models.Note;
import com.notelint.android.callbacks.ItemTouchHelperCallback;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

    public static final int STATUS_CREATED = 12345;

    private RecyclerView recyclerView;
    private List<Note> notes = new ArrayList<>();
    private boolean isArchive = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            case R.id.archive:
                isArchive = !isArchive;
                this.notes.clear();
                recyclerView.getAdapter().notifyDataSetChanged();
                fillData(isArchive);
                ((TextView) findViewById(R.id.toolbar_title)).setText(isArchive ? getString(R.string.archive) : getString(R.string.app_name));
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STATUS_CREATED) {
            long id = data.getExtras().getLong("id");
            boolean hasBeenCreated = data.getExtras().getBoolean("hasBeenCreated");
            if (hasBeenCreated) {
                Realm realm = Realm.getDefaultInstance();
                Note note = realm.where(Note.class).equalTo("id", id).findFirstAsync();
                this.notes.add(0, note);
                recyclerView.getAdapter().notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
                realm.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.fillData(false);
        this.recyclerView = findViewById(R.id.recycler);
        this.setAdapter();

        findViewById(R.id.new_note_btn).setOnClickListener(v -> startActivityForResult(new Intent(this, EditorActivity.class), STATUS_CREATED));
        setSupportActionBar(findViewById(R.id.bottom_app_bar));
        this.getDataFromIntent();
        Alarm.reInitAllAlarms();
    }

    private void getDataFromIntent() {
        Bundle intent = getIntent().getExtras();
        if (intent == null) {
            return;
        }

        int id = intent.getInt("notifyId", 0);
        if (id != 0) {
            Alarm.delete(0, id);
        }
    }

    private void fillData(boolean isArchive) {
        Realm realm = Realm.getDefaultInstance();
        RealmQuery<Note> query = realm.where(Note.class)
                .equalTo("visible", true)
                .sort("createdAt", Sort.DESCENDING);
        if (isArchive) {
            query.notEqualTo("deletedAt", 0);
        } else {
            query.equalTo("deletedAt", 0);
        }
        RealmResults result = query.findAllAsync();
        this.notes.addAll(result);
    }

    private void setAdapter() {
        MainAdapter adapter = new MainAdapter(this.notes, this);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelperCallback(adapter, this.recyclerView)).attachToRecyclerView(recyclerView);
    }
}