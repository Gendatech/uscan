package genda.uscan.utils

import android.app.*
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import genda.uscan.ui.MainActivity
import genda.uscan.R

/**
 * Custom notification object with a lot of magics.
 */
class GendaNotification(
    val context: Context,
    var notificationId: Int = (System.currentTimeMillis() % Integer.MAX_VALUE).toInt(),
    var channelId: String = "genda",
    var channelName: String = "Genda Notifications",
) {

    // region Data Members

    private var notificationBuilder: NotificationCompat.Builder = buildNotification()
    private var notification: Notification? = null

    // endregion

    // region Public Methods

    fun showNotification(): GendaNotification {

        with(NotificationManagerCompat.from(context)) {

            notification = notificationBuilder.build()

            // notificationId is a unique int for each notification that you must define
            notify(notificationId, notification!!)
        }

        return this
    }

    fun showServiceNotification(service: Service): GendaNotification {

        notification = notificationBuilder.build()

        // Start foreground with foreground notification.
        service.startForeground(notificationId, notification!!)

        return this
    }

    fun updateNotification(notificationTitle: String): GendaNotification {

        notificationBuilder.setContentTitle(notificationTitle)

        return showNotification()
    }

    fun setTitle(notificationTitle: Int): GendaNotification {

        notificationBuilder.setContentTitle(context.getString(notificationTitle))
        return this
    }

    fun setMessage(notificationMessage: Int) : GendaNotification {
        notificationBuilder.setContentText(context.getString(notificationMessage))
        return this
    }

    // endregion

    // region Private Methods

    /**
     * Create a notification builder.
     */
    private fun buildNotification(): NotificationCompat.Builder {

        // Create notification channel.
        createNotificationChannel()

        // Create the notification.
        val notificationBuilder = NotificationCompat.Builder(context, channelId)

        // Only the first time the notification appears and not for later updates.
        notificationBuilder.setOnlyAlertOnce(true)

        // Must to set icon for title will work.
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)

        // Set an intent to open activity when notification pressed.
        notificationBuilder.setContentIntent(getActivityIntent(MainActivity::class.java))

        return notificationBuilder
    }

    /**
     * Create a notification channel - Android system request.
     */
    private fun createNotificationChannel() {

        // Create notification channel.
        val notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)

        // Create notification manager and register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // register the notification channel.
        notificationManager.createNotificationChannel(notificationChannel)
    }

    /**
     * Create an activity intent to open when notification pressed.
     */
    private fun getActivityIntent(activityClass: Class<*>?): PendingIntent? {

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    /**
     * Create an broadcast intent to open when notification pressed.
     */
    private fun getBroadcastIntent(broadcastClass: Class<*>?, intentName: String, intentExtra: String): PendingIntent? {

        // Create an explicit intent for an Activity in your app
        val intent = Intent(context, broadcastClass).apply {
            putExtra(intentName, intentExtra)
        }

        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }
}

// endregion