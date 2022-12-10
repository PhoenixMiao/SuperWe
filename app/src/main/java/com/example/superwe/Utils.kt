package com.example.superwe

import android.widget.Toast

/**
 * 描述：包装Toast
 */
fun shortToast(msg: String) = Toast.makeText(SuperApp.instance, msg, Toast.LENGTH_SHORT).show()

fun longToast(msg: String) = Toast.makeText(SuperApp.instance, msg, Toast.LENGTH_LONG).show()