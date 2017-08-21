package com.guoyaohua.godseye;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.guoyaohua.godseye.fragment.Fragment_page1;
import com.guoyaohua.godseye.fragment.Fragment_page2;
import com.guoyaohua.godseye.fragment.Fragment_page3;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ImageButton ib_page1;
    private ImageButton ib_page3;
    private ImageButton ib_page2;
    private Fragment_page1 fragment_page1;
    private Fragment_page2 fragment_page2;
    private Fragment_page3 fragment_page3;
    private FragmentManager manager;
    private FragmentTransaction transaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initWidgit();
        // 设置默认的Fragment
        setDefaultFragment();
    }

    @SuppressLint("NewApi")
    private void setDefaultFragment() {
        manager = getFragmentManager();
        transaction = manager.beginTransaction();

        fragment_page1 = new Fragment_page1();
        transaction.replace(R.id.fragment_container, fragment_page1);
        transaction.commit();
    }

    private void initWidgit() {
        ib_page1 = (ImageButton) findViewById(R.id.ib_page1);
        ib_page2 = (ImageButton) findViewById(R.id.ib_page2);
        ib_page3 = (ImageButton) findViewById(R.id.ib_page3);

        ib_page1.setOnClickListener(this);
        ib_page2.setOnClickListener(this);
        ib_page3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
//        manager = getFragmentManager();
        transaction = manager.beginTransaction();
        switch (v.getId()) {
            case R.id.ib_page1:
                if (fragment_page1 == null) {
                    fragment_page1 = new Fragment_page1();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page1);
                break;

            case R.id.ib_page2:
                if (fragment_page2 == null) {
                    fragment_page2 = new Fragment_page2();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page2);
                break;

            case R.id.ib_page3:
                if (fragment_page3 == null) {
                    fragment_page3 = new Fragment_page3();
                }
                // 使用当前Fragment的布局替代id_content的控件
                transaction.replace(R.id.fragment_container, fragment_page3);
                break;

        }
        transaction.commit();
    }
}
