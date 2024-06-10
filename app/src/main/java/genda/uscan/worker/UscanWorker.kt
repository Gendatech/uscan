package genda.uscan.worker

import android.Manifest
import android.content.Context
import android.text.format.DateFormat
import androidx.core.content.PermissionChecker
import androidx.work.*
import genda.uscan.App
import genda.uscan.scanner.Scanner
import genda.uscan.scanner.UscanResult
import genda.uscan.utils.GendaNotification
import genda.uscan.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
class UscanWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy - HH:mm:ss:SSS")
    private val scanner: Scanner = Scanner()
    private var runNumber = 0

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        Logger.d("UscanWorker doWork")
        val isForeground: Boolean = inputData.getBoolean("isForeground", false)
        val runsNumber: Int = inputData.getInt("runsNumber", 100000)
        val isRetry: Boolean = inputData.getBoolean("isRetry", false)
        val workerPeriodicPolicy = inputData.getString("workerPeriodicPolicy") ?: "unknown"

        App.get().updateFirestore("workerRuns", mapOf(
            "status" to "running",
            "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
            "workerRunNumber" to ++runNumber,
            "isForeground" to isForeground,
            "runsNumber" to runsNumber,
            "isRetry" to isRetry,
            "workerPeriodicPolicy" to workerPeriodicPolicy,
            "isThereNotificationPermission" to (PermissionChecker.checkCallingOrSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PermissionChecker.PERMISSION_GRANTED)
        ))

        if (isForeground) {
            val notification = GendaNotification(applicationContext)
                .setTitle("UscanWorker running")
                .showNotification()

            setForeground(notification.getForegroundInfo())
        }

        delay(1000)

        try {

            for (scanNumber in 1..runsNumber) {

                scanner.startScanning()
                delay(30000L)
                scanner.stopScanning()

                App.get().updateFirestore("logs", mapOf(
                    "time" to LocalDateTime.now().format(dateTimeFormatter),
                    "scanResult" to UscanResult(
                        scanner.lastResult?.device?.address,
                        scanner.lastResult?.device?.name,
                        scanner.lastResult?.rssi,
                        scanner.lastResult?.txPower
                    ),
                    "batteryPercentage" to App.get().getBatteryPercentage(),
                    "scanNumber" to scanNumber,
                ))

                if (isRetry) {
                    val retryDelayTime = 90000L - (scanNumber * WorkRequest.MIN_BACKOFF_MILLIS)
                    Logger.d("UscanWorker doWork retry delayTime: $retryDelayTime")
                    delay(retryDelayTime)
                    break
                } else {
                    delay(90000L) // 1-minute delay
                }
            }

            if (isRetry) {
                Logger.d("UscanWorker doWork retry called")
                App.get().updateFirestore("workerRuns", mapOf(
                    "status" to "retry",
                    "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())
                ))
                Result.retry()
            } else {
                Logger.d("UscanWorker doWork success called")
                App.get().updateFirestore("workerRuns", mapOf(
                    "status" to "success",
                    "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())
                ))
                Result.success()
            }
        } catch (e: Exception) {
            Logger.e("UscanWorker doWork error", e)
            App.get().updateFirestore("workerRuns", mapOf(
                "status" to "error",
                "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
                "error" to (e.message ?: "Unknown error")
            ))

            Result.retry() // Retry the work in case of unexpected errors
        }
    }
}



