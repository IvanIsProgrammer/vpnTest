package ru.ivanmakogonov.vpntest2

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.blinkt.openvpn.api.IOpenVPNAPIService
import de.blinkt.openvpn.api.IOpenVPNStatusCallback
import kotlinx.coroutines.delay

@Composable
fun MainScreen() {
    var vpnService: IOpenVPNAPIService? = null
    val context = LocalContext.current
    var status: String by remember { mutableStateOf("...") }
    var config: String by remember { mutableStateOf("") }

    val statusCallback: IOpenVPNStatusCallback by lazy {
        object: IOpenVPNStatusCallback.Stub() {
            override fun newStatus(
                uuid: String?,
                state: String?,
                message: String?,
                level: String?
            ) {
                status = "$state|$message"
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            //ToDo...
        }
    }

    val connection = object: ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            vpnService = IOpenVPNAPIService.Stub.asInterface(service)
            vpnService?.registerStatusCallback(statusCallback)

            vpnService?.prepare("de.blinkt.openvpn")?.let {
                launcher.launch(it)
            }
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            vpnService = null
        }

        override fun onBindingDied(name: ComponentName?) {
            super.onBindingDied(name)
        }

        override fun onNullBinding(name: ComponentName?) {
            super.onNullBinding(name)
        }
    }

    fun bindService() {
        val icsopenvpnService = Intent(IOpenVPNAPIService::class.java.name)
        icsopenvpnService.setPackage("de.blinkt.openvpn")
        context.bindService(icsopenvpnService, connection, ComponentActivity.BIND_AUTO_CREATE)
    }

    fun startVPN() {
        vpnService?.startVPN(config)
    }

    fun stopVPN() {
        vpnService?.disconnect()
    }

    LaunchedEffect(Unit) {
        bindService()
    }

    Content(
        status = status,
        config = config,
        onConfigChange = {
            config = it
        },
        onConnectClick = {
            startVPN()
        },
        onDisconnectClick = {
            stopVPN()
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Content(
    status: String,
    config: String,
    onConfigChange: (String) -> Unit,
    onConnectClick: () -> Unit,
    onDisconnectClick: () -> Unit,
) {
    Column(
        Modifier.padding(16.dp)
    ) {
        Text(text = "status: $status")
        VerticalSpacer(8.dp)
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = config,
            onValueChange = onConfigChange,
            label = {
                Text(text = "Config")
            }
        )
        VerticalSpacer(8.dp)
        Button(onClick = onConnectClick) {
            Text("Connect")
        }
        VerticalSpacer(8.dp)
        Button(onClick = onDisconnectClick) {
            Text("Disconnect")
        }
    }
}

@Composable
fun VerticalSpacer(
    length: Dp
) {
    Spacer(Modifier.height(length))
}