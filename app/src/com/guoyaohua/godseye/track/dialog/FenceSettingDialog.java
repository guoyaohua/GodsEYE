package com.guoyaohua.godseye.track.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import com.baidu.trace.api.fence.FenceShape;
import com.baidu.trace.api.fence.FenceType;
import com.guoyaohua.godseye.R;

/**
 * 围栏设置对话框
 */
public class FenceSettingDialog extends Dialog implements View.OnClickListener {

    private Activity mParent;
    private Callback callback = null;

    private View fenceShapeLayout = null;
    private View fenceNameLayout = null;
    private View vertexesNumberLayout = null;
    private Button cancelBtn = null;
    private Button sureBtn = null;
    private RadioButton localBtn = null;
    private RadioButton serverBtn = null;
    private RadioButton createBtn = null;
    private RadioButton listBtn = null;
    private RadioButton circleBtn = null;
    private RadioButton polylineBtn = null;
    private RadioButton polygonBtn = null;

    private TextView createCaptionText = null;

    private EditText fenceNameText = null;
    private EditText vertexesNumberText = null;

    private FenceType fenceType = FenceType.local;
    private FenceShape fenceShape = FenceShape.circle;
    private int operateType = R.id.btn_fence_list;

    /**
     * @param activity ：调用的父activity
     */
    public FenceSettingDialog(Activity activity, Callback callback) {
        super(activity, android.R.style.Theme_Holo_Light_Dialog);
        this.callback = callback;
        this.mParent = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_fence_setting);

        fenceShapeLayout = findViewById(R.id.layout_fence_shape);
        fenceNameLayout = findViewById(R.id.layout_fence_name);
        vertexesNumberLayout = findViewById(R.id.layout_vertexes_number);
        cancelBtn = (Button) findViewById(R.id.btn_cancel);
        sureBtn = (Button) findViewById(R.id.btn_sure);
        localBtn = (RadioButton) findViewById(R.id.btn_local);
        serverBtn = (RadioButton) findViewById(R.id.btn_server);
        createBtn = (RadioButton) findViewById(R.id.btn_create_fence);
        listBtn = (RadioButton) findViewById(R.id.btn_fence_list);
        circleBtn = (RadioButton) findViewById(R.id.btn_circle);
        polylineBtn = (RadioButton) findViewById(R.id.btn_polyline);
        polygonBtn = (RadioButton) findViewById(R.id.btn_polygon);

        createCaptionText = (TextView) findViewById(R.id.tv_create_caption);

        fenceNameText = (EditText) findViewById(R.id.edtTxt_fence_name);
        vertexesNumberText = (EditText) findViewById(R.id.text_vertexes_number);
        cancelBtn.setOnClickListener(this);
        sureBtn.setOnClickListener(this);
        localBtn.setOnClickListener(this);
        serverBtn.setOnClickListener(this);
        createBtn.setOnClickListener(this);
        listBtn.setOnClickListener(this);
        circleBtn.setOnClickListener(this);
        polylineBtn.setOnClickListener(this);
        polygonBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_local:
                fenceType = FenceType.local;
                fenceShape = FenceShape.circle;
                fenceShapeLayout.setVisibility(View.GONE);
                vertexesNumberLayout.setVisibility(View.GONE);
                if (createBtn.isChecked()) {
                    fenceNameLayout.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.btn_server:
                fenceType = FenceType.server;
                if (createBtn.isChecked()) {
                    fenceShapeLayout.setVisibility(View.VISIBLE);
                    fenceNameLayout.setVisibility(View.VISIBLE);
                }
                if (polylineBtn.isChecked() || polygonBtn.isChecked()) {
                    vertexesNumberLayout.setVisibility(View.VISIBLE);
                } else {
                    vertexesNumberLayout.setVisibility(View.GONE);
                }
                break;

            case R.id.btn_create_fence:
                operateType = R.id.btn_create_fence;
                fenceNameLayout.setVisibility(View.VISIBLE);
                if (FenceType.server == fenceType) {
                    fenceShapeLayout.setVisibility(View.VISIBLE);
                }
                createCaptionText.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_fence_list:
                operateType = R.id.btn_fence_list;
                fenceShapeLayout.setVisibility(View.GONE);
                fenceNameLayout.setVisibility(View.GONE);
                vertexesNumberLayout.setVisibility(View.GONE);
                createCaptionText.setVisibility(View.GONE);
                break;

            case R.id.btn_circle:
                fenceShape = FenceShape.circle;
                vertexesNumberLayout.setVisibility(View.GONE);
                break;

            case R.id.btn_polyline:
                fenceShape = FenceShape.polyline;
                vertexesNumberLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_polygon:
                fenceShape = FenceShape.polygon;
                vertexesNumberLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.btn_cancel:
                dismiss();
                break;

            case R.id.btn_sure:
                String vertexesNumberStr = vertexesNumberText.getText().toString();
                int vertexesNumber = 3;
                if (!TextUtils.isEmpty(vertexesNumberStr)) {
                    try {
                        vertexesNumber = Integer.parseInt(vertexesNumberStr);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                if (null != callback) {
                    String fenceName = fenceNameText.getText().toString();
                    if (TextUtils.isEmpty(fenceName)) {
                        fenceName = mParent.getResources().getString(R.string.fence_name_hint);
                    }
                    callback.onFenceOperateCallback(fenceType, fenceShape, fenceName, vertexesNumber, operateType);
                }
                dismiss();
                break;

            default:
                break;
        }
    }

    /**
     * 操作回调接口
     */
    public interface Callback {

        void onFenceOperateCallback(FenceType fenceType, FenceShape fenceShape, String fenceName, int vertexesNumber,
                                    int operateType);
    }

}
