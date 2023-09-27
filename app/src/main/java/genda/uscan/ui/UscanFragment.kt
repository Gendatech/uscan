package genda.uscan.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.PackageManagerCompat.LOG_TAG
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.kkmcn.kbeaconlib.KBAdvPackage.KBAdvPacketEddyTLM
import com.kkmcn.kbeaconlib.KBAdvPackage.KBAdvPacketIBeacon
import com.kkmcn.kbeaconlib.KBAdvPackage.KBAdvType
import com.kkmcn.kbeaconlib.KBConnectionEvent
import com.kkmcn.kbeaconlib.KBeacon
import com.kkmcn.kbeaconlib.KBeacon.ConnStateDelegate
import com.kkmcn.kbeaconlib.KBeaconsMgr
import com.kkmcn.kbeaconlib.KBeaconsMgr.KBeaconMgrDelegate
import genda.uscan.App
import genda.uscan.R
import genda.uscan.databinding.FragmentUscanBinding
import genda.uscan.utils.Logger


class UscanFragment : Fragment() {

    // region Data Members

    private var _binding: FragmentUscanBinding? = null
    private var mBeaconsMgr: KBeaconsMgr? = null
    var mScanFailedContinueNum = 0
    var mBeaconsDictory: MutableMap<String, KBeacon> = HashMap()



    val beaconMgrDeletate: KBeaconMgrDelegate = object : KBeaconMgrDelegate {
        //get advertisement packet during scanning callback
        @SuppressLint("RestrictedApi")
        override fun onBeaconDiscovered(beacons: Array<KBeacon>) {
            for (beacon in beacons) {
                //get beacon adv common info
                Log.v(LOG_TAG, "Uscan tests beacon mac:" + beacon.mac)
                Log.v(LOG_TAG, "Uscan tests beacon name:" + beacon.name)
                Log.v(LOG_TAG, "Uscan tests beacon rssi:" + beacon.rssi)

                //get adv packet
                for (advPacket in beacon.allAdvPackets()) {
                    when (advPacket.advType) {
                        KBAdvType.KBAdvTypeIBeacon -> {
                            val advIBeacon = advPacket as KBAdvPacketIBeacon
                            Log.v(LOG_TAG, "Uscan tests iBeacon uuid:" + advIBeacon.uuid)
                            Log.v(LOG_TAG, "Uscan tests iBeacon major:" + advIBeacon.majorID)
                            Log.v(LOG_TAG, "Uscan tests iBeacon minor:" + advIBeacon.minorID)
                        }

                        KBAdvType.KBAdvTypeEddyTLM -> {
                            val advTLM = advPacket as KBAdvPacketEddyTLM
                            Log.v(LOG_TAG, "Uscan tests TLM battery:" + advTLM.batteryLevel)
                            Log.v(LOG_TAG, "Uscan tests TLM Temperature:" + advTLM.temperature)
                            Log.v(LOG_TAG, "Uscan tests TLM adv count:" + advTLM.advCount)
                        }
                    }
                }

                // Check if the beacon is already in the list
                if (!mBeaconsDictory.containsKey(beacon.mac)) {

                    mBeaconsDictory[beacon.mac] = beacon
                    Logger.d("Uscan adapter add beacon size: ${mBeaconsDictory.size}")

                    (_binding?.kbeaconList?.adapter as KbeaconAdapter).addItem(beacon)

//                    _binding?.kbeaconList?.adapter = KbeaconAdapter(mBeaconsDictory.values.toList())
//                    _binding?.kbeaconList?.adapter?.notifyItemRangeChanged(0, mBeaconsDictory.size)
                }
            }
            if (mBeaconsDictory.isNotEmpty()) {
                var mBeaconsArray:Array<KBeacon> = mBeaconsDictory.values.toTypedArray()
            }
        }

        @SuppressLint("RestrictedApi")
        override fun onCentralBleStateChang(nNewState: Int) {
            if (nNewState == KBeaconsMgr.BLEStatePowerOff) {
                Log.e(LOG_TAG, "Uscan tests BLE function is power off")
            } else if (nNewState == KBeaconsMgr.BLEStatePowerOn) {
                Log.e(LOG_TAG, "Uscan tests BLE function is power on")
            }
        }

        @SuppressLint("RestrictedApi")
        override fun onScanFailed(errorCode: Int) {
            Log.e(LOG_TAG, "Uscan tests Start N scan failed：$errorCode")
            mScanFailedContinueNum++
        }
    }





    // endregion

    // region Life Cycle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment.
        _binding = FragmentUscanBinding.inflate(inflater)

        return _binding?.root!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize fragment views.
        setupViews()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // endregion

    // region Private Methods

    /**
     * Initialize the fragment views.
     */
    @SuppressLint("RestrictedApi")
    private fun setupViews() {

        if (!App.get().isAllNeededPermissionsGranted()) {

            Logger.d("why this not working")
            findNavController().navigate(R.id.PermissionFragment)
        }

        _binding?.kbeaconList?.adapter = KbeaconAdapter(mBeaconsDictory.values.toMutableList())

        _binding?.buttonFirst?.setOnClickListener { view ->
//            App.get().startUscanService()
//            App.get().createWorkManager()

            _binding?.buttonFirst?.alpha = 0.5f

            mBeaconsMgr?.delegate = beaconMgrDeletate
            val nStartScan = mBeaconsMgr?.startScanning()
            if (nStartScan == 0) {
                Log.v(LOG_TAG, "Uscan tests start scan success")
            } else if (nStartScan == KBeaconsMgr.SCAN_ERROR_BLE_NOT_ENABLE) {
                Toast.makeText(requireContext(), "Uscan tests BLE function is not enable", Toast.LENGTH_LONG)
                    .show()
            } else if (nStartScan == KBeaconsMgr.SCAN_ERROR_NO_PERMISSION) {
                Toast.makeText(requireContext(), "Uscan tests BLE scanning has no location permission", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(requireContext(), "Uscan tests BLE scanning unknown error", Toast.LENGTH_LONG)
                    .show()
            }
        }

        mBeaconsMgr = KBeaconsMgr.sharedBeaconManager(requireContext())
        if (mBeaconsMgr == null) {

            Toast .makeText(requireContext(), "Uscan tests Bluetooth not support", Toast.LENGTH_LONG)
                .show()
        }
    }



    // endregion
}