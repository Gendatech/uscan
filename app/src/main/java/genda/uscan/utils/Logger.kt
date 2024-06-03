package genda.uscan.utils

import android.util.Log

class Logger {
    companion object {

        private val TAG ="uscan"
        private var DD_SAMPLE_RATE = 1.0f

        private var ddLogger = buildDDLogger(DD_SAMPLE_RATE)

        private fun log(text: String) {
            try {
                Log.d(TAG, text)
            } catch (t: Throwable) {
                Log.e(TAG, t.toString())
            }
        }

        private fun log(text: String, t: Throwable) {
            log(text + " " + t.message)
            ddLogger.d(text, t)
        }

        fun d(msg: String) {
            log(msg)
            ddLogger.d(msg)
        }

        fun e(msg: String) {
            log(msg)
            ddLogger.e(msg)
        }

        fun e(msg: String, t: Throwable) {
            log("$msg: Exception: ${t.message}")
            ddLogger.e(msg, t)
        }

        private fun buildDDLogger(sampleRate: Float): com.datadog.android.log.Logger {
            return com.datadog.android.log.Logger.Builder()
                .setNetworkInfoEnabled(true)
                .setLogcatLogsEnabled(false)
                .setDatadogLogsEnabled(true)
                .setBundleWithTraceEnabled(true)
                .setSampleRate(sampleRate)
                .setLoggerName("LOGCAT")
                .build()
        }
    }
}