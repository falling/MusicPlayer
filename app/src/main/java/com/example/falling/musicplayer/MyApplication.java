package com.example.falling.musicplayer;

import android.app.Activity;
import android.app.Application;

import java.util.List;

/**
 * Created by falling on 16/3/19.
 */
public class MyApplication extends Application {
    public List<SongBean> mSongList;//当前播放列表
    public int songItemPos;//当前播放音乐在列表中的位置
    public boolean isPlaying;
    public boolean isPause;
    public Activity mActivity;


    @Override
    public void onCreate() {
        super.onCreate();
        isPlaying = false;
        isPause  = false;
        mSongList = AudioUtils.getAllSongs(this);
        songItemPos = SharedPreferencesUtil.getId(this);
    }
}
