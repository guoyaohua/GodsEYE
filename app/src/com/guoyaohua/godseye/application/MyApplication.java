package com.guoyaohua.godseye.application;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

//import cn.jpush.im.android.api.JMessageClient;

/**
 * Created by 郭耀华 on 2017/8/19.
 */

public class MyApplication extends Application {
    public static Context context;
    public static String DEVICE_ID = "";
    public static UserInfo myInfo;//当前登陆用户

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
        TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        DEVICE_ID = tm.getDeviceId();
        JMessageClient.init(context);
//        JMessageClient.init(context, true);
    }
}
