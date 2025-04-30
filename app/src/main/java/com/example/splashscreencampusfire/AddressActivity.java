package com.example.splashscreencampusfire;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class AddressActivity extends AppCompatActivity {

    private TextInputEditText ipEditText;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address);

        ipEditText = findViewById(R.id.ipEditText);
        saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(v -> {
            String ip = ipEditText.getText().toString().trim();
            if (!ip.isEmpty()) {
                SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                prefs.edit().putString("ip_address", ip).apply();

                Toast.makeText(this, "IP disimpan!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AddressActivity.this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Mohon isi IP terlebih dahulu", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
