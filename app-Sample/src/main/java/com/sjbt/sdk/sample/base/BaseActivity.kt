package com.sjbt.sdk.sample.base

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.pm.ActivityInfo
import android.content.res.TypedArray
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.sjbt.sdk.sample.dialog.CallBack
import com.sjbt.sdk.sample.dialog.ConfirmDialog
import com.sjbt.sdk.sample.ui.dialog.LoadingDialog

abstract class BaseActivity : AppCompatActivity() {
    protected var mConfirmDialog: ConfirmDialog? = null
    private var loading_Dialog: LoadingDialog? = null
    protected var mHandler = Handler(Looper.getMainLooper())
    protected var isFront = false
    protected var mBluetoothAdapter: BluetoothAdapter? = null
    override fun setRequestedOrientation(requestedorientation: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && isTranslucentorFloating) {
            return
        }
        super.setRequestedOrientation(requestedorientation)
    }

    private val isTranslucentorFloating: Boolean
        private get() {
            var isTranslucentorFloating = false
            try {
                val styleableRes = Class.forName("com.android.internal.R\$styleable")
                    .getField("Window")[null] as IntArray
                val typedArray = obtainStyledAttributes(styleableRes)
                val m = ActivityInfo::class.java.getMethod(
                    "isTranslucentOrFloating",
                    TypedArray::class.java
                )
                m.isAccessible = true
                isTranslucentorFloating = m.invoke(null, typedArray) as Boolean
                m.isAccessible = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isTranslucentorFloating
        }

    fun showConfirmDialogWithCallback(tip: String?, btnName: String?, callBack: CallBack<String>?) {
        runOnUiThread(Runnable {
            if (mConfirmDialog != null && mConfirmDialog!!.isShowing) {
                return@Runnable
            }
            mConfirmDialog = ConfirmDialog(this@BaseActivity, tip, btnName, callBack)
            try {
                mConfirmDialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideConfirmDialog() {
        hideDialog(mConfirmDialog)
    }

    protected fun hideDialog(dialog: Dialog?) {
        runOnUiThread {
            if (dialog != null && dialog.isShowing) {
                dialog.dismiss()
            }
        }
    }

    //    protected void startPageAnim() {
    //        overridePendingTransition(R.anim.in_from_right, R.anim.out_to_left);
    //    }
    fun showLoadingDlg() {
        runOnUiThread(Runnable {
            if (loading_Dialog != null) {
                loading_Dialog!!.dismiss()
                loading_Dialog = null
            }
            loading_Dialog = LoadingDialog(this@BaseActivity)
            if (isFinishing) {
                return@Runnable
            }
            try {
                loading_Dialog!!.show()

//                    mHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            hideLoadingDlg();
//                        }
//                    }, 50 * 1000);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun showLoadingDlg(msg: String?) {
        runOnUiThread(Runnable {
            if (loading_Dialog != null) {
                loading_Dialog!!.dismiss()
                loading_Dialog = null
            }
            if (isFinishing) {
                return@Runnable
            }
            loading_Dialog = LoadingDialog(this@BaseActivity, msg)
            try {
                loading_Dialog!!.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        })
    }

    fun hideLoadingDlg() {
        hideDialog(loading_Dialog)
    }

    override fun onDestroy() {
        hideDialog(loading_Dialog)
        hideDialog(mConfirmDialog)
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        isFront = false
    }

    companion object {
        protected val mContactPerms = arrayOf(
            Manifest.permission.READ_CONTACTS
        )
    }
}