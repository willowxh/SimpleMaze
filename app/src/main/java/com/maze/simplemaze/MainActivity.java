package com.maze.simplemaze;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    
    ImageView setting;
    ImageView rank;
    ImageView sound;
    ImageView play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        setting = (ImageView)findViewById(R.id.setting);
        rank = (ImageView)findViewById(R.id.rank);
        sound = (ImageView)findViewById(R.id.sound);
        play = (ImageView)findViewById(R.id.play);
        play.setOnClickListener(this);
        setting.setOnClickListener(this);
        rank.setOnClickListener(this);
        sound.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.play:
                Toast.makeText(this, "tap paly", Toast.LENGTH_SHORT).show();
                break;
            case R.id.setting:
                Toast.makeText(this, "tap setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rank:
                Toast.makeText(this, "tap rank", Toast.LENGTH_SHORT).show();
                break;
            case R.id.sound:
                Toast.makeText(this, "tap sound", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
