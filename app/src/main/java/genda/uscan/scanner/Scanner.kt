package genda.uscan.scanner

import android.os.ParcelUuid
import genda.uscan.utils.Logger
import no.nordicsemi.android.support.v18.scanner.*
import java.util.*

class Scanner {
    private var callback: ScanCallback? = null

    private var count: Int = 0
    private var isScanStarted = false


    var lastResult: ScanResult? = null

    val scanner = BluetoothLeScannerCompat.getScanner()


    fun getEstimoteServiceUuid(): ParcelUuid {
        return ParcelUuid(UUID.fromString("0000fe9a-0000-1000-8000-00805f9b34fb"))
    }

    fun startScanning() {
        Logger.e("Start internal scan(${++count}) ######################################################################################### ")
        val settingsBuilder = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)

        //advanced confugurations
        settingsBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)


//        val filter =
//            ScanFilter.Builder().setServiceUuid(getEstimoteServiceUuid()).build()

//        val filters =
//            ScanFilter.Builder().setDeviceAddress("DD:34:02:07:5B:D0").build()

        val filters =
            ScanFilter.Builder().setDeviceAddress("DD:34:02:07:5B:DA").build()

        callback = object : ScanCallback() {

            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                Logger.e("Result from scan id $count sent from ID ${result.device} -- RSSI ${result.rssi}")
                lastResult = result
            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                super.onBatchScanResults(results)
                Logger.e("onBatchScanResults()")
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Logger.e("onScanFaild with code $errorCode")
            }
        }

        try {
            Logger.e("Starting scan $count")
            scanner.startScan(
                listOf(filters),
                settingsBuilder.build(),
                callback!!
            )
            isScanStarted = true
        } catch (e: Exception) {
            Logger.e("Unable to start scan. see crash" + e.message)
        }
    }

    fun stopScanning() {
        callback?.let {
            scanner.stopScan(it)
            Logger.d("Stop scan success")
        } ?: run {
            Logger.d("Stop scan null failed")
        }


        isScanStarted = false
        Logger.e("Stopping scan $count - ###################################################################################")
//            Thread.sleep(30 * 1000)
//            Logger.e("Clearing the area from scan $scanId - #######")
        callback = null
    }
}