package com.example.productmanagementassiastant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import androidx.core.content.ContextCompat
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.google.mlkit.vision.barcode.common.Barcode


class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.
    RequestPermission()) { isGranted ->
            if (isGranted) {
                startScanner()
            }
    }
    private val cameraPermission = android.Manifest.permission.CAMERA


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.goToScanning.setOnClickListener {
            requestCameraAndStartScanner()
        }
    }

    private fun requestCameraAndStartScanner() {
        if (isPermissionGranted(cameraPermission)) {
            startScanner()
        }
        else {
            requestCameraPermission()
        }
    }

    private fun startScanner() {

        ScannerActivity.startScanner(this) {barcodes ->
            barcodes.forEach { barcode ->
                when(barcode.valueType) {
                    Barcode.TYPE_URL -> {
                        val intent = Intent(this@MainActivity, MoveTheProductActivity::class.java)
                        intent.putExtra(MoveTheProductActivity.inform, barcode.url.toString())
                        startActivity(intent)
                    }
                    else -> {
                        val intent = Intent(this@MainActivity, MoveTheProductActivity::class.java)
                        intent.putExtra(MoveTheProductActivity.inform, barcode.rawValue.toString())
                        startActivity(intent)
                    }
                }
            }

        }
    }

    private fun requestCameraPermission() {
        when {
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                cameraPermissionRequest {
                    openPermissionSetting()
                }
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }

    fun Context.isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    inline fun Context.cameraPermissionRequest(crossinline positive: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Нет доступа к камере")
            .setMessage("Разрешите доступ к камере, чтобы отсканировать штрихкод или qr-код...")
            .setPositiveButton("Разрешить") { dialog, which ->
                positive.invoke()
            }
            .setNegativeButton("Отмена") { dialog, which ->
            }.show()
    }

    fun Context.openPermissionSetting() {
        Intent(ACTION_APPLICATION_DETAILS_SETTINGS).also {
            val uri: Uri = Uri.fromParts("package", packageName, null)
            it.data = uri
            startActivity(it)
        }
    }
}



