package com.notelint.android.helpers.search;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.notelint.android.MainActivity;
import com.notelint.android.R;
import com.notelint.android.adapters.MainAdapter;
import com.notelint.android.callbacks.ItemTouchHelperCallback;
import com.notelint.android.database.models.Note;
import com.notelint.android.utils.Util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class SearchListHelper {

    private RecyclerView recyclerView;
    private List<Note> list;
    private Activity activity;

    public SearchListHelper(Activity activity, RecyclerView recyclerView, List<Note> list) {
        this.activity = activity;
        this.recyclerView = recyclerView;
        this.list = list;
    }

    public TextWatcher searchableWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            final String searchText = s.toString().toLowerCase();
            List<Note> filtered = new ArrayList<>();

            if (!StringUtils.isEmpty(s)) {
                searchItems(filtered, searchText);
                setAdapter(filtered);
            } else {
                setAdapter(list);
            }
        }
    };

    private void setAdapter(List<Note> notes) {
        MainAdapter adapter = new MainAdapter(notes, activity);

        this.recyclerView.setLayoutManager(new LinearLayoutManager(activity));
        this.recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelperCallback(adapter, this.recyclerView)).attachToRecyclerView(recyclerView);
    }

    public void prepareSearch() {
        ((MainActivity) activity).findViewById(R.id.toolbar_title).setVisibility(View.GONE);
        ((MainActivity) activity).findViewById(R.id.search).setVisibility(View.VISIBLE);
        EditText editText = ((MainActivity) activity).findViewById(R.id.search);
        editText.addTextChangedListener(searchableWatcher);
        editText.requestFocus();
        Util.showKeyboard();
    }

    public void cancelSearch() {
        ((MainActivity) activity).findViewById(R.id.toolbar_title).setVisibility(View.VISIBLE);
        ((MainActivity) activity).findViewById(R.id.search).setVisibility(View.GONE);
        ((EditText) ((MainActivity) activity).findViewById(R.id.search)).removeTextChangedListener(searchableWatcher);
        setAdapter(list);
    }

    private void searchItems(List<Note> filtered, String search) {
        for (Note note : list) {
            boolean findTitle = note.getTitle().toLowerCase().contains(search.toLowerCase());
            boolean findText = note.getTitle().toLowerCase().contains(search.toLowerCase());
            if (findText || findTitle) {
                filtered.add(0, note);
            }
        }
    }
}
