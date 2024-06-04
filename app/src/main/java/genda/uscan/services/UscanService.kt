package genda.uscan.services

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.format.DateFormat
import android.util.Log
import androidx.lifecycle.LifecycleService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import genda.uscan.App
import genda.uscan.scanner.Scanner
import genda.uscan.scanner.UscanResult
import genda.uscan.utils.GendaNotification
import genda.uscan.utils.Logger
import genda.uscan.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.nordicsemi.android.support.v18.scanner.ScanResult
import java.util.*

/**
 * The on going service.
 */
class UscanService: LifecycleService(){

    // region constants

    // Update Notification every 1 second (Max that can be).
    private val UPDATE_NOTIFICATION_MILLISECONDS: Long = 1000

    // endregion

    // region Data Members

    private var foregroundNotification: GendaNotification? = null

    private val notificationHandler = Handler(Looper.getMainLooper())

    private val serviceStartTime: Long = System.currentTimeMillis()

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var logNumber = 0
    val logPath: DocumentReference =
        Firebase.firestore.collection("devices").document(Build.MODEL).collection("sessions")
            .document(App.sessionDate)



//    val scanner = Scanner()



    // endregion

    // region Life Cycle

    /**
     * The service is being created.
     */
    override fun onCreate() {
        super.onCreate()
    }

    /**
     * The service is starting, due to a call to startService().
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        Logger.d("Uscan service onStartCommand")

        if(foregroundNotification == null){
            foregroundNotification = GendaNotification(this)
            foregroundNotification?.showServiceNotification(this)

            serviceScope.launch {
                while (true) {
                    logEveryTwoMinutes()
                }
            }

//            startUpdateNotification()

//            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
//                if (!task.isSuccessful) {
//                    task.exception?.let { Logger.e("Fetching FCM registration token failed", it) }
//                    return@OnCompleteListener
//                }
//
//                // Get new FCM registration token
//                Logger.d("Firebase FCM token - ${task.result}")
//            })

        }

//        CoroutineScope(Dispatchers.IO).launch {
//
//            scanner.startScanning()
//
//            delay(20 * 1000)
//
//            scanner.stopScanning()
//
//            scanner.lastResult?.let { updateFirestore(it) }
//        }

        return START_STICKY
    }

    /**
     * The service is no longer used and is being destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        Logger.d("Uscan service onDestroy")

        // Stop Updating notification.
        stopUpdateNotification()

        serviceScope.cancel()

        // Restart Service.
//        restartService()
    }

    private fun restartService() {

        // Send restart broadcast.
        Intent().also { intent ->
            intent.action = "restartservice"
            sendBroadcast(intent)
        }
    }

    // endregion

    // region Private Methods

    private suspend fun logEveryTwoMinutes() {
        Logger.d("UscanService Log message date: ${DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())} logNumber: $logNumber")

        // Create the log entry
        val logEntry = mapOf(
            "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
            "logNumber" to logNumber
        )

        App.get().updateFirestore("logs", logEntry)

        logNumber++
        delay(60_000) // Delay for 1 minutes
    }

    private fun startUpdateNotification() {

        // Create notification runnable.
        val notificationRunnable: Runnable = object : Runnable {
            override fun run() {

                // Get time that pass in string.
                val timeThatPass =
                    Utils.convertMillisecondsToTime(System.currentTimeMillis() - serviceStartTime)

                // Update Notification.
                foregroundNotification?.updateNotification(timeThatPass)

                // Set the handler to repeat this runnable task every x millis.
                notificationHandler.postDelayed(this, UPDATE_NOTIFICATION_MILLISECONDS)
            }
        }

        // Trigger first time.
        notificationHandler.post(notificationRunnable)
    }

    fun stopUpdateNotification() {

        // Clear the notification handler.
        notificationHandler.removeCallbacksAndMessages(null);
    }

    private fun updateFirestore(lastResult: ScanResult) {

        // Create a new user with a first and last name
        val uscanResult = UscanResult(
            lastResult.device.address,
            lastResult.device.name,
            lastResult.rssi,
            lastResult.txPower
        )

        val db = Firebase.firestore

        Logger.d("updateFirestore called")
        db.collection("devices").document(Build.MODEL).collection("results")
            .document(DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()).toString())
            .set(uscanResult)
            .addOnSuccessListener {
                Log.d("uscan", "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.w("uscan", "Error adding document", e)
            }
    }
}