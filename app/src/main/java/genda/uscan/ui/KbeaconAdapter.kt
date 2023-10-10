package genda.uscan.ui

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PackageManagerCompat
import androidx.recyclerview.widget.RecyclerView
import com.kkmcn.kbeaconlib.KBCfgPackage.KBCfgCommon
import com.kkmcn.kbeaconlib.KBCfgPackage.KBCfgType
import com.kkmcn.kbeaconlib.KBConnectionEvent
import com.kkmcn.kbeaconlib.KBeacon
import genda.uscan.databinding.ItemKbeaconBinding
import genda.uscan.utils.Logger

class KbeaconAdapter(
    private val kbeacons: MutableList<KBeacon> = mutableListOf()
) : RecyclerView.Adapter<KbeaconAdapter.ViewHolder>() {

    // region Data Members


    // endregion

    // region RecyclerView Adapter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        // Create item binding.
        val kbeaconBinding =
            ItemKbeaconBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(kbeaconBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        // Bind current item.
        holder.bind(kbeacons[position])
    }

    override fun getItemCount(): Int {
        return kbeacons.size
    }

    // endregion

    // region Public Methods

    fun addItem(kbeacon: KBeacon) {
        kbeacons.add(kbeacon)

        // Notify adapter.
        notifyItemInserted(kbeacons.size - 1)
    }

    // endregion

    // region View Holder

    /**
     * Receive a site name and bind it to possible site view binding.
     */
    inner class ViewHolder(private val kbeaconBinding: ItemKbeaconBinding) :
        RecyclerView.ViewHolder(kbeaconBinding.root) {


        var nDeviceLastState: Int? = null

        @SuppressLint("RestrictedApi")
        val connectionDelegate =
            KBeacon.ConnStateDelegate { var1, state, nReason ->
                if (state == KBeacon.KBStateConnected) {
                    Log.v(PackageManagerCompat.LOG_TAG, "Uscan Adapter tests device has connected")
                    printData(var1)
                    nDeviceLastState = state
                } else if (state == KBeacon.KBStateConnecting) {
                    Log.v(
                        PackageManagerCompat.LOG_TAG,
                        "Uscan Adapter tests device start connecting"
                    )
                    nDeviceLastState = state
                } else if (state == KBeacon.KBStateDisconnecting) {
                    Log.v(
                        PackageManagerCompat.LOG_TAG,
                        "Uscan Adapter tests device start disconnecting"
                    )
                    nDeviceLastState = state
                } else if (state == KBeacon.KBStateDisconnected) {
                    nDeviceLastState = state
                    if (nReason == KBConnectionEvent.ConnAuthFail) {
                        Toast.makeText(
                            kbeaconBinding.root.context,
                            "Uscan Adapter tests password error",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else if (nReason == KBConnectionEvent.ConnTimeout) {
                        Toast.makeText(
                            kbeaconBinding.root.context,
                            "Uscan Adapter tests connection timeout",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    } else {
                        Log.e(
                            PackageManagerCompat.LOG_TAG,
                            "Uscan Adapter tests device has disconnected:$nReason"
                        )
                        Toast.makeText(
                            kbeaconBinding.root.context,
                            "Uscan Adapter tests connection other error, reason:$nReason",
                            Toast.LENGTH_LONG
                        )
                            .show()
                    }
                    Log.e(
                        PackageManagerCompat.LOG_TAG,
                        "Uscan Adapter  tests device has disconnected:$nReason"
                    )
                }
            }


        /**
         * Receive an item and bind it.
         */
        fun bind(kbeacon: KBeacon) {

            kbeaconBinding.kbeacon = kbeacon

            kbeaconBinding.root.setOnClickListener {

                kbeacon.connect("0000000000000000", 60 * 1000, connectionDelegate)
            }
        }

        @SuppressLint("RestrictedApi")
        fun printData(kbeacon: KBeacon) {

            Logger.d( "Uscan Adapter tests device has connected, print device information, kbeacon:$kbeacon")

            val isTLMEnable = false
            val isUIDEnable = false
            val isUrlEnable = false
            val commonCfg =
                kbeacon.getConfigruationByType(KBCfgType.KBConfigTypeCommon) as KBCfgCommon?

            val LOG_TAG = PackageManagerCompat.LOG_TAG

            Log.v(PackageManagerCompat.LOG_TAG, "Uscan Adapter tests device has connected")


            if (commonCfg != null) {

                //print basic capibility
                Log.v(LOG_TAG, "support iBeacon:" + commonCfg.isSupportIBeacon());
                Log.v(LOG_TAG, "support eddy url:" + commonCfg.isSupportEddyURL());
                Log.v(LOG_TAG, "support eddy tlm:" + commonCfg.isSupportEddyTLM());
                Log.v(LOG_TAG, "support eddy uid:" + commonCfg.isSupportEddyUID());
                Log.v(LOG_TAG, "support ksensor:" + commonCfg.isSupportKBSensor());
                Log.v(LOG_TAG, "beacon has button:" + commonCfg.isSupportButton());
                Log.v(LOG_TAG, "beacon can beep:" + commonCfg.isSupportBeep());
                Log.v(LOG_TAG, "support accleration sensor:" + commonCfg.isSupportAccSensor());
                Log.v(LOG_TAG, "support humidify sensor:" + commonCfg.isSupportHumiditySensor());
                Log.v(LOG_TAG, "support max tx power:" + commonCfg.getMaxTxPower());
                Log.v(LOG_TAG, "support min tx power:" + commonCfg.getMinTxPower());

//                //get support trigger
//                Log.v(LOG_TAG, "support trigger" + commonCfg.getTrigCapibility());
//
//                //check if iBeacon advertisment enable
//                Log.v(
//                    LOG_TAG,
//                    "iBeacon advertisment enable:" + ((commonCfg.getAdvType() && KBAdvType . KBAdvTypeIBeacon) > 0));
//
//                //check if KSensor advertisment enable
//                Log.v(
//                    LOG_TAG,
//                    "iBeacon advertisment enable:" + ((commonCfg.getAdvType() && KBAdvType.KBAdvTypeSensor) > 0));

                //check TLM adv interval
                Log.v(LOG_TAG, "TLM adv interval:" + commonCfg.getTLMAdvInterval());

            } else {
                Log.v(LOG_TAG, "Uscan Adapter tests device has no common configuration")
            }


        }
    }

}

// endregion

