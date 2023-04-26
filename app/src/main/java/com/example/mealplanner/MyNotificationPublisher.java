package com.example.mealplanner;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

// This class is based on code from the following KDTechs YouTube video: https://www.youtube.com/watch?v=Ijv0vcxNk78
// We were not able to get the code from the in-class example "31 - Notify" working to schedule notifications for a specific time, but this code from KDTechs works
public class MyNotificationPublisher extends BroadcastReceiver {

    public static String NOTIFICATIONID = "notification-id";
    public static String NOTIFICATION = "notification";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel notificationChannel = new NotificationChannel(MainActivity.NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(notificationChannel);
        int id = intent.getIntExtra(NOTIFICATIONID, 0);
        notificationManager.notify(id, notification);
    }
}