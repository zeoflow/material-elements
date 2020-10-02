package com.zeoflow.core.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zeoflow.material.elements.button.MaterialButtonLoading


class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val mbl = findViewById<MaterialButtonLoading>(R.id.mbl)
    mbl.setOnClickListener{
      print(21)
      mbl.setLoading(true)
    }
  }
}
