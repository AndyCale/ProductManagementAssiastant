package com.example.productmanagementassiastant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import com.example.productmanagementassiastant.databinding.ActivitySignUpBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private var _binding: ActivitySignUpBinding? = null
    private val binding: ActivitySignUpBinding
        get() = _binding ?: throw IllegalStateException("Binding in SignUp Activity must not be null")
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            signIn.setOnClickListener {
                finish()
            }


            signUp.setOnClickListener {
                if (!email.text.contains("@") || password.text.isEmpty() || fullName.text.isEmpty()) {
                    Toast.makeText(
                        this@SignUpActivity,
                        "Проверьте правильность данных",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    var sp = getSharedPreferences("email and password", MODE_PRIVATE)

                    db.collection("users")
                        .whereEqualTo("email", email.text.toString())
                        .get()
                        .addOnSuccessListener { result ->
                            if (result.isEmpty) {
                                sp.edit().putString("fullName", fullName.text.toString())
                                    .commit()
                                val intent = Intent(this@SignUpActivity, VerificationActivity::class.java)
                                intent.putStringArrayListExtra("info", arrayListOf<String>(fullName.text.toString(),
                                    email.text.toString(), password.text.toString()))
                                startActivity(intent)
                            } else {
                                Toast.makeText(
                                    this@SignUpActivity,
                                    "Данный email уже зарегистирован!", Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@SignUpActivity,
                                "Не получилось, попробуйте позже",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }



}