package com.base.sdk.entity.apps

/**
 * Notification(通知)
 */
class WmNotification(
    /**
     * Notification type(通知类型)
     */
    var notificationType: WmNotificationType = WmNotificationType.WHATSAPP,
    /**
     * Notification title(通知标题)
     */
    var title: String?,
    /**
     * Notification content(通知内容)
     */
    var content: String?,
    /**
     * Notification sub content(通知子内容)
     */
    var subContent: String?,
) {
    override fun toString(): String {
        return "WmNotification(notificationType=$notificationType, title='$title', content='$content', subContent='$subContent')"
    }
}