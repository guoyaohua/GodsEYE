package com.guoyaohua.godseye.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.guoyaohua.godseye.R;

import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//
//import com.baidu.mapapi.model.LatLng;
//import com.baidu.track.R;

/**
 * 通用工具类  先注释一下
 *
 * @author baidu
 */
public class CommonUtil {

    private static final String TAG = "BaiduTrack";

    private static DecimalFormat df = new DecimalFormat("######0.00");

    /**
     * 校验手机号码
     *
     * @param mobiles
     * @return
     */
    public static final boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(15[^4,\\D])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static final String formatDouble(double doubleValue) {
        return df.format(doubleValue);
    }

/*    *//**
     * 计算x方向每次移动的距离
     *//*
    public static double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return Constants.DISTANCE;
        }
        return Math.abs((Constants.DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    *//**
     * 根据点和斜率算取截距
     *//*
    public static double getInterception(double slope, LatLng point) {
        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    *//**
     * 算斜率
     *//*
    public static double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    *//**
     * 根据两点算取图标转的角度
     *//*
    public static double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }*/


    /**
     * 查询指定进程是否处于运行状态
     *
     * @param mContext
     * @param processName 进程名
     * @return boolean
     */
    public static boolean isProcessRunning(Context mContext, String processName) {
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcess : runningAppList) {
            if (appProcess.processName.equals(processName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 查询指定服务是否正在运行
     *
     * @param mContext
     * @param serviceName 服务名称
     * @return boolean
     */
    public static boolean isServiceRunning(Context mContext, String serviceName) {
        ActivityManager myAM = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServiceList = myAM.getRunningServices(80);
        for (ActivityManager.RunningServiceInfo runningService : runningServiceList) {
            String mName = runningService.service.getClassName().toString();
            if (mName.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static void toastShow(Activity activity, String message) {
        StringBuilder strBuilder = new StringBuilder("<font face='" + activity.getString(R.string.font_type) + "'>");
        strBuilder.append(message).append("</font>");

        View toastRoot = activity.getLayoutInflater().inflate(R.layout.self_toast, null);
        Toast toast = new Toast(activity);
        toast.setView(toastRoot);
        TextView tv = (TextView) toastRoot.findViewById(R.id.text_info);
        tv.setText(Html.fromHtml(strBuilder.toString()));
        toast.setGravity(Gravity.BOTTOM, 0, activity.getResources().getDisplayMetrics().heightPixels / 5);
        toast.show();

    }

}
