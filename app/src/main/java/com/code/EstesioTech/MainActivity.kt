package com.code.EstesioTech

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton

@SuppressLint("MissingPermission") // Permissões checadas em tempo real
class MainActivity : AppCompatActivity() {

    private lateinit var scanButton: MaterialButton
    private lateinit var statusText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var listView: ListView

    // Usamos um Map para evitar duplicatas (chave = Endereço MAC)
    private val foundDevices = mutableMapOf<String, BluetoothDevice>()
    private val devicesListInfo = mutableListOf<String>()
    private lateinit var listAdapter: ArrayAdapter<String>

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeScanner: BluetoothLeScanner? = null
    private var isScanning = false

    // Lista de permissões necessárias
    private val blePermissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(Manifest.permission.BLUETOOTH_SCAN)
            add(Manifest.permission.BLUETOOTH_CONNECT)
        }
    }.toTypedArray()

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device

            if (device.name == null) return

            if (!foundDevices.containsKey(device.address)) {
                foundDevices[device.address] = device

                val deviceName = device.name ?: getString(R.string.unknown_device)
                val deviceInfo = getString(R.string.device_info_format, deviceName, device.address)

                devicesListInfo.add(deviceInfo)
                listAdapter.notifyDataSetChanged()
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE_SCAN", "Falha no scan: código $errorCode")
            statusText.text = getString(R.string.scan_error, errorCode)
            progressBar.visibility = View.GONE
            scanButton.text = getString(R.string.scan_button_scan)
            isScanning = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanButton = findViewById(R.id.scanButton)
        statusText = findViewById(R.id.statusText)
        progressBar = findViewById(R.id.scanProgressBar)
        listView = findViewById(R.id.deviceListView)

        listAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, devicesListInfo)
        listView.adapter = listAdapter

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null) {
            Toast.makeText(this, getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show()
            finish()
            return
        }

        scanButton.setOnClickListener {
            if (!isScanning) {
                if (checkBlePermissions()) startBleScan() else requestBlePermissions()
            } else {
                stopBleScan()
            }
        }

        // ✅ CORRIGIDO: Abre a DeviceControlActivity (o chat)
        listView.setOnItemClickListener { _, _, position, _ ->
            stopBleScan()

            val deviceAddress = foundDevices.keys.toList()[position]
            val device = foundDevices[deviceAddress]

            // Inicia a DeviceControlActivity e passa o endereço
            val intent = Intent(this, DeviceControlActivity::class.java).apply {
                putExtra("DEVICE_ADDRESS", device?.address)
            }
            startActivity(intent)
            finish()
        }
    }

    private fun checkBlePermissions() =
        blePermissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

    private fun requestBlePermissions() =
        ActivityCompat.requestPermissions(this, blePermissions, 1)

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startBleScan()
        } else {
            Toast.makeText(this, getString(R.string.permissions_not_granted), Toast.LENGTH_SHORT).show()
        }
    }

    private fun startBleScan() {
        if (bluetoothAdapter?.isEnabled == false) {
            Toast.makeText(this, getString(R.string.enable_bluetooth_first), Toast.LENGTH_SHORT).show()
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
            ) {
                return
            }
            startActivity(enableBtIntent)
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && !isLocationEnabled()) {
            showLocationAlert()
            return
        }

        isScanning = true
        scanButton.text = getString(R.string.scan_button_stop)
        statusText.text = getString(R.string.scanning_devices)
        progressBar.visibility = View.VISIBLE
        foundDevices.clear()
        devicesListInfo.clear()
        listAdapter.notifyDataSetChanged()

        bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
        val settings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
        bluetoothLeScanner?.startScan(null, settings, scanCallback)

        scanButton.postDelayed({
            if (isScanning) {
                stopBleScan()
            }
        }, 10000) // 10 segundos
    }

    private fun stopBleScan() {
        isScanning = false
        scanButton.text = getString(R.string.scan_button_scan)
        bluetoothLeScanner?.stopScan(scanCallback)
        progressBar.visibility = View.GONE

        if(foundDevices.isEmpty()) {
            statusText.text = getString(R.string.scan_status_none_found)
        } else {
            statusText.text = getString(R.string.scan_status_finished)
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showLocationAlert() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.location_required_title))
            .setMessage(getString(R.string.location_required_message))
            .setPositiveButton(getString(R.string.enable)) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    override fun onPause() {
        super.onPause()
        if (isScanning) stopBleScan()
    }
}