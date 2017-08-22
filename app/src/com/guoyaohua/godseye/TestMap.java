package com.guoyaohua.godseye;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;
import com.guoyaohua.godseye.track.model.CurrentLocation;
import com.guoyaohua.godseye.track.receiver.TrackReceiver;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.MapUtil;


/**
 * 用于测试地图功能
 */
public class TestMap extends AppCompatActivity implements View.OnClickListener {

    private TrackReceiver trackReceiver = null;
    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;

    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;
    private OnEntityListener entityListener = null;

//    private RealTimeHandler realTimeHandler = new RealTimeHandler();

//    private RealTimeLocRunnable realTimeLocRunnable = null;

    private int notifyId = 0;
    private Button bt_trace;
    private Button bt_loc;

    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;

    private MyApplication myApplication = (MyApplication) getApplication();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_map);
        init();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mapUtil.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mapUtil.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopRealTimeLoc();
        mapUtil.onPause();
    }

    private void init() {
        bt_trace = (Button) findViewById(R.id.bt_trace);
        bt_loc = (Button) findViewById(R.id.bt_loc);
        bt_loc.setOnClickListener(this);
        bt_trace.setOnClickListener(this);

        initListener();

        initMap();

//        mapUtil.setCenter(trackApp);//设置为上次推出前保存的地点为中心，不需要这么弄

    }

    private void initMap() {
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.tracing_mapView));
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
                    mapUtil.updateStatus(currentLatLng, true);
                    Log.i("getLoc", CurrentLocation.locTime + "#" + CurrentLocation.latitude + "#" + CurrentLocation.longitude);
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
                    mapUtil.updateStatus(currentLatLng, true);
                    Log.i("getLoc", CurrentLocation.locTime + "#" + CurrentLocation.latitude + "#" + CurrentLocation.longitude);
                }
            }

        };

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_trace:
                Intent intent = new Intent(this, TrackQueryActivity.class);
                intent.putExtra("EntityName", myApplication.entityName);
                startActivity(intent);
                break;
        }
    }

    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();
        startRealTimeLoc(3);
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
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

            MyApplication myApplication = (MyApplication) getApplication();
            myApplication.getCurrentLocation(entityListener, trackListener);

            realTimeHandler.postDelayed(this, interval * 1000);
        }
    }
}
