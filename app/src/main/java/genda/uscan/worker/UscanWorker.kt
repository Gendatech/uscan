package genda.uscan.worker

import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.work.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import genda.uscan.App
import genda.uscan.utils.GendaNotification
import genda.uscan.utils.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Date
class UscanWorker(appContext: Context, workerParams: WorkerParameters) : CoroutineWorker(appContext, workerParams) {

    private val logPath: DocumentReference = Firebase.firestore.collection("devices")
        .document(Build.MODEL).collection("sessions").document(App.sessionDate)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {

        Logger.d("UscanWorker doWork")

        val workData = mapOf(
            "status" to "running",
            "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss", Date()),
        )

        App.get().updateFirestore("workerRuns", workData)
        val notification = GendaNotification(applicationContext)
            .setTitle("UscanWorker running")
            .showNotification()

        setForeground(notification.getForegroundInfo())

        delay(1000)

        try {
            repeat(1000) { // Run for 15 minutes (15 iterations * 1 minute delay)

                // Create the log entry
                val logEntry = mapOf(
                    "time" to DateFormat.format("MMMM d, yyyy - HH:mm:ss:SSS", Date()),
                    "timestamp" to Timestamp.now(),
                    "milliseconds" to System.currentTimeMillis(),
                    "serverTimestamp" to FieldValue.serverTimestamp()
                )

                App.get().updateFirestore("logs", logEntry)

                delay(60000L) // 1-minute delay
            }

            Result.success()

        } catch (e: Exception) {
            Logger.e("UscanWorker doWork error", e)
            Result.retry() // Retry the work in case of unexpected errors
        }
    }
}



