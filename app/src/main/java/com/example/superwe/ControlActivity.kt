package com.example.superwe

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk

class ControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        initView()
    }

    private fun initView() {
        val cbFriendsSquare : CheckBox = findViewById(R.id.cb_friends_square)
        val btnWrite : Button = findViewById(R.id.btn_write)
        val edFriends : CheckBox = findViewById(R.id.ed_friends)
        val btnReset : Button = findViewById(R.id.btn_reset)
        val btnOpenWechat : Button = findViewById(R.id.btn_open_wechat)
        val btnOpenAccessbility : Button = findViewById(R.id.btn_open_accessbility)
        val cbAddFriends : CheckBox = findViewById(R.id.cb_add_friends)

        cbFriendsSquare.isChecked = Hawk.get(Constant.FRIEND_SQUARE,false)

        btnWrite.setOnClickListener {
            val member = Gson().fromJson(edFriends.text.toString(), Member::class.java)
            Hawk.put(Constant.MEMBER, member)
            shortToast("数据写入成功！")
        }

        btnReset.setOnClickListener {
            Hawk.put(Constant.MEMBER, Member())
            edFriends.setText("")
            shortToast("数据重置成功！")
        }

        btnOpenWechat.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnOpenAccessbility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        cbAddFriends.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.ADD_FRIENDS, true) else Hawk.put(Constant.ADD_FRIENDS, false)
        }

        cbFriendsSquare.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.FRIEND_SQUARE, true) else Hawk.put(Constant.FRIEND_SQUARE, false)
        }
    }

}