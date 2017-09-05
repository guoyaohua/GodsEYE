package com.guoyaohua.godseye.application;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 生命检测服务，定时周期开启鹰眼服务，每次运行都将行为记录在日志文件，方便后期检测何时程序冻结。
 */
public class LifeService extends Service {
    File file;
    FileOutputStream fos;
    MyApplication myApplication;

    public LifeService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//创建定时器
        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        long triggerAtTime = SystemClock.elapsedRealtime() + 180 * 1000;//每3min触发一次开启鹰眼服务
        Intent i = new Intent(this, LifeService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            manager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        } else {
            manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动鹰眼服务
                myApplication.startBaiduService();
                //记录在文件
                file = new File(getFilesDir(), "log.txt");
                try {
                    fos = new FileOutputStream(file, true);
                    //获取时间戳
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                    String str = formatter.format(curDate);
                    fos.write((str + "服务存活").getBytes());
                    fos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        myApplication = (MyApplication) getApplication();
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
