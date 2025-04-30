package com.example.splashscreencampusfire;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.messaging.FirebaseMessaging;

import android.Manifest;
import android.content.pm.PackageManager;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private Button pemadamKebakaranButton;
    private final Handler handler = new Handler();
    private String ipAddress; // Tambahkan ini
    private String imageUrl;
    private String webUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ambil IP dari SharedPreferences
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        ipAddress = prefs.getString("ip_address", "192.168.1.1"); // Default jika belum di-set
        imageUrl = "http://" + ipAddress + ":5000/static/fire_detected.jpg";
        webUrl = "http://" + ipAddress + ":5000";

        // Inisialisasi komponen UI
        imageView = findViewById(R.id.imageView);
        pemadamKebakaranButton = findViewById(R.id.pemadamKebakaranButton);

        // Ambil FCM Token (opsional, jika masih dibutuhkan)
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    // Ambil token FCM
                    String token = task.getResult();
                    Log.d("FCM Token", token);
                    // Simpan token di SharedPreferences atau server
                });

        // Auto-refresh image setiap 2 detik
        handler.postDelayed(updateImageRunnable, 2000);

        // Load WebView
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webView.loadUrl(webUrl); // gunakan URL dari prefs

        // Menangani klik pada tombol "Telpon Pemadam Kebakaran"
        pemadamKebakaranButton.setOnClickListener(v -> {
            // Cek izin untuk membuka dialer
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                // Buka dialer dengan nomor pemadam kebakaran
                String phoneNumber = "0411854444"; // Nomor pemadam kebakaran
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent); // Membuka aplikasi dialer dengan nomor yang sudah terisi
            } else {
                // Jika izin tidak diberikan, minta izin terlebih dahulu
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, 1);
            }
        });
    }

    private final Runnable updateImageRunnable = new Runnable() {
        @Override
        public void run() {
            loadLatestImage();
            handler.postDelayed(this, 2000);
        }
    };

    private void loadLatestImage() {
        Glide.with(this)
                .load(imageUrl + "?timestamp=" + System.currentTimeMillis())
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateImageRunnable);
    }

    // Menghandle permintaan izin panggilan telepon
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, buka dialer dengan nomor yang sudah terisi
                String phoneNumber = "0411854444";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                startActivity(intent);
            } else {
                // Izin ditolak, beri notifikasi kepada pengguna
                Toast.makeText(this, "Izin untuk melakukan panggilan telepon ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
