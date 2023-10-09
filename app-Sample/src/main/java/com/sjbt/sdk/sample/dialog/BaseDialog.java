package com.sjbt.sdk.sample.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatDialog;

import com.blankj.utilcode.util.ScreenUtils;
import com.sjbt.sdk.sample.R;


/**
 * 统一设置了所有dialog的宽度属性
 */
public abstract class BaseDialog extends AppCompatDialog {

    private static final float SCREEN_RATE = 0.85f;

    public static final int ANIM_TYPE_NONE = 0;
    public static final int ANIM_TYPE_TOP_ENTER = 1;
    public static final int ANIM_TYPE_BOTTOM_ENTER = 2;

    public BaseDialog(Context context) {
        super(context, R.style.CustomDialogTrans);
    }

    public BaseDialog(Context context, boolean fullscreen) {
        super(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * @param gravity   Gravity.TOP ...
     * @param widthRate
     */
    protected void setWindowParam(int gravity, float widthRate, int animType) {
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();

        int width = ScreenUtils.getScreenWidth();

        params.width = widthRate == 0 ? (int) (width * SCREEN_RATE) : (int) (width * widthRate);

        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.gravity = gravity;

        switch (animType) {
            case ANIM_TYPE_NONE:
                break;

            case ANIM_TYPE_TOP_ENTER:
                window.setWindowAnimations(R.style.dialog_animation_top_enter);
                break;

            case ANIM_TYPE_BOTTOM_ENTER:
                window.setWindowAnimations(R.style.dialog_animation_bottom_enter);
                break;
        }

        window.setAttributes(params);

    }


}
