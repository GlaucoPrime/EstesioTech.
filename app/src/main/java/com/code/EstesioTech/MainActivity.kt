package com.code.EstesioTech

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import com.code.EstesioTech.ui.theme.EstesioTechTheme

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth não suportado", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        setContent {
            EstesioTechTheme {
                ScanScreen(
                    bluetoothAdapter = bluetoothAdapter,
                    onDeviceClick = { device ->
                        // ✅ MUDANÇA: Vai para o Chat (DeviceControlActivity) para validar a conexão
                        val intent = Intent(this, DeviceControlActivity::class.java).apply {
                            putExtra("DEVICE_ADDRESS", device.address)
                        }
                        startActivity(intent)
                        // Não chamamos finish() aqui para permitir voltar se a conexão falhar
                        // Mas se quiser fechar o scan, descomente a linha abaixo:
                        // finish()
                    }
                )
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun ScanScreen(
    bluetoothAdapter: BluetoothAdapter?,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var statusText by remember { mutableStateOf("Toque para escanear") }
    val foundDevicesMap = remember { mutableStateMapOf<String, BluetoothDevice>() }
    val foundDevicesList = foundDevicesMap.values.toList()

    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            Toast.makeText(context, "Permissões ok!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Permissões necessárias", Toast.LENGTH_SHORT).show()
        }
    }

    val enableBtLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(context, "Bluetooth ativado!", Toast.LENGTH_SHORT).show()
        }
    }

    val scanCallback = remember {
        object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                if (device.name != null) foundDevicesMap[device.address] = device
            }
            override fun onScanFailed(errorCode: Int) {
                isScanning = false
                statusText = "Erro: $errorCode"
            }
        }
    }

    fun startScan() {
        if (bluetoothAdapter?.isEnabled == false) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBtLauncher.launch(intent)
            return
        }

        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (permissions.any { ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }) {
            permissionsLauncher.launch(permissions.toTypedArray())
            return
        }

        foundDevicesMap.clear()
        isScanning = true
        statusText = "Procurando..."

        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothAdapter?.bluetoothLeScanner?.startScan(null, settings, scanCallback)

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isScanning) {
                bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
                isScanning = false
                statusText = if(foundDevicesMap.isEmpty()) "Nenhum dispositivo." else "Scan finalizado."
            }
        }, 10000)
    }

    fun stopScan() {
        if (isScanning) {
            bluetoothAdapter?.bluetoothLeScanner?.stopScan(scanCallback)
            isScanning = false
            statusText = "Parado."
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(16.dp)
    ) {
        Text(text = statusText, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(8.dp))
        if (isScanning) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
        ) {
            items(foundDevicesList) { device ->
                Column(modifier = Modifier.fillMaxWidth().clickable { if(isScanning) stopScan(); onDeviceClick(device) }.padding(16.dp)) {
                    Text(text = device.name ?: "Desconhecido", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = device.address, color = Color.Gray, fontSize = 14.sp)
                }
                Divider(color = Color.Gray.copy(alpha = 0.3f))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { if (isScanning) stopScan() else startScan() },
            modifier = Modifier.fillMaxWidth().height(60.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(text = if (isScanning) "Parar" else "Escanear", fontSize = 18.sp, color = Color.White)
        }
    }
}