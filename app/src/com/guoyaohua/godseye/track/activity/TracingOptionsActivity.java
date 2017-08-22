package com.guoyaohua.godseye.track.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.baidu.trace.model.LocationMode;
import com.guoyaohua.godseye.R;
import com.guoyaohua.godseye.track.utils.Constants;

import static com.baidu.trace.model.LocationMode.High_Accuracy;

public class TracingOptionsActivity extends BaseActivity {

    // 返回结果
    private Intent result = null;

    private EditText gatherIntervalText = null;
    private EditText packIntervalText = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.tracing_options_title);
        setOptionsButtonInVisible();
        init();
    }

    private void init() {
        gatherIntervalText = (EditText) findViewById(R.id.gather_interval);
        packIntervalText = (EditText) findViewById(R.id.pack_interval);

        gatherIntervalText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText textView = (EditText) view;
                String hintStr = textView.getHint().toString();
                if (hasFocus) {
                    textView.setHint("");
                } else {
                    textView.setHint(hintStr);
                }
            }
        });

        packIntervalText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                EditText textView = (EditText) view;
                String hintStr = textView.getHint().toString();
                if (hasFocus) {
                    textView.setHint("");
                } else {
                    textView.setHint(hintStr);
                }
            }
        });

    }

    public void onCancel(View v) {
        super.onBackPressed();
    }

    public void onFinish(View v) {
        result = new Intent();

        RadioGroup locationModeGroup = (RadioGroup) findViewById(R.id.location_mode);
        RadioButton locationModeRadio = (RadioButton) findViewById(locationModeGroup.getCheckedRadioButtonId());
        LocationMode locationMode = High_Accuracy;
        switch (locationModeRadio.getId()) {
            case R.id.device_sensors:
                locationMode = LocationMode.Device_Sensors;
                break;

            case R.id.battery_saving:
                locationMode = LocationMode.Battery_Saving;
                break;

            case R.id.high_accuracy:
                locationMode = High_Accuracy;
                break;

            default:
                break;
        }
        result.putExtra("locationMode", locationMode.name());

        RadioGroup needBosGroup = (RadioGroup) findViewById(R.id.object_storage);
        RadioButton needBosRadio = (RadioButton) findViewById(needBosGroup.getCheckedRadioButtonId());
        boolean isNeedObjectStorage = false;
        switch (needBosRadio.getId()) {
            case R.id.close_bos:
                isNeedObjectStorage = false;
                break;

            case R.id.open_bos:
                isNeedObjectStorage = true;
                break;

            default:
                break;
        }
        result.putExtra("isNeedObjectStorage", isNeedObjectStorage);

        EditText gatherIntervalText = (EditText) findViewById(R.id.gather_interval);
        EditText packIntervalText = (EditText) findViewById(R.id.pack_interval);
        String gatherIntervalStr = gatherIntervalText.getText().toString();
        String packIntervalStr = packIntervalText.getText().toString();

        if (!TextUtils.isEmpty(gatherIntervalStr)) {
            try {
                result.putExtra("gatherInterval", Integer.parseInt(gatherIntervalStr));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        if (!TextUtils.isEmpty(packIntervalStr)) {
            try {
                result.putExtra("packInterval", Integer.parseInt(packIntervalStr));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        //        RadioGroup supplementModeGroup = (RadioGroup) findViewById(R.id.supplement_mode);
        //        RadioButton supplementModeOptionBtn = (RadioButton) findViewById(supplementModeGroup
        // .getCheckedRadioButtonId());
        //        SupplementMode supplementMode = SupplementMode.no_supplement;
        //        switch (supplementModeOptionBtn.getId()) {
        //            case R.id.no_supplement:
        //                supplementMode = SupplementMode.no_supplement;
        //                break;
        //
        //            case R.id.straight:
        //                supplementMode = SupplementMode.straight;
        //                break;
        //
        //            case R.id.walking:
        //                supplementMode = SupplementMode.walking;
        //                break;
        //
        //            case R.id.riding:
        //                supplementMode = SupplementMode.riding;
        //                break;
        //
        //            case R.id.driving:
        //                supplementMode = SupplementMode.driving;
        //                break;
        //
        //            default:
        //                break;
        //        }
        //        result.putExtra("supplementMode", supplementMode.name());

        setResult(Constants.RESULT_CODE, result);
        super.onBackPressed();
    }

    @Override
    protected int getContentViewId() {
        return R.layout.activity_tracing_options;
    }

}
