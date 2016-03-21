package com.example.falling.musicplayer.server;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.example.falling.musicplayer.util.AudioUtils;
import com.example.falling.musicplayer.MainActivity;
import com.example.falling.musicplayer.R;
import com.example.falling.musicplayer.util.SharedPreferencesUtil;
import com.example.falling.musicplayer.bean.SongBean;

import java.io.IOException;
import java.util.ArrayList;


public class MusicServer extends Service {
    public static final int START = 0;
    public static final int PAUSE = 1;
    public static final String ACTION_BUTTON = "action_button";
    public static final String INTENT_BUTTON_ID = "intent_button_id";
    public static final String BUTTON_LAST_ID = "1";
    public static final String BUTTON_PlAY_ID = "2";
    public static final String BUTTON_NEXT_ID = "3";
    public static final String IS_PLAYING = "isPlaying";
    public static final String IS_PAUSE = "isPause";
    public static final String MUSIC_POS = "musicPos";
    private static String musicUrl;
    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static int Notification_Id = 1;

    private ArrayList<SongBean> mSongList;

    private Messenger mMessenger = new Messenger(new MusicPlayerHandler());
    private SongBean mSongBean;
    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteView;
    private Intent mIntent;

    private boolean isPlaying;
    private boolean isPause;
    private int songItemPos;
    private Message mMessage;
    private Messenger mServerMessenger;


    /**
     * 改变notification的显示
     */
    private void changeNotification() {
        if (mSongBean != null) {
            mRemoteView.setTextViewText(R.id.music_info, mSongBean.getTitle()+"\n"+mSongBean.getSinger());
            if (isPlaying && isPause) {
                mRemoteView.setImageViewResource(R.id.StartOrStop, R.mipmap.button_play);
            } else {
                mRemoteView.setImageViewResource(R.id.StartOrStop, R.mipmap.button_stop);
            }
            mBuilder.setSmallIcon(R.drawable.icon)
                    .setContent(mRemoteView);
            mNotifyMgr.notify(Notification_Id, mBuilder.build());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isPlaying = false;
        isPause = false;
        songItemPos = SharedPreferencesUtil.getId(this);
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.buttom_control);
        mIntent = new Intent(this, MainActivity.class);
        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentIntent(PendingIntent.getActivity(this, 0, mIntent, 0));
        setListen();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String ctrl_code = intent.getAction();
        String clicked_button = intent.getStringExtra(MusicServer.INTENT_BUTTON_ID);

        if (TextUtils.equals(ctrl_code, MusicServer.ACTION_BUTTON)) {
            if (TextUtils.equals(clicked_button, BUTTON_LAST_ID)) {
                //上一曲
                lastSong();
                playMusic();
            } else if (TextUtils.equals(clicked_button, BUTTON_PlAY_ID)) {
                //播放暂停
                if (isPlaying) {
                    pauseMusic();
                } else {
                    playMusic();
                }
            } else if (TextUtils.equals(clicked_button, BUTTON_NEXT_ID)) {
                //下一曲
                nextSong();
                playMusic();
            }
            changeNotification();

            sendMessageToActivity();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void sendMessageToActivity() {
        if(mServerMessenger!=null) {
            SharedPreferencesUtil.save(this, songItemPos);
            mMessage = Message.obtain();
            mMessage.what = MainActivity.MESSAGE_CODE;
            Bundle bundle = new Bundle();
            bundle.putBoolean(IS_PLAYING, isPlaying);
            bundle.putBoolean(IS_PAUSE, isPause);
            bundle.putInt(MUSIC_POS,songItemPos);
            mMessage.setData(bundle);
            try {
                mServerMessenger.send(mMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 给notification的按钮添加监听
     */
    private void setListen() {
        //点击的事件处理
        mSongList = AudioUtils.getAllSongs(this);

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
            mServerMessenger = msg.replyTo;
            // 处理消息
            switch (msg.what) {
                case START:
                    mSongList = AudioUtils.getAllSongs(MusicServer.this);
                    songItemPos = msg.arg1;
                    playMusic();

                    break;
                case PAUSE:
                    pauseMusic();
                    break;

                case MainActivity.CONNECT:
                    sendMessageToActivity();
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
        isPause = !isPause;
    }

    private void playMusic() {
        mSongBean = mSongList.get(songItemPos);
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
        isPlaying = true;
        isPause = false;
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
    public void nextSong() {
        if (songItemPos < mSongList.size() - 1) {
            songItemPos++;
        } else {
            songItemPos = 0;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
