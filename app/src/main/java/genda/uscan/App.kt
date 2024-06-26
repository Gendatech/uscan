package genda.uscan

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.text.format.DateFormat
import android.util.Log
import androidx.core.content.PermissionChecker
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import com.datadog.android.Datadog
import com.datadog.android.core.configuration.Configuration
import com.datadog.android.core.configuration.Credentials
import com.datadog.android.privacy.TrackingConsent
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.ktx.Firebase
import genda.uscan.services.UscanService
import genda.uscan.utils.Logger
import genda.uscan.worker.StartStopWorker
import genda.uscan.worker.UscanWorker
import java.util.Date
import java.util.concurrent.TimeUnit

class App : Application(), DefaultLifecycleObserver {

    val workerName = "TRACKER_WORKER"

    companion object {
        private var instance: App? = null
        var sessionDate = "not initialized ${DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())}"

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

        cancelWorkManager(from = "onCreate")

        Log.d("uscan","App onCreate 0")
        initDataDog()

        Logger.d("App onCreate")

//        startUscanService()
    }

    /**
     * This method will be called after the LifecycleOwner's onStart method returns.
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        cancelWorkManager(from = "onDestroy")
    }

    /**
     * This method will be called before the LifecycleOwner's onStop method is called.
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
    }

    private fun initDataDog() {
        val configuration = Configuration.Builder(
            logsEnabled = true,
            tracesEnabled = true,
            crashReportsEnabled = true,
            rumEnabled = false
        ).build()
        val credentials = Credentials(
            "pub7e7d36f4c11eabefe6e7401bf4dbf36a",
            "uscan",
            BuildConfig.VERSION_NAME,
            BuildConfig.APPLICATION_ID)

        Datadog.initialize(this, credentials, configuration, TrackingConsent.GRANTED)
    }

     fun startUscanService(from: String) {

//        Logger.d("Try Uscan service start from $from")
//        startForegroundService(Intent(this, UscanService().javaClass))
//
//         val logEntry = mapOf(
//             "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
//             "from" to from
//         )
//
//         val logPath: DocumentReference =
//                 Firebase.firestore.collection("devices").document(Build.MODEL).collection("sessions")
//                     .document(App.sessionDate)
//
//         logPath.update("serviceStart", FieldValue.arrayUnion(logEntry))
//             .addOnSuccessListener {
//                 // Log was successfully added
//                 Logger.d("App start Service log message added")
//             }
//             .addOnFailureListener { e ->
//
//                 // Handle the error
//                 Logger.e("App start Service log message failed", e)
//
//                 val data = hashMapOf(
//                     "serviceStart" to arrayListOf(logEntry)
//                 )
//                 logPath.set(data)
//             }
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


    fun createWorkManager() {

        // Cancel any existing work manager
        cancelWorkManager(from = "createWorkManager")

        Logger.d("Create UscanWorker")
        sessionDate = "${DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())}"

        val isPeriodic = false
        val isRetryTest = false
        val workerPeriodicPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE

        val workData = if (isPeriodic) {
            if (isRetryTest) {
                workDataOf(
                    "isForeground" to false,
                    "runsNumber" to 1,
                    "isRetry" to true,
                    "workerPeriodicPolicy" to workerPeriodicPolicy.name
                )
            } else {
                workDataOf(
                    "isForeground" to true,
                    "runsNumber" to 1000,
                    "isRetry" to false,
                    "workerPeriodicPolicy" to workerPeriodicPolicy.name
                )
            }
        } else {
            workDataOf(
                "isForeground" to true ,
                "runsNumber" to 100000,
                "isRetry" to false
            )
        }

        if (isPeriodic) {
            val request = PeriodicWorkRequestBuilder<UscanWorker>(
                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
                TimeUnit.MILLISECONDS,
            )
                .addTag(workerName)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiresStorageNotLow(false)
                        .build()
                )
                .setInputData(workData)
                .setInitialDelay(1, TimeUnit.SECONDS)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                workerName,
                workerPeriodicPolicy,
                request
            )

        } else {
            val request = OneTimeWorkRequestBuilder<UscanWorker>()
                .addTag(workerName)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                        .setRequiresBatteryNotLow(false)
                        .setRequiresCharging(false)
                        .setRequiresDeviceIdle(false)
                        .setRequiresStorageNotLow(false)
                        .build()
                )
                .setInputData(workData)
                .setBackoffCriteria(BackoffPolicy.LINEAR, WorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)

                .build()

            WorkManager.getInstance(this).enqueueUniqueWork(
                workerName,
                ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
//    fun createWorkManager2() {
//
//        // Cancel any existing work manager
////        cancelWorkManager(from = "createWorkManager")
//
//        Logger.d("Create UscanWorker")
//        sessionDate = "${DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())}"
//
//        val isPeriodic = true
//        val isRetryTest = true
//
//        val workData = if (isPeriodic) {
//            if (isRetryTest) {
//                workDataOf(
//                    "isForeground" to false,
//                    "runsNumber" to 1,
//                    "isRetry" to true
//                )
//            } else {
//                workDataOf(
//                    "isForeground" to false,
//                    "runsNumber" to 10,
//                    "isRetry" to false
//                )
//            }
//        } else {
//            workDataOf(
//                "isForeground" to true,
//                "runsNumber" to 100000,
//                "isRetry" to false
//            )
//        }
//
//        if (isPeriodic) {
//            val request = PeriodicWorkRequestBuilder<UscanWorker>(
//                PeriodicWorkRequest.MIN_PERIODIC_INTERVAL_MILLIS,
//                TimeUnit.MILLISECONDS,
//                PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS,
//                TimeUnit.MILLISECONDS
//            ).addTag(workerName)
//                .setConstraints(
//                    Constraints.Builder()
//                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//                        .setRequiresBatteryNotLow(false)
//                        .setRequiresCharging(false)
//                        .setRequiresDeviceIdle(false)
//                        .setRequiresStorageNotLow(false)
//                        .build()
//                )
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 90000, TimeUnit.MILLISECONDS)
//                    .setInputData(workData)
//                .build()
//
//            WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//                workerName,
//                ExistingPeriodicWorkPolicy.KEEP,
//                request
//            )
//        } else {
//            val request =  OneTimeWorkRequestBuilder<UscanWorker>().addTag(workerName)
//                .setConstraints(
//                    Constraints.Builder()
//                        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
//                        .setRequiresBatteryNotLow(false)
//                        .setRequiresCharging(false)
//                        .setRequiresDeviceIdle(false)
//                        .setRequiresStorageNotLow(false)
//                        .build()
//                )
//                .setBackoffCriteria(BackoffPolicy.LINEAR, 90000, TimeUnit.MILLISECONDS)
//                .setInputData(workData)
//                .build()
//
//            WorkManager.getInstance(this).enqueueUniqueWork(
//                workerName,
//                ExistingWorkPolicy.KEEP,
//                request
//            )
//        }
//
//    }

        fun checkWorkStatus(owner: LifecycleOwner) {
            WorkManager.getInstance(this).getWorkInfosByTagLiveData(workerName).observeForever { workInfos ->
                if (workInfos != null && workInfos.isNotEmpty()) {
                    for (workInfo in workInfos) {
                        Logger.d("UscanWorker checkWorkStatus: $workerName, state: ${workInfo.state}")
                    }
                } else {
                    Logger.d("UscanWorker checkWorkStatus: $workerName, no work found")
                }
            }
        }


    fun cancelWorkManager(from: String) {
        WorkManager.getInstance(this).cancelAllWorkByTag(workerName)

        val workData = mapOf(
            "status" to "canceled",
            "from" to from,
            "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
        )

//        get().updateFirestore("workerRuns", workData)
    }

    fun updateFirestore(fieldName: String, data: Map<String, Any>) {
        val logPath: DocumentReference = Firebase.firestore.collection("devices")
            .document(Build.MODEL).collection("sessions").document(App.sessionDate)

        // Update the logs field
        logPath.update(fieldName, FieldValue.arrayUnion(data))
            .addOnSuccessListener {
                // Log was successfully added
                Logger.d("updateFirestore message $fieldName added")
            }
            .addOnFailureListener { e ->
                // Handle the error
                Logger.e("updateFirestore message $fieldName failed with error", e)

                // Create the logs field if it doesn't exist
                logPath.set(hashMapOf(
                    fieldName to arrayListOf(data)
                ))
            }
    }

    fun triggerFCM(){


    }

    fun getBatteryPercentage(): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            applicationContext.registerReceiver(null, ifilter)
        }
        val batteryPct: Int = batteryStatus?.let { intent ->
            val level: Int = intent.getIntExtra("level", -1)
            val scale: Int = intent.getIntExtra("scale", -1)
            (level / scale.toFloat() * 100).toInt()
        } ?: -1
        return batteryPct
    }
}