# UNIWatchMate
UNIWatchMate智能手表的接口框架，负责与手表设备通信等功能的封装，向上提供给App操作智能手表的相关接口，向下可以对接其他手表的SDK。

# 注意事项

Sample里面没有展示的功能，都是不可用状态

-------------------------------------------------------------------------------------------------------------------------

# 导入SDK

使用sdk必须引入的库

```
dependencies {
//Required
    implementation(name: 'lib-api', ext: 'aar')
    implementation(name: 'lib-interface', ext: 'aar')
    implementation(name: 'lib-sj-sdk', ext: 'aar')//
    implementation(name: 'lib_jni_opencv_1.0.1_202310091127', ext: 'aar')
    
    implementation "androidx.core:core-ktx:$core_ktx_version"
    implementation "com.google.code.gson:gson:$gson_version"
    implementation "io.reactivex.rxjava3:rxjava:$rx_java_version"
    implementation "io.reactivex.rxjava3:rxandroid:$rx_android_version"

    implementation "androidx.appcompat:appcompat:$appcompat_version"
    implementation "com.google.android.material:material:$material_version"
    implementation "com.guolindev.permissionx:permissionx:$permissionx_version"

}
//Required
```

# 用法

- 使用`UNIWatchMate`进行初始化、扫描、搜索、回连设备
- 使用`UNIWatchMate.wmApps`进行 应用相关操作,接口层为AbWmApps
```
  /**
* 应用模块功能聚合
  */
  abstract class AbWmApps {
  /**
    * 闹钟功能
      */
      abstract val appAlarm: AbAppAlarm

  /**
    * 相机功能
      */
      abstract val appCamera: AbAppCamera

  /**
    * 通讯录
      */
      abstract val appContact: AbAppContact

  /**
    * 查找功能
      */
      abstract val appFind: AbAppFind

  /**
    * 天气功能
      */
      abstract val appWeather: AbAppWeather

  /**
    * 运动功能
      */
      abstract val appSport: AbAppSport

  /**
    * 通知功能
      */
      abstract val appNotification: AbAppNotification

  /**
    * 表盘
      */
      abstract val appDial: AbAppDial

  /**
    * 语言
      */
      abstract val appLanguage: AbAppLanguage

  /**
    * 音乐控制
      */
      abstract val appMusicControl: AbAppMusicControl

  /**
    * 时间设置
      */
      abstract val appDateTime: AbAppDateTime

}
```
- 使用`UNIWatchMate.AbWmSettings`进行 设置相关操作,接口层为AbWmSettings
```
abstract class AbWmSettings {
  /**
    * sportGoalSetting 运动目标设置
      */
      abstract val settingSportGoal: AbWmSetting<WmSportGoal>

  /**
    * personalInfoSetting 个人信息设置
      */
      abstract val settingPersonalInfo: AbWmSetting<WmPersonalInfo>

  /**
    * Sedentary reminder(久坐提醒)
      */
      abstract val settingSedentaryReminder: AbWmSetting<WmSedentaryReminder>

  /**
    * soundAndHapticSetting(声音和触感设置)
      */
      abstract val settingSoundAndHaptic: AbWmSetting<WmSoundAndHaptic>

  /**
    * unitInfoSetting(单位设置)
      */
      abstract val settingUnitInfo: AbWmSetting<WmUnitInfo>

  /**
    * wistRaiseSetting 抬腕设置
      */
      abstract val settingWistRaise: AbWmSetting<WmWistRaise>

  /**
    * appViewSetting 应用视图设置
      */
      abstract val settingAppView: AbWmSetting<WmAppView>

  /**
    * drinkWaterSetting 喝水提醒设置
      */
      abstract val settingDrinkWater: AbWmSetting<WmSedentaryReminder>

  /**
    * rateSetting 心率提醒设置
      */
      abstract val settingHeartRate: AbWmSetting<WmHeartRateAlerts>

  /**
    * sleepSetting 睡眠设置
      */
      abstract val settingSleepSettings: AbWmSetting<WmSleepSettings>
      }
```
- 使用`UNIWatchMate.AbWmSyncs`进行 数据同步相关操作,接口层为AbWmSyncs
```
**
 * 同步数据
 */
abstract class AbWmSyncs {
    /**
     * sync step(同步步数)
     */
    abstract val syncStepData: AbSyncData<List<WmStepData>>

    /**
     * sync oxygen(同步血氧)
     */
    abstract val syncOxygenData: AbSyncData<List<WmOxygenData>>

    /**
     * syncCalories(同步卡路里)
     */
    abstract val syncCaloriesData: AbSyncData<List<WmCaloriesData>>

    /**
     * syncSleep(同步睡眠)
     */
    abstract val syncSleepData: AbSyncData<List<WmSleepData>>

    /**
     * syncRealtimeRate(同步实时心率)
     */
    abstract val syncRealtimeRateData: AbSyncData<List<WmRealtimeRateData>>

    /**
     * syncAvgHeartRate(同步平均心率)
     */
    abstract val syncHeartRateData: AbSyncData<List<WmHeartRateData>>

    /**
     * syncDistance(同步距离)
     */
    abstract val syncDistanceData: AbSyncData<List<WmDistanceData>>

    /**
     * syncActivity(同步日常活动)
     */
    abstract val syncActivityData: AbSyncData<List<WmActivityData>>

    /**
     * syncSportSummary(同步运动小结)
     */
    abstract val syncSportSummaryData: AbSyncData<List<WmSportSummaryData>>

    /**
     * syncDeviceInfo(同步设备信息)
     */
    abstract val syncDeviceInfoData: AbSyncData<WmDeviceInfo>

    /**
     * syncTodayInfo(同步当日数据)
     */
    abstract val syncTodayInfoData: AbSyncData<WmTodayTotalData>

    /**
     * sync(更新电量)
     */
    abstract val syncBatteryInfo: AbSyncData<WmBatteryInfo>
}
```

- 使用`UNIWatchMate.wmTransferFile`进行 文件传输(OTA,安装表盘,音乐等)相关操作,AbWmTransferFile
```
enum class FileType(val type: Int) {
    MUSIC(1),//MP3类型
    OTA(2),//设备ota
    DIAL(3),//表盘
    DIAL_COVER(4),//表盘封面
    OTA_UPEX(5),//设备ota_upex
    TXT(6),//设备ota_upex
    AVI(7),//设备ota_upex
    SPORT(8),//运动文件（备用未定）
}
```

## 1. 初始化

调用‘UNIWatchMate’的init方法 注册要使用的设备操作对象，不同的厂商可能有不同的对象，下面代码

```

 UNIWatchMate.init(
        application, listOf(
            SJUniWatchImpl(application, 10000),//绅聚设备操作对象
//            FcUniWatchImpl(application)//拓步设备操作对象
        )
    )

```

## 2. 扫描设备
以下三种方式都会得到WmDevice的对象，包含地址，蓝牙名称，设备归属等信息

### 2.1 扫码连接

```
UNIWatchMate.connectScanQr(
                        scanContent,
                        userInfo
                        )
```

### 2.2 通过mac地址（搜索后连接及回连）

```
UNIWatchMate.connect(
                address,
                WmBindInfo(userId, name, BindType.DISCOVERY)
            )

```

### 2.3 搜索

```
UNIWatchMate.startDiscovery(timeoutPeriod, WmTimeUnit.MILLISECONDS)

```
### 2.4 断开连接
```
 UNIWatchMate.disconnect()

```
### 2.5 监听连接状态
```
      UNIWatchMate.observeConnectState.asFlow().collect{
            if (it == WmConnectState.VERIFIED) {
                //已连接并通过校验
            }
        }
 //连接状态
    enum class WmConnectState {
        BT_DISABLE,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        VERIFIED,
        }       

```





