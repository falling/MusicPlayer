package com.example.falling.musicplayer.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 保存播放哪首歌曲的工具类
 * Created by falling on 16/3/18.
 */
public class SharedPreferencesUtil {

    public static final String ID = "id";

    public static void save(Context context,int id){
        SharedPreferences sharedPreferences = context.getSharedPreferences("music_progress", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(ID, id);
        editor.apply();
    }

    public static int getId(Context context){
        SharedPreferences sharedPreferences = context.getSharedPreferences("music_progress", 0);
        return sharedPreferences.getInt(ID,0);
    }

}
