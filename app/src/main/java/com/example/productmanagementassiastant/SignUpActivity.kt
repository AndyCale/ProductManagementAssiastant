package com.example.productmanagementassiastant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        var sp = getSharedPreferences("email and password", MODE_PRIVATE)

        with(binding) {
            signIn.setOnClickListener {
                finish()
            }

            signUp.setOnClickListener {
                if (!email.text.contains("@") || password.text.isEmpty() || fullName.text.isEmpty()) {
                    Toast.makeText(this@SignUpActivity, "Проверьте правильность данных",
                        Toast.LENGTH_SHORT).show()
                }
                else {
                    val db = Firebase.firestore
                    val user = hashMapOf(
                        "full_name" to fullName.text.toString(),
                        "email" to email.text.toString(),
                        "password" to password.text.toString()
                    )

                    db.collection("users")
                        .add(user)
                        .addOnSuccessListener { documentReference ->
                                sp.edit().putString("fullName", fullName.text.toString()).commit()
                                val intent = Intent(this@SignUpActivity,
                                    MainMenuActivity::class.java)
                                startActivity(intent)
                             }
                        .addOnFailureListener { e ->
                            Toast.makeText(this@SignUpActivity,
                                "Не получилось, попробуйте позже", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}