package genda.uscan.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import genda.uscan.App
import genda.uscan.services.UscanService
import genda.uscan.utils.Logger

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.d("BootCompleteReceiver onReceive")

        if(intent.action.equals(Intent.ACTION_BOOT_COMPLETED)){

            // Start service.
//            context.startForegroundService(Intent(context, UscanService::class.java))
            App.get().startUscanService(from = "BootCompleteReceiver")
        }
    }
}