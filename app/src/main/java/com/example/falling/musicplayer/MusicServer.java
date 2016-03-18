package com.example.falling.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;

import java.io.IOException;


public class MusicServer extends Service {
    public static final int START = 0;
    public static final int PAUSE = 1;
    public static final int CONTINUE= 2;
    public static final String MUSIC_URL = "musicUrl";
    private static String musicUrl;
    private static MediaPlayer mMediaPlayer = new MediaPlayer();

    Messenger mMessenger = new Messenger(new MusicPlayerHandler());



    static class MusicPlayerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 处理消息
            switch (msg.what) {
                case START:
                    musicUrl = msg.getData().getString(MUSIC_URL);
                    try {
                        if (mMediaPlayer.isPlaying()) {
                            mMediaPlayer.stop();
                        }
                        mMediaPlayer = new MediaPlayer();
                        mMediaPlayer.setDataSource(musicUrl);
                        mMediaPlayer.prepare();
                        mMediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case PAUSE:
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    }else{
                        mMediaPlayer.start();
                    }
                    break;
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
