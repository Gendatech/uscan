package genda.uscan.worker

import android.content.Context
import android.text.format.DateFormat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import genda.uscan.App
import java.util.Date

class StartStopWorker (appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        App.get().updateFirestore("StartStopWorker", mapOf(
            "status" to "running",
            "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date())
        ))

        return Result.success()
    }
}