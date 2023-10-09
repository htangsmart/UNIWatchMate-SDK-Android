package com.sjbt.sdk.sample.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sjbt.sdk.sample.R;


/**
 * 权限提醒
 */
public class CameraBusDialog extends BaseDialog implements View.OnClickListener {

    private TextView mBtnCancel, mBtnOk, mTvTip;
    private ImageView ivCamera;
    private Context mContext;
    private int type;
    public static final int TIP_TYPE_OPEN_CAMERA = 1;
    public static final int TIP_TYPE_OPEN_CAMERA_PERMISSION = 2;
    public static final int TIP_TYPE_OPEN_STORAGE = 3;
    public static final int TIP_TYPE_OPEN_LOCATION = 4;
    private CallBack<Integer> mCallBack;

    public CameraBusDialog(@NonNull Context context, int type, CallBack<Integer> callBack) {
        super(context);
        this.type = type;
        mContext = context;
        mCallBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(mContext, R.layout.dialog_camera, null);
        setContentView(view);

        mBtnCancel = findViewById(R.id.btn_cancel);
        mBtnOk = findViewById(R.id.btn_ok);
        mTvTip = findViewById(R.id.tv_tip);
        ivCamera = findViewById(R.id.iv_camera_icon);
        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setWindowParam(Gravity.CENTER, 0, ANIM_TYPE_NONE);

        mBtnCancel.setOnClickListener(this);
        mBtnOk.setOnClickListener(this);

        if (type == TIP_TYPE_OPEN_CAMERA) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnCancel.setVisibility(View.GONE);
            mBtnOk.setText(mContext.getString(R.string.sure));
            mTvTip.setText(mContext.getString(R.string.camera_open_tip));
            ivCamera.setImageResource(R.mipmap.biu_icon_open_camera);
        } else if (type == TIP_TYPE_OPEN_CAMERA_PERMISSION) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnCancel.setText(mContext.getString(R.string.cancel));
            mBtnOk.setText(mContext.getString(R.string.open_camera_permission));
            mTvTip.setText(mContext.getString(R.string.camera_permission_reject));
            ivCamera.setImageResource(R.mipmap.biu_icon_no_camera_permission);
        } else if (type == TIP_TYPE_OPEN_STORAGE) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnCancel.setText(mContext.getString(R.string.cancel));
            mBtnOk.setText(mContext.getString(R.string.open_camera_permission));
            mTvTip.setText(mContext.getString(R.string.open_storage_permission_tip));
            ivCamera.setImageResource(R.mipmap.biu_icon_no_camera_permission);
        } else if (type == TIP_TYPE_OPEN_LOCATION) {
            mBtnOk.setVisibility(View.VISIBLE);
            mBtnCancel.setText(mContext.getString(R.string.cancel));
            mBtnOk.setText(mContext.getString(R.string.open_location_permission));
            mTvTip.setText(mContext.getString(R.string.open_location_permission_tip));
            ivCamera.setImageResource(R.mipmap.biu_icon_location_tip);
        }
    }

    @Override
    public void onClick(View view) {

        if (view.equals(mBtnCancel)) {
            dismiss();
        } else if (view.equals(mBtnOk)) {
            dismiss();
            if (null != mCallBack) {
                mCallBack.callBack(type);
            }
        }
    }

}
