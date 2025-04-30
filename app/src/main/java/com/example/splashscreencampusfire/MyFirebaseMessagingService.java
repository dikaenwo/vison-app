package com.example.splashscreencampusfire;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    private static final String CHANNEL_ID = "FireAlertChannel";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Pesan masuk: " + remoteMessage.getData().toString());

        String title = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Peringatan Kebakaran!";
        String message = remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "Api terdeteksi! Segera cek lokasi.";

        sendNotification(title, message);
    }

    private void sendNotification(String title, String message) {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Fire Alerts", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.fire_alert);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_campus_fireguard)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(soundUri);

        notificationManager.notify(1, builder.build());
    }
}
