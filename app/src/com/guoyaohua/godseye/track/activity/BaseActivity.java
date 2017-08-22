package com.guoyaohua.godseye.track.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.utils.ViewUtil;

public abstract class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtil.initSystemBar(this, R.color.system_bar_bg);
        setContentView(getContentViewId());
    }

    /**
     * 获取布局文件ID
     *
     * @return 布局文件ID
     */
    protected abstract int getContentViewId();

    /**
     * 设置Activity标题
     */
    public void setTitle(int resId) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        TextView textView = (TextView) layout.findViewById(R.id.tv_activity_title);
        textView.setText(resId);
    }

    /**
     * 设置点击监听器
     *
     * @param listener
     */
    public void setOnClickListener(View.OnClickListener listener) {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        LinearLayout optionsButton = (LinearLayout) layout.findViewById(R.id.btn_activity_options);
        optionsButton.setOnClickListener(listener);
    }

    /**
     * 不显示设置按钮
     */
    public void setOptionsButtonInVisible() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_top);
        LinearLayout optionsButton = (LinearLayout) layout.findViewById(R.id.btn_activity_options);
        optionsButton.setVisibility(View.INVISIBLE);
    }

    /**
     * 回退事件
     *
     * @param v
     */
    public void onBack(View v) {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
