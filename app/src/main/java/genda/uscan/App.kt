package genda.uscan

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.PermissionChecker
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.functions.FirebaseFunctions
import genda.uscan.services.UscanService
import genda.uscan.utils.Logger
import genda.uscan.worker.UscanWorker
import java.util.concurrent.TimeUnit

class App : Application(), DefaultLifecycleObserver {

    companion object {
        private var instance: App? = null

        fun get(): App {
            return this.instance!!
        }

        fun getApplicationContext(): Context {

            return get().applicationContext!!
        }
    }

    /**
     * This method will be called after the Application onCreate method returns.
     */
    override fun onCreate() {
        super<Application>.onCreate()
        instance = this

//        startUscanService()
    }

    /**
     * This method will be called after the LifecycleOwner's onStart method returns.
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    /**
     * This method will be called before the LifecycleOwner's onStop method is called.
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

     fun startUscanService() {
//        Logger.d("Try Uscan service start from APP")
//        startForegroundService(Intent(this, UscanService().javaClass))
//        Logger.d("Uscan service start from APP")
    }

    fun isAllNeededPermissionsGranted(): Boolean {

        return (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S &&
                checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ||
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                checkPermission(Manifest.permission.BLUETOOTH_SCAN))
    }

    fun checkPermission(permissionName: String): Boolean {

        val isPermissionGranted =
            PermissionChecker.checkCallingOrSelfPermission(
                applicationContext,
                permissionName
            ) == PackageManager.PERMISSION_GRANTED

        Logger.d("Check if the permission $permissionName granted = $isPermissionGranted")
        return isPermissionGranted
    }

    fun createWorkManager(){
        val request = PeriodicWorkRequestBuilder<UscanWorker>(
            PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
            TimeUnit.MILLISECONDS,
            PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
            TimeUnit.MILLISECONDS)

            .addTag("TRACKER_WORKER")
//            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "TRACKER_WORKER",
            ExistingPeriodicWorkPolicy.KEEP,
            request)
    }

    fun triggerFCM(){


    }
}