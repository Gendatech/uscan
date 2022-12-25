package genda.uscan.worker

import android.content.Context
import androidx.work.*
import genda.uscan.App
import genda.uscan.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UscanWorker(appContext: Context, workerParams: WorkerParameters):
    CoroutineWorker(appContext, workerParams) {


    override suspend fun doWork(): Result {
        withContext(Dispatchers.IO) {

            Logger.d("UscanWorker doWork called")
            App.get().startUscanService()
        }

        return Result.success()
    }
}