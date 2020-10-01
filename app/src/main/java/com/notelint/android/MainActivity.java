package com.notelint.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
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

import io.realm.OrderedRealmCollectionChangeListener;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {

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
                if (isArchive) {
                    ((TextView) findViewById(R.id.toolbar_title)).setText(getString(R.string.archive));
                } else {
                    ((TextView) findViewById(R.id.toolbar_title)).setText(getString(R.string.app_name));
                }
                break;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.fillData(false);
        this.recyclerView = findViewById(R.id.recycler);
        this.setAdapter();

        findViewById(R.id.new_note_btn).setOnClickListener(v -> startActivity(new Intent(this, EditorActivity.class)));
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
        Log.e("id", String.valueOf(id));
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
        RealmResults result = query.findAll();
        result.removeAllChangeListeners();
        result.addChangeListener(changeListener);
        this.notes.addAll(result);
    }

    // Костыль для обновления данных в списке. Надо бы использовать DiffUtils
    private OrderedRealmCollectionChangeListener<RealmResults<Note>> changeListener = (results, changeSet) -> {
        // Change
        int[] changes = changeSet.getChanges();
        if (changes.length != 0) {
            recyclerView.getAdapter().notifyItemChanged(changes[0]);
        }

        // Inserts
        int[] insertions = changeSet.getInsertions();
        Note inserted = results.first();
        if (insertions.length != 0 && notes.contains(inserted)) {
            int insertedPosition = 0;
            notes.add(insertedPosition, inserted);
            recyclerView.getAdapter().notifyItemInserted(insertions[0]);
            recyclerView.smoothScrollToPosition(insertedPosition);
        }
    };

    private void setAdapter() {
        MainAdapter adapter = new MainAdapter(this.notes, this);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelperCallback(adapter, this.recyclerView)).attachToRecyclerView(recyclerView);
    }
}