package com.notelint.android.receivers;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.notelint.android.Application;
import com.notelint.android.MainActivity;
import com.notelint.android.R;
import com.notelint.android.helpers.DateHelper;

public class Alarm extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        final String CHANNEL_ID = "channel_notify";
        int id = arg1.getExtras().getInt("notifyId", 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(Application.getInstance(), "01")
                .setContentTitle(arg1.getExtras().getString("title", "Уведомление"))
                .setSmallIcon(R.drawable.ic_notifications_active_24px)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(arg1.getExtras().getString("text", DateHelper.getFormattedDate(System.currentTimeMillis()))))
                .setContentText(arg1.getExtras().getString("text", DateHelper.getFormattedDate(System.currentTimeMillis())))
                .setAutoCancel(true)
                .setContentIntent(contentIntent(id))
                .setChannelId(CHANNEL_ID);

        NotificationManager notificationManager = (NotificationManager) Application.getInstance().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {       // For Oreo and greater than it, we required Notification Channel.
            CharSequence name = "My New Channel";                   // The user-visible name of the channel.
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance); //Create Notification Channel
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(id, notificationBuilder.build());
    }

    private PendingIntent contentIntent(int id) {
        Intent intent = new Intent(Application.getInstance(), MainActivity.class);
        intent.putExtra("notifyId", id);
        return PendingIntent.getActivity(Application.getInstance(), id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancelAlarm(int requestCode) {
        Intent intent = new Intent(Application.getInstance(), Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(Application.getInstance(), requestCode, intent, 0);
        AlarmManager alarmManager = (AlarmManager) Application.getInstance().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
