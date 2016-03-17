package com.example.falling.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    public static final int REQUEST_CODE = 1;
    private ListView mListView;
    private ImageView mImage_last_one;
    private ImageView mImage_start;
    private ImageView mImage_next_one;
    private ArrayList<SongBean> mSongList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findView();
        setListener();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE);
        } else {
            mSongList = AudioUtils.getAllSongs(this);
            ListViewAdapter listViewAdapter = new ListViewAdapter(this, mSongList);
            mListView.setAdapter(listViewAdapter);
        }

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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.last_one:

                break;
            case R.id.StartOrStop:

                break;
            case R.id.next_one:

                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = mSongList.get(position).getFileUrl();
    }
}
