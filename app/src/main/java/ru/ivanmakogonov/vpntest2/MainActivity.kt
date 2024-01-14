package ru.ivanmakogonov.vpntest2

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import de.blinkt.openvpn.api.IOpenVPNAPIService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.ivanmakogonov.vpntest2.ui.theme.VpnTest2Theme

class MainActivity : ComponentActivity() {
    private var vpnService: IOpenVPNAPIService? = null

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //ToDo receive status
        }
    }

    private val connection = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected")
            vpnService = IOpenVPNAPIService.Stub.asInterface(service)

            vpnService?.getPermissions()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            Log.d(TAG, "onServiceDisconnected")
            vpnService = null
        }

        override fun onBindingDied(name: ComponentName?) {
            Log.d(TAG, "onBindingDied: $name")
            super.onBindingDied(name)
        }

        override fun onNullBinding(name: ComponentName?) {
            Log.d(TAG, "onNullBinding: $name")
            super.onNullBinding(name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        CoroutineScope(Dispatchers.Main).launch {
//            delay(2000L)
//            bindService()
//            delay(2000L)
//            startVPN()
//        }

        setContent {
            VpnTest2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }

//        setContentView(R.layout.activity_main)
//
//        if (savedInstanceState == null) {
//            fragmentManager.beginTransaction()
//                .add(R.id.container, MainFragment())
//                .commit()
//        }
    }

    private fun IOpenVPNAPIService.getPermissions() {
        this.prepare(this@MainActivity.packageName)?.let {
            launcher.launch(it)
        } ?: startService(Intent(this@MainActivity, this::class.java))
    }

    private fun bindService() {
        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)
        icsopenvpnService.setPackage("de.blinkt.openvpn")
//        icsopenvpnService.setPackage("ru.ivanmakogonov.vpnmytest")
        application.bindService(icsopenvpnService, connection, BIND_AUTO_CREATE)
    }

    private fun startVPN() {
        val config = assets.open("test.conf").bufferedReader().use { it.readText() }
        vpnService?.startVPN(config)
    }

    companion object {
        private const val TAG = "Ivan3311"
    }
}