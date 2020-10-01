package com.notelint.android.database.models;

import android.util.Log;
import android.widget.Toast;

import com.notelint.android.Application;

import java.io.Serializable;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.Sort;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Note extends RealmObject implements Serializable {
    @PrimaryKey
    private long id;

    public long getId() {
        return id;
    }

    private String title;
    private String text;
    private boolean visible;

    @Index
    private int position;

    private long createdAt;
    private long updatedAt;


    private long deletedAt;

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public long getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(long deletedAt) {
        this.deletedAt = deletedAt;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    // todo доделать. Научиться правильно считать позицию
    public void updatePosition(int position) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // Обновляем позицию той, выше или ниже которой переместили заметку
        Note movingNote = realm.where(Note.class).equalTo("position", position).findFirst();
        if (movingNote != null) {
            Log.e("TAG", String.valueOf(this.getPosition() > position ? position + 1 : position - 1));
            movingNote.setPosition(this.getPosition() > position ? position + 1 : position - 1);
        }

        // Обнволяем позицию перемещенной заметки
        Note movedNote = realm.where(Note.class).equalTo("id", this.getId()).findFirst();
        if (movedNote != null) {
            Toast.makeText(Application.getInstance(), "Updated", Toast.LENGTH_SHORT).show();
            movedNote.setPosition(position);
        }
        realm.commitTransaction();
        realm.close();
    }

    public void softDelete() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        Note deletedNote = realm.where(Note.class).equalTo("id", this.getId()).findFirstAsync();
        if (deletedNote != null) {
            deletedNote.setDeletedAt(System.currentTimeMillis());
            realm.commitTransaction();
            realm.close();
            return;
        }
        Toast.makeText(Application.getInstance(), "Not found", Toast.LENGTH_SHORT).show();
    }

    public void hardDelete() {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Note deletedNote = realm.where(Note.class).equalTo("id", this.getId()).findFirstAsync();
        if (deletedNote != null) {
            deletedNote.deleteFromRealm();
            realm.commitTransaction();
        }
        realm.close();
    }

    /**
     * Return id crated note
     * @param title - title
     * @param text - text note
     * @param visible - visible
     * @return long
     */
    public static long create(String title, String text, boolean visible) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Note lastNote = realm.where(Note.class).sort("id", Sort.DESCENDING).findFirst();
        int lastPosition = lastNote != null ? lastNote.getPosition() + 1 : 1;

        long id = System.currentTimeMillis();
        Note note = realm.createObject(Note.class, id);
        note.setTitle(title);
        note.setText(text);
        note.setCreatedAt(System.currentTimeMillis());
        note.setUpdatedAt(System.currentTimeMillis());
        note.setVisible(visible);
        note.setPosition(lastPosition);

        realm.commitTransaction();
        realm.close();

        return id;
    }

    public static void update(long id, String title, String text, boolean visible) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Note note = realm.where(Note.class).equalTo("id", id).findFirstAsync();
        if (note == null) {
            Toast.makeText(Application.getInstance(), "Not found", Toast.LENGTH_SHORT).show();
            return;
        }

        note.setTitle(title);
        note.setText(text);
        note.setUpdatedAt(System.currentTimeMillis());
        note.setVisible(visible);

        realm.commitTransaction();
        realm.close();
    }

    public static Note getLastNote() {
        Realm realm = Realm.getDefaultInstance();
        Note lastNote = realm.where(Note.class).sort("id", Sort.DESCENDING).findFirst();
        realm.close();
        return lastNote;
    }
}
