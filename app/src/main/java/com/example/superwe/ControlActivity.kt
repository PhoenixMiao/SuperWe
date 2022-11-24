package com.example.superwe

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.orhanobut.hawk.HawkBuilder

class ControlActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        initView()
    }

    private fun initView() {
        val cbAutoZan : CheckBox = findViewById(R.id.cb_auto_zan)
        val btnOpenWechat : Button = findViewById(R.id.btn_open_wechat)
        val cbAutoReceiveLuckyMoney : CheckBox = findViewById(R.id.cb_lucky_money)
        val btnOpenAccessbility : Button = findViewById(R.id.btn_open_accessibility)
        val btnReset : Button = findViewById(R.id.btn_reset)
        val btnSure : Button = findViewById(R.id.btn_sure)
        val cbAddFriendsIntoGroup : CheckBox = findViewById(R.id.cb_invite_friends_into_group)
        val editGroup : EditText = findViewById(R.id.edit_group)
        val editFriends : EditText = findViewById(R.id.edit_friends)
        val btnRecordAction : Button = findViewById(R.id.btn_record_action)
        val btnRepeatAction : Button = findViewById(R.id.btn_repeat_action)
        val btnGroupCharge : Button = findViewById(R.id.btn_group_charge)
        val cbAutoReply : CheckBox = findViewById(R.id.auto_reply)
        val autoReplyContent : EditText = findViewById(R.id.auto_reply_content)
        val btnCheck : Button = findViewById(R.id.content_check)
        val cbBatchReply : CheckBox = findViewById(R.id.batch_reply)
        val batchReplyContent : EditText = findViewById(R.id.batch_reply_content)
        val btnConfirm : Button = findViewById(R.id.content_confirm)
        val btnBatchRead : Button = findViewById(R.id.btn_batch_read)

        editGroup.visibility = View.GONE
        editFriends.visibility = View.GONE
        autoReplyContent.visibility = View.GONE
        btnCheck.visibility = View.GONE
        batchReplyContent.visibility = View.GONE
        btnConfirm.visibility = View.GONE
        btnSure.visibility = View.GONE
        btnReset.visibility = View.GONE

        btnOpenWechat.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnOpenAccessbility.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        cbAutoReceiveLuckyMoney.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.AUTO_RECEIVE_LUCKY_MONEY, true) else Hawk.put(Constant.AUTO_RECEIVE_LUCKY_MONEY, false)
        }

        cbAutoZan.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) Hawk.put(Constant.AUTO_ZAN, true) else Hawk.put(Constant.AUTO_ZAN, false)
        }

        btnRecordAction.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.RECORD_ACTION,true)
            startActivity(intent)
        }

        btnRepeatAction.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.REPEAT_ACTION,true)
            startActivity(intent)
        }

        btnReset.setOnClickListener {
            editFriends.setText("")
            editGroup.setText("")
        }

        cbAddFriendsIntoGroup.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editGroup.visibility = View.VISIBLE
                editFriends.visibility = View.VISIBLE
                btnReset.visibility = View.VISIBLE
                btnSure.visibility = View.VISIBLE
                editGroup.setText(Hawk.get(Constant.GROUP_NAME,""))
                editFriends.setText(Hawk.get(Constant.FRIEND_LIST,""))
            }
            else{
                editGroup.visibility = View.GONE
                editFriends.visibility = View.GONE
                btnReset.visibility = View.GONE
                btnSure.visibility = View.GONE
                Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
            }
        }

        btnSure.setOnClickListener {
            val group = editGroup.text.toString()
            val friends = editFriends.text.toString()
            Log.d(TAG,group)
            Log.d(TAG,friends)
            Hawk.put(Constant.GROUP_NAME, group)
            Hawk.put(Constant.FRIEND_LIST,friends)
            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnCheck.setOnClickListener {
            val contents = autoReplyContent.text.toString()
            Hawk.put(Constant.AUTO_REPLY_CONTENT,contents)
        }

        cbAutoReply.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoReplyContent.visibility = View.VISIBLE
                btnCheck.visibility = View.VISIBLE
                autoReplyContent.setText(Hawk.get(Constant.AUTO_REPLY_CONTENT,"wait a minute"))
                Hawk.put(Constant.AUTO_REPLY, true)
            } else {
                autoReplyContent.visibility = View.GONE
                btnCheck.visibility = View.GONE
                Hawk.put(Constant.AUTO_REPLY, false)
            }
        }

        btnConfirm.setOnClickListener {
            val contents = batchReplyContent.text.toString()
            Hawk.put(Constant.BATCH_REPLY_CONTENT,contents)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        cbBatchReply.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                batchReplyContent.visibility = View.VISIBLE
                btnConfirm.visibility = View.VISIBLE
                batchReplyContent.setText(Hawk.get(Constant.BATCH_REPLY_CONTENT,"test"))
                Hawk.put(Constant.BATCH_REPLY, true)
            } else {
                batchReplyContent.visibility = View.GONE
                btnConfirm.visibility = View.GONE
                Hawk.put(Constant.BATCH_REPLY, false)
            }
        }

        btnGroupCharge.setOnClickListener {
            Hawk.put(Constant.GROUP_CHARGE,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnRepeatAction.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.REPEAT_ACTION,true)
            startActivity(intent)
        }

        btnBatchRead.setOnClickListener {
            Hawk.put(Constant.BATCH_READ,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

    }

}