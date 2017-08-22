package com.guoyaohua.godseye.track.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;

/**
 * 轨迹分析对话框
 *
 * @author baidu
 */
public class TrackAnalysisDialog extends PopupWindow {

    private CheckBox speedingCBx = null;
    private CheckBox harshBreakingCBx = null;
    private CheckBox harshAccelCBx = null;
    private CheckBox stayPointCBx = null;
    private TextView titleText = null;
    private Button cancelBtn = null;

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public TrackAnalysisDialog(final TrackQueryActivity parent) {
        super(parent);
        LayoutInflater inflater = (LayoutInflater) parent
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mView = inflater.inflate(R.layout.dialog_track_analysis, null);
        speedingCBx = (CheckBox) mView.findViewById(R.id.chk_speeding);
        harshBreakingCBx = (CheckBox) mView.findViewById(R.id.chk_harsh_breaking);
        harshAccelCBx = (CheckBox) mView.findViewById(R.id.chk_harsh_accel);
        stayPointCBx = (CheckBox) mView.findViewById(R.id.chk_stay_point);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        cancelBtn = (Button) mView.findViewById(R.id.btn_all_cancel);

        speedingCBx.setOnCheckedChangeListener(parent);
        harshBreakingCBx.setOnCheckedChangeListener(parent);
        harshAccelCBx.setOnCheckedChangeListener(parent);
        stayPointCBx.setOnCheckedChangeListener(parent);
        titleText.setText(R.string.track_analysis_title);

        setContentView(mView);
        setTouchable(true);
        setFocusable(false);
        setOutsideTouchable(false);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.dialog_anim_style);
        setBackgroundDrawable(null);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

}
