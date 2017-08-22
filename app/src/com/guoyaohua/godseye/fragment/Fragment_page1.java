package com.guoyaohua.godseye.fragment;


import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.track.activity.TrackQueryActivity;


public class Fragment_page1 extends Fragment implements View.OnClickListener {
    private Button bt_showTrace;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_page1, container, false);

//        http://www.cnblogs.com/Gaojiecai/p/4084252.html

        initWidgit(view);
        return view;

    }

    private void initWidgit(View view) {
        bt_showTrace = (Button) view.findViewById(R.id.bt_showTrace);
        bt_showTrace.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_showTrace:
                Intent intent = new Intent(getActivity(), TrackQueryActivity.class);
                intent.putExtra("EntityName", MyApplication.myInfo.getUserName());
                startActivity(intent);
                break;
        }
    }
}
