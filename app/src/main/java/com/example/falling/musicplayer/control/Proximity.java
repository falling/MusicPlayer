package com.example.falling.musicplayer.control;

import android.util.Log;

import com.example.falling.musicplayer.MainActivity;

/**
 * 实现距离传感器多次触发能有不同的动作
 * 一次表示暂停
 * 两次表示下一曲
 * 三次表示上一曲
 *
 * Created by falling on 16/3/25.
 */
public class Proximity implements Runnable {
    private boolean mIsRunning;
    private int count;
    private MainActivity mActivity;


    public Proximity(MainActivity activity) {
        mActivity = activity;
        mIsRunning = false;
        count = 0;
    }


    public boolean isRunning(){
        return mIsRunning;
    }

    /**
     * 记录在0.5S 传感器触发的次数
     */
    public synchronized void addCount() {
        count++;
    }


    /**
     * 判断0.5S内传感器触发的次数执行对应的动作
     */
    @Override
    public void run() {
        mIsRunning = true;
        count++;
        try {
            Thread.sleep(500);//等待0.5S，记录这0.5S内传感器触发的次数，然后根据次数执行对应的操作。
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (count == 1) {
                        //TODO:触发一次的动作  暂停
                        mActivity.pause();
                    } else if (count == 2) {
                        //TODO:触发两次的动作 下一曲
                        mActivity.nextOne();
                    } else if (count == 3) {
                        //TODO:触发三次的动作 上一曲
                        mActivity.lastOne();
                    }
                }
            });
            Thread.sleep(500);//因为 runOnUiThread 需要时间，在运行完前，count数据不能变
            count = 0;     //重置传感器触发的次数
            mIsRunning = false;//表示运行完毕，取消标志
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
