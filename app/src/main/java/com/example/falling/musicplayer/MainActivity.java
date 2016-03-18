package com.example.falling.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
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

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_CODE = 1;
    public static final String IS_PLAYING = "isPlaying";
    public static final String IS_PAUSE = "isPause";
    private ListView mListView;
    private ImageView mImage_last_one;
    private ImageView mImage_start;
    private ImageView mImage_next_one;
    private ArrayList<SongBean> mSongList;
    private TextView mMusicInfo;
    private boolean isPlaying = false;
    private boolean isPause = false;


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
    private int mId;
    private Intent mIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListener();
        if (savedInstanceState != null) {
            isPlaying = savedInstanceState.getBoolean(IS_PLAYING);
            isPause = savedInstanceState.getBoolean(IS_PAUSE);
            if (!isPause && isPlaying) {
                mImage_start.setImageResource(R.mipmap.button_stop);
            }else {
                mImage_start.setImageResource(R.mipmap.button_play);
            }
        }
        mIntent = new Intent(this, MusicServer.class);
        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            mSongList = AudioUtils.getAllSongs(this);
            ListViewAdapter listViewAdapter = new ListViewAdapter(this, mSongList);
            mListView.setAdapter(listViewAdapter);
            mId = SharedPreferencesUtil.getId(this);
            if (mId >= 0 && mId < mSongList.size())
                mMusicInfo.setText(mSongList.get(mId).getTitle() + "\n" + mSongList.get(mId).getSinger());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PLAYING, isPlaying);
        outState.putBoolean(IS_PAUSE, isPause);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        stopService(mIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ArrayList<SongBean> songList = AudioUtils.getAllSongs(this);
                    ListViewAdapter listViewAdapter = new ListViewAdapter(this, songList);
                    mListView.setAdapter(listViewAdapter);

                } else {
                    Toast.makeText(this, "没有内存卡权限，无法读取SD卡媒体信息", Toast.LENGTH_SHORT).show();
                }
            }
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
    public void onClick(View v) {

        switch (v.getId()) {
            //上一曲
            case R.id.last_one:
                if (mId > 0) {
                    mId--;
                } else {
                    mId = mSongList.size() - 1;
                }
                sendStartMessage(mId);
                break;


            //暂停播放
            case R.id.StartOrStop:
                if (isPlaying) {
                    sendPauseMessage();
                } else {
                    sendStartMessage(mId);
                }
                break;
            //下一曲
            case R.id.next_one:
                if (mId < mSongList.size() - 1) {
                    mId++;
                } else {
                    mId = 0;
                }
                sendStartMessage(mId);
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


        if (mMessenger != null) {
            sendStartMessage(position);
        }

    }

    //发送播放的信息
    private void sendStartMessage(int position) {
        mMusicInfo.setText(mSongList.get(position).getTitle() + "\n" + mSongList.get(position).getSinger());
        Message message = Message.obtain();
        message.what = MusicServer.START;
        message.obj = mSongList.get(position);

        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        mImage_start.setImageResource(R.mipmap.button_stop);
        SharedPreferencesUtil.save(this, position);
        mId = position;
        isPlaying = true;
        isPause = false;
    }

    private void sendPauseMessage() {
        Message message = Message.obtain();
        message.what = MusicServer.PAUSE;
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if(isPlaying && isPause) {
            mImage_start.setImageResource(R.mipmap.button_stop);
            isPause = false;
        }else{
            mImage_start.setImageResource(R.mipmap.button_play);
            isPause = true;
        }
    }
}
