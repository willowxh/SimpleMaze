package com.maze.simplemaze;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

public class RankActivity extends AppCompatActivity {
    private List<Rank> mData = null;
    private RankAdapter mAdapter = null;
    private ListView lv;
    private DataBaseHelper myDBHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_rank);
        lv = findViewById(R.id.list_view);
        myDBHelper = new DataBaseHelper(this, "rank", null, 1);
        db = myDBHelper.getWritableDatabase();
        Cursor cursor = db.query("rank", null, null, null, null, null, "stars");
        mData = new LinkedList<>();
        int i = 1;
        if (cursor.moveToFirst()) {
            do {
                String id = String.valueOf(i++);
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String stars = cursor.getString(2);
                String date = cursor.getString(3);
                mData.add(new Rank(id, name, stars, date));
            } while (cursor.moveToNext());
        }
        mData.add(new Rank("2", "test1", "540", "2019/4/24"));
        mData.add(new Rank("3", "test2", "55", "2019/4/24"));
        mData.add(new Rank("4", "test3", "54", "2019/4/24"));
        mData.add(new Rank("5", "test4", "53", "2019/4/24"));
        mData.add(new Rank("6", "test5", "51", "2019/4/24"));
        mData.add(new Rank("7", "test6", "42", "2019/4/24"));
        cursor.close();
        mAdapter = new RankAdapter((LinkedList<Rank>) mData, this);
        View headView = getLayoutInflater().inflate(R.layout.table_header, null, false);
        lv.addHeaderView(headView);
        lv.setAdapter(mAdapter);
    }
}
