package com.example.falling.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_CODE = 1;
    private ListView mListView;
    private ImageView mImage_last_one;
    private ImageView mImage_start;
    private ImageView mImage_next_one;
    private TextView mMusicInfo;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Messenger mMessenger;

    private Intent mIntent;
    private MyApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListener();
        mApplication = (MyApplication) getApplication();
        mApplication.mActivity = this;
        changeIcon();

        mIntent = new Intent(this, MusicServer.class);
        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            ListViewAdapter listViewAdapter = new ListViewAdapter(this, mApplication.mSongList);
            mListView.setAdapter(listViewAdapter);
            if (mApplication.songItemPos >= 0 && mApplication.songItemPos < mApplication.mSongList.size())
                mMusicInfo.setText(getMusicInfo(mApplication.songItemPos));
        }
    }


    private void setListener() {
        mListView.setOnItemClickListener(this);
        mImage_last_one.setOnClickListener(this);
        mImage_next_one.setOnClickListener(this);
        mImage_start.setOnClickListener(this);
    }

    private void findView() {
        mListView = (ListView) findViewById(R.id.listview);
        mImage_last_one = (ImageView) findViewById(R.id.last_one);
        mImage_start = (ImageView) findViewById(R.id.StartOrStop);
        mImage_next_one = (ImageView) findViewById(R.id.next_one);
        mMusicInfo = (TextView) findViewById(R.id.music_info);

    }




    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        stopService(mIntent);
    }


    //权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mApplication.mSongList = AudioUtils.getAllSongs(this);
                    ListViewAdapter listViewAdapter = new ListViewAdapter(this, mApplication.mSongList);
                    mListView.setAdapter(listViewAdapter);

                } else {
                    Toast.makeText(this, "没有内存卡权限，无法读取SD卡媒体信息", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    /**
     * 改变 播放 或者 暂停 的图标
     */
    private void changeIcon() {
        if (mApplication.isPlaying && mApplication.isPause) {
            mImage_start.setImageResource(R.mipmap.button_play);
        } else {
            mImage_start.setImageResource(R.mipmap.button_stop);
        }
    }

    @Override
    public void onClick(View v) {

        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        switch (v.getId()) {
            //上一曲
            case R.id.last_one:
                if (mApplication.songItemPos > 0) {
                    mApplication.songItemPos--;
                } else {
                    mApplication.songItemPos = mApplication.mSongList.size() - 1;
                }
                sendStartMessage(mApplication.songItemPos);
                break;


            //暂停播放
            case R.id.StartOrStop:
                if (mApplication.isPlaying) {
                    sendPauseMessage();
                } else {
                    sendStartMessage(mApplication.songItemPos);
                }
                break;
            //下一曲
            case R.id.next_one:
                if (mApplication.songItemPos < mApplication.mSongList.size() - 1) {
                    mApplication.songItemPos++;
                } else {
                    mApplication.songItemPos = 0;
                }
                sendStartMessage(mApplication.songItemPos);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);


        if (mMessenger != null) {
            sendStartMessage(position);
        }

    }


    //获取歌曲信息用于显示
    @NonNull
    private String getMusicInfo(int position) {
        return mApplication.mSongList.get(position).getTitle() + "\n" + mApplication.mSongList.get(position).getSinger();
    }

    //发送播放的信息
    private void sendStartMessage(int position) {
        mMusicInfo.setText(getMusicInfo(position));
        sendMessage(MusicServer.START);

        mImage_start.setImageResource(R.mipmap.button_stop);
        SharedPreferencesUtil.save(this, position);

        mApplication.songItemPos = position;
        mApplication.isPlaying = true;
        mApplication.isPause = false;
    }

    //发送暂停的信息
    private void sendPauseMessage() {
        sendMessage(MusicServer.PAUSE);

        mApplication.isPause = !mApplication.isPause;
        changeIcon();
    }

    //发送消息
    private void sendMessage(int what) {
        Message message = Message.obtain();
        message.what = what;
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
