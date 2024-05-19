package com.example.productmanagementassiastant

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils.split
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.productmanagementassiastant.ScannerActivity.Companion.startScanner
import com.example.productmanagementassiastant.databinding.ActivityInfoTheProductBinding
import com.example.productmanagementassiastant.databinding.ActivityLogBinding
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class LogActivity : AppCompatActivity() {
    private var _binding: ActivityLogBinding? = null
    private val binding: ActivityLogBinding
        get() = _binding ?: throw IllegalStateException("Binding in LogActivity must not be null")

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.
    RequestPermission()) { isGranted ->
        if (isGranted) {
            startScanner()
        }
    }
    private val cameraPermission = android.Manifest.permission.CAMERA


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMenu.setOnClickListener {
            finish()
        }


        binding.scanning.setOnClickListener {
            requestCameraAndStartScanner()
        }

        binding.search.setOnClickListener {
            uploadingLogs(binding.whatSearch.text.toString().trim())
        }
    }

    private fun uploadingLogs(name: String) {
        val db = Firebase.firestore

        db.collection("changes")
            .whereEqualTo("who", name)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    db.collection("changes")
                        .whereEqualTo("name", name)
                        .get()
                        .addOnSuccessListener { result2 ->
                            if (result2.isEmpty) {
                                Toast.makeText(
                                    this@LogActivity,
                                    "Данный товар или человек еще не добавлен в базу данных",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            else {
                                val listItem = ArrayList<String>()

                                for (res2 in result2) {
                                    listItem.add(parseToLog(res2.get("who").toString(),
                                        res2.get("name").toString(),
                                        res2.get("what").toString(),
                                        res2.get("date").toString(),
                                        res2.get("quantity").toString()))
                                }

                                val dateFormat = SimpleDateFormat("dd.MM.yyyy/HH:mm:ss", Locale("ru"))
                                listItem.sortByDescending { dateFormat.parse(it.split("\n").last()) }

                                binding.changes.adapter = ArrayAdapter<String>(this@LogActivity,
                                    R.layout.list_item, R.id.item, listItem)
                            }

                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@LogActivity,
                                "Произошла ошибка, попробуйте позже",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                else {
                    val listItem = ArrayList<String>()

                    for (res in result) {
                        listItem.add(parseToLog(res.get("who").toString(),
                            res.get("name").toString(),
                            res.get("what").toString(),
                            res.get("date").toString(),
                            res.get("quantity").toString()))
                    }

                    val dateFormat = SimpleDateFormat("dd.MM.yyyy/HH:mm:ss", Locale("ru"))
                    listItem.sortByDescending { dateFormat.parse(it.split("\n").last()) }

                    binding.changes.adapter = ArrayAdapter<String>(this@LogActivity,
                        R.layout.list_item, R.id.item, listItem)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@LogActivity,
                    "Произошла ошибка, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun parseToLog(who: String, name: String, what: String, date: String, quantity: String) : String {
        /*
        Добавил на склад новый товар: 83984
        data

        Переместил со склада в ремонт товар: 329
        Причина ремонта: лгцпулцупдц
        Серийный номер: 21кн80а
        data

        Переместил из ремонта на склад товар: 9485
        date

        Выдал товар: 9389324
        data

        Изменил количество товара: 87273
        Было 287, стало 826732
        data
         */

        var str = "$who\n"
        val temp = what.split("_")
        if (temp[0] == "add") {
            str += "Добавил "
            if (temp[1] == "s")
                str += "на склад новый товар: $name\n"
            else
                str += "в ремонт новый товар: $name\nПричина ремонта: ${temp[2]}\n" +
                        "Серийный номер: ${temp[3]}\n"
        }

        else if (temp[0] == "move") {
            if (temp[2] == "s") {
                str += "Переместил из ремонта на склад товар: $name\n"
            }
            else if (temp[2] == "r") {
                str += "Переместил со склада в ремонт товар: $name\n" +
                        "Причина ремонта: ${temp[3]}\nСерийный номер: ${temp[4]}\n"
            }
            else
                str += "Выдал товар: $name\n"
        }
        else {
            str += "Изменил количество товара: $name\nБыло: ${temp[1]},Стало: ${temp[2]}\n"
        }
        if (quantity != "null")
            str += "Количество товара: $quantity\n"
        str += date

        return str
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
        val db = Firebase.firestore

        ScannerActivity.startScanner(this) {barcodes ->
            barcodes.forEach { barcode ->

                Toast.makeText(
                    this@LogActivity, "Загрузка товара", Toast.LENGTH_SHORT).show()

                uploadingLogs(barcode.rawValue.toString()) // имя товара
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
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).also {
            val uri: Uri = Uri.fromParts("package", packageName, null)
            it.data = uri
            startActivity(it)
        }
    }
}