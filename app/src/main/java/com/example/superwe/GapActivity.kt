package com.example.superwe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class GapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gap)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
        startActivity(intent)
    }
}