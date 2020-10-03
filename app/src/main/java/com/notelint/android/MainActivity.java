package com.notelint.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.notelint.android.activities.EditorActivity;
import com.notelint.android.activities.PreferencesActivity;
import com.notelint.android.adapters.MainAdapter;
import com.notelint.android.callbacks.ItemTouchHelperCallback;
import com.notelint.android.database.models.Alarm;
import com.notelint.android.database.models.Note;
import com.notelint.android.helpers.MenuHelper;
import com.notelint.android.helpers.search.SearchListHelper;
import com.notelint.android.utils.PrefUtil;
import com.notelint.android.utils.ThemeUtil;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    private static MainActivity instance;
    private Realm realm;

    public static final int STATUS_CREATED = 12345;

    private RecyclerView recyclerView;
    private List<Note> notes = new ArrayList<>();
    private SearchListHelper searchListHelper;
    private boolean isArchive = false;
    private boolean isSearch = false;

    public static MainActivity getInstance() {
        return instance != null ? instance : new MainActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        instance = this;
        ThemeUtil.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        initViews();
    }

    private void init() {
        realm = Realm.getDefaultInstance();
        this.fillData(builder().equalTo("deletedAt", 0));
        this.getDataFromIntent();
        Alarm.reInitAllAlarms();
    }

    private void initViews() {
        this.recyclerView = findViewById(R.id.recycler);
        this.setAdapter();
        findViewById(R.id.new_note_btn).setOnClickListener(v -> startActivityForResult(new Intent(this, EditorActivity.class), STATUS_CREATED));
        findViewById(R.id.search_btn).setOnClickListener(v -> prepareSearch());
        setSupportActionBar(findViewById(R.id.bottom_app_bar));
        searchListHelper = new SearchListHelper(this, recyclerView, this.notes);
    }

    private void prepareSearch() {
        isSearch = true;
        searchListHelper.prepareSearch();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_menu, menu);
        MenuHelper.setColorIcons(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                break;
            case R.id.archive:
                setArchive();
                break;
        }
        return true;
    }

    private void setArchive() {
        isArchive = !isArchive;

        this.notes.clear();
        recyclerView.getAdapter().notifyDataSetChanged();

        RealmQuery<Note> builder = builder();
        if (isArchive) builder.notEqualTo("deletedAt", 0);
        else builder.equalTo("deletedAt", 0);

        fillData(builder);
        ((TextView) findViewById(R.id.toolbar_title)).setText(isArchive ? getString(R.string.archive) : getString(R.string.app_name));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == STATUS_CREATED) {
            long id = data.getExtras().getLong("id");
            boolean hasBeenCreated = data.getExtras().getBoolean("hasBeenCreated");
            if (hasBeenCreated) {
                Note note = realm.where(Note.class).equalTo("id", id).findFirstAsync();
                this.notes.add(0, note);
                recyclerView.getAdapter().notifyItemInserted(0);
                recyclerView.smoothScrollToPosition(0);
            }
        }
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

    private RealmQuery<Note> builder() {
        RealmQuery<Note> builder = realm.where(Note.class)
                .equalTo("visible", true)
                .sort("createdAt", Sort.DESCENDING);
        return builder;
    }

    private void fillData(RealmQuery<Note> builder) {
        RealmResults result = builder.findAllAsync();
        this.notes.addAll(result);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void onBackPressed() {
        if (isSearch) {
            isSearch = false;
            searchListHelper.cancelSearch();
            return;
        }
        if (isArchive && PrefUtil.isExitArchiveBackPressed()) {
            setArchive();
            return;
        }
        super.onBackPressed();
    }

    private void setAdapter() {
        MainAdapter adapter = new MainAdapter(this.notes, this);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        this.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelperCallback(adapter, this.recyclerView)).attachToRecyclerView(recyclerView);
    }
}