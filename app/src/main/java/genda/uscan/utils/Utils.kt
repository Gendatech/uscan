package genda.uscan.utils

import java.util.concurrent.TimeUnit

class Utils {

    // region Static Methods

    companion object{

        fun convertMillisecondsToTime(milliseconds: Long): String {

            val HH: Long = TimeUnit.MILLISECONDS.toHours(milliseconds)
            val MM: Long = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60
            val SS: Long = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60

            return String.format("Time that pass is %02d:%02d:%02d", HH, MM, SS)
        }
    }

    // endregion

    // region Public Methods

    // endregion

    // region Private Methods

    // endregion
}