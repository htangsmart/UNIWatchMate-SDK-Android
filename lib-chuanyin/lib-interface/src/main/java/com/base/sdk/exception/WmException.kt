package com.base.sdk.exception

/**
 * 统一抛出的异常，其他SDK抛出的异常最好转换为 WmException或其子类
 */
abstract class WmException : RuntimeException {

    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(cause: Throwable?) : super(cause)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

}