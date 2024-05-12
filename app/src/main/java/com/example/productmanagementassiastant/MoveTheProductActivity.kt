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
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")
    val db = Firebase.firestore

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

        val info = intent.getStringExtra("info")
        val found = intent.getIntExtra("found", 0)

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
                }
                else { // в ремонте
                    binding.placeProduct.text = getString(R.string.repair)
                    binding.button1.text = getString(R.string.onWarehouse)
                    binding.button2.text = getString(R.string.issue)
                }
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
            "quantity" to binding.count.text.toString().toInt()
        )

        if (place[0] == 's')
            addChange(name, sp.getString("fullName", "Ошибка").toString(), "add_s", currentDate.toString())
        else {
            addChange(name, sp.getString("fullName", "Ошибка").toString(), "add_r_${reason}_/Серийник/", currentDate.toString())
        }

        db.collection("products")
            .add(product)
            .addOnSuccessListener { documentReference ->
                if (place[0] == 's')
                    Toast.makeText(this@MoveTheProductActivity,
                        "Товар перемещен на склад", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this@MoveTheProductActivity,
                        "Товар перемещен в ремонт", Toast.LENGTH_SHORT).show()

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
            .whereEqualTo("name", name)
            .get()
            .addOnSuccessListener { result ->
                for (product in result) {
                    /*
                    if (product.get("quantity").toString().toInt() < binding.count.text.toString().toInt()) {
                        Toast.makeText(this, "Количество данного товара", Toast.LENGTH_SHORT).show()
                    }

                     */
                    val currentDate = SimpleDateFormat("dd.M.yyyy/HH:mm:ss", Locale("ru")).format(Date()).toString()
                    val sp = getSharedPreferences("email and password", MODE_PRIVATE)
                    val from = product.get("place").toString().get(0)

                    if (from == 's') {
                        if (place[0] == 'r')
                            addChange(name, sp.getString("fullName", "Ошибка").toString(), "move_s_r_${reason}_/Серийник/", currentDate)
                        else
                            addChange(name, sp.getString("fullName", "Ошибка").toString(), "move_s_d", currentDate)
                    }
                    else {
                        if (place[0] == 's')
                            addChange(name, sp.getString("fullName", "Ошибка").toString(), "move_r_s", currentDate)
                        else
                            addChange(name, sp.getString("fullName", "Ошибка").toString(), "move_r_d", currentDate)
                    }

                    db.collection("products").document(product.id).update("place", place)
                    db.collection("products").document(product.id).update("change",
                        currentDate)
                    db.collection("products").document(product.id).update("who_changed",
                        sp.getString("fullName", "Произошла непредвиденная ошибка"))
                    if (reason != null)
                        db.collection("products").document(product.id).update("reason_repair",
                        reason)

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
            .addOnFailureListener { e ->
                Toast.makeText(
                    this@MoveTheProductActivity,
                    "Не получилось, попробуйте позже",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addChange(name: String, who: String, what: String, date: String) {
        val change = hashMapOf(
            "name" to name,
            "who" to who,
            "what" to what,
            "date" to date
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
