package genda.uscan.utils

import android.util.Log

class Logger {
    companion object {

        private val TAG ="uscan"

        private fun log(text: String) {
            try {
                Log.e(TAG, text)
            } catch (t: Throwable) {
                Log.e(TAG, t.toString())
            }
        }

        private fun log(text: String, t: Throwable) {
            log(text + " " + t.message)
        }

        fun d(msg: String) {
            log(msg)
        }

        fun e(msg: String) {
            log(msg)
        }

        fun e(msg: String, t: Throwable) {
            log("$msg: Exception: ${t.message}")
        }
    }
}