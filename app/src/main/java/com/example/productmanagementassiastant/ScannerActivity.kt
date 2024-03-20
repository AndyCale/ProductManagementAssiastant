
package com.example.productmanagementassiastant


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.productmanagementassiastant.databinding.ActivityMainBinding
import com.example.productmanagementassiastant.databinding.ActivityScannerBinding

class ScannerActivity : AppCompatActivity() {
    private var _binding: ActivityScannerBinding? = null
    private val binding: ActivityScannerBinding
        get() = _binding ?: throw IllegalStateException("Binding in Main Activity must not be null")

    companion object {
        const val inform = "info"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val info = intent.getStringExtra(inform)
        if (info != null) {
            binding.nameProduct.text = info
        }


        binding.backToMenu.setOnClickListener {
            finish()
        }
    }
}
