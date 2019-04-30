package com.maze.simplemaze;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.Window;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    
    ImageView setting;
    ImageView rank;
    ImageView sound;
    ImageView play;
    MediaPlayer mp = null;
    Boolean isPlayMusic = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        setting = (ImageView)findViewById(R.id.setting);
        rank = (ImageView)findViewById(R.id.rank);
        //sound = (ImageView)findViewById(R.id.sound);
        play = (ImageView)findViewById(R.id.play);
        play.setOnClickListener(this);
        setting.setOnClickListener(this);
        rank.setOnClickListener(this);
        //sound.setOnClickListener(this);

        playMusic();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.play:
                //Toast.makeText(this, "tap paly", Toast.LENGTH_SHORT).show();
                //Intent intent_game = new Intent(MainActivity.this,GameActivity.class);
                //startActivity(intent_game);
                Intent intent_menu = new Intent(MainActivity.this,MenuActivity.class);
                startActivity(intent_menu);
                break;
            case R.id.setting:
                Toast.makeText(this, "tap setting", Toast.LENGTH_SHORT).show();
                break;
            case R.id.rank:
                Intent intent_rank = new Intent(MainActivity.this,RankActivity.class);
                startActivity(intent_rank);
                //Toast.makeText(this, "tap rank", Toast.LENGTH_SHORT).show();
                break;
            /*case R.id.sound:
//                Toast.makeText(this, "tap sound", Toast.LENGTH_SHORT).show();
                if (isPlayMusic){
                    if(mp != null)
                        mp.pause();
                    sound.setImageResource(R.drawable.sound2);
                    isPlayMusic = false;
                }else{
                    if(mp != null)
                        mp.start();
                    sound.setImageResource(R.drawable.sound3);
                    isPlayMusic = true;
                }
                break;*/
        }
    }

    @SuppressLint("NewApi")
    public void playMusic(){
        AssetManager am = getAssets();
        AssetFileDescriptor afd = null;
        if(mp == null) {
            mp = new MediaPlayer();
        }
        try {
            afd = am.openFd("bg_hdl.mp3");
            mp.setDataSource(afd);
            mp.prepare();
            mp.setLooping(true);
            mp.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        if (mp != null && mp.isPlaying()) {
                        mp.stop();
                         mp.release();
                        mp = null;
                  }
        super.onDestroy();
    }
}
