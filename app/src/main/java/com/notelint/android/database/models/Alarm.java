package com.notelint.android.database.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.notelint.android.Application;
import com.notelint.android.utils.PrefUtil;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmResults;
import io.realm.Sort;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Alarm extends RealmObject {
    @PrimaryKey
    public long id;

    public long getNoteId() {
        return noteId;
    }

    public void setNoteId(long noteId) {
        this.noteId = noteId;
    }

    @Index
    public long noteId;

    public String title;

    @Index
    public int notificationId;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long timestamp; // время, на которое установлен будильник

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
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

    public String text;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public static final void create(long noteId, String title, String text, int notificationId, long timestamp) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Alarm alarm = realm.createObject(Alarm.class, System.currentTimeMillis());
        alarm.setNoteId(noteId);
        alarm.setTitle(title);
        alarm.setText(text);
        alarm.setNotificationId(notificationId);
        alarm.setTimestamp(timestamp);

        realm.commitTransaction();
        realm.close();
    }

    public static final void delete(int noteId, int notificationId) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Alarm alarm = realm.where(Alarm.class)
                .equalTo("notificationId", notificationId)
                .or()
                .equalTo("noteId", noteId)
                .findFirstAsync();
        if (alarm != null) {
            com.notelint.android.receivers.Alarm.cancelAlarm(notificationId);
            alarm.deleteFromRealm();

            Log.e("TAG", "Alarm is canceled and deleted");

            realm.commitTransaction();
        }

        realm.close();
    }

    // Ставим уведомления заново (после ребута или установки новой версии)
    public static final void reInitAllAlarms() {
        Realm realm = Realm.getDefaultInstance();

        RealmResults<Alarm> alarms = realm.where(Alarm.class).findAllAsync();
        Log.e("TAG", "Count active alarms - " + alarms.size());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            alarms.forEach(el -> {
                Date current = new Date();
                Date next = new Date(el.getTimestamp());

                // Устанавливаем только на будущее и исключаем просроченные
                if (next.after(current)) {
                    AlarmManager am = (AlarmManager) Application.getInstance().getSystemService(Context.ALARM_SERVICE);

                    // Prepare data
                    Intent i = new Intent(Application.getInstance(), com.notelint.android.receivers.Alarm.class);
                    i.putExtra("notifyId", el.getNotificationId());
                    i.putExtra("title", el.getTitle());
                    i.putExtra("text", el.getText());
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(Application.getInstance(), el.getNotificationId(), i, 0);

                    // Set alarm
                    am.setRepeating(AlarmManager.RTC, el.getTimestamp(), PrefUtil.getNotifyTimeRepeater() * 60 * 1000, pendingIntent);
                } else { // удаляем просроченные будильники
                    com.notelint.android.receivers.Alarm.cancelAlarm(el.getNotificationId());
                    realm.beginTransaction();
                    el.deleteFromRealm();
                    realm.commitTransaction();
                }
            });
        }
        realm.close();
    }
}
