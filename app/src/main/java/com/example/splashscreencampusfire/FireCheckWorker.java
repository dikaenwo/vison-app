package com.example.splashscreencampusfire;

import androidx.work.ListenableWorker;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

public class FireCheckWorker extends Worker {
    private static final String CHANNEL_ID = "fire_alert_channel";
    private String STATUS_URL;

    public FireCheckWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        createNotificationChannel();

        // Ambil IP dari SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String ipAddress = prefs.getString("ip_address", "192.168.1.1"); // Default jika belum di-set
        STATUS_URL = "http://" + ipAddress + ":5000/status"; // Dinamis menggunakan IP
    }

    @NonNull
    @Override
    public Result doWork() {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(STATUS_URL).build();
        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                JSONObject jsonObject = new JSONObject(response.body().string());
                boolean fireDetected = jsonObject.getBoolean("fire_detected");

                if (fireDetected) {
                    showNotification();
                }
            }
        } catch (Exception e) {
            Log.e("FireCheckWorker", "Error checking fire status", e);
        }
        return Result.success();
    }

    private void showNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_campus_fireguard)
                .setContentTitle("🔥 Peringatan!")
                .setContentText("Api terdeteksi! Periksa kamera sekarang!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Fire Alert", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
