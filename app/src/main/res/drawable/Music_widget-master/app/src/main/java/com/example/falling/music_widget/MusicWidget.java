package com.example.falling.music_widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.widget.RemoteViews;


/**
 * Created by falling on 2016/2/3.
 */
public class MusicWidget extends AppWidgetProvider {

    public static final String WIDGET_BUTTON_PLAY = "widget_button_play";
    public static final String WIDGET_BUTTON_LAST_ONE = "widget_button_lastOne";
    public static final String WIDGET_BUTTON_NEXT_ONE = "widget_button_nextOne";
    public static final String SERVICE_MESSAGE = "Service_Message";


    /**
     * 接受的信息来源有两个，一个是widget的点击信息。根据点击信息发送给service对应的
     * 另外一个是来自于service的歌曲信息和播放状态信息，根据此信息显示歌曲名，和开始\暂停图标的切换
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        //收到点击信息，发送给服务的动作信息。比如开始播放，切歌
        Intent actionIntent = new Intent(context, MusicService.class);
        if (intent != null && !TextUtils.equals(intent.getAction(),SERVICE_MESSAGE)) {
            if (TextUtils.equals(intent.getAction(), WIDGET_BUTTON_PLAY)) {
                actionIntent.putExtra("action", "开始");
            } else if (TextUtils.equals(intent.getAction(), WIDGET_BUTTON_LAST_ONE)) {
                actionIntent.putExtra("action", "上一首");
            } else if (TextUtils.equals(intent.getAction(), WIDGET_BUTTON_NEXT_ONE)) {
                actionIntent.putExtra("action", "下一首");
            }
            context.startService(actionIntent);

            //收到service的信息，修改界面上的信息
        }else if(intent != null && TextUtils.equals(intent.getAction(),SERVICE_MESSAGE)){

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_music);
           //修改播放和暂停的图标
            if(intent.getBooleanExtra(MusicService.STATE, false)){
                remoteViews.setImageViewResource(R.id.start, R.mipmap.button_stop);
            }else{
                remoteViews.setImageViewResource(R.id.start, R.mipmap.button_play);
            }
            //修改歌曲名
            remoteViews.setTextViewText(R.id.musicName,intent.getStringExtra(MusicService.MUSIC_NAME));
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, MusicWidget.class);
            appWidgetManager.updateAppWidget(componentName, remoteViews);

        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_music);
        Intent intent = new Intent(context, MusicWidget.class);

        //开始播放
        intent.setAction(WIDGET_BUTTON_PLAY);
        PendingIntent pendingIntent_play = PendingIntent.getBroadcast(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.start, pendingIntent_play);

        //上一首
        intent.setAction(WIDGET_BUTTON_LAST_ONE);
        PendingIntent pendingIntent_lastOne = PendingIntent.getBroadcast(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.LastOne, pendingIntent_lastOne);

        //下一首
        intent.setAction(WIDGET_BUTTON_NEXT_ONE);
        PendingIntent pendingIntent_NextOne = PendingIntent.getBroadcast(context, 0, intent, 0);
        remoteViews.setOnClickPendingIntent(R.id.NextOne, pendingIntent_NextOne);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }

}
