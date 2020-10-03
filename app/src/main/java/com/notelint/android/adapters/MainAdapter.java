package com.notelint.android.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.notelint.android.MainActivity;
import com.notelint.android.R;
import com.notelint.android.activities.EditorActivity;
import com.notelint.android.database.models.Note;
import com.notelint.android.helpers.DateHelper;
import com.notelint.android.interfaces.ItemTouchHelperAdapter;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import io.realm.ObjectChangeSet;
import io.realm.RealmModel;
import io.realm.RealmObjectChangeListener;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> implements ItemTouchHelperAdapter {

    public List<Note> notes;
    private Activity context;
    private boolean isDelete = true;

    public MainAdapter(List<Note> notes, Activity context) {
        this.notes = notes;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(final @NonNull ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.main_recycler_content, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.itemView.setOnClickListener(v -> {
            final int position = viewHolder.getAdapterPosition();
            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            Note note = this.notes.get(position);
            note.addChangeListener((realmModel, changeSet) -> notifyItemChanged(position));

            Intent intent = new Intent(this.context, EditorActivity.class);
            intent.putExtra("note", note);
            this.context.startActivity(intent);
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Note note = this.notes.get(holder.getAdapterPosition());
        holder.title.setText(note.getTitle());
        holder.text.setText(note.getText());
        holder.createdAt.setText(DateHelper.getFormattedDate(note.getCreatedAt()));
    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }

    @Override
    public void onItemDismiss(int position, RecyclerView recyclerView) {
        Note note = notes.get(position);
        boolean isDeleted = note.getDeletedAt() != 0; // перемещена в архив

        notifyItemRemoved(position);
        notes.remove(position);
        Snackbar snackbar = Snackbar.make(recyclerView, isDeleted ? "Заметка удалена" : "Заметка перемещена в архив", Snackbar.LENGTH_LONG);
        snackbar.setAction(android.R.string.cancel, v -> {
            isDelete = false;
            notes.add(position, note);
            notifyItemInserted(position);
            recyclerView.scrollToPosition(position);
            snackbar.dismiss();
        });
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                super.onDismissed(transientBottomBar, event);
                if (isDelete) {
                    if (isDeleted) {
                        context.runOnUiThread(note::hardDelete); // удаляем из базы
                    } else {
                        context.runOnUiThread(note::softDelete); // кидаем в архив
                    }
                }
            }
        });
        snackbar.show();
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(notes, i, i + 1);
            }
        } else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(notes, i, i - 1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
        new Handler(Looper.getMainLooper()).post(() -> notes.get(toPosition).updatePosition(toPosition));
    }


    static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public TextView text;
        public TextView createdAt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = itemView.findViewById(R.id.title);
            this.text = itemView.findViewById(R.id.text);
            this.createdAt = itemView.findViewById(R.id.created_at);
        }
    }
}
