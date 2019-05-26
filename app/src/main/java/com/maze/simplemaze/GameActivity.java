package com.maze.simplemaze;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        Intent intent = getIntent();
        int level = intent.getIntExtra("level",1);
        int mode = intent.getIntExtra("mode",0);
        GameView gameView = findViewById(R.id.game_view);
        gameView.mode = mode;
        gameView.level = level;
        gameView.readMaze("maze"+level+".json");
    }
}
