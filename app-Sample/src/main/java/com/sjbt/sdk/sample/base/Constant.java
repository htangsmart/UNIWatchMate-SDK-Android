package com.sjbt.sdk.sample.base;

import org.jetbrains.annotations.NotNull;

public class Constant {
    public static final String[] TYPES = {"全屏（竖屏）", "全屏（横屏）", "弹窗（竖屏）",
            "弹窗（横屏）", "底部弹窗", "自定义View", "自定义View（Xml）","自定义Gif背景","自定义视频背景(mov,mp4)","自定义图片背景","全屏（竖屏弹窗）", "全屏（竖屏弹窗自定义View）","全屏（横屏弹窗)"};
    /**
     * 全屏（竖屏）
     */
    public static final int FULL_PORT = 0;
    /**
     * 全屏（横屏）
     */
    public static final int FULL_LAND = 1;
    /**
     * 弹窗（竖屏）
     */
    public static final int DIALOG_PORT = 2;
    /**
     * "弹窗（横屏）
     */
    public static final int DIALOG_LAND = 3;
    /**
     * 底部弹窗
     */
    public static final int DIALOG_BOTTOM = 4;
    /**
     * 自定义View
     */
    public static final int CUSTOM_VIEW = 5;
    /**
     * 自定义View（Xml）
     */
    public static final int CUSTOM_XML = 6;
    /**
     *  自定义背景GIF
     */
    public static final int CUSTOM_GIF = 7;
    /**
     *  自定义背景视频
     */
    public static final int CUSTOM_MOV = 8;
    /**
     *  自定义背景图片
     */
    public static final int CUSTOM_PIC= 9;

    /**
     *  全屏（竖屏弹窗）
     */
    public static final int FULL_PORT_PRIVACY = 10;
    /**
     *  全屏（竖屏弹窗自定义view）
     */
    public static final int FULL_PORT_PRIVACY_XML = 11;

    /**
     * 全屏（横屏弹窗
     */
    public static final int FULL_LAND_PRIVACY = 12;

    public static final String THEME_KEY = "theme";

    public static final String LOGIN_TYPE = "login_type";
    public static final int LOGIN = 1;
    public static final int LOGIN_DELAY = 2;
    @NotNull
    public static final String W20 = "W20";
    @NotNull
    public static final String SJJC = "sjjc";

    public static final String DE = "de";
    public static final String EN = "en";
    public static final String ES = "es";
    public static final String FR = "fr";
    public static final String IN = "in";
    public static final String IT = "it";
    public static final String JA = "ja";
    public static final String KO = "ko";
    public static final String PT = "pt";
    public static final String RU = "ru";
    public static final String VI = "vi";
    public static final String ZH = "zh";
    public static final String UP = "up", UP_EX = "upex";

}
