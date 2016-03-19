package com.example.falling.musicplayer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;
import java.util.ArrayList;


public class MusicServer extends Service {
    public static final int START = 0;
    public static final int PAUSE = 1;
    public static final String MUSIC_URL = "musicUrl";
    public static final String IS_PLAYING = "isPlaying";
    public static final String ACTION_BUTTON = "action_button";
    public static final String INTENT_BUTTON_ID = "intent_button_id";
    public static final String BUTTON_LAST_ID = "1";
    public static final String BUTTON_PlAY_ID = "2";
    public static final String BUTTON_NEXT_ID = "3";
    private static String musicUrl;
    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static int id = 1;

    private ArrayList<SongBean> mSongList;

    private Messenger mMessenger = new Messenger(new MusicPlayerHandler());
    private SongBean mSongBean;
    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteView;
    private Intent mIntent;
    private MyApplication mApplication;


    /**
     * 改变notification的显示
     */
    private void changeNotification() {
        if (mSongBean != null) {
            mRemoteView.setTextViewText(R.id.music_info, mSongBean.getTitle());
            if (mApplication.isPlaying && mApplication.isPause) {
                mRemoteView.setImageViewResource(R.id.StartOrStop, R.mipmap.button_play);
            } else {
                mRemoteView.setImageViewResource(R.id.StartOrStop, R.mipmap.button_stop);
            }

            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon)
                    .setContent(mRemoteView);

            mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, mIntent, 0));

            mNotifyMgr.notify(id, mBuilder.build());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = (MyApplication) getApplication();
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.buttom_control);
        mIntent = new Intent(this, MainActivity.class);
        setListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ctrl_code = intent.getAction();
        String clicked_button = intent.getStringExtra(MusicServer.INTENT_BUTTON_ID);

        Log.i("server", "startCommand");
        if (TextUtils.equals(ctrl_code, MusicServer.ACTION_BUTTON)) {

            if (TextUtils.equals(clicked_button, BUTTON_LAST_ID)) {
                Log.i("server", "last");
                //上一曲
                if (mApplication.songItemPos > 0) {
                    mApplication.songItemPos--;
                } else {
                    mApplication.songItemPos = mApplication.mSongList.size() - 1;
                }
                playMusic();

            } else if (TextUtils.equals(clicked_button, BUTTON_PlAY_ID)) {
                Log.i("server", "play");
                if (mApplication.isPlaying) {
                    pauseMusic();
                } else {
                    playMusic();
                }

            } else if (TextUtils.equals(clicked_button, BUTTON_NEXT_ID)) {
                Log.i("server", "next");
                //下一曲
                if (mApplication.songItemPos < mApplication.mSongList.size() - 1) {
                    mApplication.songItemPos++;
                } else {
                    mApplication.songItemPos = 0;
                }
                playMusic();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 给notification的按钮添加监听
     */
    private void setListen() {
        //点击的事件处理

        Intent buttonIntent = new Intent(this, MusicServer.class);
        buttonIntent.setAction(ACTION_BUTTON);
        /* 上一首按钮 */
        buttonIntent.putExtra(INTENT_BUTTON_ID, BUTTON_LAST_ID);
        PendingIntent intent_prev = PendingIntent.getService(this, 1, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteView.setOnClickPendingIntent(R.id.last_one, intent_prev);
        /* 播放/暂停  按钮 */
        buttonIntent.putExtra(INTENT_BUTTON_ID, BUTTON_PlAY_ID);
        PendingIntent intent_paly = PendingIntent.getService(this, 2, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteView.setOnClickPendingIntent(R.id.StartOrStop, intent_paly);
        /* 下一首 按钮  */
        buttonIntent.putExtra(INTENT_BUTTON_ID, BUTTON_NEXT_ID);
        PendingIntent intent_next = PendingIntent.getService(this, 3, buttonIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteView.setOnClickPendingIntent(R.id.next_one, intent_next);
    }


    class MusicPlayerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // mSongList = AudioUtils.getAllSongs(getBaseContext());

            // 处理消息
            switch (msg.what) {
                case START:
                    playMusic();
                    break;
                case PAUSE:
                    pauseMusic();
                    break;
            }
            changeNotification();
        }
    }

    private void pauseMusic() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        } else {
            mMediaPlayer.start();
        }
    }

    private void playMusic() {
        mSongBean = mApplication.mSongList.get(mApplication.songItemPos);
        musicUrl = mSongBean.getFileUrl();
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
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }


}
