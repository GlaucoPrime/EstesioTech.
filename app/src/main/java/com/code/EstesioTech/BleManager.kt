package com.code.EstesioTech

import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.os.Build
import android.util.Log
import java.util.*

@SuppressLint("MissingPermission")
object BleManager {
    private const val TAG = "BleManager"

    // UUIDs padrão do serviço UART
    private val SERVICE_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")
    // private val CHARACTERISTIC_UUID_RX = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E") // REMOVIDO
    private val CHARACTERISTIC_UUID_TX = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E") // Recepção (ESP32 -> App)
    private val CCCD_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var txCharacteristic: BluetoothGattCharacteristic? = null // Para receber (Notify)
    // private var rxCharacteristic: BluetoothGattCharacteristic? = null // REMOVIDO

    private var isDeviceConnected = false
    fun getConnectedDeviceAddress(): String? = connectedDeviceAddress
    private var connectedDeviceAddress: String? = null

    interface ConnectionListener {
        fun onConnected()
        fun onDisconnected()
        fun onDataReceived(data: String)
        fun onError(message: String)
    }

    private var listener: ConnectionListener? = null

    fun setListener(l: ConnectionListener?) {
        listener = l
    }

    fun initialize(context: Context): Boolean {
        if (bluetoothAdapter == null) {
            val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bluetoothAdapter = btManager.adapter
            if (bluetoothAdapter == null) {
                listener?.onError("Bluetooth não suportado")
                return false
            }
        }
        return true
    }

    fun isConnected(): Boolean = isDeviceConnected

    fun connectToDevice(address: String, context: Context) {
        if (!initialize(context)) return

        val device: BluetoothDevice?
        try {
            device = bluetoothAdapter!!.getRemoteDevice(address)
        } catch (e: IllegalArgumentException) {
            listener?.onError("Endereço MAC inválido: $address")
            return
        }

        if (device == null) {
            listener?.onError("Dispositivo não encontrado: $address")
            return
        }

        Log.d(TAG, "Conectando a ${device.name ?: "Dispositivo"} ($address)...")

        bluetoothGatt?.close()
        bluetoothGatt = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(context, false, gattCallback)
        }
        connectedDeviceAddress = address
    }

    fun disconnect() {
        Log.d(TAG, "Desconectando...")

        isDeviceConnected = false
        connectedDeviceAddress = null

        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null

        txCharacteristic = null
        // rxCharacteristic = null // REMOVIDO

        listener?.onDisconnected()
    }

    // A função de escrita agora reporta o erro corretamente
    fun write(text: String) {
        listener?.onError("Função de escrita não suportada (ESP32 apenas envia).")
        Log.e(TAG, "Tentativa de escrita falhou: Característica RX (...0002) não existe.")
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Conectado a ${gatt.device.address}")
                    isDeviceConnected = true
                    connectedDeviceAddress = gatt.device.address
                    gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Desconectado de ${gatt.device.address}")
                    isDeviceConnected = false
                    connectedDeviceAddress = null
                    bluetoothGatt?.close()
                    bluetoothGatt = null
                    listener?.onDisconnected()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service == null) {
                    listener?.onError("Serviço UART (6E400001) não encontrado.")
                    disconnect()
                    return
                }

                // Procura APENAS a característica TX
                txCharacteristic = service.getCharacteristic(CHARACTERISTIC_UUID_TX)

                if (txCharacteristic == null) {
                    listener?.onError("Característica TX (Recepção) ...0003 não encontrada.")
                    disconnect()
                    return
                }

                if (!gatt.setCharacteristicNotification(txCharacteristic, true)) {
                    listener?.onError("Falha ao habilitar notificação local.")
                    disconnect()
                    return
                }

                val descriptor = txCharacteristic?.getDescriptor(CCCD_DESCRIPTOR_UUID)
                if (descriptor == null) {
                    listener?.onError("Descriptor CCCD (0x2902) não encontrado.")
                    disconnect()
                    return
                }

                val data = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val writeStatus = gatt.writeDescriptor(descriptor, data)
                    if (writeStatus != BluetoothGatt.GATT_SUCCESS) {
                        listener?.onError("Falha ao escrever no descriptor (API 33+ status: $writeStatus).")
                        disconnect()
                    }
                } else {
                    @Suppress("DEPRECATION")
                    descriptor.value = data
                    @Suppress("DEPRECATION")
                    val success = gatt.writeDescriptor(descriptor)
                    if (!success) {
                        listener?.onError("Falha ao escrever no descriptor (API < 33).")
                        disconnect()
                    }
                }
                Log.i(TAG, "Iniciando subscrição de notificações...")

            } else {
                listener?.onError("Falha ao descobrir serviços: $status")
                disconnect()
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener?.onError("Falha ao habilitar notificações no dispositivo (write status: $status)")
                disconnect()
                return
            }
            if (descriptor.uuid == CCCD_DESCRIPTOR_UUID) {
                Log.i(TAG, "NOTIFICAÇÕES HABILITADAS COM SUCESSO!")
                listener?.onConnected()
            }
        }

        // Função Helper (sem mudanças)
        private fun handleCharacteristicChange(uuid: UUID?, value: ByteArray?) {
            if (uuid == CHARACTERISTIC_UUID_TX) {
                val text = value?.toString(Charsets.UTF_8) ?: ""
                Log.i(TAG, "Dado Recebido: '$text'")
                listener?.onDataReceived(text)
            }
        }

        // --- ✅ AQUI ESTÁ A CORREÇÃO ---

        // MÉTODO ANTIGO (só roda em API < 33)
        @Suppress("DEPRECATION")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            super.onCharacteristicChanged(gatt, characteristic)
            // ✅ SÓ executa se for um Android ANTIGO (menor que API 33)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                handleCharacteristicChange(characteristic.uuid, characteristic.value)
            }
        }

        // NOVO MÉTODO (só roda em API 33+)
        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            // ✅ SÓ executa se for um Android NOVO (API 33 ou superior)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                handleCharacteristicChange(characteristic.uuid, value)
            }
        }
        // --- FIM DA CORREÇÃO ---

        override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            // (Esta função não é mais chamada por você, mas é bom mantê-la)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                Log.e(TAG, "Falha ao enviar dado (write status: $status)")
                listener?.onError("Falha ao enviar dado para o dispositivo.")
            } else {
                @Suppress("DEPRECATION")
                val valueStr = characteristic.value?.toString(Charsets.UTF_8) ?: "N/A"
                Log.i(TAG, "Dado enviado com sucesso: $valueStr")
            }
        }
    }
}