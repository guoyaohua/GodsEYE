package com.guoyaohua.godseye.track;

import android.annotation.TargetApi;
import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.LocRequest;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.api.track.LatestPointRequest;
import com.baidu.trace.api.track.OnTrackListener;
import com.baidu.trace.model.BaseRequest;
import com.baidu.trace.model.OnCustomAttributeListener;
import com.baidu.trace.model.ProcessOption;
import com.guoyaohua.godseye.MainActivity;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.model.ItemInfo;
import com.guoyaohua.godseye.track.utils.CommonUtil;
import com.guoyaohua.godseye.track.utils.NetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

//import com.guoyaohua.godseye.track.activity.FenceActivity;

/**
 * Created by baidu on 17/1/12.
 */

public class TrackApplication extends Application {

    public static int screenWidth = 0;
    public static int screenHeight = 0;
    public Context mContext = null;
    public List<ItemInfo> itemInfos = new ArrayList<>();
    public SharedPreferences trackConf = null;
    /**
     * 轨迹客户端
     */
    public LBSTraceClient mClient = null;
    /**
     * 轨迹服务
     */
    public Trace mTrace = null;
    /**
     * 轨迹服务ID
     */
    public long serviceId = 148652;
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
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    private LocRequest locRequest = null;
    //服务开启常驻通知条
    private Notification notification = null;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
        entityName = CommonUtil.getImei(this);

        // 若为创建独立进程，则不初始化成员变量
        if ("com.godseye.track:remote".equals(CommonUtil.getCurProcessName(mContext))) {
            return;
        }

        SDKInitializer.initialize(mContext);
//        initView();
        initNotification();
        mClient = new LBSTraceClient(mContext);
        mTrace = new Trace(serviceId, entityName);
        mTrace.setNotification(notification);

        trackConf = getSharedPreferences("track_conf", MODE_PRIVATE);

        locRequest = new LocRequest(serviceId);

        mClient.setOnCustomAttributeListener(new OnCustomAttributeListener() {
            @Override
            public Map<String, String> onTrackAttributeCallback() {
                Map<String, String> map = new HashMap<>();
                map.put("key1", "value1");
                map.put("key2", "value2");
                return map;
            }
        });

        clearTraceStatus();
    }

    /**
     * 获取当前位置
     */
    public void getCurrentLocation(OnEntityListener entityListener, OnTrackListener trackListener) {
        // 网络连接正常，开启服务及采集，则查询纠偏后实时位置；否则进行实时定位
        if (NetUtil.isNetworkAvailable(mContext)
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

    /*private void initView() {
        ItemInfo tracing = new ItemInfo(R.mipmap.icon_tracing, R.string.tracing_title, R.string.tracing_desc,
                TracingActivity.class);
        ItemInfo trackQuery = new ItemInfo(R.mipmap.icon_track_query, R.string.track_query_title,
                R.string.track_query_desc, TrackQueryActivity.class);
        ItemInfo fence = new ItemInfo(R.mipmap.icon_fence, R.string.fence_title,
                R.string.fence_desc, FenceActivity.class);
        ItemInfo bos = new ItemInfo(R.mipmap.icon_bos, R.string.bos_title, R.string.bos_desc, BosActivity.class);
        ItemInfo cacheManage = new ItemInfo(R.mipmap.icon_cache_manage,
                R.string.cache_manage_title, R.string.cache_manage_desc, CacheManageActivity.class);
        ItemInfo faq = new ItemInfo(R.mipmap.icon_fag, R.string.fag_title, R.string.faq_desc, FAQActivity.class);
        itemInfos.add(tracing);
        itemInfos.add(trackQuery);
        itemInfos.add(fence);
        itemInfos.add(bos);
        itemInfos.add(cacheManage);
        itemInfos.add(faq);

        getScreenSize();
    }*/

    /**
     * 初始化通知条
     */
    @TargetApi(16)
    private void initNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        Intent notificationIntent = new Intent(this, MainActivity.class);//点击通知后跳转到MainActivity

        Bitmap icon = BitmapFactory.decodeResource(this.getResources(),
                R.mipmap.icon_tracing);

        // 设置PendingIntent
        builder.setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, 0))
                .setLargeIcon(icon)  // 设置下拉列表中的图标(大图标)
                .setContentTitle("天眼追踪") // 设置下拉列表里的标题
                .setSmallIcon(R.mipmap.icon_tracing) // 设置状态栏内的小图标
                .setContentText("服务正在运行...") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间

        notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
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
     * 清除Trace状态：初始化app时，判断上次是正常停止服务还是强制杀死进程，根据trackConf中是否有is_trace_started字段进行判断。
     * <p>
     * 停止服务成功后，会将该字段清除；若未清除，表明为非正常停止服务。
     */
    private void clearTraceStatus() {
        if (trackConf.contains("is_trace_started") || trackConf.contains("is_gather_started")) {
            SharedPreferences.Editor editor = trackConf.edit();
            editor.remove("is_trace_started");
            editor.remove("is_gather_started");
            editor.apply();
        }
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
     * 获取请求标识
     *
     * @return
     */
    public int getTag() {
        return mSequenceGenerator.incrementAndGet();
    }

    public void clear() {
        itemInfos.clear();
    }

}
