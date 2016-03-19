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
import android.widget.RemoteViews;

import java.io.IOException;


public class MusicServer extends Service {
    public static final int START = 0;
    public static final int PAUSE = 1;
    public static final String MUSIC_URL = "musicUrl";
    private static String musicUrl;
    private static MediaPlayer mMediaPlayer = new MediaPlayer();
    private static int id = 1;

    Messenger mMessenger = new Messenger(new MusicPlayerHandler());
    private SongBean mSongBean;
    private NotificationManager mNotifyMgr;
    private NotificationCompat.Builder mBuilder;
    private RemoteViews mRemoteView;


    private void changeNotification() {
        if (mSongBean != null) {
            mRemoteView.setTextViewText(R.id.music_info, mSongBean.getTitle());

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
            mBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon)
                    .setContent(mRemoteView);

            mBuilder.setContentIntent(pendingIntent);
            mNotifyMgr.notify(id, mBuilder.build());
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mRemoteView = new RemoteViews(getPackageName(), R.layout.buttom_control);

    }


    class MusicPlayerHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 处理消息
            switch (msg.what) {
                case START:
                    mSongBean = (SongBean) msg.obj;
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
                    break;
                case PAUSE:
                    if (mMediaPlayer.isPlaying()) {
                        mMediaPlayer.pause();
                    } else {
                        mMediaPlayer.start();
                    }
                    break;
            }
            changeNotification();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
