package com.example.productmanagementassiastant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.productmanagementassiastant.databinding.ActivityInfoTheProductBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class InfoTheProductActivity : AppCompatActivity() {
    private var _binding: ActivityInfoTheProductBinding? = null
    private val binding: ActivityInfoTheProductBinding
        get() = _binding ?: throw IllegalStateException("Binding in Info Activity must not be null")
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityInfoTheProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMenu.setOnClickListener {
            finish()
        }

        val document = intent.getStringExtra("id")
        if (document != null) {
            val db = Firebase.firestore
            db.collection("products").document(document).get().addOnSuccessListener { document ->
                if (document != null) {
                    binding.nameProduct.text = document.get("name").toString()
                    if (document.get("place").toString()[0] == 'd')
                        binding.placeProduct.text = "Товар был выдан"
                    else
                        binding.placeProduct.text = document.get("place").toString()
                    binding.countProduct.text = document.get("quantity").toString()
                    binding.timeChangeProduct.text = document.get("change").toString().substring(1)
                    binding.whoChangeProduct.text = document.get("who_changed").toString()

                    if (document.get("place").toString().get(0) == 's')
                        binding.changeProduct.text = "Товар был перемещен на склад "
                    else if (document.get("place").toString().get(0) == 'd')
                        binding.changeProduct.text = "Товар был выдан"
                    else {
                        binding.reason.visibility = View.VISIBLE
                        binding.changeProduct.text = "Товар был перемещен в ремонт"
                        binding.reasonRepairProduct.text = document.get("reason_repair").toString()
                    }
                } else {
                    Toast.makeText(this@InfoTheProductActivity, "Что-то пошло не так1", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
                .addOnFailureListener { exception ->
                    Toast.makeText(this@InfoTheProductActivity, "Что-то пошло не так2", Toast.LENGTH_SHORT).show()
                    finish()
                }


        }
        else {
            Toast.makeText(this@InfoTheProductActivity, "Что-то пошло не так3", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}