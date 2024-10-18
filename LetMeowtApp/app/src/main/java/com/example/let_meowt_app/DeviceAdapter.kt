package com.example.let_meowt_app

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView


class DeviceAdapter(
    private val context: Context, // Context for permission check
    private val devices: MutableList<BluetoothDevice>, // Change to MutableList to allow modification
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    // Variable to hold the currently selected device
    var selectedDevice: BluetoothDevice? = null

    inner class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(android.R.id.text1)

        init {
            view.setOnClickListener {
                selectedDevice = devices[adapterPosition] // Set the selected device
                if (checkBluetoothPermissions()) {
                    onDeviceClick(selectedDevice!!)
                } else {
                    // Notify the user about permissions not granted
                    // Toast.makeText(context, "Permissions not granted", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun checkBluetoothPermissions(): Boolean {
            val bluetoothConnectPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not required for older versions
            }

            val bluetoothScanPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not required for older versions
            }

            return bluetoothConnectPermission && bluetoothScanPermission
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        // Check permissions before binding the device name
        if (checkBluetoothPermissions()) {
            holder.deviceName.text = devices[position].name ?: "Unknown Device"
        } else {
            holder.deviceName.text = "Permissions not granted"
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    private fun checkBluetoothPermissions(): Boolean {
        val bluetoothConnectPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }

        val bluetoothScanPermission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Not required for older versions
        }

        return bluetoothConnectPermission && bluetoothScanPermission
    }

    // Method to remove unknown devices
    fun removeUnknownDevices() {
            devices.removeAll { it.name == null || it.name == "Unknown Device" }
            notifyDataSetChanged() // Notify the adapter about the data change

    }
}
