package com.guoyaohua.godseye.track.dialog;

import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.TextView;
//import com.guoyaohua.godseye.track.activity.FenceActivity;

/**
 * 服务控制对话框
 *
 * @author baidu
 */
public class FenceOperateDialog extends PopupWindow {

    private View mView = null;
    private Button alarmBtn = null;
    private Button updateBtn = null;
    private Button deleteBtn = null;
    private Button cancelBtn = null;
    private TextView titleText = null;

   /* @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public FenceOperateDialog(final FenceActivity parent) {
        super(parent);
        LayoutInflater inflater = (LayoutInflater) parent
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = inflater.inflate(R.layout.dialog_fence_operate, null);
        alarmBtn = (Button) mView.findViewById(R.id.btn_fenceOperate_alarm);
        updateBtn = (Button) mView.findViewById(R.id.btn_fenceOperate_update);
        deleteBtn = (Button) mView.findViewById(R.id.btn_fenceOperate_delete);
        cancelBtn = (Button) mView.findViewById(R.id.btn_all_cancel);
        titleText = (TextView) mView.findViewById(R.id.tv_dialog_title);
        alarmBtn.setOnClickListener(parent);
        updateBtn.setOnClickListener(parent);
        deleteBtn.setOnClickListener(parent);
        titleText.setText(R.string.all_fence_operate);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        setContentView(mView);
        setFocusable(false);
        setOutsideTouchable(false);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.dialog_anim_style);
        setBackgroundDrawable(null);

        // 点击弹框外部，弹框消失
        //        ColorDrawable dw = new ColorDrawable(0x00000000);
        //        setBackgroundDrawable(dw);
        //        mView.setOnTouchListener(new OnTouchListener() {
        //
        //            public boolean onTouch(View v, MotionEvent event) {
        //                int height = mView.findViewById(R.id.layout_fence_operate).getTop();
        //                int y = (int) event.getY();
        //                if (event.getAction() == MotionEvent.ACTION_UP) {
        //                    if (y < height) {
        //                        dismiss();
        //                    }
        //                }
        //                return true;
        //            }
        //        });

    }*/

}
