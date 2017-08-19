package com.guoyaohua.godseye;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;

/**
 * Created by 郭耀华 on 2017/8/19.
 */

public class ChangeFaceImageDialog extends PopupWindow {

    private ImageButton bt_pickPicture = null;
    private ImageButton bt_takePhoto = null;

    private Button bt_cancel = null;

    private View mMenuView = null;

    @SuppressLint({"InflateParams", "ClickableViewAccessibility"})
    public ChangeFaceImageDialog(final Activity context, View.OnClickListener logoutListener) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.dialog_change_face_layout, null);
        bt_pickPicture = (ImageButton) mMenuView.findViewById(R.id.bt_pickPicture);
        bt_takePhoto = (ImageButton) mMenuView.findViewById(R.id.bt_takePhoto);
        bt_cancel = (Button) mMenuView.findViewById(R.id.bt_cancel);

        bt_cancel.setOnClickListener(logoutListener);
        bt_pickPicture.setOnClickListener(logoutListener);
        bt_takePhoto.setOnClickListener(logoutListener);

        this.setContentView(mMenuView);
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setFocusable(true);
        this.setAnimationStyle(R.style.change_face_dialog_anim_style);
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setBackgroundDrawable(dw);
        mMenuView.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.changeFace_pop_layout).getTop();//获得顶部位置
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}
