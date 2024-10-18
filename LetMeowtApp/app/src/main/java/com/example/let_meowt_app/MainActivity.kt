package com.example.let_meowt_app

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val SERVICE_UUID: UUID = UUID.fromString("635477fa-2364-4d09-92b5-0007c47cfef3")
    private val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")


    private lateinit var bluetoothAdapter: BluetoothAdapter
    private val bluetoothLeScanner by lazy { bluetoothAdapter.bluetoothLeScanner }
    private var scanning = false
    private val handler = Handler()
    private val scannedDevices = mutableListOf<BluetoothDevice>()
    private lateinit var deviceRecyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private lateinit var connectButton: Button
    private lateinit var openButton: Button
    private lateinit var closeButton: Button
    private lateinit var scanButton: Button
    private lateinit var statusTextView: TextView
    private var bluetoothGatt: BluetoothGatt? = null // BluetoothGatt instance
    private var writeCharacteristic: BluetoothGattCharacteristic? = null // Characteristic for writing data

    private val SCAN_PERIOD: Long = 10000

    private val leScanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            deviceAdapter.removeUnknownDevices() // Call the method to remove unknown devices

            if (checkBluetoothConnectPermission()) {
                val device = result.device
                if (!scannedDevices.contains(device)) {
                    scannedDevices.add(device)
                    updateDeviceList()
                }
                Log.i("BLEScan", "Device found: ${device.name} - ${device.address}")
            } else {
                Log.e("BLEScan", "Bluetooth connect permission not granted.")
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            for (result in results) {
                deviceAdapter.removeUnknownDevices() // Call the method to remove unknown devices

                val device = result.device
                if (!scannedDevices.contains(device)) {
                    scannedDevices.add(device)
                    updateDeviceList()
                }
                if (checkBluetoothConnectPermission()) {
                    Log.i("BLEScan", "Device found in batch: ${device.name} - ${device.address}")
                } else {
                    Log.e("BLEScan", "Bluetooth connect permission not granted.")
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLEScan", "Scan failed with error: $errorCode")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Set the content view to the XML layout

        // Initialize views
        deviceRecyclerView = findViewById(R.id.rvDevices)
        connectButton = findViewById(R.id.btnConnect)
        openButton = findViewById(R.id.btnOpen)
        closeButton = findViewById(R.id.btnLock)
        scanButton = findViewById(R.id.btnScan)
        statusTextView = findViewById(R.id.tvStatus)

        // Disable the Send Data button initially
        openButton.isEnabled = false
        closeButton.isEnabled = false

        // Initialize Bluetooth adapter
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        // Initialize the RecyclerView
        deviceAdapter = DeviceAdapter(this, scannedDevices) { device ->
            deviceAdapter.removeUnknownDevices() // Call the method to remove unknown devices
            if (checkBluetoothConnectPermission()) {
                Log.i("BLEScan", "Selected device: ${device.name} - ${device.address}")
                statusTextView.text = "Connecting to ${device.name}..."
                openButton.isEnabled = true
                closeButton.isEnabled = true
                connectToDevice(device) // Attempt to connect to the selected device
            } else {
                Log.e("BLEScan", "Bluetooth connect permission not granted.")
            }
            // Implement your connection logic here
        }

        deviceRecyclerView.layoutManager = LinearLayoutManager(this)
        deviceRecyclerView.adapter = deviceAdapter

        // Request necessary permissions
        if (!checkPermissions()) {
            requestPermissions()
            statusTextView.text = "Requesting Permissions..."
        } else {
            enableBluetooth()
            statusTextView.text = "Enableling Bluetooth..."
        }

        connectButton.setOnClickListener {
            Log.i("BLEScan", "Connect button clicked")
            // Add your connection logic here

            //check if bluetooth is enabled
            if (!bluetoothAdapter.isEnabled) {
                enableBluetooth()
            } else {
                scanLeDevice()
            }
        }

        // Add logic to disable the Send Data button when no device is selected
        connectButton.setOnClickListener {
            val selectedDevice = deviceAdapter.selectedDevice
            if (selectedDevice != null && checkBluetoothConnectPermission()) {
                Log.i("BLEScan", "Connecting to ${selectedDevice.name} - ${selectedDevice.address}")
                // Implement your connection logic here
            } else {
                // Notify the user about permissions not granted
                // Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show()
                openButton.isEnabled = false
                closeButton.isEnabled = false
            }
        }

        if (checkPermissions()) {
            scanLeDevice()
        }

        openButton.setOnClickListener {
            sendDataToDevice("Open") // Example message to send
        }

        closeButton.setOnClickListener {
            sendDataToDevice("Close") // Example message to send
        }

        scanButton.setOnClickListener {
            if (scanning) {
                stopScanning() // Stop scanning if it's already in progress
            }
            scanLeDevice() // Start scanning for devices
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        if (checkBluetoothConnectPermission()) {
            // Disconnect from any existing connection
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            statusTextView.text = "Connecting to ${device.name} - ${device.address}..."
            // Connect to the selected device
            bluetoothGatt = device.connectGatt(this, false, gattCallback)
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                Log.i("BLEScan", "Connected to GATT server.")
                statusTextView.text = "Connected to ${gatt.device.name}" // Update status text
                // Discover services after successful connection
                if (checkBluetoothConnectPermission()) {
                    gatt.discoverServices()
                }
            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                Log.i("BLEScan", "Disconnected from GATT server.")
                statusTextView.text = "Disconnected" // Update status text
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service: BluetoothGattService? = gatt.getService(SERVICE_UUID) // Use your service UUID
                writeCharacteristic = service?.getCharacteristic(CHARACTERISTIC_UUID) // Use your characteristic UUID
                Log.i("BLEScan", "Services discovered successfully.")
                // You can read/write characteristics here
            } else {
                Log.w("BLEScan", "onServicesDiscovered received: $status")
            }
        }

    override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("BLEScan", "Characteristic read: ${characteristic.value}")
            }
        }

        // Override other callbacks as needed for characteristics


    }

    private fun sendDataToDevice(message: String) {
        if (writeCharacteristic != null) {
            writeCharacteristic!!.value = message.toByteArray() // Convert message to bytes
            val status = bluetoothGatt?.writeCharacteristic(writeCharacteristic!!)
            if (status == true) {
                Log.i("BLEScan", "Message sent: $message")
                statusTextView.text = "Sent: $message"
            } else {
                Log.e("BLEScan", "Failed to send message.")
            }
        } else {
            Log.e("BLEScan", "Write characteristic is null. Not connected to a device.")
            statusTextView.text = "Not connected to a device."
        }
    }

    private fun checkPermissions(): Boolean {
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val fineLocationPermission = ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val bluetoothScanPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // BLUETOOTH_SCAN not needed below Android 12
        }

        val bluetoothConnectPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // BLUETOOTH_CONNECT not needed below Android 12
        }

        return coarseLocationPermission && fineLocationPermission &&
                bluetoothScanPermission && bluetoothConnectPermission
    }

    private fun requestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                permissions.entries.forEach {
                    Log.i("Permission", "${it.key} = ${it.value}")
                }

                // Check permissions again after the user interaction
                if (checkPermissions()) {
                    enableBluetooth()
                    scanLeDevice()
                } else {
                    Log.e("Permissions", "Required permissions not granted.")
                }
            }

            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun enableBluetooth() {
        if (checkBluetoothConnectPermission()) {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, 1)
                statusTextView.text = "BL is not enabled..."
            } else {
                scanLeDevice() // Start scanning if Bluetooth is already enabled
            }
        }

    }

    private fun scanLeDevice() {
        statusTextView.text = "Scanning for devices..."
        if (!scanning && checkBluetoothConnectPermission()) {
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
                statusTextView.text = "Scanning stopped after $SCAN_PERIOD ms."
            }, SCAN_PERIOD)
            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            statusTextView.text = "Scanning already in progress."
        }
    }

    private fun updateDeviceList() {
        deviceAdapter.notifyDataSetChanged() // Notify the adapter that data has changed
    }

    private fun stopScanning() {
        if (scanning && checkBluetoothConnectPermission()) {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
            statusTextView.text = "Scanning stopped."
        }
    }



    private fun checkBluetoothConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }
    }
}
