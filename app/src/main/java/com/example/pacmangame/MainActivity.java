package com.example.pacmangame;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout gameLayout = findViewById(R.id.gameContainer);
        gameView = new GameView(this, null); // Pass null for AttributeSet
        gameLayout.addView(gameView);
    }
}
