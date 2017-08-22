package com.guoyaohua.godseye.track.activity;

import android.Manifest;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.baidu.trace.api.bos.BosGeneratePresignedUrlRequest;
import com.baidu.trace.api.bos.BosGeneratePresignedUrlResponse;
import com.baidu.trace.api.bos.BosGetObjectRequest;
import com.baidu.trace.api.bos.BosGetObjectResponse;
import com.baidu.trace.api.bos.BosObjectType;
import com.baidu.trace.api.bos.BosPutObjectRequest;
import com.baidu.trace.api.bos.BosPutObjectResponse;
import com.baidu.trace.api.bos.FontFamily;
import com.baidu.trace.api.bos.ImageProcessCommand;
import com.baidu.trace.api.bos.OnBosListener;
import com.baidu.trace.api.bos.TextWatermarkCommand;
import com.baidu.trace.model.StatusCodes;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.TrackApplication;
import com.guoyaohua.godseye.track.utils.ViewUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * 图片服务（对象存储）
 */
public class BosActivity extends BaseActivity {

    private static final int IMAGE_CODE = 0;
    private static final int GRANTED = 1;
    private static final String IMAGE_TYPE = "image/*";

    private TrackApplication trackApp = null;
    private OnBosListener bosListener = null;
    private BosHandler bosHandler = new BosHandler(this);
    private String objectKey = "";

    private ViewUtil viewUtil = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.bos_title);
        setOptionsButtonInVisible();
        trackApp = (TrackApplication) getApplicationContext();
        init();
    }

    private void init() {
        viewUtil = new ViewUtil();

        bosListener = new OnBosListener() {
            @Override
            public void onPutObjectCallback(BosPutObjectResponse response) {
                viewUtil.showToast(BosActivity.this, response.getMessage());
            }

            @Override
            public void onGetObjectCallback(final BosGetObjectResponse response) {
                if (response.getStatus() != StatusCodes.SUCCESS) {
                    viewUtil.showToast(BosActivity.this, response.getMessage());
                    return;
                }
                viewUtil.showToast(BosActivity.this, getString(R.string.save_picture));
                new Thread() {
                    @Override
                    public void run() {
                        saveObject(response);
                    }
                }.start();
            }

            @Override
            public void onGeneratePresignedUrlCallback(BosGeneratePresignedUrlResponse response) {
                if (response.getStatus() != StatusCodes.SUCCESS) {
                    viewUtil.showToast(BosActivity.this, response.getMessage());
                    return;
                }
                // 获取Object的URL
                Uri uri = Uri.parse(response.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                // 执行URL
                BosActivity.this.startActivity(intent);
            }
        };
    }

    /**
     * 上传对象（图片）
     *
     * @param view
     */
    public void onPutObject(View view) {
        selectImage();
    }

    private void putObject(String path) {
        // 对象key（即文件名称包括后缀，如track.jpg、track.png）
        String[] paths = path.split("/");
        objectKey = paths[paths.length - 1];
        // 对象类型
        BosObjectType objectType = BosObjectType.image;
        try {
            // 通过文件形式上传
            File file = new File(path);
            BosPutObjectRequest request = BosPutObjectRequest.buildFileRequest(trackApp.getTag(),
                    trackApp.serviceId, objectKey, objectType, file);
            trackApp.mClient.putObject(request, bosListener);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取对象（图片）
     *
     * @param view
     */
    public void onGetObject(View view) {
        // 对象类型
        BosObjectType objectType = BosObjectType.image;
        BosGetObjectRequest request = new BosGetObjectRequest(trackApp.getTag(),
                trackApp.serviceId, objectKey, objectType);
        trackApp.mClient.getObject(request, bosListener);
    }

    /**
     * 获取对象（图片）URL
     *
     * @param view
     */
    public void onGeneratePresignedUrl(View view) {
        // 对象类型
        BosObjectType objectType = BosObjectType.image;

        BosGeneratePresignedUrlRequest request = new BosGeneratePresignedUrlRequest(trackApp.getTag(),
                trackApp.serviceId, objectKey, objectType);

        // 图片处理命令
        ImageProcessCommand imageProcessCommand = new ImageProcessCommand();
        imageProcessCommand.setAngle(180);
        request.setImageProcessCommand(imageProcessCommand);

        // 文字水印命令
        TextWatermarkCommand textWatermarkCommand = new TextWatermarkCommand();
        textWatermarkCommand.setText("百度鹰眼");
        textWatermarkCommand.setFontFamily(FontFamily.KaiTi);
        textWatermarkCommand.setAngle(45);
        textWatermarkCommand.setFontColor("0000FF");
        request.setTextWatermarkCommand(textWatermarkCommand);

        trackApp.mClient.generatePresignedUrl(request, bosListener);
    }

    /**
     * 选择图片
     */
    private void selectImage() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, GRANTED);
        } else {
            openAlbum();
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(IMAGE_TYPE);
        try {
            startActivityForResult(intent, IMAGE_CODE);
        } catch (Exception ex) {
            viewUtil.showToast(this, getString(R.string.open_album_failed));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IMAGE_CODE:
                if (resultCode == RESULT_OK) {
                    handleImage(data);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 处理图片
     *
     * @param data
     */
    private void handleImage(Intent data) {
        Uri uri = data.getData();
        String imagePath = "";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                    String[] ids = docId.split(":");
                    if (null != ids && ids.length >= 2) {
                        String id = ids[1];
                        String selection = MediaStore.Images.Media._ID + "=" + id;
                        imagePath = getImagePath(uri, selection);
                    }
                } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                }
            } else if ("content".equalsIgnoreCase(uri.getScheme())) {
                imagePath = getImagePath(uri, null);
            } else if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
            }
        } else {
            imagePath = getImagePath(uri, null);
        }

        if (TextUtils.isEmpty(imagePath)) {
            viewUtil.showToast(this, getString(R.string.not_select_picture));
            return;
        }

        putObject(imagePath);
    }

    /**
     * 获取图片路径
     *
     * @param uri
     * @param selection
     * @return
     */
    private String getImagePath(Uri uri, String selection) {
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (null != cursor && cursor.getColumnCount() > 0 && cursor.moveToFirst()) {
            int index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
            if (index > 0) {
                return cursor.getString(index);
            }
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case GRANTED:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else {
                    viewUtil.showToast(this, getString(R.string.not_granted_album_permission));
                }
                break;

            default:
                break;
        }
    }

    /**
     * 保存对象（图片）
     *
     * @param response
     */
    private void saveObject(BosGetObjectResponse response) {
        ByteArrayOutputStream outputStream = response.getObjectContent();
        String filename = response.getObjectKey();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                + getPackageName());
        if (!dir.exists()) {
            dir.mkdirs();
        }

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File(dir, filename), true);
            fos.write(outputStream.toByteArray());
            fos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    outputStream.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        bosHandler.obtainMessage(0, "图片保存成功,存储在" + getPackageName() + "文件夹下!").sendToTarget();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_bos;
    }

    static class BosHandler extends Handler {
        WeakReference<BosActivity> bosActivity;

        public BosHandler(BosActivity bosActivity) {
            this.bosActivity = new WeakReference<>(bosActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            bosActivity.get().viewUtil.showToast(bosActivity.get(), (String) msg.obj);
        }
    }

}
