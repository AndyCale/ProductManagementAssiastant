package com.example.productmanagementassiastant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import com.example.productmanagementassiastant.databinding.ActivityMainMenuBinding
import com.google.mlkit.vision.barcode.common.Barcode


class MainMenuActivity : AppCompatActivity() {
    private var _binding: ActivityMainMenuBinding? = null
    private val binding: ActivityMainMenuBinding
        get() = _binding ?: throw IllegalStateException("Binding in MainMenu Activity must not be null")


    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.
    RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanner()
        }
    }
    private val cameraPermission = android.Manifest.permission.CAMERA

    private var backPressedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sp = getSharedPreferences("email and password", Context.MODE_PRIVATE)
        sp.edit().putString("TY", "9").commit()

        binding.fullName.text = sp.getString("fullName", "Произошла непредвиденная ошибка")

        binding.logOut.setOnClickListener {
            sp.edit().putString("TY", "null").commit()
            finish()
        }

        binding.goToScanning.setOnClickListener {
            requestCameraAndStartScanner()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed()
            finish()
        }
        else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show()
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

                val intent = Intent(this@MainMenuActivity, MoveTheProductActivity::class.java)
                intent.putExtra(MoveTheProductActivity.inform, barcode.rawValue.toString())
                startActivity(intent)
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

