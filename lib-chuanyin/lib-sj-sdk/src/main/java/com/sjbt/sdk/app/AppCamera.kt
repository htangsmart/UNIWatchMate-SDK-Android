package com.sjbt.sdk.app

import com.base.sdk.`interface`.app.AbAppCamera
import com.base.sdk.`interface`.app.WMCameraFlashMode
import com.base.sdk.`interface`.app.WMCameraPosition
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

class AppCamera :AbAppCamera() {

    override fun isSupport(): Boolean {
        TODO("Not yet implemented")
    }

    override var observeCameraState: Observable<Boolean>
        get() = TODO("Not yet implemented")
        set(value) {}

    override var observeCameraFlash: Observable<WMCameraFlashMode>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun cameraFlashSwitch(type: WMCameraFlashMode): Observable<WMCameraFlashMode> {
        TODO("Not yet implemented")
    }

    override var observeCameraFrontBack: Observable<WMCameraPosition>
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun cameraBackSwitch(isBack: WMCameraPosition): Observable<WMCameraPosition> {
        TODO("Not yet implemented")
    }

    override fun isCameraPreviewEnable(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCameraPreviewReady(): Single<Boolean> {
        TODO("Not yet implemented")
    }

    override fun updateCameraPreview(data: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun stopCameraPreview() {
        TODO("Not yet implemented")
    }
}