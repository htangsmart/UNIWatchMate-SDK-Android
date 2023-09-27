package com.base.sdk.exception

/**
 * 表示设备断开的异常
 */
class WmDisconnectedException(
    address: String? = null
) : WmException()