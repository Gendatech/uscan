package genda.uscan.scanner

import com.google.firebase.Timestamp
import genda.uscan.BuildConfig

data class UscanResult(

    var id: String? = null,
    var name: String? = null,
    var rssi: Int? = null,
    var txPower: Int? = null,
    var timestamp: Timestamp = Timestamp.now(),
    var version: String = BuildConfig.VERSION_NAME
){
}