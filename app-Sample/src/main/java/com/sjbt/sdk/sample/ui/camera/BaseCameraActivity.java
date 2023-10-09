package com.sjbt.sdk.sample.ui.camera;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.sjbt.sdk.sample.dialog.CallBack;
import com.sjbt.sdk.sample.dialog.ConfirmDialog;
import com.sjbt.sdk.utils.LogUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public abstract class BaseCameraActivity extends AppCompatActivity {

    protected ConfirmDialog mConfirmDialog;

    protected Handler mHandler = new Handler(Looper.getMainLooper());
    protected boolean isFront;
    protected BluetoothAdapter mBluetoothAdapter;

    protected static final String[] mBTPermsR = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };

    protected static final String[] mBTPermsRBelow = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN
    };

    protected static final String[] mContactPerms = new String[]{
            Manifest.permission.READ_CONTACTS
    };


    @Override
    public void setRequestedOrientation(int requestedorientation) {

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentorFloating()) {

            return;
        }

        super.setRequestedOrientation(requestedorientation);
    }

    private boolean isTranslucentorFloating() {
        boolean isTranslucentorFloating = false;
        try {
            int[] styleableRes = (int[]) Class.forName("com.android.internal.R$styleable").getField("Window").get(null);
            final TypedArray typedArray = obtainStyledAttributes(styleableRes);
            Method m = ActivityInfo.class.getMethod("isTranslucentOrFloating", TypedArray.class);
            m.setAccessible(true);
            isTranslucentorFloating = (boolean) m.invoke(null, typedArray);
            m.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isTranslucentorFloating;
    }

    private boolean fixorientation() {
        try {
            Field field = Activity.class.getDeclaredField("mActivityInfo");
            field.setAccessible(true);
            ActivityInfo o = (ActivityInfo) field.get(this);
            o.screenOrientation = SCREEN_ORIENTATION_UNSPECIFIED;
            field.setAccessible(false);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //打开设置界面
    public void checkNotificationPermission(Context context) {
        if (!isNotificationListenerEnabled(context)) {
            LogUtils.logCommon("通知权限检查 未开启");
            context.startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        }
    }

    // 判断是否打开了通知监听权限
    public boolean isNotificationListenerEnabled(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(), "enabled_notification_listeners");
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentorFloating()) {
            boolean result = fixorientation();
        }
//        if (android.os.Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }

        super.onCreate(savedInstanceState);


    }


    public void showConfirmDialogWithCallback(String tip, String btnName, CallBack callBack) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
                    return;
                }

                mConfirmDialog = new ConfirmDialog(BaseCameraActivity.this, tip, btnName, callBack);

                try {
                    mConfirmDialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public void hideConfirmDialog() {
        hideDialog(mConfirmDialog);
    }


    protected void hideDialog(Dialog dialog) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    /**
     * 设置扫描时间
     *
     * @param btClass
     */
    public void setDiscoveryTimeOut(Class btClass, BluetoothAdapter adapter, int time) {
        try {
            Method setDiscoverableTimeout = btClass.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.invoke(adapter, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    protected void startPageAnim() {
//        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
//    }

    @Override
    protected void onPause() {
        super.onPause();
        isFront = false;
    }

}
