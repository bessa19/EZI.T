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


class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var binding: ActivityMainBinding

    //for testing purposes for the security measures regarding matrix addition
    private val securityMatrix = arrayOf(
        intArrayOf(12, 45, 78, 14),
        intArrayOf(15, 45, 62, 35),
        intArrayOf(2, 20, 45, 90),
        intArrayOf(69, 96, 96, 58)
    )

    private fun getMatrixValue(row: Int, col: Int): Int{
        return securityMatrix[row][col]
    }

    fun onSecurityCheckRequest(row: Int, col: Int){
        val expectedValue = getMatrixValue(row, col)
        //sendBluetoothMessage(expectedValue.toString())
    }

//    fun onEsp32Response(response: String) {
//        runOnUiThread {
//            if (response == "ACCESS_GRANTED") {
//                binding.Status.text = R.string.access_granted.toString()
//                binding.Status.setTextColor(Color.GREEN)
//            } else {
//                binding.Status.text = R.string.access_denied.toString()
//                binding.Status.setTextColor(Color.RED)
//            }
//        }
//    }

//HARD CODED function for security testing
    private fun simulateEsp32(){
        val requestedRow = 1
        val requestedCol = 2

        onEsp32Response(requestedRow, requestedCol)
    }

    private fun onEsp32Response(row: Int, col: Int) {
        runOnUiThread {
            if (row in 0..3 && col in 0..3) {
                if (getMatrixValue(row, col) == 62) {
                    binding.Status.text = getText(R.string.access_granted)
                    binding.Status.setTextColor(Color.GREEN)
                } else {
                    binding.Status.text = getText(R.string.access_denied)
                    binding.Status.setTextColor(Color.RED)
                }
            } else {
                binding.Status.text = getText(R.string.invalid)
                binding.Status.setTextColor(Color.YELLOW)
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
            checkBluetooth()
        }

        // Here is where i need to add functionality to check security and open the gate
        binding.btnOpenGate.setOnClickListener {
            //onEsp32Response(3, 2)
            simulateEsp32()
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
