package com.example.superwe

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.superwe.RedrawHelper.getFileByUri
import com.example.superwe.RedrawHelper.redrawScreenshot
import com.example.superwe.toast.XToast
import com.example.superwe.toast.draggable.SpringDraggable
import com.google.android.material.navigation.NavigationView
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.orhanobut.hawk.Hawk


class ControlActivity : AppCompatActivity() {
    private val TAG = "ControlActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"ControlActivity is created")
        setContentView(R.layout.activity_control)
        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setHomeAsUpIndicator(R.drawable.more)
        }
        val navView : NavigationView = findViewById(R.id.navView)
        navView.setNavigationItemSelectedListener {
            val drawerLayout : DrawerLayout= findViewById(R.id.drawerLayout)
            when (it.itemId) {
                R.id.configuration_details_item -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this,ConfigurationActivity::class.java))
                }
                R.id.edit_group_item -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this,EditFriendGroupActivity::class.java))
                }
                R.id.friend_information -> {
                    drawerLayout.closeDrawers()
                    startActivity(Intent(this,FriendInformationActivity::class.java))
                }
                else -> Log.d(
                    TAG,
                    "There still has some items of navigationView don't  config!"
                )
            }
            true
        }

        Hawk.put(Constant.DISPOSABLE_ACTION,false)
        initView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val drawerLayout : DrawerLayout= findViewById(R.id.drawerLayout)
        when (item.itemId){
            android.R.id.home -> drawerLayout.openDrawer(GravityCompat.START)
        }
        return true
    }

//    @SuppressLint("Range")
//    private fun initSpinner(){
//        val spinner : Spinner = findViewById(R.id.group_name_spinner)
//        var list: ArrayList<String> = arrayListOf<String>()
//        var cursor:Cursor = SuperWeDatabaseUtils.query(this,"friend_group",null,null,null);
//        while (cursor.moveToNext()){
//            list.add(cursor.getString(cursor.getColumnIndex("group_name")))
//        }
//        var adapter : ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, list)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.setAdapter(adapter)
//
//        spinner.onItemSelectedListener= object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>?,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val group_name = list[position]
//                var cursor:Cursor = SuperWeDatabaseUtils.query(this@ControlActivity,"friend_group",null,"group_name=?",arrayOf(group_name))
//                if(cursor.moveToNext()){
//                    val editGroup : EditText = findViewById(R.id.edit_group)
//                    val editFriends : EditText = findViewById(R.id.edit_friends)
//                    editGroup.setText( cursor.getString(cursor.getColumnIndex("group_name")))
//                    editFriends.setText( cursor.getString(cursor.getColumnIndex("friends_list")) )
//
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {
//            }
//        }
//    }

    @SuppressLint("Range")
    private fun initView() {

        Hawk.put(Constant.ANOTHER_AUTO_ZAN,false)
        //开启一次性操作组件
        val btnAddFriendsIntoGroup : Button = findViewById(R.id.cb_invite_friends_into_group)
        val btnRecordAction : Button = findViewById(R.id.btn_record_action)
        val btnGroupCharge : Button = findViewById(R.id.btn_group_charge)
        val btnBatchReply : Button = findViewById(R.id.batch_reply)
        val btnBatchRead : Button = findViewById(R.id.btn_batch_read)
        val btnContactFile : Button  = findViewById(R.id.btn_contact_file)
        val btnRedraw : Button = findViewById(R.id.btn_redraw)
        //开启非一次性操作组件
        val cbAutoZan : CheckBox = findViewById(R.id.cb_auto_zan)
        val cbAutoReceiveLuckyMoney : CheckBox = findViewById(R.id.cb_lucky_money)
        val cbAutoReply : CheckBox = findViewById(R.id.auto_reply)
        //辅助组件
        val btnOpenWechat : Button = findViewById(R.id.btn_open_wechat)
        val btnOpenAccessbility : Button = findViewById(R.id.btn_open_accessibility)
        val btnReset : Button = findViewById(R.id.btn_reset)
        val btnSure : Button = findViewById(R.id.btn_sure)
        val editGroup : EditText = findViewById(R.id.edit_group)
        val editFriends : EditText = findViewById(R.id.edit_friends)
        val autoReplyContent : EditText = findViewById(R.id.auto_reply_content)
        val btnCheck : Button = findViewById(R.id.content_check)
        val batchReplyContent : EditText = findViewById(R.id.batch_reply_content)
        val btnConfirm : Button = findViewById(R.id.content_confirm)
        val btnDisplayWindow : Button = findViewById(R.id.btn_display_window)
        val btnFoldWindow : Button = findViewById(R.id.btn_fold_window)
        var xToast : XToast<XToast<*>> = XToast<XToast<*>>(application)
        val groupNameRepeat : TextView = findViewById(R.id.group_name_repeat_warn_text)
        val btnFinishRecord : Button = findViewById(R.id.btn_finish_record)
        val btnCheckActionList : Button = findViewById(R.id.btn_check_action_list)

        editGroup.visibility = View.GONE
        editFriends.visibility = View.GONE
        autoReplyContent.visibility = View.GONE
        btnCheck.visibility = View.GONE
        batchReplyContent.visibility = View.GONE
        btnConfirm.visibility = View.GONE
        btnSure.visibility = View.GONE
        btnReset.visibility = View.GONE
        groupNameRepeat.visibility = View.GONE
        val context = this

        editGroup.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val cursor = SuperWeDatabaseUtils.query(context,"friend_group",null,"group_name=?",arrayOf(s.toString()))
                if(cursor.moveToNext()){
                    groupNameRepeat.visibility = View.VISIBLE
                }
                else{
                    groupNameRepeat.visibility = View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        btnOpenWechat.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnContactFile.setOnClickListener{
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.GET_CONTACTS,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
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

        btnAddFriendsIntoGroup.setOnClickListener {
            if(btnSure.visibility==View.GONE){
                editGroup.visibility = View.VISIBLE
                editFriends.visibility = View.VISIBLE
                btnReset.visibility = View.VISIBLE
                btnSure.visibility = View.VISIBLE
                editGroup.setText(Hawk.get(Constant.GROUP_NAME,""))
                editFriends.setText(Hawk.get(Constant.FRIEND_LIST,""))
            }
            else if(btnSure.visibility==View.VISIBLE){
                val group = editGroup.text.toString()
                val friends = editFriends.text.toString()
                Hawk.put(Constant.GROUP_NAME, group)
                Hawk.put(Constant.FRIEND_LIST,friends)
                editGroup.visibility = View.GONE
                editFriends.visibility = View.GONE
                btnReset.visibility = View.GONE
                btnSure.visibility = View.GONE
                groupNameRepeat.visibility = View.GONE
            }
        }

        btnSure.setOnClickListener {
            val group = editGroup.text.toString()
            val friends = editFriends.text.toString()
            Hawk.put(Constant.GROUP_NAME, group)
            Hawk.put(Constant.FRIEND_LIST,friends)
            val cursor = SuperWeDatabaseUtils.query(this,"friend_group",null,"group_name=?",arrayOf(group))
            if(cursor.moveToNext()){
                AlertDialog.Builder(this).apply {
                    setTitle("提示")
                    setMessage("当前分组名与已有分组名重复，请更改")
                    setPositiveButton("确认"){dialog,which->}
                }.show()
            }
            else{
                val contentValues = ContentValues().apply {
                    put("friends_list",friends)
                    put("group_name",group)
                }
                SuperWeDatabaseUtils.add(this,"friend_group",contentValues)
                shortToast("已帮您将\""+group+"\"添加至好友分组列表")
                Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,true)
                Hawk.put(Constant.DISPOSABLE_ACTION,true)
                val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
                startActivity(intent)
            }
        }

        val editText = EditText(this)
        val alertDialog : AlertDialog.Builder = AlertDialog.Builder(this).apply {
            setTitle("请输入行为名称")
            setView(editText)
            setCancelable(false)
            setPositiveButton("保存") { dialog, which ->
                val actions : MutableMap<Int,Action> = Hawk.get(Constant.ACTIONS)
                val action = Action(editText.text.toString(),Hawk.get(Constant.WATCHER))
                actions.put(action.id,action)
                Hawk.put(Constant.ACTIONS,actions)
                Hawk.put(Constant.WATCHER,arrayListOf<Pair<String?,String?>>())
            }
            setNegativeButton("取消") {dialog,which->
            }
        }
        val dialog : AlertDialog = alertDialog.create()

        btnFinishRecord.setOnClickListener {
            Hawk.put(Constant.REPEAT_ACTION,false)
            Hawk.put(Constant.RECORD_ACTION,false)
            editText.text.clear()
            dialog.show()
        }

        btnCheckActionList.setOnClickListener {

        }

        btnCheck.setOnClickListener {
            val contents = autoReplyContent.text.toString()
            Hawk.put(Constant.AUTO_REPLY_CONTENT,contents)
            Hawk.put(Constant.AUTO_REPLY, true)
            shortToast("已成功设置自动回复内容")
        }

        cbAutoReply.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                autoReplyContent.visibility = View.VISIBLE
                btnCheck.visibility = View.VISIBLE
                autoReplyContent.setText(Hawk.get(Constant.AUTO_REPLY_CONTENT,"wait a minute"))
            } else {
                Hawk.put(Constant.AUTO_REPLY_CONTENT,autoReplyContent.text.toString())
                autoReplyContent.visibility = View.GONE
                btnCheck.visibility = View.GONE
                Hawk.put(Constant.AUTO_REPLY, false)
            }
        }


        btnBatchReply.setOnClickListener{
            if(batchReplyContent.visibility == View.GONE){
                batchReplyContent.visibility = View.VISIBLE
                btnConfirm.visibility = View.VISIBLE
                batchReplyContent.setText(Hawk.get(Constant.BATCH_REPLY_CONTENT,"Hi~ o(*￣▽￣*)ブ"))
            }
            else if(batchReplyContent.visibility == View.VISIBLE){
                val contents = batchReplyContent.text.toString()
                Hawk.put(Constant.BATCH_REPLY_CONTENT,contents)
                batchReplyContent.visibility = View.GONE
                btnConfirm.visibility = View.GONE
            }
        }


        btnConfirm.setOnClickListener {
            val contents = batchReplyContent.text.toString()
            Hawk.put(Constant.BATCH_REPLY_CONTENT,contents)
            Hawk.put(Constant.BATCH_REPLY,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

        btnGroupCharge.setOnClickListener {
            Hawk.put(Constant.GROUP_CHARGE,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            startActivity(intent)
        }

//        btnRepeatAction.setOnClickListener {
//            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
//            Hawk.put(Constant.REPEAT_ACTION,true)
//            Hawk.put(Constant.DISPOSABLE_ACTION,true)
//            startActivity(intent)
//        }

        btnRecordAction.setOnClickListener {
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.RECORD_ACTION,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            startActivity(intent)
        }

        btnBatchRead.setOnClickListener {
            Hawk.put(Constant.BATCH_READ,true)
            val intent = packageManager.getLaunchIntentForPackage("com.tencent.mm")
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            startActivity(intent)
        }

        btnDisplayWindow.setOnClickListener {
            if (!xToast.isShowing) {
                XXPermissions.with(this)
                    .permission(com.hjq.permissions.Permission.SYSTEM_ALERT_WINDOW)
                    .request(object : OnPermissionCallback {
                        override fun onGranted(granted: List<String>, all: Boolean) {
                            xToast = showGlobalWindow(application, btnFinishRecord)
                        }

                        override fun onDenied(denied: List<String>, never: Boolean) {
                            shortToast("fail")
                        }
                    })
            } else {
                toast("有一个悬浮窗正在运行中！")
            }
        }

        btnFoldWindow.setOnClickListener {
            if (xToast.isShowing) {
                xToast.cancel()
            }
        }

        btnRedraw.setOnClickListener {
            val intent = Intent("com.example.superwe.redraw")
            startActivity(intent)
        }

        btnCheckActionList.setOnClickListener {
            val intent = Intent(this,ActionActivity::class.java)
            startActivity(intent)
        }

    }

    fun showGlobalWindow(application: Application,btn : Button) : XToast<XToast<*>> {
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
                            val intent = Intent("android.intent.action.MAIN")
                            toast.cancel()
                            startActivity(intent)
                            btn.callOnClick()
                        }
                    })
                    .show()
            }
            .show()
        return xToast
    }

    /**
     * 判断是否缺少”无障碍服务“权限
     */
    fun judgeAccessbilityServicePerssion(mContexts:Context,permission:String) : Boolean{
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED
    }

    /**
     * 判断是否缺少”悬浮窗“权限
     */
    fun judgeAlertWindowPerssion(mContexts:Context,permission:String) : Boolean{
        return ContextCompat.checkSelfPermission(mContexts, permission) ==
                PackageManager.PERMISSION_DENIED
    }

    private fun toast(str: String) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }
}