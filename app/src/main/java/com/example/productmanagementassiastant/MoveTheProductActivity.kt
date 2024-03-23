package com.example.productmanagementassiastant

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.productmanagementassiastant.databinding.ActivityMoveTheProductBinding
import com.example.productmanagementassiastant.databinding.ActivityScannerBinding

class MoveTheProductActivity : AppCompatActivity() {
    private var _binding: ActivityMoveTheProductBinding? = null
    private val binding: ActivityMoveTheProductBinding
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")

    companion object {
        const val inform = "info"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMoveTheProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backToMenu.setOnClickListener {
            finish()
        }

        val info = intent.getStringExtra(inform)
        if (info != null) {
            binding.nameProduct.text = info
        }

    }
}