package com.sjbt.sdk.utils;

import com.sjbt.sdk.uparser.UparserJni;
import com.sjbt.sdk.uparser.model.JpgInfo;

public class UParseUtil {
    private final String TAG = UParseUtil.class.getSimpleName();
    private static UParseUtil _instance;
    public UparserJni upaserJni = null;

    private UParseUtil() {
        upaserJni = new UparserJni();
    }

    public static UParseUtil getInstance() {
        if (_instance == null) {
            synchronized (UParseUtil.class) {
                if (_instance == null) {
                    _instance = new UParseUtil();
                }
            }
        }
        return _instance;
    }

    /**
     * 从dial中提取jpg图片和信息
     * @param filePath
     * @param jpgInfo
     * @return
     */
    public int getJpgFromDial(String filePath, JpgInfo jpgInfo) {
        return upaserJni.peekJpgFromDial(filePath, jpgInfo);
    }

}

