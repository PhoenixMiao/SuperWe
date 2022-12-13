package com.example.superwe

import android.content.ComponentName
import android.content.Intent
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
        val intent = Intent(Intent.ACTION_MAIN)
        val cmp = ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI")
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.component = cmp
        startActivity(intent)
    }
}