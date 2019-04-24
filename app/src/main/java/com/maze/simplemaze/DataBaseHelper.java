package com.maze.simplemaze;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author: chasen
 * @date: 2019/4/24
 */
public class DataBaseHelper extends SQLiteOpenHelper {
    public DataBaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table rank (id integer primary key,name varchar(50),stars varchar(50),date varchar(50))");
        ContentValues cv = new ContentValues();
        cv.put("name","chasen");
        cv.put("stars","1000");
        cv.put("date","2019/4/24");
        db.insert("rank",null,cv);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
