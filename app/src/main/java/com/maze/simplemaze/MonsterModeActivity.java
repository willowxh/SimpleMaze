package com.maze.simplemaze;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.List;

public class MonsterModeActivity extends AppCompatActivity {
    private Context mContext;
    private GridView monster_mode;
    private BaseAdapter mAdapter = null;
    private List<Level> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_monster_mode);
        monster_mode = findViewById(R.id.monster_mode);
        mContext = getApplicationContext();
        mData.add(new Level(1,true));
        mData.add(new Level(2,true));
        mData.add(new Level(3,true));
        mData.add(new Level(4,true));
        mData.add(new Level(5,false));
        mData.add(new Level(6,false));
        mData.add(new Level(7,false));
        mData.add(new Level(8,false));
        mData.add(new Level(9,false));
        mData.add(new Level(10,false));
        mData.add(new Level(11,false));
        mData.add(new Level(12,false));
        mData.add(new Level(13,false));
        mData.add(new Level(14,false));
        mData.add(new Level(15,false));
        mData.add(new Level(16,false));


        mAdapter = new LevelAdapter(mData,mContext);
        monster_mode.setAdapter(mAdapter);

        monster_mode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(mContext, "你点击了~" + position + "~项", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(mContext, GameActivity.class);
                intent.putExtra("level",position+1);
                intent.putExtra("mode",1);
                startActivity(intent);
            }
        });
    }
}
