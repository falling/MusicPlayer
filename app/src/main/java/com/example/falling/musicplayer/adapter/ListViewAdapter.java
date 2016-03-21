package com.example.falling.musicplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.falling.musicplayer.R;
import com.example.falling.musicplayer.bean.SongBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by falling on 2016/1/23.
 */
public class ListViewAdapter extends BaseAdapter{
    private Context mContext;
    private List<SongBean> mList = new ArrayList<>();
    private LayoutInflater mLayoutInflater;

    public ListViewAdapter(Context context, List<SongBean> mlist) {
        this.mContext=context;
        mLayoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.mList = mlist;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int i) {
        return mList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder;
        if(view == null){
            view = mLayoutInflater.inflate(R.layout.list_item,null);
            viewHolder = new ViewHolder();
            viewHolder.mMusicName = (TextView) view.findViewById(R.id.musicName);
            viewHolder.mMusicAlbum = (TextView) view.findViewById(R.id.musicAlbum);
            viewHolder.mMusicSinger = (TextView) view.findViewById(R.id.musicSinger);
            view.setTag(viewHolder);

        }else{
            viewHolder = (ViewHolder) view.getTag();
        }
        String music_name = i+1 +" - "+mList.get(i).getTitle();
        viewHolder.mMusicName.setText(music_name);
        viewHolder.mMusicAlbum.setText(mList.get(i).getAlbum());
        viewHolder.mMusicSinger.setText(mList.get(i).getSinger());

        return view;
    }

    class ViewHolder{
        TextView mMusicName;
        TextView mMusicAlbum;
        TextView mMusicSinger;
    }
}
