package com.example.productmanagementassiastant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.productmanagementassiastant.databinding.ActivitySignUpBinding
import com.example.productmanagementassiastant.databinding.ActivityVerificationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class VerificationActivity : AppCompatActivity() {
    private var _binding: ActivityVerificationBinding? = null
    private val binding: ActivityVerificationBinding
        get() = _binding ?: throw IllegalStateException("Binding in SignUp Activity must not be null")
    val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val info = intent.getStringArrayListExtra(MoveTheProductActivity.inform)
        if (info != null) {
            val user = hashMapOf(
                "full_name" to info[0],
                "email" to info[1],
                "password" to info[2]
            )

            db.collection("users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    val intent =
                        Intent(this@VerificationActivity, MainMenuActivity::class.java)
                    startActivity(intent)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this@VerificationActivity,
                        "Не получилось, попробуйте позже",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        else {
            var sp = getSharedPreferences("email and password", MODE_PRIVATE)
            sp.edit().putString("fullName", "null")
                .commit()
            Toast.makeText(
                this@VerificationActivity,
                "Не получилось, попробуйте позже",
                Toast.LENGTH_SHORT
            ).show()
            finish()
        }

    }
}