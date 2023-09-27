package com.sjbt.sdk.sample.ui.bind

import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import cn.bertsir.zbar.CameraPreview
import cn.bertsir.zbar.Qr.Symbol
import cn.bertsir.zbar.QrConfig
import cn.bertsir.zbar.ScanCallback
import cn.bertsir.zbar.utils.QRUtils
import cn.bertsir.zbar.view.ScanLineView
import com.sjbt.sdk.sample.R
import com.sjbt.sdk.sample.base.BaseFragment
import com.sjbt.sdk.sample.databinding.FragmentDeviceCustomQrBinding
import com.sjbt.sdk.sample.ui.device.bind.DeviceBindFragment
import com.sjbt.sdk.sample.utils.viewbinding.viewBinding
import timber.log.Timber

class DeviceCustomQrFragment : BaseFragment(R.layout.fragment_device_custom_qr), SensorEventListener,
    View.OnClickListener {
    //    , PromptDialogFragment.OnPromptListener {
    private /*const*/ val promptBindSuccessId = 1

    private val viewBind: FragmentDeviceCustomQrBinding by viewBinding()
    private var options: QrConfig? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var cp: CameraPreview? = null
    private val AUTOLIGHTMIN = 10f
    private var soundPool: SoundPool? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //        Log.i("zBarLibary", "version: "+BuildConfig.VERSION_NAME);
        val scan_type = QrConfig.TYPE_QRCODE
        var scan_view_type = 0
        var screen = 1
        val line_style = ScanLineView.style_radar

        screen = QrConfig.SCREEN_PORTRAIT
        scan_view_type = QrConfig.SCANVIEW_TYPE_QRCODE

        options = QrConfig.Builder()
            .setDesText("扫描设备") //扫描框下文字
            .setShowDes(false) //是否显示扫描框下面文字
            .setShowLight(true) //显示手电筒按钮
            .setShowTitle(false) //显示Title
            .setShowAlbum(false) //显示从相册选择按钮
            .setNeedCrop(false) //是否从相册选择后裁剪图片
            .setCornerColor(Color.parseColor("#FFFFFF")) //设置扫描框颜色
            .setLineColor(Color.parseColor("#e631436c")) //设置扫描线颜色
            .setLineSpeed(QrConfig.LINE_MEDIUM) //设置扫描线速度
            .setScanType(scan_type) //设置扫码类型（二维码，条形码，全部，自定义，默认为二维码）
            .setScanViewType(scan_view_type) //设置扫描框类型（二维码还是条形码，默认为二维码）
            .setCustombarcodeformat(QrConfig.BARCODE_PDF417) //此项只有在扫码类型为TYPE_CUSTOM时才有效
            .setPlaySound(true) //是否扫描成功后bi~的声音
            .setDingPath(R.raw.qrcode) //设置提示音(不设置为默认的Ding~)
            .setIsOnlyCenter(false) //是否只识别框中内容(默认为全屏识别)
            .setTitleText("") //设置Tilte文字
            .setTitleBackgroudColor(Color.parseColor("#FFFFFF")) //设置状态栏颜色
            .setTitleTextColor(Color.WHITE) //设置Title文字颜色
            .setShowZoom(false) //是否开始滑块的缩放
            .setAutoZoom(false) //是否开启自动缩放(实验性功能，不建议使用)
            .setFingerZoom(false) //是否开始双指缩放
            .setDoubleEngine(true) //是否开启双引擎识别(仅对识别二维码有效，并且开启后只识别框内功能将失效)
            .setScreenOrientation(screen) //设置屏幕方式
            .setOpenAlbumText("") //打开相册的文字
            .setLooperScan(false) //是否连续扫描二维码
            .setLooperWaitTime(5 * 1000) //连续扫描间隔时间
            .setScanLineStyle(line_style) //扫描线样式
            .setAutoLight(false) //自动灯光
            .setShowVibrator(true) //是否震动提醒
            .create()
        initView(view)
    }

    private fun initView(view: View) {
        cp = view.findViewById<View>(cn.bertsir.zbar.R.id.cp) as CameraPreview
        //bi~
        soundPool = SoundPool(10, AudioManager.STREAM_SYSTEM, 5)

        viewBind.vsbZoom.setVisibility(if (options!!.isShow_zoom) View.VISIBLE else View.GONE)
        viewBind.sv.setCornerColor(options!!.corneR_COLOR)
        viewBind.sv.setLineSpeed(options!!.getLine_speed())
        viewBind.sv.setLineColor(options!!.linE_COLOR)
        viewBind.sv.setScanLineStyle(options!!.getLine_style())
//        viewBind.ivClose.setOnClickListener(this)
        initParam()
    }
    override
    fun onClick(v: View) {
        if (v.id == cn.bertsir.zbar.R.id.iv_album) {
//            fromAlbum()
        } else if (v.id == R.id.iv_flash) {
            if (cp != null) {
                cp!!.setFlash()
            }
        }/* else if (v.id == R.id.iv_close) {
//            setFragmentResult()
//            setResult(com.metawatch.app.ui.device.CustomQRActivity.RESULT_CANCELED) //兼容混合开发
//            finish()
            findNavController().popBackStack()
        }*/
    }

    override fun onResume() {
        super.onResume()
        if (cp != null) {
            cp!!.setScanCallback(resultCallback)
            cp!!.start()
        }

        if (sensorManager != null) {
            //一般在Resume方法中注册
            /**
             * 第三个参数决定传感器信息更新速度
             * SensorManager.SENSOR_DELAY_NORMAL:一般
             * SENSOR_DELAY_FASTEST:最快
             * SENSOR_DELAY_GAME:比较快,适合游戏
             * SENSOR_DELAY_UI:慢
             */
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private fun initParam() {
        when (options!!.screeN_ORIENTATION) {
            QrConfig.SCREEN_LANDSCAPE -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            QrConfig.SCREEN_PORTRAIT -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            QrConfig.SCREEN_SENSOR -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
            else -> activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        Symbol.scanType = options!!.getScan_type()
        Symbol.scanFormat = options!!.getCustombarcodeformat()
        Symbol.is_only_scan_center = options!!.isOnly_center
        Symbol.is_auto_zoom = options!!.isAuto_zoom
        Symbol.doubleEngine = options!!.isDouble_engine
        Symbol.looperScan = options!!.isLoop_scan
        Symbol.looperWaitTime = options!!.getLoop_wait_time()
        Symbol.screenWidth = QRUtils.getInstance().getScreenWidth(requireContext())
        Symbol.screenHeight = QRUtils.getInstance().getScreenHeight(requireContext())
        if (options!!.isAuto_light) {
            getSensorManager()
        }
    }

    /**
     * 获取光线传感器
     */
    fun getSensorManager() {
        sensorManager = requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        if (sensorManager != null) {
            sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)
        }
    }

    /**
     * 识别结果回调
     */
    private val resultCallback = ScanCallback { result ->
        if (options!!.isPlay_sound) {
            soundPool?.play(1, 1f, 1f, 0, 0, 1f)
        }
        if (options!!.isShow_vibrator) {
            QRUtils.getInstance().getVibrator(requireContext().applicationContext)
        }
        if (cp != null) {
            cp!!.setFlash(false)
        }
        this::class.simpleName?.let { Timber.tag(it).i("scanResult=$result") }
        setFragmentResult(DeviceBindFragment.DEVICE_QR_CODE, Bundle().apply {
            putSerializable(DeviceBindFragment.EXTRA_SCAN_RESULT, result)
        })
        findNavController().popBackStack()
    }
    companion object {
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val light = event!!.values[0]
        if (light < AUTOLIGHTMIN) { //暂定值
            if (cp!!.isPreviewStart) {
                cp!!.setFlash(true)
                sensorManager!!.unregisterListener(this, sensor)
                sensor = null
                sensorManager = null
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

}