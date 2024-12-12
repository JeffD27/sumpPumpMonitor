package com.example.sumppumpbeta3

import kotlinx.datetime.Instant
lateinit var notificationServerErrorDeployed: Pair<Boolean, Instant>
lateinit var notificationWaterLevelSensorErrorDeployed: Pair<Boolean, Instant>
lateinit var notificationWaterLevelSensorErrorBDeployed: Pair<Boolean, Instant>
lateinit var notificationACPowerDeployed: Pair<Boolean, Instant>
lateinit var notificationHighWaterDeployed: Pair<Boolean, Instant>
lateinit var notificationMainRunWarnDeployed: Pair<Boolean, Instant>
lateinit var notificationBackupRan: Pair<Boolean, Instant>
lateinit var notificationWaterTooLow: Pair<Boolean, Instant>
lateinit var notificationBattery12Low: Pair<Boolean, Instant>
lateinit var notificationNoPumpControl: Pair<Boolean, Instant>
lateinit var notificationMainRunning: Pair<Boolean, Instant>
class LateClass {


    fun isNotificationServerErrorDeployedInitialized() = ::notificationServerErrorDeployed.isInitialized
    fun isNotificationWaterLevelSensorErrorDeployedInitialized() = ::notificationWaterLevelSensorErrorDeployed.isInitialized
    fun isNotificationWaterLevelSensorErrorBDeployedInitialized() = ::notificationWaterLevelSensorErrorBDeployed.isInitialized
    fun isNotificationACPowerDeployedInitialized() = ::notificationACPowerDeployed.isInitialized
    fun isNotificationHighWaterDeployedInitialized() = ::notificationHighWaterDeployed.isInitialized
    fun isNotificationMainRunWarnDeployedInitialized() = ::notificationMainRunWarnDeployed.isInitialized
    fun isNotificationBackupRanInitialized() = ::notificationBackupRan.isInitialized
    fun isNotificationWaterTooLowInitialized() = ::notificationWaterTooLow.isInitialized
    fun isNotificationBattery12LowInitialized() = ::notificationBattery12Low.isInitialized
    fun isNotificationNoPumpControlInitialized() = ::notificationNoPumpControl.isInitialized
    fun isNotificationMainRunningInitialized() = ::notificationMainRunning.isInitialized
}
