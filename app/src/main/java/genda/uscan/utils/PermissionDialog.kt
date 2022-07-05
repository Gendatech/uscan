package genda.uscan.utils

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import genda.uscan.databinding.FragmentPermissionBinding

/**
 * The genda location permission request dialog view.
 */
class PermissionDialog: Fragment() {

    // region Data Members

    private var _binding: FragmentPermissionBinding? = null

    // The foreground location permission listener.
    private val foregroundLocationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

        // Check if permission granted.
        if (permissions.entries.all { it.value }) {

            // Mark foreground  permission as granted.
            Logger.d("Foreground permission granted")
            binding.permissionLocationForegroundLayout.isPermissionGranted = true

            // Check background location.
            checkBackgroundLocation()
        } else {

            // Check if is the first time the user denied the permission request.
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Save permission status because this is the only way to know why we don't need to show rationale.
                PersistentData.setPermissionBlockedOnce(true)
                Logger.d("Foreground permission denied, we have one more try")
            } else {

                // Check if the permission blocked already once and that's the reason we don't need to show rationale.
                if (PersistentData.getPermissionBlockedOnce()){

                    Logger.d("Foreground permission denied, the permission request blocked")
                    PersistentData.setPermissionsIsBlocked(true)
                } else {

                    Logger.d("Foreground request - User not choose any permission level, like new")
                }
            }
        }
    }

    // The background location permission listener.
    private val backgroundLocationRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // Check if permission granted.
            if (permissions.entries.all { it.value }) {

                // Mark all permissions as granted.
                Logger.d("Background permission granted")
                binding.permissionLocationBackgroundLayout.isPermissionGranted = true
                binding.isUserGrantAllPermissions = true
            } else {

                // Check if is the first time the user denied the permission request.
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {

                    Logger.d("Background permission denied, we have one more try")
                    PersistentData.setPermissionBlockedOnce(true)
                } else {

                    Logger.d("Background permission denied, the permission request blocked")
                    PersistentData.setPermissionsIsBlocked(true)
                }
            }
        }

    // The nearby devices permission listener.
    private val nearbyDevicesRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // Check if permission granted.
            if (permissions.entries.all { it.value }) {

                // Mark all permissions as granted.
                Logger.d("Nearby devices permission granted")
                binding.permissionNearbyLayout.isPermissionGranted = true
                binding.isUserGrantAllPermissions = true
            } else {

                // Check if is the first time the user denied the permission request.
                if (shouldShowRequestPermissionRationale(Manifest.permission.BLUETOOTH_SCAN)) {

                    // Save permission status because this is the only way to know why we don't need to show rationale
                    PersistentData.setPermissionBlockedOnce(true)
                    Logger.d("Nearby devices permission denied, we have one more try")
                } else {

                    // Check if the permission blocked already once and that's the reason we don't need to show rationale.
                    if (PersistentData.getPermissionBlockedOnce()){

                        Logger.d("Nearby devices permission denied, the permission request blocked")
                        PersistentData.setPermissionsIsBlocked(true)
                    } else {

                        Logger.d("Nearby devices - User not choose any permission level, like new")
                    }
                }
            }
        }


    // endregion

    // region Properties

    private val binding get() = _binding!!

    // endregion

    // region Life Cycle

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        // Inflate the layout for this fragment.
        _binding = FragmentPermissionBinding.inflate(inflater)

        return binding.root
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
    private fun setupViews() {

        // Check the permissions we need to show on ui.
        checkTheNecessaryPermissions()

        // Set on foreground location permission view pressed listener.
        binding.permissionLocationForegroundLayout.root.setOnClickListener {

            // Pop the foreground location permission request
            openNextPermissionRequest()
        }

        // Set on background location permission view pressed listener.
        binding.permissionLocationBackgroundLayout.root.setOnClickListener {

            // Pop the background location permission request
            openNextPermissionRequest()
        }

        // Set on foreground location permission view pressed listener.
        binding.permissionNearbyLayout.root.setOnClickListener {

            // Pop the nearby devices permission request.
            openNextPermissionRequest()
        }

        // The turn on button on click listener.
        binding.turnOnBtn.setOnClickListener {

            // Check if all permissions already granted.
            if (binding.isUserGrantAllPermissions == true){

                // Close permissions dialog.
                activity?.onBackPressed()
            } else {

                // Open the next permission request according which permission missing.
                openNextPermissionRequest()
            }
        }

        // The not now button on click listener.
        binding.notNowBtn.setOnClickListener {

            // Close permissions dialog.
            activity?.onBackPressed()
        }
    }

    /**
     * Open the next permission request according which permission missing.
     */
    private fun openNextPermissionRequest(){

        // Check if the permissions request blocked.
        if (PersistentData.getPermissionsIsBlocked()){

            // Open app settings.
            startActivity(Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts("package", requireContext().packageName, null)
            })
        } else {

            // Start checking permissions without waiting to user press on permission requests views.
            checkTheNecessaryPermissions(true)
        }
    }

    /**
     * Check the permissions according the android version.
     */
    private fun checkTheNecessaryPermissions(isForceOpenRequest: Boolean = false) {

        // Check if the user sdk is android 12+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){

            // Check bluetooth permission.
            checkNearbyDevices(isForceOpenRequest)
        } else {

            // Check location permission.
            checkForegroundLocation(isForceOpenRequest)
        }
    }

    /**
     * Handle the foreground location permission check.
     */
    private fun checkForegroundLocation(isForceOpenRequest: Boolean = false){

        // Check it the permission already granted.
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {

            // Mark the foreground location request as granted.
            Logger.d("Foreground permission already granted")
            binding.permissionLocationForegroundLayout.root.isEnabled = false
            binding.permissionLocationForegroundLayout.isPermissionGranted = true
            binding.isUserBlockedPermissionRequest = false
            PersistentData.setPermissionBlockedOnce(false)

            // Check if we need the background permission.
            checkBackgroundLocation(isForceOpenRequest)
        } else {

            // Check that the permission not blocked already by the user.
            if (!PersistentData.getPermissionsIsBlocked()){

                // Check if we should pop the system request without waiting to user press on permission request view.
                if (isForceOpenRequest){

                    // Pop the foreground location permission request
                    foregroundLocationRequestLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                } else {

                    Logger.d("Foreground permission not granted, set permission request clickable")
                }
            } else {

                // Mark the permissions request as blocked.
                binding.isUserBlockedPermissionRequest = true
                Logger.d("The foreground location permission blocked by the user, the only way is from the app settings screen")
            }
        }
    }

    /**
     * Handle the background location permission check.
     */
    private fun checkBackgroundLocation(isForceOpenRequest: Boolean = false){

        // Check if we need also the background location permission, android 10 and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){

            // Check it the permission already granted.
            if (checkPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {

                // Mark the background location request as granted.
                Logger.d("Background permission already granted")
                binding.permissionLocationForegroundLayout.isPermissionGranted = true
                binding.permissionLocationBackgroundLayout.isPermissionGranted = true
                binding.isUserBlockedPermissionRequest = false
                binding.isUserGrantAllPermissions = true
            } else {

                // Check that the permission not blocked already by the user.
                if (!PersistentData.getPermissionsIsBlocked()) {

                    // Check if we should pop the system request without waiting to user press on permission request view.
                    if (isForceOpenRequest) {

                        // Pop the background location permission request
                        backgroundLocationRequestLauncher.launch(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                    } else {

                        Logger.d("Background permission not granted, set permission request clickable")
                    }
                } else {

                    // Mark the permissions request as blocked.
                    binding.isUserBlockedPermissionRequest = true
                    binding.permissionLocationForegroundLayout.isPermissionGranted = false
                    binding.permissionLocationForegroundLayout.root.isEnabled = true
                    Logger.d("The background location permission blocked by the user, the only way is from the app settings screen")
                }
            }
        } else {

            // Mark all permissions as granted.
            binding.isUserGrantAllPermissions = true
        }
    }

    /**
     * Handle the nearby devices permission check.
     */
    private fun checkNearbyDevices(isForceOpenRequest: Boolean = false){

        // Check it the permission already granted.
        if (checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {

            // Check it the permission already granted.
            if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {

                // Pop the nearby devices permission request.
                Logger.d("All UI permission requested granted, now we can request the non ui permissions.")
                nearbyDevicesRequestLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            }

            // Mark the nearby devices request as granted.
            Logger.d("Nearby devices permission already granted")
            binding.permissionNearbyLayout.isPermissionGranted = true
            binding.isUserBlockedPermissionRequest = false
            binding.isUserGrantAllPermissions = true
        } else {

            // Check that the permission not blocked already by the user.
            if (!PersistentData.getPermissionsIsBlocked()){

                // Check if we should pop the system request without waiting to user press on permission request view.
                if (isForceOpenRequest){

                    // Pop the nearby devices permission request.
                    nearbyDevicesRequestLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN))
                } else {

                    Logger.d("Nearby devices permission not granted, set permission request clickable")
                }
            } else {

                // Mark the permissions request as blocked.
                binding.isUserBlockedPermissionRequest = true
                Logger.d("The Nearby devices permission blocked by the user, the only way is from the app settings screen")
            }
        }
    }

    private fun checkPermission(permissionName: String): Boolean {

        val isPermissionGranted =
            PermissionChecker.checkCallingOrSelfPermission(
                requireContext(),
                permissionName
            ) == PackageManager.PERMISSION_GRANTED

        Logger.d("Check if the permission $permissionName granted = $isPermissionGranted")
        return isPermissionGranted
    }

    // endregion
}