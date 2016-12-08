package com.example.sinovoice.util;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sinovoice.hcicloudsdk.android.ocr.capture.CameraPreview;
import com.sinovoice.hcicloudsdk.android.ocr.capture.CaptureEvent;
import com.sinovoice.hcicloudsdk.android.ocr.capture.OCRCapture;
import com.sinovoice.hcicloudsdk.android.ocr.capture.OCRCaptureListener;
import com.sinovoice.hcicloudsdk.common.ocr.OcrConfig;
import com.sinovoice.hcicloudsdk.common.ocr.OcrCornersResult;
import com.sinovoice.hcicloudsdk.common.ocr.OcrInitParam;
import com.sinovoice.hcicloudsdk.common.ocr.OcrRecogRegion;
import com.sinovoice.hcicloudsdk.common.ocr.OcrRecogResult;
import com.sinovoice.hcicloudsdk.common.ocr.OcrTemplateId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Created by miaochangchun on 2016/12/5.
 */
public class HciCloudOcrHelper {
    private static final String TAG = HciCloudOcrHelper.class.getSimpleName();
    public static final int CAPTURE_RESULT = 1;
    public static final int CAPTURE_ERROR = 2;
    private static HciCloudOcrHelper mHciCloudOcrHelper = null;
    private OCRCapture ocrCapture;
    private Handler myHandler;
    private OcrTemplateId currTemplateId;

    public void setMyHandler(Handler myHandler) {
        this.myHandler = myHandler;
    }

    public Handler getMyHandler() {
        return myHandler;
    }

    private HciCloudOcrHelper(){
    }

    public static HciCloudOcrHelper getInstance() {
        if (mHciCloudOcrHelper == null) {
            return new HciCloudOcrHelper();
        }
        return mHciCloudOcrHelper;
    }

    /**
     * 拍照器初始化功能
     * @param context
     * @param initCapkeys
     * @return
     */
    public int init(Context context, String initCapkeys) {
        ocrCapture = new OCRCapture();
        String strConfig = getOcrInitParam(context, initCapkeys);
        int errorCode = ocrCapture.hciOcrCaptureInit(context, strConfig, new ocrCaptureListener());
        return errorCode;
    }

    /**
     * 获取ocr初始化时的配置参数
     * @param context
     * @param initCapkeys
     * @return
     */
    private String getOcrInitParam(Context context, String initCapkeys) {
        OcrInitParam ocrInitParam = new OcrInitParam();
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String packageName = context.getPackageName();
        String dataPath = sdPath + File.separator + "sinovoice"
                + File.separator + packageName + File.separator + "data"
                + File.separator;
        //身份证识别所需的资源文件
        String[] strs = new String[]{"IDCard_EN.xml", "iRead_Binary_GB.dat","iRead_Cmn_LM.dat","iRead_Gray_GBK.dat",
                "iRead_IDCard_Gender.dat","iRead_IDCard_Gray.dat","iRead_IDCard_LM.dat","iRead_IDCard_Nation.dat"};
        for (String str : strs) {
            copyData(context, dataPath + str, str);
        }
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_DATA_PATH, dataPath);
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_FILE_FLAG, "none");
        ocrInitParam.addParam(OcrInitParam.PARAM_KEY_INIT_CAP_KEYS, initCapkeys);
        return ocrInitParam.getStringConfig();
    }

    /**
     * 加载模板
     * @param context  上下文
     * @return
     */
    public int loadTemplate(Context context) {
        String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String packageName = context.getPackageName();
        String dataPath = sdPath + File.separator + "sinovoice"
                + File.separator + packageName + File.separator + "data"
                + File.separator;

        String templatePath = dataPath + "IDCard_EN.xml";
        currTemplateId = new OcrTemplateId();
        int errorCode = ocrCapture.hciOcrCaptureLoadTemplate("", templatePath, currTemplateId);
        return errorCode;
    }

    /**
     * 拷贝资源文件
     * @param context 上下文参数
     * @param dataPath  需要拷贝的目标文件路径
     * @param dataAssetPath    assets目录下需要拷贝的文件名称
     */
    private void copyData(Context context, String dataPath, String dataAssetPath) {
        File file = new File(dataPath);
        if (!file.getParentFile().exists()) {
            file.mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {    //文件已存在，不需要再次拷贝了。
            return;
        }
        InputStream inputStream = null;
        FileOutputStream fos = null;
        try {
            inputStream = context.getAssets().open(dataAssetPath);
            fos = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer)) != -1) {
                fos.write(buffer);
                fos.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {      //关闭读取流
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {          //关闭写入流
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 拍照器反初始化功能
     * @return
     */
    public int release(){
        int errorCode = 0;
        if (ocrCapture != null) {
            errorCode = ocrCapture.hciOcrCaptureRelease();
        }
        return errorCode;
    }

    /**
     *
     * @param capkey
     */
    public void startCapture(String capkey){
        String strConfig = getOcrRecogParam(capkey);
        ocrCapture.hciOcrCaptureStart(strConfig);
    }

    /**
     * 获取摄像机的预览图片
     * @return
     */
    public CameraPreview previewCapture() {
        return ocrCapture.getCameraPreview();
    }

    /**
     * 关闭拍照器并开始识别
     */
    public void stopCapture(){
        ocrCapture.hciOcrCaptureStopAndRecog();
    }

    /**
     * 文本图片识别，需要获取到图像之后手动调用此函数识别。
     * @param data  拍照器获取的图像数据
     * @param recogRegionList   要识别的区域列表
     */
    public void recogCapture(byte[] data, ArrayList<OcrRecogRegion> recogRegionList){
        String recogConfig = "capkey=ocr.local";
        ocrCapture.hciOcrCaptureRecog(data, recogConfig, recogRegionList);
    }

    private String getOcrRecogParam(String capkey) {
        OcrConfig ocrConfig = new OcrConfig();
        ocrConfig.addParam(OcrConfig.SessionConfig.PARAM_KEY_CAP_KEY, capkey);
        ocrConfig.addParam(OcrConfig.InputConfig.PARAM_KEY_TEMPLATE_CAT_EDGE, "yes");
        ocrConfig.addParam(OcrConfig.TemplateConfig.PARAM_KEY_TEMPLATE_ID, currTemplateId.getTemplateId() + "");
        ocrConfig.addParam(OcrConfig.TemplateConfig.PARAM_KEY_TEMPLATE_INDEX, "0");
        //0是正面，1是反面
        ocrConfig.addParam(OcrConfig.TemplateConfig.PARAM_KEY_TEMPLATE_PATE_INDEX, "0");
        return ocrConfig.getStringConfig();
    }


    /**
     * 拍照器回调类
     */
    private class ocrCaptureListener implements OCRCaptureListener {

        @Override
        public void onCaptureEventError(CaptureEvent captureEvent, int errorCode) {
            String error = "" + errorCode;

            //把拍照器错误结果传递到Activity上
            Message message = new Message();
            message.arg1 = CAPTURE_ERROR;
            Bundle bundle = new Bundle();
            bundle.putString("error", "拍照器返回的错误码：" + error);
            message.setData(bundle);
            myHandler.sendMessage(message);
        }

        @Override
        public void onCaptureEventStateChange(CaptureEvent captureEvent) {

        }

        @Override
        public void onCaptureEventCapturing(CaptureEvent captureEvent, byte[] bytes, OcrCornersResult ocrCornersResult) {

        }

        @Override
        public void onCaptureEventRecogFinish(CaptureEvent captureEvent, OcrRecogResult ocrRecogResult) {
            Log.d(TAG, "ocrRecogResult = " + ocrRecogResult.getResultText());
            String result = ocrRecogResult.getResultText();

            //把拍照器识别结果传递到Activity上
            Message message = new Message();
            message.arg1 = CAPTURE_RESULT;
            Bundle bundle = new Bundle();
            bundle.putString("result", "识别结果：" + result);
            message.setData(bundle);
            myHandler.sendMessage(message);
        }
    }
}
