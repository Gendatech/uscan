package genda.uscan.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import genda.uscan.utils.GendaNotification
import genda.uscan.utils.Utils

/**
 * The on going service.
 */
class UscanService : Service(){

    // region constants

    // Update Notification every 1 second (Max that can be).
    private val UPDATE_NOTIFICATION_MILLISECONDS: Long = 1000

    // endregion

    // region Data Members

    private var foregroundNotification: GendaNotification? = null

    private val notificationHandler = Handler(Looper.getMainLooper())

    private val serviceStartTime: Long = System.currentTimeMillis()

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

        foregroundNotification = GendaNotification(this)
        foregroundNotification?.showServiceNotification(this)

        startUpdateNotification()

        return START_STICKY
    }

    /**
     * A client is binding to the service with bindService().
     */
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    /**
     * All clients have unbound with unbindService().
     */
    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    /**
     * A client is binding to the service with bindService(),
     * after onUnbind() has already been called.
     */
    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    /**
     * The service is no longer used and is being destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

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
}