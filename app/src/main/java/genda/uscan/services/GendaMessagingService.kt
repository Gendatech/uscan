package genda.uscan.services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import genda.uscan.utils.Logger

class FCMService: FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {

        Logger.d("FCM onMessageReceived from ${message.from} - notification ${message.notification?.body}")

        if (message.data.isNotEmpty()) {
            Logger.d("Message data payload: ${message.data}")
        }
    }

    override fun onNewToken(token: String) {
        Logger.d("FCM onNewToken $token")
    }
}