package com.example.pacmangame;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        int score = getIntent().getIntExtra("SCORE", 0);

        TextView scoreTextView = findViewById(R.id.scoreTextView);
        scoreTextView.setText("Ваш счёт: " + score);

        // Таймер на 3 секунды перед возвратом в меню
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(ScoreActivity.this, MenuActivity.class);
            startActivity(intent);
            finish(); // Закрываем текущую активность, чтобы нельзя было вернуться назад
        }, 3000); // 3000 миллисекунд = 3 секунды
    }
}
