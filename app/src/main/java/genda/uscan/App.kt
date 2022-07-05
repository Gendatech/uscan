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
import genda.uscan.service.UscanService
import genda.uscan.utils.Logger

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

        startUscanService()
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

    private fun startUscanService() {
        startForegroundService(Intent(this, UscanService().javaClass))
        Logger.d("Uscan service start from APP")
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
}