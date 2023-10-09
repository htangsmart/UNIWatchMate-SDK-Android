package com.sjbt.sdk.sample.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.sjbt.sdk.sample.R;


/**
 * 用于操作确认弹窗
 */
public class ConfirmDialog extends BaseDialog {

    private String mTip;
    private String mBtnName;

    private Context mContext;
    private CallBack<String> mCallBack;

    public ConfirmDialog(@NonNull Context context, String tip) {
        super(context);
        mContext = context;
        mTip = tip;
    }

    public ConfirmDialog(@NonNull Context context, String tip, String btnName) {
        super(context);
        mContext = context;
        mTip = tip;
        mBtnName = btnName;
    }

    public ConfirmDialog(@NonNull Context context, String tip, String btnName, CallBack<String> callBack) {
        super(context);
        mContext = context;
        mTip = tip;
        mBtnName = btnName;
        mCallBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(mContext, R.layout.dialog_confirm, null);
        setContentView(view);

        TextView mBtnOk=findViewById(R.id.btn_ok);
        TextView mTvTip=findViewById(R.id.tv_tip);

        setCancelable(false);
        setCanceledOnTouchOutside(false);
        setWindowParam(Gravity.CENTER, 0, ANIM_TYPE_NONE);
        if (!TextUtils.isEmpty(mTip)) {
            mTvTip.setText(mTip);
        }

        if (!TextUtils.isEmpty(mBtnName)) {
            mBtnOk.setText(mBtnName);
        }

        mBtnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mCallBack != null) {
                    mCallBack.callBack(mTvTip.getText().toString());
                }
                dismiss();
            }
        });

    }


}
