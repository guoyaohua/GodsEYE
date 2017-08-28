package com.guoyaohua.godseye.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;
import com.guoyaohua.godseye.track.model.CurrentLocation;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.MapUtil;

/**
 * 用于显示自身位置
 */
public class Fragment_page1 extends Fragment implements View.OnClickListener {
    /**
     * 地图工具
     */
    public static MapUtil mapUtil = null;

    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;
    private OnEntityListener entityListener = null;
    private int notifyId = 0;
    private Button bt_trace;
    private Button bt_loc;
    private View view;
    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();

    private RealTimeLocRunnable realTimeLocRunnable = null;

    private MyApplication myApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("Map", "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fragment_page1, container, false);

//        http://www.cnblogs.com/Gaojiecai/p/4084252.html
        Log.i("Map", "onCreateView");
        init();
        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myApplication = (MyApplication) getActivity().getApplication();
        Log.i("Map", "onActivityCreated");
    }

    private void init() {
        bt_trace = (Button) view.findViewById(R.id.bt_trace_f1);
        bt_loc = (Button) view.findViewById(R.id.bt_loc_f1);
        bt_loc.setOnClickListener(this);
        bt_trace.setOnClickListener(this);

        initListener();

        initMap();


    }

    private void initMap() {
        Log.i("Map", "initMap");
        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) view.findViewById(R.id.tracing_mapView_f1));
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
                    if (mapUtil.baiduMap == null) {
                        initMap();
                        mapUtil.onResume();
                    }
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
                    if (mapUtil.baiduMap == null) {
                        initMap();
                        mapUtil.onResume();
                    }
                    mapUtil.updateStatus(currentLatLng, true);
                    Log.i("getLoc", CurrentLocation.locTime + "#" + CurrentLocation.latitude + "#" + CurrentLocation.longitude);
                }
            }

        };

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_trace_f1:
                Intent intent = new Intent(getActivity(), TrackQueryActivity.class);
                intent.putExtra("EntityName", MyApplication.myInfo.getUserName());
                startActivity(intent);
                break;
        }
    }

    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    @Override
    public void onResume() {
//        Log.i("Map", "onResume");
        super.onResume();
        initMap();
        mapUtil.onResume();
        startRealTimeLoc(5);
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
    }

    @Override
    public void onStop() {
        Log.i("Map", "onStop");
        super.onStop();
//        mapUtil.clear();
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

    @Override
    public void onStart() {
        super.onStart();
        Log.i("Map", "onStart");
//        initMap();
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
            myApplication.getCurrentLocation(entityListener, trackListener);

            realTimeHandler.postDelayed(this, interval * 1000);
        }
    }
}
