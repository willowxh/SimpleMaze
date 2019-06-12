package com.maze.simplemaze;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class GameActivity extends AppCompatActivity {
    GameView gameView;
    ImageView imageView;
    ImageView speedup;

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
        gameView = findViewById(R.id.game_view);
        imageView = findViewById(R.id.replay_button);
        speedup = findViewById(R.id.speedup_button);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.readMaze("maze"+gameView.level+".json");
                gameView.numOfMove = 0;
                gameView.handler.sendEmptyMessage(77);
            }
        });
        speedup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gameView.speedNum += 1;
                switch (gameView.speedNum % 3){
                    case 0:
                        gameView.speed = 70;
                        break;
                    case 1:
                        gameView.speed = 50;
                        break;
                    case 2:
                        gameView.speed = 20;
                }
            }
        });
        LinearLayout linearLayout = findViewById(R.id.main_bg);
        if(mode == 0 || mode == 2){
            linearLayout.setBackgroundResource(R.drawable.menu);
            imageView.setImageResource(R.drawable.yellow_replay);
            speedup.setImageResource(R.drawable.right_arrow);

        }
        gameView.mode = mode;
        gameView.level = level;
        gameView.readMaze("maze"+level+".json");
    }

    @Override
    protected void onDestroy() {
        if(gameView.moveThread != null){
            gameView.moveThread = null;
        }
        if(gameView.monsterThread != null){
            gameView.monsterThread = null;
        }
        if(gameView.handler != null){
            gameView.handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();
    }
}
