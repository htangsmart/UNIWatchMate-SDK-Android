package com.base.sdk.entity.settings

/**
 * Sound and haptic(声音和触感)
 */
data class WmSoundAndHaptic(
    /**
     * Whether to ring when there is a call(来电时是否响铃)
     */
    var isRingtoneEnabled: Boolean,
    /**
     * Whether to have notification haptic(是否有通知触感)
     */
    var isNotificationHaptic: Boolean,
    /**
     * Whether the crown has haptic feedback(表冠是否有触感反馈)
     */
    var isCrownHapticFeedback: Boolean,
    /**
     * Whether the system has haptic feedback(系统是否有触感反馈)
     */
    var isSystemHapticFeedback: Boolean,
    /**
     * Whether to mute(是否静音)
     */
    var isMuted: Boolean
) {
    override fun toString(): String {
        return "WmSoundAndHaptic(isRingtoneEnabled=$isRingtoneEnabled, isNotificationHaptic=$isNotificationHaptic, isCrownHapticFeedback=$isCrownHapticFeedback, isSystemHapticFeedback=$isSystemHapticFeedback, isMuted=$isMuted)"
    }
}
