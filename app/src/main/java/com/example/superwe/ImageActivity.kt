package com.example.superwe

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class ImageActivity : AppCompatActivity() {
    val TAG = "ImageActivity"
    lateinit var screenImage : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"ImageActivity is created")
        setContentView(R.layout.activity_image)
        screenImage = findViewById(R.id.img_screenshot)


        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, 2)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2) {
            // 从相册返回的数据
            if (data != null) {
                // 得到图片的全路径
                val imageUri : Uri? = data.getData();
                if (imageUri != null) {
                    val file = RedrawHelper.getFileByUri(imageUri, this)
                    println(file.absolutePath)
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ),
                        0
                    )
                    val returnUri = RedrawHelper.redrawScreenshot(this,imageUri)
                    screenImage.setImageURI(returnUri)
                }
            }
        }
    }

    private fun toast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }
}