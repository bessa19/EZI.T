package com.example.ezit_app

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter() ?: run {
//            Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_LONG).show()
//            //finish() //fecha a app quando o bt não é suportado
//            return
//        }

        binding.btnConnect.setOnClickListener {
            checkBluetooth()
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
