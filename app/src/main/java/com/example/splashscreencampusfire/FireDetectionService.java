package com.example.splashscreencampusfire;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FireDetectionService extends Service {

    private static final String TAG = "FireDetectionService";
    private static final String CHANNEL_ID = "FireDetectionChannel";
    private String API_URL; // IP akan diset dinamis
    private static final int NOTIFICATION_ID = 1;
    private static final int CHECK_INTERVAL = 5000; // 5 detik

    private final Handler handler = new Handler();
    private boolean isFireDetected = false;
    private boolean isFirstCheck = true;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service created");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification("Memulai monitoring kebakaran..."));

        // Ambil IP dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String ipAddress = prefs.getString("ip_address", "192.168.1.1"); // Default jika belum diset
        API_URL = "http://" + ipAddress + ":5000/status"; // Dinamis menggunakan IP

        // Mulai cek status API secara berkala
        handler.post(checkFireStatus);
    }

    private final Runnable checkFireStatus = new Runnable() {
        @Override
        public void run() {
            try {
                Log.d(TAG, "Checking fire status...");
                boolean fireDetected = checkFireAPI();
                handleFireStatus(fireDetected);
            } catch (Exception e) {
                Log.e(TAG, "Error in fire detection check: " + e.getMessage());
                updateNotification("⚠️ Gagal memeriksa status");
            } finally {
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        }
    };

    private boolean checkFireAPI() {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(8000);
            connection.setReadTimeout(8000);

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                Log.e(TAG, "HTTP error code: " + responseCode);
                return false;
            }

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            Log.d(TAG, "API Response: " + response.toString());
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.getBoolean("fire_detected");

        } catch (Exception e) {
            Log.e(TAG, "Error reading API response: " + e.getMessage());
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader: " + e.getMessage());
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void handleFireStatus(boolean fireDetected) {
        Log.d(TAG, "New fire status: " + fireDetected + ", Previous: " + isFireDetected);

        // Selalu update pada pertama kali atau ketika status berubah
        if (isFirstCheck || fireDetected != isFireDetected) {
            isFireDetected = fireDetected;
            isFirstCheck = false;

            String message = fireDetected ? "🔥 Api Terdeteksi!" : "✅ Aman, tidak ada api.";
            updateNotification(message);

            // Kirim broadcast jika perlu memberi tahu Activity
            sendStatusBroadcast(fireDetected);
        }
    }

    private void sendStatusBroadcast(boolean fireDetected) {
        Intent intent = new Intent("FIRE_DETECTION_STATUS_UPDATE");
        intent.putExtra("fire_detected", fireDetected);
        sendBroadcast(intent);
    }

    private void updateNotification(String status) {
        Log.d(TAG, "Updating notification: " + status);
        Notification notification = createNotification(status);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID, notification);
        }
    }

    private Notification createNotification(String status) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Fire Detection System")
                .setContentText(status)
                .setSmallIcon(R.drawable.logo_campus_fireguard)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .setOnlyAlertOnce(true) // Hanya bunyi sekali saat perubahan
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Fire Detection",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifikasi status deteksi api");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed");
        handler.removeCallbacks(checkFireStatus);
        updateNotification("Monitoring dihentikan");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
