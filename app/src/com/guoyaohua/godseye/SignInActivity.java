package com.guoyaohua.godseye;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.utils.CommonUtil;
import com.guoyaohua.godseye.utils.DialogCreator;
import com.guoyaohua.godseye.utils.HandleResponseCode;

import java.io.File;
import java.lang.ref.WeakReference;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;
import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    private static final int REGISTER = 200;
    private final SignInActivity.MyHandler myHandler = new SignInActivity.MyHandler(this);
    ChangeFaceImageDialog changeFaceImageDialog;
    private CircleImageView faveImage;
    private Button sign;
    private Button bt_username_clear;
    private Button bt_nickname_clear;
    //    private Button bt_pwd_clear;
//    private Button bt_pwd_clear2;
    private Button bt_pwd_eye;
    private Button bt_pwd_eye2;
    private EditText et_UserName;
    private EditText et_NickName;
    private EditText et_password;
    private EditText et_password2;
    private boolean showPsw = false;
    private ChangeFaceImageHelper changeFaceImageHelper;
    private File faceImageFile;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        changeFaceImageHelper = new ChangeFaceImageHelper(SignInActivity.this);
        initWidgit();

    }

    /**
     * 初始化UI组件
     */
    private void initWidgit() {
        faveImage = (CircleImageView) findViewById(R.id.face_image);
        sign = (Button) findViewById(R.id.bt_sign);
        bt_username_clear = (Button) findViewById(R.id.bt_username_clear);
        bt_nickname_clear = (Button) findViewById(R.id.bt_nickname_clear);
//        bt_pwd_clear = (Button) findViewById(R.id.bt_pwd_clear);
//        bt_pwd_clear2 = (Button) findViewById(R.id.bt_pwd_clear2);
        bt_pwd_eye = (Button) findViewById(R.id.bt_pwd_eye);
        bt_pwd_eye2 = (Button) findViewById(R.id.bt_pwd_eye2);
        et_UserName = (EditText) findViewById(R.id.et_UserName);
        et_NickName = (EditText) findViewById(R.id.et_NickName);
        et_password = (EditText) findViewById(R.id.et_password);
        et_password2 = (EditText) findViewById(R.id.et_password2);
        //添加事件监听器
        et_NickName.setOnFocusChangeListener(this);
        et_UserName.setOnFocusChangeListener(this);
        et_password.setOnFocusChangeListener(this);
        et_password2.setOnFocusChangeListener(this);
        bt_username_clear.setOnClickListener(this);
        bt_nickname_clear.setOnClickListener(this);
        bt_pwd_eye.setOnClickListener(this);
        bt_pwd_eye2.setOnClickListener(this);
        sign.setOnClickListener(this);
        //更换头像
        faveImage.setOnClickListener(this);
    }


    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_NickName:
                if (hasFocus) {
                    bt_nickname_clear.setVisibility(View.VISIBLE);
                } else {
                    bt_nickname_clear.setVisibility(View.GONE);
                }
                break;
            case R.id.et_UserName:
                if (hasFocus) {
                    bt_username_clear.setVisibility(View.VISIBLE);
                } else {
                    bt_username_clear.setVisibility(View.GONE);
                }

                break;
            case R.id.et_password:
                if (hasFocus) {
                    bt_pwd_eye.setVisibility(View.VISIBLE);
                } else {
                    bt_pwd_eye.setVisibility(View.GONE);
                }
                break;
            case R.id.et_password2:
                if (hasFocus) {
                    bt_pwd_eye2.setVisibility(View.VISIBLE);
                } else {
                    bt_pwd_eye2.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_username_clear:
                et_UserName.setText("");
                break;
            case R.id.bt_nickname_clear:
                et_NickName.setText("");
                break;
            case R.id.bt_pwd_eye:
            case R.id.bt_pwd_eye2:
                if (!showPsw) {
                    et_password.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    et_password2.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    et_password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    et_password2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                break;
            case R.id.face_image:
                changeFaceImage();
                break;
            case R.id.bt_sign:
                submitSign();
        }


    }

    /**
     * 注册
     */
    private void submitSign() {
        if (checkInput()) {
            final Dialog dialog = DialogCreator.createLoadingDialog(this, this.getString(R.string.jmui_registering));
            dialog.show();
            JMessageClient.register(et_UserName.getText().toString(), et_password.getText().toString(), new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    dialog.dismiss();
                    if (status == 0) {//注册成功
                        myHandler.sendEmptyMessage(REGISTER);
//                        Toast.makeText(mContext, getString(R.string.jmui_username) + " " + mMyName
//                                + getString(R.string.jmui_register_success), Toast.LENGTH_SHORT).show();
                    } else {//注册失败
                        HandleResponseCode.onHandle(MyApplication.getContext(), status, false);
                    }
                }
            });
        }

    }

    /**
     * 检查输入是否合法
     *
     * @return
     */
    private boolean checkInput() {
        if (et_UserName.getText().toString().equals("")) {
            CommonUtil.toastShow(this, "请输入用户名");
            return false;
        } else if (et_NickName.getText().toString().equals("")) {
            CommonUtil.toastShow(this, "请输入昵称");
            return false;
        } else if (!et_password.getText().toString().equals(et_password2.getText().toString())) {
            CommonUtil.toastShow(this, "两次密码输入不相同");
            return false;
        } else if (et_password.getText().toString().equals("") && et_password2.getText().toString().equals("")) {
            CommonUtil.toastShow(this, "请输入密码");
            return false;
        }
        return true;
    }

    /**
     * 显示更换头像POPWINDOW
     */
    private void changeFaceImage() {
        // ChangeFaceImageDialog
        changeFaceImageDialog =
                new ChangeFaceImageDialog(this, new ChangeFaceOnClickListener());
        // 显示窗口
        changeFaceImageDialog.showAtLocation(this.findViewById(R.id.sign_in_ParentLayout), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeFaceImageHelper.openAlbum();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        // 将拍摄的照片显示出来
//                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(changeFaceImageHelper.imageUri));
                        faceImageFile = new File(getExternalCacheDir(), "face_Image.jpg");
                        Glide.with(this).load(changeFaceImageHelper.imageUri).into(faveImage);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        changeFaceImageHelper.handleImageOnKitKat(data);
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        changeFaceImageHelper.handleImageBeforeKitKat(data);
                    }
                    changeFaceImageHelper.displayImage(changeFaceImageHelper.imagePath, faveImage);
                    faceImageFile = new File(changeFaceImageHelper.imagePath);
                }
                break;
            default:
                break;
        }
    }

    /**
     * 登陆
     */
    public void login() {
        final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, "注册成功，正在登录...");
        loadingDialog.show();
        JMessageClient.login(et_UserName.getText().toString(), et_password.getText().toString(), new BasicCallback() {
            @Override
            public void gotResult(int status, String desc) {
//                loadingDialog.dismiss();
                if (status == 0) {//登录成功
                    //登录成功
                    pref = PreferenceManager.getDefaultSharedPreferences(SignInActivity.this);
                    editor = pref.edit();
                    editor.putString("username", et_UserName.getText().toString());
                    editor.putString("psw", et_password.getText().toString());
                    editor.apply();
                    //更新nickname，deviceID，头像
                    final UserInfo myInfo = JMessageClient.getMyInfo();
                    if (myInfo != null) {
                        /**=================     1.更新nickName    =================*/
                        myInfo.setNickname(et_NickName.getText().toString());
                        JMessageClient.updateMyInfo(UserInfo.Field.nickname, myInfo, new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                if (i == 0) {
                                    //更新NickName 成功
                                } else {
                                    //更新失败
                                }
                            }
                        });
                        /**=================    2.更新Device_ID    =================*/

                       /* myInfo.setSignature(MyApplication.DEVICE_ID);
                        JMessageClient.updateMyInfo(UserInfo.Field.signature, myInfo, new BasicCallback() {
                            @Override
                            public void gotResult(int i, String s) {
                                if (i == 0) {
                                    //更新成功
                                } else {
                                    //更新失败
                                }
                            }
                        });*/
                        /**=================  3.更新头像   =================*/
                        if (faceImageFile != null) {
                            try {
                                JMessageClient.updateUserAvatar(faceImageFile, new BasicCallback() {
                                    @Override
                                    public void gotResult(int i, String s) {
                                        if (i == 0) {
                                            loadingDialog.dismiss();
                                            Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
                                            //登陆成功后，向loginactivity返回结果
                                            Intent intent = new Intent();
                                            intent.putExtra("isLogIn", true);
                                            setResult(RESULT_OK, intent);
                                            //获取当前用户的信息;
                                            MyApplication.myInfo = JMessageClient.getMyInfo();
                                            //跳转到主activity
                                            intent = new Intent(SignInActivity.this, MainActivity.class);
                                            startActivity(intent);
                                            finish();

                                        } else {

                                            Toast.makeText(getApplicationContext(), "修改失败", Toast.LENGTH_SHORT).show();
                                            Log.i("UpdateUserAvatar", "JMessageClient.updateUserAvatar" + ", responseCode = " + i + " ; LoginDesc = " + s);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            loadingDialog.dismiss();
                            //登陆成功后，向loginactivity返回结果
                            Intent intent = new Intent();
                            intent.putExtra("isLogIn", true);
                            setResult(RESULT_OK, intent);
                            //获取当前用户的信息;
                            MyApplication.myInfo = JMessageClient.getMyInfo();
                            //跳转到主activity
                            intent = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
/*//启动百度服务
                        MyApplication myApplication = (MyApplication) getApplication();
                        myApplication.entityName = MyApplication.myInfo.getUserName();
                        myApplication.startBaiduService();*/
//应该关闭登陆界面
                    } else {//登录失败
                        HandleResponseCode.onHandle(MyApplication.getContext(), status, false);
                    }
                }
            }
        });
    }

    private static class MyHandler extends Handler {

        private WeakReference<SignInActivity> mActivity;

        public MyHandler(SignInActivity activity) {
            mActivity = new WeakReference<SignInActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            SignInActivity signInActivity = mActivity.get();
            if (signInActivity != null) {
                switch (msg.what) {
                    case REGISTER:
                        signInActivity.login();

                        break;
                }
            }
        }
    }

    /**
     * 头像点击监听器
     */
    class ChangeFaceOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.bt_takePhoto) {
                changeFaceImageHelper.takePhoto();
                changeFaceImageDialog.dismiss();
            } else if (v.getId() == R.id.bt_pickPicture) {
                changeFaceImageHelper.pickPicture();
                changeFaceImageDialog.dismiss();
            } else if (v.getId() == R.id.bt_cancel) {
                // 销毁弹出框
                changeFaceImageDialog.dismiss();
            }
        }
    }
}
