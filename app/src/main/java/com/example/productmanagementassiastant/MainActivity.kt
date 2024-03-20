package com.example.productmanagementassiastant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.widget.Toast
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.result.contract.ActivityResultContracts
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.qrcode.encoder.QRCode
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        if (isGranted) {
            scanCode()
        }
        else {
            // ---------
        }
    }

    private val scanLauncher = registerForActivityResult(ScanContract()) {
        result: ScanIntentResult -> run{
            if (result.contents == null) {
                Toast.makeText(this, "Не удается распознать",
                    Toast.LENGTH_SHORT).show()
            } else {
                val res = result.contents
                Toast.makeText(this, res,
                    Toast.LENGTH_SHORT).show()

                val intent = Intent(this@MainActivity, ScannerActivity::class.java)
                intent.putExtra(ScannerActivity.inform, res)
                startActivity(intent)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goToScanning.setOnClickListener {
            checkCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED) { // если дано разрешение
            scanCode()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 11)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                scanCode()
            }
            else {
                Toast.makeText(this, "Разрешите использование камеры в настройках",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scanCode() {
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Scan Your Barcode")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        options.setOrientationLocked(false)
        //options.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES)

        scanLauncher.launch(options)
    }

}



