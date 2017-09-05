package com.guoyaohua.godseye;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.chat.ChatActivity;

import java.util.List;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * Created by John Kwok on 2017/8/24.
 */

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private static final String TARGET_ID = "targetId";
    private static final String TARGET_APP_KEY = "targetAppKey";
    public List<UserInfo> userInfos;

    public UserAdapter(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    @Override
    public UserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(UserAdapter.ViewHolder holder, final int position) {
//初始化UI
        UserInfo userInfo = userInfos.get(position);
        Glide.with(MyApplication.getContext()).load(userInfo.getAvatarFile()).into(holder.iv_FaceImage_item);
        holder.tv_GPS_item.setText(userInfo.getAddress().toString());
        holder.loc_item.setText(userInfo.getNoteText().toString());
        holder.tv_NickName_item.setText(userInfo.getNickname().toString());
        if (userInfo.getNotename().toString().equals("在线")) {
            holder.tv_state_item.setText("在线");
            Glide.with(MyApplication.getContext()).load(userInfo.getAvatarFile()).into(holder.iv_state_item);
        } else {
            holder.tv_state_item.setText("离线");
            Glide.with(MyApplication.getContext()).load(R.drawable.bt_refresh).into(holder.iv_state_item);
        }
        holder.InfoLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MyApplication.getContext(), RealLocationActivity.class);
                intent.putExtra("userName", userInfos.get(position).getUserName().toString());
                intent.putExtra("nickName", userInfos.get(position).getNickname().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(intent);
            }
        });

        holder.iv_FaceImage_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MyApplication.getContext(), RealLocationActivity.class);
                intent.putExtra("userName", userInfos.get(position).getUserName().toString());
                intent.putExtra("nickName", userInfos.get(position).getNickname().toString());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MyApplication.getContext().startActivity(intent);
            }
        });
        holder.ib_SentMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    Toast.makeText(MyApplication.getContext(), "2", Toast.LENGTH_SHORT).show();
                final Intent intent = new Intent();
                if (JMessageClient.getMyInfo() != null) {
                    intent.putExtra(TARGET_ID, userInfos.get(position).getUserName().toString());
                    intent.putExtra(TARGET_APP_KEY, JMessageClient.getMyInfo().getAppKey());

                    intent.setClass(MyApplication.getContext(), ChatActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(intent);
                } else {
                    Toast.makeText(MyApplication.getContext(), "用户掉线，请重新登陆", Toast.LENGTH_SHORT).show();
                       /* final Dialog dialog = DialogCreator.createLoadingDialog(MyApplication.getContext(),
                                MyApplication.getContext().getString(R.string.jmui_logging));
                        dialog.show();
                        JMessageClient.login(mMyName, mMyPassword, new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                dialog.dismiss();
                                if (status == 0) {
                                    intent.putExtra(TARGET_ID, MyApplication.myInfo.getUserName());
                                    intent.setClass(MyApplication.getContext(), ChatActivity.class);
                                    MyApplication.getContext().startActivity(intent);
                                } else {
                                    HandleResponseCode.onHandle(MyApplication.getContext(), status, false);
                                }
                            }
                        });*/
                }


            }
        });
    }

    @Override
    public int getItemCount() {
        return userInfos.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView iv_FaceImage_item;
        ImageView iv_state_item;
        TextView tv_NickName_item;
        TextView tv_state_item;
        TextView tv_GPS_item;
        ImageButton ib_SentMsg;
        TextView loc_item;

        View InfoLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            InfoLayout = itemView.findViewById(R.id.info_item_layout);
            iv_FaceImage_item = (ImageView) itemView.findViewById(R.id.iv_FaceImage_item);
            iv_state_item = (ImageView) itemView.findViewById(R.id.iv_state_item);
            ib_SentMsg = (ImageButton) itemView.findViewById(R.id.ib_SentMsg);
            tv_NickName_item = (TextView) itemView.findViewById(R.id.tv_NickName_item);
            tv_state_item = (TextView) itemView.findViewById(R.id.tv_state_item);
            tv_GPS_item = (TextView) itemView.findViewById(R.id.tv_GPS_item);
            loc_item = (TextView) itemView.findViewById(R.id.loc_item);


        }
    }
}
