package com.guoyaohua.godseye;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import com.guoyaohua.godseye.fragment.Fragment_page1;
import com.guoyaohua.godseye.fragment.Fragment_page2;
import com.guoyaohua.godseye.fragment.Fragment_page3;
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
    private ImageButton ib_page1;
    private ImageButton ib_page3;
    private ImageButton ib_page2;
    private Fragment_page1 fragment_page1;
    private Fragment_page2 fragment_page2;
    private Fragment_page3 fragment_page3;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgit();
        // 设置默认的Fragment
        setDefaultFragment();
        init();
        BitmapUtil.init();
        Intent intent = new Intent(this, TestMap.class);
        startActivity(intent);

    }
    @SuppressLint("NewApi")
    private void setDefaultFragment() {
        manager = getFragmentManager();
        transaction = manager.beginTransaction();

        fragment_page1 = new Fragment_page1();
        transaction.replace(R.id.fragment_container, fragment_page1);
        transaction.commit();
    }

    private void initWidgit() {
        ib_page1 = (ImageButton) findViewById(R.id.ib_page1);
        ib_page2 = (ImageButton) findViewById(R.id.ib_page2);
        ib_page3 = (ImageButton) findViewById(R.id.ib_page3);

        ib_page1.setOnClickListener(this);
        ib_page2.setOnClickListener(this);
        ib_page3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        manager = getFragmentManager();
        transaction = manager.beginTransaction();
        switch (v.getId()) {
            case R.id.ib_page1:
                if (fragment_page1 == null) {
                    fragment_page1 = new Fragment_page1();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page1);
                break;

            case R.id.ib_page2:
                if (fragment_page2 == null) {
                    fragment_page2 = new Fragment_page2();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page2);
                break;

            case R.id.ib_page3:
                if (fragment_page3 == null) {
                    fragment_page3 = new Fragment_page3();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page3);
                break;

        }
        transaction.commit();
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
