package genda.uscan.utils

import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.core.content.edit
import genda.uscan.App
import genda.uscan.BuildConfig
import java.util.*

//TODO: complete mess... not clear what is save in DB and what is saved here... what is server from App class and what is servered here
// refactor all this
object PersistentData {
    const val PREF_KEY_UID = "user_id"
    private const val PREF_KEY_CONFIGURATION_SET_NUMBER = "configuration_set_number"
    private const val PREF_KEY_SCAN_DURATION = "scan_duration"
    private const val PREF_KEY_DURATION_BETWEEN_SCANS = "duration_between"
    private const val PREF_KEY_BG_TRACKING_ENABLED = "bg_tracking"
    private const val PREF_KEY_BREADCRUMB = "last_breadcrumb"
    private const val PREF_KEY_STARBUCK1_SHOWN = "starbucks1"
    private const val PREF_KEY_NULL_BEAONCON_TIMESTAMP = "null_beacon_timestamp"
    private const val PREF_KEY_LAST_REPORT_WAS_EXIT = "last_report_was_exit"
    private const val PREF_KEY_LIFETIME_BREADCRUMS = "lifetime_breadcrum_count"

    //    private const val PREF_KEY_USER_NAME = "user_name"
    private const val PREF_KEY_BEACON_POWER = "beacons_power"
    private const val PREF_KEY_BEACON_ENV_FACTOR = "env_factor"
    private const val PREF_KEY_LAST_SCAN_TIMESTAMP = "last_scan_ts"
    private const val PREF_KEY_LAST_BREADCRUMB_ID = "last_breadcrumb_id"
    private const val PREF_KEY_USER_STATE = "user_state"
    private const val PREF_KEY_USER_ACCESS_ID = "user_access_request_id"
    private const val PREF_KEY_NOTIFICATION_SAFETY_ALERT_IDS = "safety_alerts_ids"
    private const val PREF_KEY_USER_FIRST_TIME_MESSAGING = "user_first_time_messaging"
    private const val PREF_KEY_USER_INVITATION_ID = "user_invitation_id"
    private const val PREF_KEY_PERMISSION_BLOCKED_ONCE = "permission_blocked_once"
    private const val PREF_KEY_LOCATION_PERMISSION_BLOCKED = "location_permission_blocked"
    private const val PREF_KEY_LAST_PERMISSION_REQUEST_TIMESTAMP = "permission_request_timestamp"


    private const val PREF_KEY_WRONG_BEAONCON_TIMESTAMP = "wrong_beacon_timestamp"
    private const val PREF_KEY_WRONG_BEAONCON_SITE_NAME = "wrong_beacon_site_name"

    // private const val PREF_KEY_SCAN_ERROR_LAST_QUARTER = "scan_error_last_quarter"
    private const val PREF_KEY_SCAN_ERROR_LAST_TIMESTAMP = "scan_error_last_timestamp_" + BuildConfig.VERSION_CODE // Let keep the last error timestamp only between version


    fun getPrefs(): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(App.get())
    }

    fun getConfigurationSetNumber(): Int {
        return getPrefs().getInt(PREF_KEY_CONFIGURATION_SET_NUMBER, 1)

    }

    fun setConfigurationSetNumber(setNumber: Int) {
        return getPrefs().edit {
            putInt(PREF_KEY_CONFIGURATION_SET_NUMBER, setNumber)
        }
    }


    fun getSafetyNotificationIds(): List<String>? {
        return getPrefs()
            .getString(PREF_KEY_NOTIFICATION_SAFETY_ALERT_IDS, "")
            ?.split(';')
    }

    fun setSafetyNotificationIds(ids: List<String>) {
        return getPrefs().edit {
            putString(PREF_KEY_NOTIFICATION_SAFETY_ALERT_IDS, ids.joinToString(";"))
        }
    }

    fun getScanDurationSeconds(): Int {
        // return 30 //getPrefs().getInt(PREF_KEY_SCAN_DURATION, 15)
        return 30
    }

    fun getSleepDurationBetweenScansSeconds(): Int {
        return getPrefs().getInt(PREF_KEY_DURATION_BETWEEN_SCANS, 1) * 60
        //return 30
    }

    @Deprecated("Use getSleepDurationBetweenScansSeconds")
    fun getDurationBetweenScansMinutes(): Int {
        return getPrefs().getInt(PREF_KEY_DURATION_BETWEEN_SCANS, 1)
    }

    fun saveDurationBetweenScannsMinutes(minutes: Int) {
        getPrefs().edit {
            putInt(PREF_KEY_DURATION_BETWEEN_SCANS, minutes)
        }
    }

    fun saveScanDuration(seconds: Int) {
        getPrefs().edit {
            putInt(PREF_KEY_SCAN_DURATION, seconds)
        }
    }

    fun getBackgroundTrackingEnabled(): Boolean {
        return getPrefs().getBoolean(PREF_KEY_BG_TRACKING_ENABLED, true)
    }

    fun saveBackgroundTrackingEnabled(isEnable: Boolean) {
        return getPrefs().edit {
            putBoolean(PREF_KEY_BG_TRACKING_ENABLED, isEnable)
        }
    }

    fun saveBeconsMeasuredPower(power: Int) {
        return getPrefs().edit {
            putInt(PREF_KEY_BEACON_POWER, power)
        }

    }

    fun getBeconsMeasuredPower(): Int {
        return getPrefs().getInt(PREF_KEY_BEACON_POWER, -60)
    }

    fun saveBeconsEnvironmentalFactor(factor: Int) {
        return getPrefs().edit {
            putInt(PREF_KEY_BEACON_ENV_FACTOR, factor)
        }
    }

    fun getBeconsEnvironmentalFactor(): Int {
        return getPrefs().getInt(PREF_KEY_BEACON_ENV_FACTOR, 2)
    }

    fun getSiteyeDisplayTolerancMS(): Long {
        return 600 * 60 * 1000 //One hour
    }

    fun stampLastScan() {
        getPrefs().edit {
            putLong(PREF_KEY_LAST_SCAN_TIMESTAMP, Date().time)
        }
    }

    fun getLastScanTime(): Long {
        return getPrefs().getLong(PREF_KEY_LAST_SCAN_TIMESTAMP, 0)
    }

    fun setUserState(stateName: String) {
        getPrefs().edit {
            putString(PREF_KEY_USER_STATE, stateName)
        }
    }

    fun getUserStateName(): String? {
        return getPrefs().getString(PREF_KEY_USER_STATE, null)
    }

    fun setUserInvitationId(invitationId: String) {
        getPrefs().edit {
            putString(PREF_KEY_USER_INVITATION_ID, invitationId)
        }
    }

    fun getUserInvitationId(): String? {
        return getPrefs().getString(PREF_KEY_USER_INVITATION_ID, null)
    }

    fun getSnapshotsTTL(): Long {
        return 15 * 60 * 1000 // !5 minutes
    }

    fun stampStarbucks1Shown() {
        getPrefs().edit {
            putBoolean(PREF_KEY_STARBUCK1_SHOWN, true)
        }
    }

    fun isStarbuck1ToBeShown(): Boolean {
        return !getPrefs().getBoolean(PREF_KEY_STARBUCK1_SHOWN, false)
    }

    fun stampNullBeacoonTimestamp() {
        getPrefs().edit {
            putLong(PREF_KEY_NULL_BEAONCON_TIMESTAMP, Date().time)
        }
    }

    fun stampBeaconIsNotNull() {
        getPrefs().edit {
            remove(PREF_KEY_NULL_BEAONCON_TIMESTAMP)
            putBoolean(PREF_KEY_LAST_REPORT_WAS_EXIT, false)
        }
    }

    fun stampLastReportExit() {
        getPrefs().edit {
            putBoolean(PREF_KEY_LAST_REPORT_WAS_EXIT, true)
        }
    }

    fun isLastReportExit(): Boolean {
        return getPrefs().getBoolean(PREF_KEY_LAST_REPORT_WAS_EXIT, false)
    }


    fun getNullBeacoonTimestamp(): Long? {
        var ret: Long? = null
        if (getPrefs().contains(PREF_KEY_NULL_BEAONCON_TIMESTAMP)) {
            ret = getPrefs().getLong(PREF_KEY_NULL_BEAONCON_TIMESTAMP, 0)
        }
        return ret
    }


    fun getAndIncreaseBreadcrumbLifeTimeCount(): Long {
        val currentCount = getPrefs().getLong(PREF_KEY_LIFETIME_BREADCRUMS, 0)
        getPrefs().edit {
            putLong(PREF_KEY_LIFETIME_BREADCRUMS, currentCount + 1)
        }
        return currentCount
    }

    fun getBreadcrumbLifeTimeCount(): Long {
        return getPrefs().getLong(PREF_KEY_LIFETIME_BREADCRUMS, 0)
    }

    fun stampWrongBeaconSiteId(siteId: String) {
        getPrefs().edit {
            putString(PREF_KEY_WRONG_BEAONCON_TIMESTAMP, siteId)
        }
    }

    fun clearWrongSite() {
        getPrefs().edit {
            remove(PREF_KEY_WRONG_BEAONCON_TIMESTAMP)
        }
    }

    fun getWrongSiteId(): String? {
        var ret: String? = null
        if (getPrefs().contains(PREF_KEY_WRONG_BEAONCON_TIMESTAMP)) {
            ret = getPrefs().getString(PREF_KEY_WRONG_BEAONCON_TIMESTAMP, null)
        }
        return ret
    }

    fun stampUserFirstTimeMessaging(isFirstTimeUserMessaging: Boolean) {
        getPrefs().edit {
            putBoolean(PREF_KEY_USER_FIRST_TIME_MESSAGING, isFirstTimeUserMessaging)
        }
    }

    fun getUserFirstTimeMessaging(): Boolean {
        var isFirstTimeUserMessaging = true
        if (getPrefs().contains(PREF_KEY_USER_FIRST_TIME_MESSAGING)) {
            isFirstTimeUserMessaging =
                getPrefs().getBoolean(PREF_KEY_USER_FIRST_TIME_MESSAGING, true)
        }
        return isFirstTimeUserMessaging
    }

    fun stampScanError() {
        Logger.e("Stamping SCAN ERROR !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!")
        getPrefs().edit {
            putLong(PREF_KEY_SCAN_ERROR_LAST_TIMESTAMP, Date().time)
        }
    }

    fun getScanErrorTimestamp(): Long {
        var ret = Long.MAX_VALUE
        if (getPrefs().contains(PREF_KEY_SCAN_ERROR_LAST_TIMESTAMP)) {
            ret = getPrefs().getLong(PREF_KEY_SCAN_ERROR_LAST_TIMESTAMP, Long.MAX_VALUE)
        }
        return ret
    }

    fun setPermissionBlockedOnce(isUserBlockPermissionOnce: Boolean) {
        getPrefs().edit {
            putBoolean(PREF_KEY_PERMISSION_BLOCKED_ONCE, isUserBlockPermissionOnce)
        }
    }

    fun getPermissionBlockedOnce(): Boolean {
        return getPrefs().getBoolean(PREF_KEY_PERMISSION_BLOCKED_ONCE, false)
    }

    fun setPermissionsIsBlocked(isUserBlockedLocationPermission: Boolean) {
        getPrefs().edit {
            putBoolean(PREF_KEY_LOCATION_PERMISSION_BLOCKED, isUserBlockedLocationPermission)
        }
    }

    fun getPermissionsIsBlocked(): Boolean {
        return getPrefs().getBoolean(PREF_KEY_LOCATION_PERMISSION_BLOCKED, false)
    }

    fun setLastPermissionRequestTimeStamp() {
        getPrefs().edit {
            putLong(PREF_KEY_LAST_PERMISSION_REQUEST_TIMESTAMP, Date().time)
        }
    }

    fun getIsPermissionNotRequestedToday(): Boolean {
        val day = (24 * 60 * 60 * 1000).toLong()
        return Date().time - day > getPrefs().getLong(PREF_KEY_LAST_PERMISSION_REQUEST_TIMESTAMP, 0)
    }
//    fun setLastBreadcrumId(id: String) {
//        getPrefs().edit {
//            putString(PREF_KEY_LAST_BREADCRUMB_ID, id)
//        }
//    }
//
//    fun getLastBreadcrumbId():String {
//        return getPrefs().getString(PREF_KEY_LAST_BREADCRUMB_ID, "Unknown-empty")!!
//    }
}