package com.example.falling.musicplayer;

import android.app.Application;

import java.util.List;

/**
 * Created by falling on 16/3/19.
 */
public class MyApplication extends Application {
    public List<SongBean> mSongList;//当前播放列表
    public int songItemPos;//当前播放音乐在列表中的位置
    public boolean isPlaying;//是否播放的状态
    public boolean isPause;//是否暂停


    @Override
    public void onCreate() {
        super.onCreate();
        isPlaying = false;
        isPause  = false;
        songItemPos = SharedPreferencesUtil.getId(this);
    }

    //上一曲
    public void lastSong() {
        if (songItemPos > 0) {
            songItemPos--;
        } else {
            songItemPos = mSongList.size() - 1;
        }
    }

    //下一曲
    public void  nextSong(){
        if (songItemPos < mSongList.size() - 1) {
            songItemPos++;
        } else {
            songItemPos = 0;
        }
    }
}
