package com.sjbt.sdk.sample

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import com.base.sdk.entity.apps.WmConnectState
import com.sjbt.sdk.sample.di.Injector
import com.sjbt.sdk.sample.ui.LaunchActivity
import com.sjbt.sdk.sample.utils.NotificationHelper
import kotlinx.coroutines.launch
import timber.log.Timber

class DeviceService : LifecycleService() {

    private lateinit var notificationManager: NotificationManager

    private val deviceManager = Injector.getDeviceManager()

    private var hasSetForegrounded = false

    override fun onCreate() {
        super.onCreate()
        Timber.tag(TAG).i("onCreate")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        NotificationHelper.createDeviceChannel(this, notificationManager)
        lifecycleScope.launch {
            deviceManager.flowConnectorState.collect {
                changeState(it)
            }
        }
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                if (!hasSetForegrounded) {
                    changeState(deviceManager.flowConnectorState.value)
                }
            }
        })
    }

    private fun isProcessForeground(): Boolean {
        return ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    private fun changeState(state: WmConnectState) {
        val notification = createNotification(state)
        if (hasSetForegrounded || !isProcessForeground()) {
            Timber.tag(TAG).i("state notify:$state")
            notificationManager.notify(NotificationHelper.DEVICE_SERVICE_NOTIFICATION_ID, notification)
        } else {
            Timber.tag(TAG).i("state foreground:$state")
            try {
                startForeground(NotificationHelper.DEVICE_SERVICE_NOTIFICATION_ID, notification
                    .apply {
                        flags = flags or Notification.FLAG_NO_CLEAR or Notification.FLAG_FOREGROUND_SERVICE
                    })
                hasSetForegrounded = true
            } catch (e: Exception) {
                Timber.w(e)
            }
        }
    }

    private fun createNotification(state: WmConnectState): Notification {
        val device = deviceManager.flowDevice?.value
        val contentTitle = device?.name ?: getString(R.string.device_state_no_device)

        val contentText = when (state) {
            WmConnectState.BT_DISABLE -> getString(R.string.device_state_bt_disabled)
            WmConnectState.DISCONNECTED -> getString(R.string.device_state_disconnected)
            WmConnectState.CONNECTING -> getString(R.string.device_state_connecting)
            WmConnectState.CONNECTED -> getString(R.string.device_state_connected)
            WmConnectState.VERIFIED -> getString(R.string.device_state_verified)
            else -> {""}
        }

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(this@DeviceService, LaunchActivity::class.java)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        return NotificationHelper.notificationDeviceChannel(this)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .setWhen(System.currentTimeMillis())
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    companion object {
        private const val TAG = "DeviceService"
    }
}