package com.guoyaohua.godseye.track.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.Button;

import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.track.LatestPoint;
import com.baidu.trace.api.track.LatestPointResponse;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.LocationMode;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.baidu.trace.model.TraceLocation;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.TrackApplication;
import com.guoyaohua.godseye.track.model.CurrentLocation;
import com.guoyaohua.godseye.track.receiver.TrackReceiver;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.Constants;
import com.guoyaohua.godseye.track.utils.MapUtil;
import com.guoyaohua.godseye.track.utils.ViewUtil;

/**
 * 轨迹追踪
 */
public class TracingActivity extends BaseActivity implements View.OnClickListener {

    /**
     * 打包周期,可以把这些常量存到数据库或者sharepreferance里
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;
    private TrackApplication trackApp = null;
    private ViewUtil viewUtil = null;
    private Button traceBtn = null;
    private Button gatherBtn = null;
    private NotificationManager notificationManager = null;
    private PowerManager powerManager = null;
    private PowerManager.WakeLock wakeLock = null;
    private TrackReceiver trackReceiver = null;
    /**
     * 地图工具
     */
    private MapUtil mapUtil = null;
    /**
     * 轨迹服务监听器
     */
    private OnTraceListener traceListener = null;
    /**
     * 轨迹监听器(用于接收纠偏后实时位置回调)
     */
    private OnTrackListener trackListener = null;
    /**
     * Entity监听器(用于接收实时定位回调)
     */
    private OnEntityListener entityListener = null;
    /**
     * 实时定位任务
     */
    private RealTimeHandler realTimeHandler = new RealTimeHandler();
    private RealTimeLocRunnable realTimeLocRunnable = null;
    private int notifyId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_title);
        setOnClickListener(this);
        init();
    }

    private void init() {
        initListener();
        trackApp = (TrackApplication) getApplicationContext();
        viewUtil = new ViewUtil();


        mapUtil = MapUtil.getInstance();
        mapUtil.init((MapView) findViewById(R.id.tracing_mapView));
//        mapUtil.setCenter(trackApp);//设置为上次推出前保存的地点为中心，不需要这么弄
        startRealTimeLoc(Constants.LOC_INTERVAL);//开启实时定位

        powerManager = (PowerManager) trackApp.getSystemService(POWER_SERVICE);

        traceBtn = (Button) findViewById(R.id.btn_trace);
        gatherBtn = (Button) findViewById(R.id.btn_gather);

        traceBtn.setOnClickListener(this);
        gatherBtn.setOnClickListener(this);
        setTraceBtnStyle();
        setGatherBtnStyle();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // 追踪选项设置
            case R.id.btn_activity_options:
                ViewUtil.startActivityForResult(this, TracingOptionsActivity.class, Constants
                        .REQUEST_CODE);
                break;

            case R.id.btn_trace:
                if (trackApp.isTraceStarted) {
                    trackApp.mClient.stopTrace(trackApp.mTrace, traceListener);
                    stopRealTimeLoc();
                } else {
                    trackApp.mClient.startTrace(trackApp.mTrace, traceListener);
                    if (Constants.DEFAULT_PACK_INTERVAL != packInterval) {
                        stopRealTimeLoc();
                        startRealTimeLoc(packInterval);
                    }
                }
                break;

            case R.id.btn_gather:
                if (trackApp.isGatherStarted) {
                    trackApp.mClient.stopGather(traceListener);
                } else {
                    trackApp.mClient.startGather(traceListener);
                }
                break;

            default:
                break;
        }

    }

    /**
     * 设置服务按钮样式
     */
    private void setTraceBtnStyle() {
        boolean isTraceStarted = trackApp.trackConf.getBoolean("is_trace_started", false);
        if (isTraceStarted) {
            traceBtn.setText(R.string.stop_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color
                    .white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            traceBtn.setText(R.string.start_trace);
            traceBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                traceBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                traceBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    /**
     * 设置采集按钮样式
     */
    private void setGatherBtnStyle() {
        boolean isGatherStarted = trackApp.trackConf.getBoolean("is_gather_started", false);
        if (isGatherStarted) {
            gatherBtn.setText(R.string.stop_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_sure, null));
            }
        } else {
            gatherBtn.setText(R.string.start_gather);
            gatherBtn.setTextColor(ResourcesCompat.getColor(getResources(), R.color.layout_title, null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                gatherBtn.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            } else {
                gatherBtn.setBackgroundDrawable(ResourcesCompat.getDrawable(getResources(),
                        R.mipmap.bg_btn_cancel, null));
            }
        }
    }

    public void startRealTimeLoc(int interval) {
        realTimeLocRunnable = new RealTimeLocRunnable(interval);
        realTimeHandler.post(realTimeLocRunnable);
    }

    public void stopRealTimeLoc() {
        if (null != realTimeHandler && null != realTimeLocRunnable) {
            realTimeHandler.removeCallbacks(realTimeLocRunnable);
        }
    }

    //设置activie  返回值  放在设置页面
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (null == data) {
            return;
        }

        if (data.hasExtra("locationMode")) {
            LocationMode locationMode = LocationMode.valueOf(data.getStringExtra("locationMode"));
            trackApp.mClient.setLocationMode(locationMode);
        }

        if (data.hasExtra("isNeedObjectStorage")) {
            boolean isNeedObjectStorage = data.getBooleanExtra("isNeedObjectStorage", false);
            trackApp.mTrace.setNeedObjectStorage(isNeedObjectStorage);
        }

        if (data.hasExtra("gatherInterval") && data.hasExtra("packInterval")) {
            int gatherInterval = data.getIntExtra("gatherInterval", Constants.DEFAULT_GATHER_INTERVAL);
            int packInterval = data.getIntExtra("packInterval", Constants.DEFAULT_PACK_INTERVAL);
            TracingActivity.this.packInterval = packInterval;
            trackApp.mClient.setInterval(gatherInterval, packInterval);
        }

        //        if (data.hasExtra("supplementMode")) {
        //            mSupplementMode = SupplementMode.valueOf(data.getStringExtra("supplementMode"));
        //        }
    }

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
                }
            }

        };

        traceListener = new OnTraceListener() {

            /**
             * 绑定服务回调接口
             * @param errorNo  状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>1：失败</pre>
             */
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
                viewUtil.showToast(TracingActivity.this,
                        String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>10000：请求发送失败</pre>
             *                <pre>10001：服务开启失败</pre>
             *                <pre>10002：参数错误</pre>
             *                <pre>10003：网络连接失败</pre>
             *                <pre>10004：网络未开启</pre>
             *                <pre>10005：服务正在开启</pre>
             *                <pre>10006：服务已开启</pre>
             */
            @Override
            public void onStartTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.START_TRACE_NETWORK_CONNECT_FAILED <= errorNo) {
                    trackApp.isTraceStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_trace_started", true);
                    editor.apply();
                    setTraceBtnStyle();
                    registerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止服务回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>11000：请求发送失败</pre>
             *                <pre>11001：服务停止失败</pre>
             *                <pre>11002：服务未开启</pre>
             *                <pre>11003：服务正在停止</pre>
             */
            @Override
            public void onStopTraceCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.CACHE_TRACK_NOT_UPLOAD == errorNo) {
                    trackApp.isTraceStarted = false;
                    trackApp.isGatherStarted = false;
                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_trace_started");
                    editor.remove("is_gather_started");
                    editor.apply();
                    setTraceBtnStyle();
                    setGatherBtnStyle();
                    unregisterPowerReceiver();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>12000：请求发送失败</pre>
             *                <pre>12001：采集开启失败</pre>
             *                <pre>12002：服务未开启</pre>
             */
            @Override
            public void onStartGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STARTED == errorNo) {
                    trackApp.isGatherStarted = true;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.putBoolean("is_gather_started", true);
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止采集回调接口
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功</pre>
             *                <pre>13000：请求发送失败</pre>
             *                <pre>13001：采集停止失败</pre>
             *                <pre>13002：服务未开启</pre>
             */
            @Override
            public void onStopGatherCallback(int errorNo, String message) {
                if (StatusCodes.SUCCESS == errorNo || StatusCodes.GATHER_STOPPED == errorNo) {
                    trackApp.isGatherStarted = false;
                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
                    editor.remove("is_gather_started");
                    editor.apply();
                    setGatherBtnStyle();
                }
                viewUtil.showToast(TracingActivity.this,
                        String.format("onStopGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 推送消息回调接口
             *
             * @param messageType 状态码
             * @param pushMessage 消息
             *                  <p>
             *                  <pre>0x01：配置下发</pre>
             *                  <pre>0x02：语音消息</pre>
             *                  <pre>0x03：服务端围栏报警消息</pre>
             *                  <pre>0x04：本地围栏报警消息</pre>
             *                  <pre>0x05~0x40：系统预留</pre>
             *                  <pre>0x41~0xFF：开发者自定义</pre>
             */
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
                if (messageType < 0x03 || messageType > 0x04) {
                    viewUtil.showToast(TracingActivity.this, pushMessage.getMessage());
                    return;
                }
                FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
                if (null == alarmPushInfo) {
                    viewUtil.showToast(TracingActivity.this,
                            String.format("onPushCallback, messageType:%d, messageContent:%s ", messageType,
                                    pushMessage));
                    return;
                }
                StringBuffer alarmInfo = new StringBuffer();
                alarmInfo.append("您于")
                        .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                        .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
                        .append(messageType == 0x03 ? "云端" : "本地")
                        .append("围栏：").append(alarmPushInfo.getFenceName());

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Notification notification = new Notification.Builder(trackApp)
                            .setContentTitle(getResources().getString(R.string.alarm_push_title))
                            .setContentText(alarmInfo.toString())
                            .setSmallIcon(R.mipmap.icon_app)
                            .setWhen(System.currentTimeMillis()).build();
                    notificationManager.notify(notifyId++, notification);
                }
            }
        };

    }

    /**
     * 注册广播（电源锁、GPS状态）
     */
    private void registerReceiver() {
        if (trackApp.isRegisterReceiver) {
            return;
        }

        if (null == wakeLock) {
            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "track upload");
        }
        if (null == trackReceiver) {
            trackReceiver = new TrackReceiver(wakeLock);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        filter.addAction(StatusCodes.GPS_STATUS_ACTION);
        trackApp.registerReceiver(trackReceiver, filter);
        trackApp.isRegisterReceiver = true;

    }

    private void unregisterPowerReceiver() {
        if (!trackApp.isRegisterReceiver) {
            return;
        }
        if (null != trackReceiver) {
            trackApp.unregisterReceiver(trackReceiver);
        }
        trackApp.isRegisterReceiver = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        startRealTimeLoc(packInterval);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapUtil.onResume();

        // 在Android 6.0及以上系统，若定制手机使用到doze模式，请求将应用添加到白名单。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = trackApp.getPackageName();
            boolean isIgnoring = powerManager.isIgnoringBatteryOptimizations(packageName);
            if (!isIgnoring) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                try {
                    startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapUtil.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopRealTimeLoc();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapUtil.clear();
        stopRealTimeLoc();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing;
    }

    static class RealTimeHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }

    /**
     * 实时定位任务 应该放到service里运行
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
            trackApp.getCurrentLocation(entityListener, trackListener);
            realTimeHandler.postDelayed(this, interval * 1000);
        }
    }

}
