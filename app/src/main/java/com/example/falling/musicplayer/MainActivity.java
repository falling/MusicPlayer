package com.example.falling.musicplayer;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
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

import com.example.falling.musicplayer.adapter.ListViewAdapter;
import com.example.falling.musicplayer.application.MyApplication;
import com.example.falling.musicplayer.control.Proximity;
import com.example.falling.musicplayer.server.MusicServer;
import com.example.falling.musicplayer.util.AudioUtils;
import com.example.falling.musicplayer.util.SharedPreferencesUtil;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener, SensorEventListener {

    public static final int REQUEST_CODE = 1;
    public static final int MESSAGE_CODE = 4;
    public static final int CONNECT = 3;
    private ListView mListView;
    private ImageView mImage_last_one;
    private ImageView mImage_start;
    private ImageView mImage_next_one;
    public TextView mMusicInfo;


    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMessenger = new Messenger(service);
            sendMessage(CONNECT);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private Messenger mMessenger;

    private Messenger ActivityMessenger = new Messenger(new Handler() {
        @Override
        public void handleMessage(Message msgFromServer) {
            switch (msgFromServer.what) {
                case MESSAGE_CODE:
                    Bundle bundle = msgFromServer.getData();
                    mApplication.isPlaying = bundle.getBoolean(MusicServer.IS_PLAYING);
                    mApplication.isPause = bundle.getBoolean(MusicServer.IS_PAUSE);
                    mApplication.songItemPos = bundle.getInt(MusicServer.MUSIC_POS);
                    if(mApplication.mSongList.size()!=0) {
                        mMusicInfo.setText(getMusicInfo(mApplication.songItemPos));
                    }
                    changeIcon();
                    break;
            }
            super.handleMessage(msgFromServer);
        }
    });

    private Intent mIntent;
    private MyApplication mApplication;
    private SensorManager mManager;
    private Proximity mProximity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        setListener();
        mApplication = (MyApplication) getApplication();
        mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mProximity = new Proximity(this);
        changeIcon();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            doAfterPermissionGet();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        mManager.unregisterListener(this);
        super.onPause();
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
        SharedPreferencesUtil.save(this, mApplication.songItemPos);
        unbindService(mServiceConnection);
    }


    //权限申请
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doAfterPermissionGet();

                } else {
                    Toast.makeText(this, "没有内存卡权限，无法读取SD卡媒体信息", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void doAfterPermissionGet() {
        initSongList();
        mIntent = new Intent(this, MusicServer.class);
        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initSongList() {
        mApplication.mSongList = AudioUtils.getAllSongs(this);
        if(mApplication.mSongList.size() == 0 ){
            Toast.makeText(this,"SD卡没有歌曲！",Toast.LENGTH_LONG).show();
        }
        else {
            ListViewAdapter listViewAdapter = new ListViewAdapter(this, mApplication.mSongList);
            mListView.setAdapter(listViewAdapter);
            if (mApplication.songItemPos >= 0 && mApplication.songItemPos < mApplication.mSongList.size())
                mMusicInfo.setText(getMusicInfo(mApplication.songItemPos));
        }
    }


    /**
     * 改变播放或者暂停的图标
     */
    private void changeIcon() {
        if (!mApplication.isPlaying || (mApplication.isPlaying && mApplication.isPause)) {
            mImage_start.setImageResource(R.mipmap.button_play);
        } else {
            mImage_start.setImageResource(R.mipmap.button_stop);
        }
    }

    @Override
    public void onClick(View v) {
        startService(mIntent);
        bindService(mIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

        if(mApplication.mSongList.size()!=0) {
            switch (v.getId()) {
                //上一曲
                case R.id.last_one:
                    lastOne();
                    break;

                //暂停播放
                case R.id.StartOrStop:
                    pause();
                    break;
                //下一曲
                case R.id.next_one:
                    nextOne();
                    break;
            }
        }else{
            Toast.makeText(v.getContext(),"SD卡没有歌曲",Toast.LENGTH_LONG).show();
        }
    }

    public void pause() {
        if (mApplication.isPlaying) {
            sendPauseMessage();
        } else {
            sendStartMessage(mApplication.songItemPos);
        }
        mProximity.controlOver();
    }

    public void nextOne() {
        mApplication.nextSong();
        sendStartMessage(mApplication.songItemPos);
        mProximity.controlOver();

    }

    public void lastOne() {
        mApplication.lastSong();
        sendStartMessage(mApplication.songItemPos);
        mProximity.controlOver();
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
        mImage_start.setImageResource(R.mipmap.button_stop);
        mApplication.songItemPos = position;
        mApplication.isPlaying = true;
        mApplication.isPause = false;
        sendMessage(MusicServer.START);
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
        message.arg1 = mApplication.songItemPos;
        message.replyTo = ActivityMessenger;
        try {
            mMessenger.send(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 传感器
     *
     * @param event
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] values = event.values;
        if (values != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (values[0] == 0.0) {
                if(mProximity.isRunning()){
                    mProximity.addCount();
                }else {
                    new Thread(mProximity).start();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}