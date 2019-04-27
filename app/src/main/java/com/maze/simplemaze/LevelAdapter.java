package com.maze.simplemaze;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * @author: chasen
 * @date: 2019/4/25
 */
public class LevelAdapter extends BaseAdapter {
    private List<Level> mData;
    private Context mContext;
    private boolean flag =true;

    public LevelAdapter(List<Level> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.level_item,null);
        }
        TextView textView= convertView.findViewById(R.id.level_id);
        ImageView imageView = convertView.findViewById(R.id.level_image);
        if(mData.get(position).isPassed()){
            textView.setText(String.valueOf(mData.get(position).getId()));
            imageView.setImageResource(R.drawable.unlock);
        }else if(flag){
            textView.setText(String.valueOf(mData.get(position).getId()));
            textView.setTextColor(Color.BLACK);
            imageView.setImageResource(R.drawable.unlock1);
            flag = false;
        }else {
            imageView.setImageResource(R.drawable.lock);
        }
        return convertView;
    }
}
