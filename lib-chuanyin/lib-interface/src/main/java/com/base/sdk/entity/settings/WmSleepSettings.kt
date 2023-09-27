package com.base.sdk.entity.settings

/**
 * The date and time information of the watch
 * (日期与时间同步信息)
 */
data class WmSleepSettings(
   var open:Boolean,
   var startTime:Long,
   var endTime:Long){
   override fun toString(): String {
      return "WmSleepSettings(open=$open, startTime=$startTime, endTime=$endTime)"
   }
}

