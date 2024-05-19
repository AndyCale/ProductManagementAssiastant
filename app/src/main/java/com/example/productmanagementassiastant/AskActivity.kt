package com.example.productmanagementassiastant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.productmanagementassiastant.databinding.ActivityAskBinding
import com.example.productmanagementassiastant.databinding.ActivityMoveTheProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AskActivity : AppCompatActivity() {
    private var _binding: ActivityAskBinding? = null
    private val binding: ActivityAskBinding
        get() = _binding ?: throw IllegalStateException("Binding in Ask Activity must not be null")

    val listItem = ArrayList<String>()
    val listItemId = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding =ActivityAskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = Firebase.firestore
        val info = intent.getStringExtra("info")
        val choice = intent.getIntExtra("choice", 1)
        if (info != null) {
            var countRepair = 0
            db.collection("products")
                .whereEqualTo("name", info)
                .get()
                .addOnSuccessListener { result ->

                    if (choice == 1) {
                        listItem.add("Добавить новый товар")
                        listItemId.add("new")
                    }

                    for (res in result) {
                        listItemId.add(res.id)
                        var str = "Название: ${res.get("name").toString()}\nМестоположение: "
                        if (res.get("place").toString()[0] == 's')
                            str += "Склад"
                        else if (res.get("place").toString()[0] == 'r') {
                            countRepair += 1
                            str += "Ремонт\nПричина: "
                            str += "${res.get("reason_repair").toString()}\nСерийный номер: ${res.get("number").toString()}"
                        }
                        else
                            str += "Выдано"

                        listItem.add(str)
                    }

                    binding.products.adapter = ArrayAdapter<String>(this@AskActivity,
                        R.layout.list_item, R.id.item, listItem)
                }

            binding.products.setOnItemClickListener { parent, view, position, id ->
                if (choice == 1) {
                    val intent = Intent(this@AskActivity, MoveTheProductActivity::class.java)
                    intent.putExtra("id", listItemId.get(position))
                    if (position == 0)
                        intent.putExtra("found", 0)
                    else {
                        val temp = listItem.get(position).split(":")
                        if (temp.size == 3) {
                            if (temp[2] == " Склад")
                                intent.putExtra("found", 1) // склад
                            else
                                intent.putExtra("found", 3) // выдано
                        } else
                            intent.putExtra("found", 2) // ремонт
                    }
                    intent.putExtra("info", info)
                    intent.putExtra("countRepair", countRepair)
                    startActivity(intent)
                }
                else {
                    val intent = Intent(this@AskActivity, InfoTheProductActivity::class.java)
                    intent.putExtra("id", listItemId.get(position))
                    startActivity(intent)
                }
            }
        }
        else {
            Toast.makeText(
                this@AskActivity,
                "Что-то пошло не так",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(this@AskActivity, MainMenuActivity::class.java))
        }
    }
}