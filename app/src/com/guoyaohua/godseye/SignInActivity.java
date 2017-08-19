package com.guoyaohua.godseye;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignInActivity extends AppCompatActivity implements View.OnFocusChangeListener, View.OnClickListener {
    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
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

    private void submitSign() {

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

                }
                break;
            default:
                break;
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
