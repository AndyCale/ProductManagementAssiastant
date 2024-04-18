package com.example.productmanagementassiastant

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import com.example.productmanagementassiastant.databinding.ActivityMainMenuBinding

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var sp = getSharedPreferences("email and password", MODE_PRIVATE)
        if (sp.getString("TY", "null") != "null") {
            val intent = Intent(this@MainActivity, MainMenuActivity::class.java)
            startActivity(intent)
        }
        else {
            binding.signUp.setOnClickListener {
                val intent = Intent(this@MainActivity, SignUpActivity::class.java)
                startActivity(intent)
            }

        }
    }
}


