package com.notelint.android.database.models;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.notelint.android.Application;
import com.notelint.android.utils.PrefUtil;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Note extends RealmObject implements Parcelable {
    @PrimaryKey
    private long id;

    public Note() {

    }

    public Note(Parcel in) {
        id = in.readLong();
        title = in.readString();
        text = in.readString();
        visible = in.readByte() != 0;
        position = in.readInt();
        createdAt = in.readLong();
        updatedAt = in.readLong();
        deletedAt = in.readLong();
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel in) {
            return new Note(in);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isDeleted() {
        return this.getDeletedAt() > 0;
    }

    // todo доделать. Научиться правильно считать позицию
    public void updatePosition(int position) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        // Обновляем позицию той, выше или ниже которой переместили заметку
        Note movingNote = realm.where(Note.class).equalTo("position", position).findFirst();
        if (movingNote != null) {
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
     *
     * @param title   - title
     * @param text    - text note
     * @param visible - visible
     * @return long
     */
    public static long create(String title, String text, boolean visible) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Number currentPosition = realm.where(Note.class).max("position");
        int nextPosition = currentPosition == null ? 1 : currentPosition.intValue() + 1;

        long id = System.currentTimeMillis();
        Note note = realm.createObject(Note.class, id);
        note.setTitle(title);
        note.setText(text);
        note.setCreatedAt(System.currentTimeMillis());
        note.setUpdatedAt(System.currentTimeMillis());
        note.setVisible(visible);
        note.setPosition(nextPosition);

        realm.commitTransaction();
        realm.close();

        return id;
    }

    public static void update(long id, String title, String text, boolean visible) {
        Realm realm = Realm.getDefaultInstance();

        Note note = realm.where(Note.class).equalTo("id", id).findFirstAsync();
        if (note == null) {
            Toast.makeText(Application.getInstance(), "Not found", Toast.LENGTH_SHORT).show();
            realm.close();
            return;
        }

        realm.beginTransaction();
        note.setTitle(title);
        note.setText(text);
        note.setUpdatedAt(System.currentTimeMillis());
        note.setVisible(visible);
        if (note.getDeletedAt() > 0 && PrefUtil.isReturnFromArchive()) {
            note.setDeletedAt(0);
        }

        realm.commitTransaction();
        realm.close();
    }

    public static final void clearArchive() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Note> results = realm.where(Note.class).notEqualTo("deletedAt", 0).findAllAsync();
        results.addChangeListener(realmResults -> {
            realm.beginTransaction();
            results.deleteAllFromRealm();
            realm.commitTransaction();
            realm.close();
            Toast.makeText(Application.getInstance(), "Архив очищен", Toast.LENGTH_SHORT).show();
        });
    }

    public static final void clearAll() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Note> results = realm.where(Note.class).findAllAsync();
        results.addChangeListener(realmResult -> {
            realm.beginTransaction();
            results.deleteAllFromRealm();
            realm.commitTransaction();
            realm.close();
            Toast.makeText(Application.getInstance(), "Все заметки удалены", Toast.LENGTH_SHORT).show();
        });
    }

    public static Note getLastNote() {
        Realm realm = Realm.getDefaultInstance();
        Note lastNote = realm.where(Note.class).sort("id", Sort.DESCENDING).findFirst();
        realm.close();
        return lastNote;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.getId());
        dest.writeString(this.getTitle());
        dest.writeString(this.getText());
        dest.writeLong(this.getDeletedAt());
        dest.writeLong(this.getCreatedAt());
        dest.writeLong(this.getUpdatedAt());
    }
}
