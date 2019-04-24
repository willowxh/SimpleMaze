package com.maze.simplemaze;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.LinkedList;

/**
 * @author: chasen
 * @date: 2019/4/24
 */
public class RankAdapter extends BaseAdapter {
    private LinkedList<Rank> mData;
    private Context mContext;

    public RankAdapter(LinkedList<Rank> mData, Context mContext) {
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
        convertView = LayoutInflater.from(mContext).inflate(R.layout.rank_item,null);
        ImageView uid = convertView.findViewById(R.id.uid);
        TextView uname = convertView.findViewById(R.id.uname);
        TextView ustars = convertView.findViewById(R.id.ustars);
        TextView udate = convertView.findViewById(R.id.udate);
        if(mData.get(position).getId().equals("1")){
            uid.setImageResource(R.drawable.place1);
        }
        else if(mData.get(position).getId().equals("2")){
            uid.setImageResource(R.drawable.place2);
        }
        else if(mData.get(position).getId().equals("3")){
            uid.setImageResource(R.drawable.place3);
        }
        else{
            uid.setImageResource(R.drawable.place_other);
        }
        uname.setText(mData.get(position).getName());
        ustars.setText(mData.get(position).getStars());
        udate.setText(mData.get(position).getDate());
        return convertView;
    }
}
