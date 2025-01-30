package com.example.pacmangame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button btnStartGame = findViewById(R.id.btnStartGame);
        Button btnExit = findViewById(R.id.btnExit);

        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            startActivity(intent);
        });

        btnExit.setOnClickListener(v -> {
            finishAffinity(); // Закрывает всё приложение
        });
    }
}
