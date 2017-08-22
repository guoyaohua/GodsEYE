package com.guoyaohua.godseye.track.activity;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.TrackApplication;
import com.guoyaohua.godseye.track.model.ItemInfo;
import com.guoyaohua.godseye.track.utils.BitmapUtil;
import com.guoyaohua.godseye.track.utils.CommonUtil;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private TrackApplication trackApp;

    private ListView mListView = null;

    private MyAdapter myAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        BitmapUtil.init();
    }

    private void init() {
        trackApp = (TrackApplication) getApplicationContext();
        mListView = (ListView) findViewById(android.R.id.list);
        myAdapter = new MyAdapter(trackApp.itemInfos);
        mListView.setAdapter(myAdapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> arg0, View v, int index, long arg3) {
                Intent intent = new Intent(MainActivity.this, trackApp.itemInfos.get(index).clazz);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 适配android M，检查权限
        List<String> permissions = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isNeedRequestPermissions(permissions)) {
            requestPermissions(permissions.toArray(new String[permissions.size()]), 0);
        }
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_main;
    }

    private boolean isNeedRequestPermissions(List<String> permissions) {
        // 定位精确位置
        addPermission(permissions, Manifest.permission.ACCESS_FINE_LOCATION);
        // 存储权限
        addPermission(permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        // 读取手机状态
        addPermission(permissions, Manifest.permission.READ_PHONE_STATE);
        return permissions.size() > 0;
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CommonUtil.saveCurrentLocation(trackApp);
        if (trackApp.trackConf.contains("is_trace_started")
                && trackApp.trackConf.getBoolean("is_trace_started", true)) {
            // 退出app停止轨迹服务时，不再接收回调，将OnTraceListener置空
            trackApp.mClient.setOnTraceListener(null);
            trackApp.mClient.stopTrace(trackApp.mTrace, null);
        } else {
            trackApp.mClient.clear();
        }
        trackApp.isTraceStarted = false;
        trackApp.isGatherStarted = false;
        SharedPreferences.Editor editor = trackApp.trackConf.edit();
        editor.remove("is_trace_started");
        editor.remove("is_gather_started");
        editor.apply();

        BitmapUtil.clear();
    }

    //    @Override
    //    public boolean onKeyDown(int keyCode, KeyEvent event) {
    //        // back转home键
    //        if (keyCode == KeyEvent.KEYCODE_BACK) {
    //            Intent intent = new Intent(Intent.ACTION_MAIN);
    //            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //            intent.addCategory(Intent.CATEGORY_HOME);
    //            startActivity(intent);
    //            return true;
    //        }
    //        return super.onKeyDown(keyCode, event);
    //    }

    public class MyAdapter extends BaseAdapter
            implements AbsListView.OnScrollListener {

        private List<ItemInfo> itemInfos = null;

        public MyAdapter(List<ItemInfo> itemInfos) {
            this.itemInfos = itemInfos;
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {

        }

        @Override
        public int getCount() {
            return itemInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return itemInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (null != convertView) {
                viewHolder = (ViewHolder) convertView.getTag();
            } else {
                convertView = View.inflate(MainActivity.this, R.layout.item_list, null);
                viewHolder = new ViewHolder();

                viewHolder.titleIcon = (ImageView) convertView
                        .findViewById(R.id.iv_all_title);
                viewHolder.title = (TextView) convertView
                        .findViewById(R.id.tv_all_title);
                viewHolder.desc = (TextView) convertView
                        .findViewById(R.id.tv_all_desc);
                convertView.setTag(viewHolder);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                viewHolder.titleIcon.setBackground(ResourcesCompat.getDrawable(getResources(),
                        itemInfos.get(position).titleIconId, null));
            } else {
                viewHolder.titleIcon.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        itemInfos.get(position).titleIconId, null));
            }
            viewHolder.title.setText(itemInfos.get(position).titleId);
            viewHolder.desc.setText(itemInfos.get(position).descId);
            return convertView;
        }

        public class ViewHolder {
            /**
             * 标题图标
             */
            ImageView titleIcon;
            /**
             * 标题
             */
            TextView title;
            /**
             * 说明
             */
            TextView desc;
        }
    }
}
