package com.guoyaohua.godseye;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.track.utils.BitmapUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PowerManager powerManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        BitmapUtil.init();
        Intent intent = new Intent(this, TestMap.class);
        startActivity(intent);
    }

    private void init() {
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);
    }

    /**
     * 将本程序加入DOZE白名单
     */
    @Override
    protected void onPostResume() {
        super.onPostResume();
        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 检查运行时权限，启动鹰眼服务
     */
    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
        //启动鹰眼服务;
        MyApplication myApplication = (MyApplication) getApplication();
        myApplication.entityName = MyApplication.myInfo.getUserName();

        myApplication.startBaiduService();
    }

    /**
     * 运行时权限申请
     *
     * @param permissions
     * @return
     */
    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, android.Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, android.Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }
}
