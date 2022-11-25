package com.example.superwe

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class GapActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gap)
        val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
        startActivity(intent)
        finish()
    }
}