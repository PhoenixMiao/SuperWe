package com.example.superwe

import android.app.Application
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.superwe.toast.XToast
import com.example.superwe.toast.draggable.SpringDraggable
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.orhanobut.hawk.Hawk

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
        val btnDisplayWindow : Button = findViewById(R.id.btn_display_window)
        val btnFoldWindow : Button = findViewById(R.id.btn_fold_window)

        var xToast : XToast<XToast<*>> = XToast<XToast<*>>(application)

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
            Hawk.put(Constant.BATCH_REPLY,true)
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

        btnRecordAction.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.RECORD_ACTION,true)
            startActivity(intent)
        }

        btnBatchRead.setOnClickListener {
            Hawk.put(Constant.BATCH_READ,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnDisplayWindow.setOnClickListener {
            XXPermissions.with(this)
                .permission(com.hjq.permissions.Permission.SYSTEM_ALERT_WINDOW)
                .request(object : OnPermissionCallback {
                    override fun onGranted(granted: List<String>, all: Boolean) {
                        xToast = showGlobalWindow(application)
                    }
                    override fun onDenied(denied: List<String>, never: Boolean) {
                        shortToast("fail")
                    }
                })
        }

        btnFoldWindow.setOnClickListener {
            if (xToast.isShowing) {
                xToast.cancel();
            }
        }

    }

    fun showGlobalWindow(application: Application) : XToast<XToast<*>> {
        val xToast : XToast<XToast<*>> = XToast<XToast<*>>(application)
        // 传入 Application 表示这个是一个全局的 Toast
        xToast
            .setContentView(R.layout.window_wechat)
            .setGravity(Gravity.END or Gravity.BOTTOM)
            .setYOffset(200)
            .setDraggable(SpringDraggable())
            .setOnClickListener(R.id.logo) { toast: XToast<*>, view: View?  ->
                XToast<XToast<*>>(application)
                    .setContentView(R.layout.window_hint)
                    .setOutsideClick()
                    .setOnTouchListener { toast, view, event ->
                        if (event?.action == MotionEvent.ACTION_OUTSIDE) {
                            Log.d(TAG, "onTouch: ")
                            toast?.cancel()
                        }
                        false
                    }
                    .setAnimStyle(R.style.IOSAnimStyle)
                    .setImageDrawable(R.id.icon1, R.drawable.payment)
                    .setImageDrawable(R.id.icon2, R.drawable.clear)
                    .setImageDrawable(R.id.icon3, R.drawable.record)
                    .setImageDrawable(R.id.icon4, R.drawable.repeat)
                    .setOnClickListener(R.id.icon1, object : XToast.OnClickListener<View?> {
                        override fun onClick(toast: XToast<*>, view: View?) {
                            Hawk.put(Constant.GROUP_CHARGE, true)
                            toast.cancel()
                            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
                            startActivity(intent)
                        }
                    })
                    .setOnClickListener(R.id.icon2, object : XToast.OnClickListener<View?> {
                        override fun onClick(toast: XToast<*>, view: View?) {
                            Hawk.put(Constant.BATCH_READ, true)
                            toast.cancel()
                            val intent = Intent("com.example.superwe.gap")
                            startActivity(intent)
                            onPause()
                        }
                    })
                    .setOnClickListener(R.id.icon3, object : XToast.OnClickListener<View?> {
                        override fun onClick(toast: XToast<*>, view: View?) {
                            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
                            Hawk.put(Constant.RECORD_ACTION, true)
                            toast.cancel()
                            startActivity(intent)
                        }
                    })
                    .setOnClickListener(R.id.icon4, object : XToast.OnClickListener<View?> {
                        override fun onClick(toast: XToast<*>, view: View?) {
                            Hawk.put(Constant.REPEAT_ACTION, true)
                            toast.cancel()
                            val intent = Intent("com.example.superwe.gap")
                            startActivity(intent)
                            onPause()
                        }
                    })
                    .show()
            }
            .show()
        return xToast
    }
}