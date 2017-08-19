package com.guoyaohua.godseye.application;

import android.app.Application;
import android.content.Context;

/**
 * Created by 郭耀华 on 2017/8/19.
 */

public class MyApplication extends Application {
    public static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
    }
}
