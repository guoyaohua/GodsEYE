package com.guoyaohua.godseye.track.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;

/**
 * 轨迹分析详情对话框布局
 *
 * @author baidu
 */
public class TrackAnalysisInfoLayout extends LinearLayout {

    public TextView titleText = null;
    public TextView key1 = null;
    public TextView key2 = null;
    public TextView key3 = null;
    public TextView key4 = null;
    public TextView value1 = null;
    public TextView value2 = null;
    public TextView value3 = null;
    public TextView value4 = null;

    public View mView = null;

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public TrackAnalysisInfoLayout(final TrackQueryActivity parent, final BaiduMap baiduMap) {
        super(parent);
        LayoutInflater inflater = (LayoutInflater) parent
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_track_analysis_info, null);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        key1 = (TextView) mView.findViewById(R.id.info_key_1);
        key2 = (TextView) mView.findViewById(R.id.info_key_2);
        key3 = (TextView) mView.findViewById(R.id.info_key_3);
        key4 = (TextView) mView.findViewById(R.id.info_key_4);
        value1 = (TextView) mView.findViewById(R.id.info_value_1);
        value2 = (TextView) mView.findViewById(R.id.info_value_2);
        value3 = (TextView) mView.findViewById(R.id.info_value_3);
        value4 = (TextView) mView.findViewById(R.id.info_value_4);

        setFocusable(true);
        mView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                baiduMap.hideInfoWindow();
            }
        });
    }

}
