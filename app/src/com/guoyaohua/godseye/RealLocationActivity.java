package com.guoyaohua.godseye;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.EntityListRequest;
import com.baidu.trace.api.entity.EntityListResponse;
import com.baidu.trace.api.entity.FilterCondition;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.chat.ChatActivity;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;
import com.guoyaohua.godseye.track.model.CurrentLocation;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.MapUtil;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.JMessageClient;

public class RealLocationActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int OTHER_POSITION = 1;
    public static final int MY_POSITION = 2;
    private static final String TARGET_ID = "targetId";
    private static final String TARGET_APP_KEY = "targetAppKey";
    public String userName;
    public String nickName;
    List<String> userEntity = new ArrayList<String>();
    private Button bt_showTrace;
    private Button bt_showME;
    private Button bt_findTA;
    private Button bt_sentMsg;
    private TextView tv_title_realLoc;
    private LinearLayout bt_back_realLoc;
    private boolean showME = true;
    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;
    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;
    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;
    private OnEntityListener entityListener = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_location);
        Intent intent = getIntent();
        nickName = intent.getStringExtra("nickName");
        userName = intent.getStringExtra("userName");
        userEntity.add(userName);
        initWidgit();

        initListener();

        initMap();
    }

    private void initMap() {
        Log.i("Map", "initMap");
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.realLoc_mapView));
    }


    private void initWidgit() {
        bt_showTrace = (Button) findViewById(R.id.bt_showTrace);
        bt_showME = (Button) findViewById(R.id.bt_showME);
        bt_findTA = (Button) findViewById(R.id.bt_findTA);
        bt_sentMsg = (Button) findViewById(R.id.bt_sentMsg);
        tv_title_realLoc = (TextView) findViewById(R.id.tv_title_realLoc);
        bt_back_realLoc = (LinearLayout) findViewById(R.id.bt_back_realLoc);

        bt_showTrace.setOnClickListener(this);
        bt_showME.setOnClickListener(this);
        bt_findTA.setOnClickListener(this);
        bt_sentMsg.setOnClickListener(this);
        bt_back_realLoc.setOnClickListener(this);

        tv_title_realLoc.setText(nickName);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {

            case R.id.bt_showTrace:
                intent = new Intent(this, TrackQueryActivity.class);
                intent.putExtra("EntityName", userName);
                startActivity(intent);
                break;
            case R.id.bt_showME:

                break;
            case R.id.bt_findTA:

                break;
            case R.id.bt_sentMsg:
                if (JMessageClient.getMyInfo() != null) {
                    intent = new Intent();
                    intent.putExtra(TARGET_ID, userName);
                    intent.putExtra(TARGET_APP_KEY, JMessageClient.getMyInfo().getAppKey());
                    intent.setClass(this, ChatActivity.class);
                    MyApplication.getContext().startActivity(intent);
                }
                break;
            case R.id.bt_back_realLoc:

                break;

        }
    }

    @Override
    public void onResume() {
//        Log.i("Map", "onResume");
        super.onResume();
        initMap();
        mapUtil.onResume();
        startRealTimeLoc(5);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("Map", "onDestroy");
        mapUtil.clear();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("Map", "onPause");
        stopRealTimeLoc();
        mapUtil.onPause();
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
    }

    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    private void getLocation() {
// 请求标识
        int tag = 5;
// 过滤条件
        FilterCondition filterCondition = new FilterCondition();
// 查找当前时间5分钟之内有定位信息上传的entity
//        filterCondition.setActiveTime(activeTime);
        filterCondition.setEntityNames(userEntity);
// 返回结果坐标类型
        CoordType coordTypeOutput = CoordType.bd09ll;
// 分页索引
        int pageIndex = 1;
// 分页大小
        int pageSize = 100;
// 创建Entity列表请求实例
        EntityListRequest request = new EntityListRequest(tag, MyApplication.serviceId, filterCondition, coordTypeOutput, pageIndex, pageSize);

// 初始化监听器
        OnEntityListener entityListener = new OnEntityListener() {
            @Override
            public void onEntityListCallback(EntityListResponse response) {
                if (response.getEntities() != null) {
                    response.getEntities().get(0).getLatestLocation();
                    if (null != mapUtil) {
                        if (mapUtil.baiduMap == null) {
                            initMap();
                            mapUtil.onResume();
                        }
                        LatLng currentLatLng = mapUtil.convertTrace2Map(response.getEntities().get(0).getLatestLocation().getLocation());
//                        response.getEntities().get(0).getLatestLocation();
                        mapUtil.updateStatus(currentLatLng, true);
//                        mapUtil.updatePosition(currentLatLng, OTHER_POSITION);
                    }
                }
            }
        };

// 查询Entity列表
        MyApplication.mClient.queryEntityList(request, entityListener);

    }

    /**
     * 初始化监听器，用于实时位置查询
     */
    private void initListener() {

        trackListener = new OnTrackListener() {

            @Override
            public void onLatestPointCallback(LatestPointResponse response) {
                if (StatusCodes.SUCCESS != response.getStatus()) {
                    return;
                }

                LatestPoint point = response.getLatestPoint();
                //判断一下是否获取成功
                if (null == point || CommonUtil.isZeroPoint(point.getLocation().getLatitude(), point.getLocation()
                        .getLongitude())) {
                    return;
                }

                LatLng currentLatLng = mapUtil.convertTrace2Map(point.getLocation());
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = point.getLocTime();
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    //更新地图，标注实时点
                    mapUtil.updatePosition(currentLatLng, MY_POSITION);
//                    Log.i("MyPosition", CurrentLocation.locTime + "#" + CurrentLocation.latitude + "#" + CurrentLocation.longitude);
                }
            }
        };

        entityListener = new OnEntityListener() {

            @Override
            public void onReceiveLocation(TraceLocation location) {

                if (StatusCodes.SUCCESS != location.getStatus() || CommonUtil.isZeroPoint(location.getLatitude(),
                        location.getLongitude())) {
                    return;
                }
                LatLng currentLatLng = mapUtil.convertTraceLocation2Map(location);
                if (null == currentLatLng) {
                    return;
                }
                CurrentLocation.locTime = CommonUtil.toTimeStamp(location.getTime());
                CurrentLocation.latitude = currentLatLng.latitude;
                CurrentLocation.longitude = currentLatLng.longitude;

                if (null != mapUtil) {
                    mapUtil.updatePosition(currentLatLng, MY_POSITION);
//                    Log.i("MyPosition", CurrentLocation.locTime + "#" + CurrentLocation.latitude + "#" + CurrentLocation.longitude);
                }
            }

        };

    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

        }
    }

    /**
     * 实时定位任务
     *
     * @author baidu
     */
    class RealTimeLocRunnable implements Runnable {

        private int interval = 0;

        public RealTimeLocRunnable(int interval) {
            this.interval = interval;
        }

        @Override
        public void run() {
//            Log.i("getLoc", "获取一次");

//            MyApplication myApplication = (MyApplication) getApplication();
//            myApplication.getCurrentLocation(entityListener, trackListener);
            getLocation();
            if (showME) {
                MyApplication myApplication = (MyApplication) getApplication();
                myApplication.getCurrentLocation(entityListener, trackListener);
            }
            realTimeHandler.postDelayed(this, interval * 1000);
        }
    }

}
