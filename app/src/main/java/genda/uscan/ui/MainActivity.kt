package genda.uscan.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import genda.uscan.R
import genda.uscan.databinding.ActivityMainBinding
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import genda.uscan.utils.Logger
import genda.uscan.utils.PersistentData

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        nearbyDevicesRequestLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    // The nearby devices permission listener.
    private val nearbyDevicesRequestLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->

            // Check if permission granted.
            if (permissions.entries.all { it.value }) {

                // Mark all permissions as granted.
                Logger.d("Nearby devices permission granted")
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

}