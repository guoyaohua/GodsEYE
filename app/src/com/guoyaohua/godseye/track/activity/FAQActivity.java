package com.guoyaohua.godseye.track.activity;

import android.os.Bundle;

import com.guoyaohua.godseye.R;

public class FAQActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.fag_title);
        setOptionsButtonInVisible();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_faq;
    }
}
