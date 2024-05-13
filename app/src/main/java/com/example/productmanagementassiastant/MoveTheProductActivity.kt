package com.example.productmanagementassiastant

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import com.example.productmanagementassiastant.databinding.ActivityMoveTheProductBinding
import com.example.productmanagementassiastant.databinding.ActivityScannerBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class MoveTheProductActivity : AppCompatActivity() {
    private var _binding: ActivityMoveTheProductBinding? = null
    private val binding: ActivityMoveTheProductBinding
        get() = _binding ?: throw IllegalStateException("Binding in Move Activity must not be null")
    val db = Firebase.firestore
    private var id : String = "null"
    private var countRepair : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMoveTheProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMenu.setOnClickListener {
            finish()
        }

        binding.plus.setOnClickListener {
            val num = binding.count.text.toString().toInt() + 1
            binding.count.setText(num.toString())
        }

        binding.minus.setOnClickListener {
            val num = binding.count.text.toString().toInt() - 1
            if (num > -1)
                binding.count.setText(num.toString())
        }

        id = intent.getStringExtra("id")!!
        val info = intent.getStringExtra("info")
        val found = intent.getIntExtra("found", 0)
        countRepair = intent.getIntExtra("countRepair", 0)

        if (info != null) {
            if (found == 0 || found == 3) { // еще нет такого или он выдан
                if (found == 0)
                    binding.placeProduct.text = getString(R.string.arrived)
                else
                    binding.placeProduct.text = getString(R.string.issued)
                binding.button1.text = getString(R.string.onWarehouse)
                binding.button2.text = getString(R.string.forRepairs)
            }
            else {
                if (found == 1) { // на складе
                    binding.placeProduct.text = getString(R.string.warehouse)
                    binding.button1.text = getString(R.string.forRepairs)
                    binding.button2.text = getString(R.string.issue)
                } else { // в ремонте
                    binding.placeProduct.text = getString(R.string.repair)
                    binding.button1.text = getString(R.string.onWarehouse)
                    binding.button2.text = getString(R.string.issue)
                }
            }
            db.collection("products").document(id).get().addOnSuccessListener { it ->
                binding.countAvail.text = it.get("quantity").toString()
            }
            binding.nameProduct.text = info

            binding.button1.setOnClickListener {
                if (found == 0)
                    addNewProduct(info, "s", "null")
                else if (found == 1) {
                    binding.reason.visibility = View.VISIBLE
                    if (binding.reason.text.isEmpty()) {
                        Toast.makeText(this@MoveTheProductActivity, "Введите причину ремонта",
                            Toast.LENGTH_SHORT).show()
                    }
                    else
                        moveProduct(info, "r", binding.reason.text.toString())
                }
                else
                    moveProduct(info, "s", "null")
            }

            binding.button2.setOnClickListener {
                if (found == 0) {
                    binding.reason.visibility = View.VISIBLE
                    if (binding.reason.text.isEmpty()) {
                        Toast.makeText(this@MoveTheProductActivity, "Введите причину ремонта",
                            Toast.LENGTH_SHORT).show()
                    }
                    else
                        addNewProduct(info, "r", binding.reason.text.toString())
                }
                else if (found == 3) {
                    binding.reason.visibility = View.VISIBLE
                    if (binding.reason.text.isEmpty()) {
                        Toast.makeText(this@MoveTheProductActivity, "Введите причину ремонта",
                            Toast.LENGTH_SHORT).show()
                    }
                    else
                        moveProduct(info, "r", binding.reason.text.toString())
                }
                else
                    moveProduct(info, "d", "null")
            }
        }
        else {
            Toast.makeText(
                this@MoveTheProductActivity,
                "Что-то пошло не так",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this@MoveTheProductActivity, MainMenuActivity::class.java))
        }
    }


    fun addNewProduct(name : String, place : String, reason: String) {
        val sp = getSharedPreferences("email and password", MODE_PRIVATE)
        val sdf = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru"))
        val currentDate = sdf.format(Date())

        val product = hashMapOf(
            "name" to name,
            "place" to place,
            "change" to "m$currentDate",
            "reason_repair" to reason,
            "who_changed" to sp.getString("fullName", "Ошибка"),
            "quantity" to binding.count.text.toString(),
            "number" to "null"
        )

        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                if (place[0] == 's')
                    Toast.makeText(this@MoveTheProductActivity,
                        "Товар перемещен на склад", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MoveTheProductActivity,
                        "Товар перемещен в ремонт", Toast.LENGTH_SHORT).show()

                if (place[0] == 's')
                    addChange(name, sp.getString("fullName", "Ошибка").toString(),
                        "add_s", currentDate.toString(), binding.count.text.toString())
                else {
                    val serNum = documentReference.id
                    addChange(name, sp.getString("fullName", "Ошибка").toString(),
                        "add_r_${reason}_${serNum + "0"}", currentDate.toString(), binding.count.text.toString())

                    db.collection("products").document(serNum).update("number", serNum + "0")
                }

                startActivity(Intent(this@MoveTheProductActivity, MainMenuActivity::class.java))
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    fun moveProduct(name : String, place : String, reason : String?) {

        db.collection("products")
            .document(id)
            .get()
            .addOnSuccessListener { product ->

                if (product.get("quantity").toString().toInt() < binding.count.text.toString().toInt()) {
                    Toast.makeText(this, "Выбранное количество превышает имеющееся: " +
                            "${product.get("quantity").toString()}", Toast.LENGTH_SHORT).show()
                }
                else {

                    val currentDate = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru")).format(Date()).toString()
                    val sp = getSharedPreferences("email and password", MODE_PRIVATE)
                    val from = product.get("place").toString()[0]

                    if (from == 's') {
                        if (place[0] == 'r') {
                            replaceToRepair(name, place, reason!!, from)
                        }
                        else
                            addChange(name, sp.getString("fullName", "Ошибка").toString(),
                                "move_s_d", currentDate, binding.count.text.toString())
                    }
                    else if (from == 'r') {
                        if (place[0] == 's')
                            addChange(name, sp.getString("fullName", "Ошибка").toString(),
                                "move_r_s", currentDate, binding.count.text.toString())
                        else
                            addChange(name, sp.getString("fullName", "Ошибка").toString(),
                                "move_r_d", currentDate, binding.count.text.toString())
                    }
                    else {
                        if (place[0] == 's')
                            addChange(name, sp.getString("fullName", "Ошибка").toString(),
                                "move_d_s", currentDate, binding.count.text.toString())
                        else
                            replaceToRepair(name, place, reason!!, from)
                    }

                    changeDate(id, product.get("quantity").toString())

                    if (place[0] == 's')
                        Toast.makeText(this@MoveTheProductActivity,
                            "Товар перемещен на склад", Toast.LENGTH_SHORT).show()
                    else if (place[0] == 'r')
                        Toast.makeText(this@MoveTheProductActivity,
                            "Товар перемещен в ремонт", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(this@MoveTheProductActivity,
                            "Товар был выдан", Toast.LENGTH_SHORT).show()

                    startActivity(Intent(this@MoveTheProductActivity, MainMenuActivity::class.java))

                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun replace(name : String, place : String) {
        db.collection("products")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->

                val currentDate = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru")).format(Date()).toString()
                val sp = getSharedPreferences("email and password", MODE_PRIVATE)
                var flag = false

                for (product in result) {
                    if (product.get("place").toString()[0] == place[0]) {
                        flag = true
                        db.collection("products").document(product.id)
                            .update("quantity", product.get("quantity").toString()
                                .toInt() + binding.count.text.toString().toInt())

                        db.collection("products").document(product.id).update("change",
                            currentDate)
                        db.collection("products").document(product.id).update("who_changed",
                            sp.getString("fullName", "Произошла непредвиденная ошибка"))

                    }
                }

                if (!flag) { // если на таком месте нет товара с данным именем

                    val product = hashMapOf(
                        "name" to name,
                        "place" to place,
                        "change" to "m$currentDate",
                        "reason_repair" to "null",
                        "who_changed" to sp.getString("fullName", "Ошибка"),
                        "quantity" to binding.count.text.toString()
                    )

                    db.collection("products")
                        .add(product)
                        .addOnFailureListener {
                            Toast.makeText(
                                this@MoveTheProductActivity,
                                "Не получилось, попробуйте позже",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun replaceToRepair(name : String, place : String, reason: String, from : Char) {
        db.collection("products")
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->

                val currentDate = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru")).format(Date()).toString()
                val sp = getSharedPreferences("email and password", MODE_PRIVATE)
                var count = 0
                var flag = false

                for (product in result) {
                    if (product.get("place").toString()[0] == place[0]) {
                        count += 1

                        if (product.get("reason_repair").toString() == reason) {
                            flag = true
                            db.collection("products").document(product.id)
                                .update(
                                    "quantity", product.get("quantity").toString()
                                        .toInt() + binding.count.text.toString().toInt()
                                )

                            db.collection("products").document(product.id).update(
                                "change",
                                currentDate
                            )
                            db.collection("products").document(product.id).update(
                                "who_changed",
                                sp.getString("fullName", "Произошла непредвиденная ошибка"))

                            addChange(name, sp.getString("fullName",
                                "Ошибка").toString(),
                                "move_${from}_r_${reason}_${product.get("number")}",
                                currentDate, binding.count.text.toString())

                        }

                    }
                }

                if (!flag) { // если на таком месте нет товара с данным именем

                    val product = hashMapOf(
                        "name" to name,
                        "place" to place,
                        "change" to "m$currentDate",
                        "reason_repair" to reason,
                        "who_changed" to sp.getString("fullName", "Ошибка"),
                        "quantity" to binding.count.text.toString(),
                        "number" to "null"
                    )

                    db.collection("products")
                        .add(product)
                        .addOnSuccessListener { documentReference ->
                            db.collection("products").document(documentReference.id)
                                .update("number", documentReference.id + count.toString())

                            addChange(name, sp.getString("fullName",
                                "Ошибка").toString(),
                                "move_${from}_r_${reason}_${documentReference.id +
                                        count.toString()}", currentDate, binding.count.text.toString())
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this@MoveTheProductActivity,
                                "Не получилось, попробуйте позже",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun changeDate(id : String, quantity : String) {
        // изменить дату и человека, количество, если надо - удалить

        if (quantity.toInt() == binding.count.text.toString().toInt())
            db.collection("products").document(id).delete()
        else {
            val currentDate = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru")).format(Date()).toString()
            val sp = getSharedPreferences("email and password", MODE_PRIVATE)

            db.collection("products").document(id)
                .update("quantity", quantity.toInt() - binding.count.text.toString().toInt())
            db.collection("products").document(id)
                .update("change", currentDate)
            db.collection("products").document(id)
                .update("who_changed", sp.getString("fullName",
                    "Произошла непредвиденная ошибка"))
        }
    }

    private fun addChange(name: String, who: String, what: String, date: String, quantity: String) {
        val change = hashMapOf(
            "name" to name,
            "who" to who,
            "what" to what,
            "date" to date,
            "quantity" to quantity
        )

        db.collection("changes")
            .add(change)
            .addOnFailureListener {
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

}
