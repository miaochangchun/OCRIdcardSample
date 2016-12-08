package com.example.sinovoice.ocridcardsample;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sinovoice.util.ConfigUtil;
import com.example.sinovoice.util.HciCloudOcrHelper;
import com.example.sinovoice.util.HciCloudSysHelper;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.ocr.OcrTemplateId;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tvResult;
    private HciCloudSysHelper mHciCloudSysHelper;
    private HciCloudOcrHelper mHciCloudOcrHelper;
    private Button btnPlay;
    private FrameLayout cameraPreviewLayout;
    private ProgressDialog pDialog;

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.arg1) {
                case HciCloudOcrHelper.CAPTURE_RESULT: //显示识别结果
                    Bundle resultBundle = msg.getData();
                    String result = resultBundle.getString("result");
//                    tvResult.setText(result);
                    showResultView(result);
                    break;
                case HciCloudOcrHelper.CAPTURE_ERROR:  //显示错误信息
                    Bundle errorBundle = msg.getData();
                    String error = errorBundle.getString("error");
//                    tvResult.setText(error);
                    showResultView(error);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 显示识别结果界面
     * @param result
     */
    private void showResultView(String result) {
        if(cameraPreviewLayout != null){
            cameraPreviewLayout.removeAllViews();
            cameraPreviewLayout = null;
        }

        setContentView(R.layout.activity_result);

        TextView tvResult = (TextView) findViewById(R.id.tv_result);
        tvResult.setText(result);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //无标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置窗体全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //设置为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_result);
        tvResult = (TextView) findViewById(R.id.tv_result);
        pDialog = ProgressDialog.show(this, getText(R.string.dialog_title_tips), getText(R.string.dialog_msg_hcicloud_sysinit));
        initSinovoice();
    }

    private void initView() {
        setContentView(R.layout.activity_main);
        btnPlay = (Button) findViewById(R.id.btn_take_picture);
        cameraPreviewLayout = (FrameLayout) findViewById(R.id.layout_camera_preview);
        cameraPreviewLayout.addView(mHciCloudOcrHelper.previewCapture());
        mHciCloudOcrHelper.startCapture(ConfigUtil.CAP_KEY_OCR_LOCAL_TEMPLATE);

        btnPlay.setOnClickListener(this);
    }

    private void initSinovoice() {
        mHciCloudSysHelper = HciCloudSysHelper.getInstance();
        mHciCloudOcrHelper = HciCloudOcrHelper.getInstance();
        int errorCode = mHciCloudSysHelper.init(this);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "系统初始化失败，错误码=" + errorCode);
            Toast.makeText(this, "系统初始化失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        mHciCloudOcrHelper.setMyHandler(new MyHandler());
        errorCode = mHciCloudOcrHelper.init(this, ConfigUtil.CAP_KEY_OCR_LOCAL_TEMPLATE);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "拍照器初始化失败，错误码=" + errorCode);
            Toast.makeText(this, "拍照器初始化失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        errorCode = mHciCloudOcrHelper.loadTemplate(this);
        if (errorCode != HciErrorCode.HCI_ERR_NONE) {
            Log.e(TAG, "拍照器加载模板失败，错误码=" + errorCode);
            Toast.makeText(this, "拍照器加载模板失败，错误码=" + errorCode, Toast.LENGTH_SHORT).show();
            return;
        }
        dismissDialog();
        initView();
    }

    /**
     * 关闭对话框
     */
    private void dismissDialog() {
        if(pDialog != null && pDialog.isShowing()){
            pDialog.dismiss();
            pDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_take_picture:
                mHciCloudOcrHelper.stopCapture();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        if (mHciCloudOcrHelper != null) {
            mHciCloudOcrHelper.release();
        }
        if (mHciCloudSysHelper != null) {
            mHciCloudSysHelper.release();
        }
        super.onDestroy();
    }
}
