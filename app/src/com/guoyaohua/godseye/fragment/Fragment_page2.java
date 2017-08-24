package com.guoyaohua.godseye.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.baidu.trace.api.entity.EntityInfo;
import com.baidu.trace.api.entity.EntityListRequest;
import com.baidu.trace.api.entity.EntityListResponse;
import com.baidu.trace.api.entity.FilterCondition;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.model.CoordType;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.RecycleViewDivider;
import com.guoyaohua.godseye.UserAdapter;
import com.guoyaohua.godseye.application.MyApplication;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Fragment_page2 extends Fragment {

    UserAdapter adapter;
    RecyclerView recyclerView;
    //    private List<UserInfo> userInfos;
    private MyApplication myApplication;
    private List<EntityInfo> entityInfos;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        myApplication = (MyApplication) getActivity().getApplication();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_page2, container, false);

//        userInfos = MyApplication.userInfos;
        inituserInfos();
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_UserList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(MyApplication.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new RecycleViewDivider(MyApplication.getContext(), LinearLayoutManager.HORIZONTAL));
        recyclerView.addItemDecoration(new RecycleViewDivider(
                MyApplication.getContext(), LinearLayoutManager.HORIZONTAL, R.drawable.divider_mileage));
//        recyclerView.addItemDecoration(new RecycleViewDivider(
//                mContext, LinearLayoutManager.VERTICAL, 10, getResources().getColor(R.color.divide_gray_color)));


        adapter = new UserAdapter(MyApplication.userInfos);
        recyclerView.setAdapter(adapter);


        return view;
    }

    private void inituserInfos() {
// 请求标识
        int tag = 5;
//// 轨迹服务ID
//        long serviceId = 0;
//设置活跃时间
//        long activeTime = System.currentTimeMillis() / 1000 - 5 * 60;
// 过滤条件
        FilterCondition filterCondition = new FilterCondition();
// 查找当前时间5分钟之内有定位信息上传的entity
//        filterCondition.setActiveTime(activeTime);
        filterCondition.setEntityNames(myApplication.entityNames);
// 返回结果坐标类型
        CoordType coordTypeOutput = CoordType.bd09ll;
// 分页索引
        int pageIndex = 1;
// 分页大小
        int pageSize = 100;


// 创建Entity列表请求实例
        EntityListRequest request = new EntityListRequest(tag, MyApplication.serviceId, filterCondition, coordTypeOutput, pageIndex, pageSize);


// 初始化监听器
        OnEntityListener entityListener = new OnEntityListener() {
            @Override
            public void onEntityListCallback(EntityListResponse response) {
                entityInfos = response.getEntities();
//                //先将所有用户置为离线状态
//                for (int i = 0; i < MyApplication.userInfos.size(); i++) {
//                    MyApplication.userInfos.get(i).setNotename("离线");
//                }
                if (entityInfos != null) {
                    for (int i = 0; i < entityInfos.size(); i++) {
                        for (int j = 0; j < MyApplication.userInfos.size(); j++) {
                            if (entityInfos.get(i).getEntityName().toString().equals(MyApplication.userInfos.get(j).getUserName().toString())) {

                                DecimalFormat df = new DecimalFormat("###.000");
                                MyApplication.userInfos.get(j).setAddress("经度：" + df.format(entityInfos.get(i).getLatestLocation().getLocation().getLatitude()) + "  纬度：" + df.format(entityInfos.get(i).getLatestLocation().getLocation().getLongitude()));
                                Long locTime = entityInfos.get(i).getLatestLocation().getLocTime();//获取最新定位时间
                                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//前面的lSysTime是秒数，先乘1000得到毫秒数，再转为java.util.Date类型
                                java.util.Date dt = new Date(locTime * 1000);
                                String sDateTime = sdf.format(dt);  //得到精确到秒的表示：08/31/2006 21:08:00
//                                System.out.println(sDateTime);
                                if (System.currentTimeMillis() / 1000 - locTime > 5 * 60) {
                                    MyApplication.userInfos.get(i).setNotename("离线");
                                } else {
                                    MyApplication.userInfos.get(i).setNotename("在线");
                                }

                                MyApplication.userInfos.get(j).setNoteText("时间：" + sDateTime);

                            }
                        }
                    }
                }
                adapter.userInfos = MyApplication.userInfos;
                adapter.notifyDataSetChanged();

            }
        };


// 查询Entity列表
        MyApplication.mClient.queryEntityList(request, entityListener);


    }

}
