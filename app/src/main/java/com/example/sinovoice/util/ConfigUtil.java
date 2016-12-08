package com.example.sinovoice.util;

/**
 * 灵云配置信息
 * Created by 10048 on 2016/12/3.
 */
public class ConfigUtil {
    /**
     * 灵云APP_KEY
     */
    public static final String APP_KEY = "c85d54f1";

    /**
     * 开发者密钥
     */
    public static final String DEVELOPER_KEY = "712ddd892cf9163e6383aa169e0454e3";

    /**
     * 灵云云服务的接口地址
     */
    public static final String CLOUD_URL = "http://test.api.hcicloud.com:8888";

    /**
     * 需要运行的灵云能力
     */
    //银行卡识别功能
    public static final String CAP_KEY_OCR_LOCAL_BANKCARD = "ocr.local.bankcard.v7";
    //身份证识别功能
    public static final String CAP_KEY_OCR_LOCAL_TEMPLATE = "ocr.local.template.v6";
    //名片识别功能
    public static final String CAP_KEY_OCR_LOCAL_BIZCARD = "ocr.local.bizcard.v6";
    //本地文本图片识别功能
    public static final String CAP_KEY_OCR_LOCAL_RECOG= "ocr.local";
}
