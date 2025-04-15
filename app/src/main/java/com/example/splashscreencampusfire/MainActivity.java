package com.example.splashscreencampusfire;

import androidx.appcompat.app.AppCompatActivity;
import android.content.ClipboardManager;
import android.content.ClipData;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private final String imageUrl = "http://192.168.1.32:5000/static/fire_detected.jpg";
    private TextView tokenTextView;
    private Button copyButton;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load gambar terbaru dari Raspberry Pi
        imageView = findViewById(R.id.imageView);
        loadLatestImage();

        tokenTextView = findViewById(R.id.tokenTextView);
        copyButton = findViewById(R.id.copyButton);

        // Ambil FCM Token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    // Dapatkan token
                    String token = task.getResult();
                    Log.d("FCM Token", token);

                    // Tampilkan di TextView
                    tokenTextView.setText(token);
                });

        // Tombol Copy Token
        copyButton.setOnClickListener(v -> {
            String token = tokenTextView.getText().toString();
            if (!token.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("FCM Token", token);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Token disalin ke clipboard!", Toast.LENGTH_SHORT).show();
            }
        });

        // Auto-refresh gambar tiap 2 detik
        handler.postDelayed(updateImageRunnable, 2000);

        // Load halaman web Flask ke WebView
        WebView webView = findViewById(R.id.webView);
        webView.setWebViewClient(new WebViewClient());

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webView.loadUrl("http://192.168.1.32:5000"); // IP Raspberry Pi
    }

    private final Runnable updateImageRunnable = new Runnable() {
        @Override
        public void run() {
            loadLatestImage();
            handler.postDelayed(this, 2000); // Ulang tiap 2 detik
        }
    };

    private void loadLatestImage() {
        Glide.with(this)
                .load(imageUrl + "?timestamp=" + System.currentTimeMillis()) // Tambah timestamp agar URL unik tiap refresh
                .skipMemoryCache(true) // Hindari cache di RAM
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Hindari cache di storage
                .into(imageView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateImageRunnable); // Hentikan auto-refresh saat aplikasi ditutup
    }
}
