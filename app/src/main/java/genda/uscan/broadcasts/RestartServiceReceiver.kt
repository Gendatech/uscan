package genda.uscan.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import genda.uscan.App
import genda.uscan.services.UscanService

class RestartServiceReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.i("Broadcast Listened", "Service tried to stop")

        Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show()
//        context.startForegroundService(Intent(context, UscanService::class.java))
        App.get().startUscanService(from = "RestartServiceReceiver")
    }
}