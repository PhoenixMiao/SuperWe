package com.example.superwe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class GapActivity : AppCompatActivity() {
    val TAG = "GapActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"GapActivity is created")
        setContentView(R.layout.activity_gap)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
        startActivity(intent)
    }
}