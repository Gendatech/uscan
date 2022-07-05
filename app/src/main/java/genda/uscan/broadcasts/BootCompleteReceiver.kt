package genda.uscan.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import genda.uscan.service.UscanService

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        if(intent.action.equals(Intent.ACTION_BOOT_COMPLETED)){

            // Start service.
            context.startForegroundService(Intent(context, UscanService::class.java))
        }
    }
}