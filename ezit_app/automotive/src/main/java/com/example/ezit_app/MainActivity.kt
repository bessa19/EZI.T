package com.example.ezit_app

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.ezit_app.databinding.ActivityMainBinding
import android.view.animation.AnimationUtils
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import java.io.IOException
import java.util.UUID
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var binding: ActivityMainBinding

    private var btSocket: BluetoothSocket? = null
    private var btDevice: BluetoothDevice? = null
    private var outputStream: java.io.OutputStream? = null
    private var inputStream: java.io.InputStream? = null

    private val EZI_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID
    private val DEVICE_NAME = "EZI.T-Gate" // ESP32 Bluetooth name

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted){
            connectToDevice()
        } else {
            Toast.makeText(this, "Bluetooth permissions are required", Toast.LENGTH_SHORT)
        }
    }

    private fun checkBluetoothPermissions(): Boolean{
        val permissionsNeeded = mutableListOf<String>()

        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            permissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }

        return if (permissionsNeeded.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsNeeded.toTypedArray())
            false
        } else {
            true
        }
    }

    private fun connectToDevice(){
        if (checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Missing Bluetooth permission", Toast.LENGTH_SHORT).show()
            return
        }
        val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter.bondedDevices
        btDevice = pairedDevices.find { it.name == DEVICE_NAME }

        if (btDevice == null){
            binding.btStatus.text = R.string.not_found_device.toString()
            binding.btStatus.setTextColor(Color.YELLOW)
        }

        thread {
            try {
                btSocket = btDevice!!.createRfcommSocketToServiceRecord(EZI_UUID)
                btSocket!!.connect()
                outputStream = btSocket!!.outputStream
                inputStream = btSocket!!.inputStream

                runOnUiThread{
                    binding.btStatus.text = R.string.connected_bluetooth.toString()
                    binding.btStatus.setTextColor(Color.CYAN)
                }
            }catch (e: IOException){
                e.printStackTrace()
                runOnUiThread {
                    binding.btStatus.text = R.string.connected_bluetooth_failed.toString()
                    binding.btStatus.setTextColor(Color.RED)
                }
            }
        }
    }

    private fun listenForEsp32Response(){
        thread {
            val buffer = ByteArray(1024)
            while (true){
                try {
                    val bytes = inputStream?.read(buffer) ?: break
                    val message = String(buffer, 0, bytes).trim()

                    runOnUiThread {
                        when (message){
                            "AUTH_OK" -> {
                                binding.Status.text = getString(R.string.access_granted)
                                binding.Status.setTextColor(Color.GREEN)
                            }
                            "AUTH_FAIL" -> {
                                binding.Status.text = getString(R.string.access_denied)
                                binding.Status.setTextColor(Color.RED)
                            }
                            else -> {
                                binding.Status.text = getString(R.string.unknown)
                            }
                        }
                    }
                }catch (e: IOException) { break }
            }
        }
    }

    //for testing purposes for the security measures regarding matrix addition
    private val securityMatrix = arrayOf(
        intArrayOf(12, 45, 78, 14),
        intArrayOf(15, 45, 62, 35),
        intArrayOf(2, 20, 45, 90),
        intArrayOf(69, 96, 96, 58)
    )

    // Retrieves the value asked for on the matrix
    private fun getMatrixValue(row: Int, col: Int): Int {
        return securityMatrix[row][col]
    }

    private fun onSecurityCheckRequest(row: Int, col: Int) {
        val expectedValue = getMatrixValue(row, col)
        val authString = "AUTH: $row,$col,$expectedValue\n"
        sendBluetoothMessage(authString)
        listenForEsp32Response()
    }

    private fun sendBluetoothMessage(message: String) {
        thread {
            try {
                outputStream?.write(message.toByteArray())
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show()
            //finish() //fecha a app quando o bt não é suportado
            return
        }

        binding.btnConnect.setOnClickListener {
            if (checkBluetoothPermissions()) {
                checkBluetooth()
                connectToDevice()
            }
        }

        // Here is where i need to add functionality to check security and open the gate
        binding.btnOpenGate.setOnClickListener {
            val row = 1  //randomize this number later on (number need to be compreended on the dimentions of the matrix)
            val col = 2  //randomize this number later on (number need to be compreended on the dimentions of the matrix)
            onSecurityCheckRequest(row, col)
        }
    }

    private fun checkBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(enableBluetoothIntent)
        } else {
            Toast.makeText(this, R.string.bluetooth_already_enabled, Toast.LENGTH_SHORT).show()
        }
    }

    private val requestBluetooth =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.bluetooth_enabled, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, R.string.bluetooth_failed, Toast.LENGTH_SHORT).show()
            }
        }
}

//HARD CODED function for security testing
//    private fun simulateEsp32() {
//        val requestedRow = 1
//        val requestedCol = 2
//
//        onEsp32Response(requestedRow, requestedCol)
//    }
//  *************************************************************************************************
// Hard coded version for UI tests
//    private fun onEsp32Response(row: Int, col: Int) {
//        runOnUiThread {
//            if (row in 0..3 && col in 0..3) {
//                if (getMatrixValue(row, col) == 62) {
//                    binding.Status.text = getText(R.string.access_granted)
//                    binding.Status.setTextColor(Color.GREEN)
//                } else {
//                    binding.Status.text = getText(R.string.access_denied)
//                    binding.Status.setTextColor(Color.RED)
//                }
//            } else {
//                binding.Status.text = getText(R.string.invalid)
//                binding.Status.setTextColor(Color.YELLOW)
//            }
//        }
//    }