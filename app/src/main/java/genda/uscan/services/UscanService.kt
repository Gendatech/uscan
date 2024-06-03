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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import genda.uscan.scanner.Scanner
import genda.uscan.scanner.UscanResult
import genda.uscan.utils.GendaNotification
import genda.uscan.utils.Logger
import genda.uscan.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

    val scanner = Scanner()



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

        Logger.d("Uscan service onStartCommand")

        if(foregroundNotification == null){
            foregroundNotification = GendaNotification(this)
            foregroundNotification?.showServiceNotification(this)

            startUpdateNotification()

            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { Logger.e("Fetching FCM registration token failed", it) }
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                Logger.d("Firebase FCM token - ${task.result}")
            })

        }

        CoroutineScope(Dispatchers.IO).launch {

            scanner.startScanning()

            delay(20 * 1000)

            scanner.stopScanning()

            pushlishNextWakeupIntent()

            scanner.lastResult?.let { updateFirestore(it) }
        }

        return START_STICKY
    }

//    /**
//     * A client is binding to the service with bindService().
//     */
//    override fun onBind(intent: Intent): IBinder? {
//        return super.onBind(intent)
//    }
//
//    /**
//     * All clients have unbound with unbindService().
//     */
//    override fun onUnbind(intent: Intent?): Boolean {
//        return super.onUnbind(intent)
//    }
//
//    /**
//     * A client is binding to the service with bindService(),
//     * after onUnbind() has already been called.
//     */
//    override fun onRebind(intent: Intent?) {
//        super.onRebind(intent)
//    }

    /**
     * The service is no longer used and is being destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        Logger.d("Uscan service onDestroy")

        // Stop Updating notification.
        stopUpdateNotification()

        // Restart Service.
        restartService()
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




    private var alarmIntent: PendingIntent? = null

    private fun pushlishNextWakeupIntent() {
        Logger.e("pushlishNextWakeupIntent()")
        val alarmMgr = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextTime = System.currentTimeMillis() + (10 * 1000)

        alarmIntent = getWakeupIntent(this, "Watchdog Pending Intent").let { intent ->
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
            )
        }


//        alarmMgr.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            nextTime,
//            alarmIntent
//        )
    }


    fun getWakeupIntent(context: Context, wakeupReason: String): Intent {
        val intent = Intent(context, UscanService::class.java)
        intent.putExtra("WAK_UP_REASON", wakeupReason)
        return intent
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