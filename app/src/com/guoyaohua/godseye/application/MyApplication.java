package com.guoyaohua.godseye.application;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.fence.FenceAlarmPushInfo;
import com.baidu.trace.api.fence.MonitoredAction;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.ProcessOption;
import com.baidu.trace.model.PushMessage;
import com.baidu.trace.model.StatusCodes;
import com.guoyaohua.godseye.MainActivity;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.Constants;
import com.guoyaohua.godseye.track.utils.NetUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.model.UserInfo;


/**
 * Created by 郭耀华 on 2017/8/19.
 */

public class MyApplication extends Application {
    public static Context context;
    public static UserInfo myInfo;//当前登陆用户
    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public static List<UserInfo> userInfos = new ArrayList<UserInfo>();//监控对象列表
    public static List<String> entityNames;//带监控对象实例名，也是登陆名。
    /**
     * 轨迹服务ID
     */
    public static long serviceId = 148652;
    /**
     * 轨迹客户端
     */
    public static LBSTraceClient mClient = null;
    /**
     * 轨迹服务
     */
    public Trace mTrace = null;
    /**
     * Entity标识
     */
    public String entityName = "myTrace";
    public boolean isRegisterReceiver = false;
    /**
     * 服务是否开启标识
     */
    public boolean isTraceStarted = false;
    /**
     * 采集是否开启标识
     */
    public boolean isGatherStarted = false;

    public SharedPreferences trackConf = null;
    /**
     * 打包周期,可以把这些常量存到数据库或者sharepreferance里
     */
    public int packInterval = Constants.DEFAULT_PACK_INTERVAL;
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    private LocRequest locRequest = null;
    //服务开启常驻通知条
    private Notification notification = null;
    private NotificationManager notificationManager = null;
    /**
     * 地图工具
     */
//    private MapUtil mapUtil = null;
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
    private int notifyId = 0;//记录是第几条通知

    //    、*******************************************************************
    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        super.onCreate();
        //初始化极光推送sdk
        JMessageClient.init(context);
//        JMessageClient.init(context, true);//漫游同步，为啥不好使？
        // 若为创建独立进程，则不初始化成员变量
        if ("com.guoyaohua.godseye:remote".equals(CommonUtil.getCurProcessName(context))) {
            return;
        }
        //地图配置用到的
        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);

        SDKInitializer.initialize(context);
        initNotification();


        initListener();
        initUserInfosList();
    }

    /**
     * 初始化监控对象列表
     * 1.从数据库中查询待监控用户名
     * 2.新建一个子线程，遍历查询用户名信息
     * 3.将用户信息保存在userInfos中
     */
    private void initUserInfosList() {
        entityNames = new ArrayList<String>();
        entityNames.add("xiaoming");
        entityNames.add("xiaozhang");
        entityNames.add("xiaohehe");


        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < entityNames.size(); i++)
                    JMessageClient.getUserInfo(entityNames.get(i), new GetUserInfoCallback() {
                        @Override
                        public void gotResult(int i, String s, UserInfo userInfo) {
                            userInfos.add(userInfo);
                            Log.i("getUserInfo", userInfo.getUserName().toString() + "  " + userInfos.size());
                        }
                    });
            }
        }).start();

    }


    public void startBaiduService() {
        mClient = new LBSTraceClient(context);
        mTrace = new Trace(serviceId, entityName);
        mTrace.setNotification(notification);
        locRequest = new LocRequest(serviceId);
        mClient.startTrace(mTrace, traceListener);
    }


    /**
     * 获取当前位置
     */
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetUtil.isNetworkAvailable(context)
                && trackConf.contains("is_trace_started")
                && trackConf.contains("is_gather_started")
                && trackConf.getBoolean("is_trace_started", false)
                && trackConf.getBoolean("is_gather_started", false)) {
            LatestPointRequest request = new LatestPointRequest(getTag(), serviceId, entityName);
            ProcessOption processOption = new ProcessOption();
            processOption.setNeedDenoise(true);
            processOption.setRadiusThreshold(100);
            request.setProcessOption(processOption);
            mClient.queryLatestPoint(request, trackListener);
        } else {
            mClient.queryRealTimeLoc(locRequest, entityListener);
        }
    }

    /**
     * 初始化通知条
     */
    @TargetApi(16)
    private void initNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent notificationIntent = new Intent(this, MainActivity.class);//点击通知后跳转到MainActivity

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(), R.mipmap.icon_tracing);

        // 设置PendingIntent
        builder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .setLargeIcon(icon)  // 设置下拉列表中的图标(大图标)
                .setContentTitle("天眼追踪") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.icon_tracing) // 设置状态栏内的小图标
                .setContentText("服务正在运行...") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        notification = builder.build(); // 获取构建好的Notification
//        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
    }

    /**
     * 获取屏幕尺寸
     */
    private void getScreenSize() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        screenHeight = dm.heightPixels;
        screenWidth = dm.widthPixels;
    }

    /**
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * 初始化请求公共参数
     *
     * @param request
     */
    public void initRequest(BaseRequest request) {
        request.setTag(getTag());
        request.setServiceId(serviceId);
    }

    /**
     * 初始化鹰眼服务开启监听器
     */
    private void initListener() {

        traceListener = new OnTraceListener() {

            /**
             * 绑定服务回调接口
             *
             * @param errorNo 状态码
             * @param message 消息
             *                <p>
             *                <pre>0：成功 </pre>
             *                <pre>1：失败</pre>
             */
            @Override
            public void onBindServiceCallback(int errorNo, String message) {
//                com.guoyaohua.godseye.utils.CommonUtil.toastShow(getContext(),String.format("onBindServiceCallback, errorNo:%d, message:%s ", errorNo, message));

            }

            /**
             * 开启服务回调接口
             *
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
                    isTraceStarted = true;
                    Log.i("serviceInit", String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
                    mClient.startGather(traceListener);//如果服务开启成功，则开启采集
                }
//                viewUtil.showToast(TracingActivity.this,
//                        String.format("onStartTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止服务回调接口
             *
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
                    isTraceStarted = false;
                    isGatherStarted = false;
//                    // 停止成功后，直接移除is_trace_started记录（便于区分用户没有停止服务，直接杀死进程的情况）
//                    SharedPreferences.Editor editor = trackApp.trackConf.edit();
//                    editor.remove("is_trace_started");
//                    editor.remove("is_gather_started");
//                    editor.apply();
//                    setTraceBtnStyle();
//                    setGatherBtnStyle();
//                    unregisterPowerReceiver();
//                    //如果手动停止了服务，则关闭采集
//                    mClient.stopGather(traceListener);
                }
//                viewUtil.showToast(TracingActivity.this,
//                        String.format("onStopTraceCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 开启采集回调接口
             *
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
                    isGatherStarted = true;
                    Log.i("serviceInit", "服务已开启");
                    Log.i("serviceInit", String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
//                    SharedPreferences.Editor editor = trackConf.edit();
//                    editor.putBoolean("is_gather_started", true);
//                    editor.apply();
//                    setGatherBtnStyle();
                }
//                viewUtil.showToast(TracingActivity.this,
//                        String.format("onStartGatherCallback, errorNo:%d, message:%s ", errorNo, message));
            }

            /**
             * 停止采集回调接口
             *
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
                    isGatherStarted = false;
                }
            }

            /**
             * 推送消息回调接口
             *
             * @param messageType 状态码
             * @param pushMessage 消息
             *                    <p>
             *                    <pre>0x01：配置下发</pre>
             *                    <pre>0x02：语音消息</pre>
             *                    <pre>0x03：服务端围栏报警消息</pre>
             *                    <pre>0x04：本地围栏报警消息</pre>
             *                    <pre>0x05~0x40：系统预留</pre>
             *                    <pre>0x41~0xFF：开发者自定义</pre>
             */
            @Override
            public void onPushCallback(byte messageType, PushMessage pushMessage) {
                if (messageType < 0x03 || messageType > 0x04) {

                    return;
                }
                FenceAlarmPushInfo alarmPushInfo = pushMessage.getFenceAlarmPushInfo();
                if (null == alarmPushInfo) {
//                    viewUtil.showToast(TracingActivity.this,
//                            String.format("onPushCallback, messageType:%d, messageContent:%s ", messageType,
//                                    pushMessage));
                    return;
                }
                StringBuffer alarmInfo = new StringBuffer();
                alarmInfo.append("您于")
                        .append(CommonUtil.getHMS(alarmPushInfo.getCurrentPoint().getLocTime() * 1000))
                        .append(alarmPushInfo.getMonitoredAction() == MonitoredAction.enter ? "进入" : "离开")
                        .append(messageType == 0x03 ? "云端" : "本地")
                        .append("围栏：").append(alarmPushInfo.getFenceName());

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
                    Notification notification = new Notification.Builder(getContext())
                            .setContentTitle(getResources().getString(R.string.alarm_push_title))
                            .setContentText(alarmInfo.toString())
                            .setSmallIcon(R.mipmap.icon_app)
                            .setWhen(System.currentTimeMillis()).build();
                    notificationManager.notify(notifyId++, notification);
                }
            }
        };

    }
}
