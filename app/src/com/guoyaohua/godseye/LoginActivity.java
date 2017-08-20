package com.guoyaohua.godseye;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import com.guoyaohua.godseye.application.MyApplication;
import com.guoyaohua.godseye.utils.CommonUtil;
import com.guoyaohua.godseye.utils.DialogCreator;
import com.guoyaohua.godseye.utils.HandleResponseCode;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnFocusChangeListener {
    private Button bt_login;
    private Button bt_to_sign;
    private EditText et_UserName;
    private EditText et_PSW;
    private CheckBox cb_savePSW;
    private CheckBox cb_autoLogin;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Button bt_pwd_eye;
    private Button bt_username_clear;
    private boolean showPSW = false;
    private boolean autoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initWigit();
        initData();
        if (autoLogin) {//自动登录
            logIn();
        }
    }

    /**
     * 初始化保存的数据
     */
    private void initData() {
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String psw = pref.getString("psw", "");
        String userName = pref.getString("username", "");
        boolean savePSW = pref.getBoolean("savePSW", true);
        autoLogin = pref.getBoolean("autoLogin", false);
        et_UserName.setText(userName);
        if (savePSW) {
            et_PSW.setText(psw);
        }
    }

    /**
     * 初始化控件
     */
    private void initWigit() {
        bt_login = (Button) findViewById(R.id.bt_login);
        bt_to_sign = (Button) findViewById(R.id.bt_sign_in);
        et_UserName = (EditText) findViewById(R.id.et_login_username);
        et_PSW = (EditText) findViewById(R.id.et_login_password);
        cb_savePSW = (CheckBox) findViewById(R.id.cb_save_psw);
        cb_autoLogin = (CheckBox) findViewById(R.id.cb_auto_login);
        bt_login.setOnClickListener(this);
        bt_to_sign.setOnClickListener(this);
        ;
        bt_pwd_eye = (Button) findViewById(R.id.bt_pwd_eye_login);
        bt_username_clear = (Button) findViewById(R.id.bt_username_clear_login);
        bt_username_clear.setOnClickListener(this);
        bt_pwd_eye.setOnClickListener(this);

        et_UserName.setOnFocusChangeListener(this);
        et_PSW.setOnFocusChangeListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login:
                logIn();
                break;
            case R.id.bt_sign_in:
                goToSign();
                break;
            case R.id.bt_username_clear_login:
                et_UserName.setText("");
                break;
            case R.id.bt_pwd_eye_login:
                if (!showPSW) {
                    et_PSW.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    showPSW = true;
                } else {
                    et_PSW.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    showPSW = false;
                }
                break;
        }
    }

    /**
     * 跳转到注册界面
     */
    private void goToSign() {
        Intent intent = new Intent(this, SignInActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    if (data.getBooleanExtra("isLogIn", false)) {
                        finish();//关闭
                    }
                }
                break;
        }
    }

    /**
     * 直接登录
     */
    private void logIn() {
        if (checkForm()) {
            final Dialog loadingDialog = DialogCreator.createLoadingDialog(this, "正在登录");
            loadingDialog.show();
            JMessageClient.login(et_UserName.getText().toString(), et_PSW.getText().toString(), new BasicCallback() {
                @Override
                public void gotResult(int status, String desc) {
                    if (status == 0) {
                        //获取当前用户的信息;
                        MyApplication.myInfo = JMessageClient.getMyInfo();
                        //登录成功
                        editor = pref.edit();
                        editor.putBoolean("savePSW", cb_savePSW.isChecked());
                        editor.putBoolean("autoLogin", cb_autoLogin.isChecked());
                        editor.putString("username", et_UserName.getText().toString());
                        if (cb_savePSW.isChecked()) {
                            editor.putString("psw", et_PSW.getText().toString());
                        }
                        editor.apply();
                        loadingDialog.dismiss();
                        //跳转到主界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        //登陆失败
                        loadingDialog.dismiss();
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
    private boolean checkForm() {
        if (et_UserName.getText().toString().equals("")) {
            CommonUtil.toastShow(this, "请输入用户名");
            return false;
        } else if (et_PSW.getText().toString().equals("")) {
            CommonUtil.toastShow(this, "请输入密码");
            return false;
        }
        return true;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_login_username:
                if (hasFocus) {
                    bt_username_clear.setVisibility(View.VISIBLE);
                } else {
                    bt_username_clear.setVisibility(View.GONE);
                }
                break;
            case R.id.et_login_password:
                if (hasFocus) {
                    bt_pwd_eye.setVisibility(View.VISIBLE);
                } else {
                    bt_pwd_eye.setVisibility(View.GONE);
                }
                break;
        }
    }
}
