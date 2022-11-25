package com.example.superwe

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Application
import android.app.Notification
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.os.*
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.ImageView
import android.widget.TextView
import com.example.superwe.toast.XToast
import com.example.superwe.toast.draggable.SpringDraggable
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SuperService : AccessibilityService() {

    private val TAG = "SuperService"
    private val handler = Handler()
    private var watcher = arrayListOf<Pair<String?,String?>>()
    private var lastLaunchUI = listOf<AccessibilityNodeInfo>()
    @JvmField var isAuto = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        val config = AccessibilityServiceInfo();
        //配置监听的事件类型为界面变化|点击事件
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED.or(AccessibilityEvent.TYPE_VIEW_CLICKED).or(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        serviceInfo = config;
    }

    override fun onInterrupt() {}

    @OptIn(DelicateCoroutinesApi::class)
    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        val eventType = event.eventType
        val classNameChr = event.className
        val className = classNameChr.toString()
        Log.d(TAG, event.toString())
        if(isAuto) return
        var nodeInfo = event.source //当前界面的可访问界点
        when (eventType) {
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                if(Hawk.get(Constant.RECORD_ACTION,false)) {
//                    if(event.packageName == "com.tencent.mm"){
                        if(event.recordCount==1){
                            for(i in lastLaunchUI.indices) {
                                if(nodeInfo == lastLaunchUI[i]) {
                                    watcher.add(Pair<String?,String?>("com.tencent.mm:id/bth",i.toString()))
                                    break
                                }
                            }
                        }else if(className=="android.widget.Button" && nodeInfo.text=="发送") {
                            Thread.sleep(1000)
                            val nodes = rootInActiveWindow
                            val messages = nodes.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b4b")
                            val list = Hawk.get(Constant.WATCHER, arrayListOf<Pair<String?,String?>>())
                            var flag = true
                            for(i in 1 until list.size + 1) {
                                if(list[list.size - i].first=="com.tencent.mm:id/bth") {
                                    if(flag) {
                                        list[list.size - i] = Pair<String?,String?>("com.tencent.mm:id/bth", 0.toString())
                                        flag = false;
                                    }else {
                                        list[list.size - i] = Pair<String?,String?>("com.tencent.mm:id/bth", ((list[list.size - i].second?.toInt()?:1) - 1).toString())
                                    }
                                }
                            }
                            watcher = list
                            watcher.add(Pair<String?,String?>("send",messages[messages.size-1].text.toString()))
                        }
                        else if(nodeInfo.className=="android.widget.EditText") watcher.add(Pair<String?,String?>("input",""))
                        else if(nodeInfo.viewIdResourceName!=null) watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,""))
                        else if(nodeInfo.text!=null) watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,nodeInfo.text.toString()))
                        else {
                            var ch = ""
                            while(ch == "" && nodeInfo.childCount>0 && nodeInfo.getChild(0).text!=null) {
                                ch = nodeInfo.getChild(0).text.toString()
                                nodeInfo = nodeInfo.getChild(0)
                            }
                            watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,ch))
                        }
                        Hawk.put(Constant.WATCHER,watcher)
//                    }
                }
            }

            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if(className == "com.tencent.mm.ui.LauncherUI") {
                    val nodes = rootInActiveWindow
                    if(nodes!=null) lastLaunchUI = nodes.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
                }
                if(Hawk.get(Constant.BATCH_READ,false)) {
                    isAuto = true
                    GlobalScope.launch {
                        groupCharge()
                        Hawk.put(Constant.GROUP_CHARGE,false)
                        isAuto = false
                    }
                }
                if(Hawk.get(Constant.REPEAT_ACTION,false)) {
                    isAuto = true
                    Hawk.put(Constant.RECORD_ACTION,false)
                    repeatAction()
                    Hawk.put(Constant.REPEAT_ACTION,false)
                    isAuto = false
                }
                if(Hawk.get(Constant.GROUP_CHARGE,false)) {
                    isAuto = true
                    GlobalScope.launch {
                        groupCharge()
                        Hawk.put(Constant.GROUP_CHARGE,false)
                        isAuto = false
                    }
                }
                if(Hawk.get(Constant.BATCH_REPLY,false)) {
                    isAuto = true
                    batchReply()
                    Hawk.get(Constant.BATCH_REPLY,false)
                    isAuto = false
                }
                val adding = Hawk.get(Constant.ADDING_FRIENDS_INTO_GROUP, false)
                if (adding) {
                    isAuto = true
                    if(className=="com.tencent.mm.ui.LauncherUI") openGroup()
                    else if(className == "com.tencent.mm.ui.contact.ChatroomContactUI") searchGroup()
                    else if(className == "com.tencent.mm.ui.chatting.ChattingUI") openGroupSetting()
                    else if(className == "com.tencent.mm.chatroom.ui.ChatroomInfoUI") openSelectContact()
                    else if(className == "com.tencent.mm.ui.mvvm.MvvmSelectContactUI") addMembers()
                    else performBackClick()
                    isAuto = false
                }
//                if (className == "com.tencent.mm.ui.widget.a.c") {
//                    dialogClick()
//                }
                else if(className == "com.tencent.mm.ui.LauncherUI"){
                    if(Hawk.get(Constant.AUTO_REPLY,false)) {
                        isAuto = true
                        fill()
                        isAuto = false
                    }
                    //自动收红包
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        isAuto = true
                        val rootNode = rootInActiveWindow
                        val hongBaoList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b47")
                        if(hongBaoList.size>0){
                            Thread.sleep(300)
                            hongBaoList.get(hongBaoList.size-1).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        isAuto = false
                    }
                }
                else if(className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI"){
                    //自动打开红包
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        isAuto = true
                        Thread.sleep(300)
                        val source = event.source
                        val openButton = source.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/giq")
                        if(openButton.size>0){
                            openButton[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        }
                        isAuto = false
                    }
                }
                else if(className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"){
                    //从红包界面退回
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        isAuto = true
                        performBackClick()
                        isAuto = false
                    }
                }
                else if (Hawk.get(Constant.AUTO_ZAN, false)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
                        isAuto = true
                        autoZan()
                        isAuto = false
                    }
                }
            }

            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                if (event.parcelableData != null && event.parcelableData is Notification) {
                    val notification = event.parcelableData as Notification
                    val content = notification.tickerText.toString()
                    if ((Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false) && content.contains("[微信红包]")) || Hawk.get(Constant.AUTO_REPLY,false)) {
                        isAuto = true
                        val pendingIntent = notification.contentIntent
                        try {
                            pendingIntent.send()
                        } catch (e: PendingIntent.CanceledException) {
                            e.printStackTrace()
                        }finally {
                            isAuto = false
                        }
                    }
                }
            }

            //            //滚动的时候也去监听红包，不过有点卡
////            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
////                if (className == "android.widget.ListView") {
////                    openRedPacket()
////                }
////            }
//        }
//    }
        }
    }

    //1.打开群聊
    private fun openGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val tabNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f2s")
            for (tabNode in tabNodes) {
                if (tabNode.text.toString() == "通讯录") {
                    tabNode.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    handler.postDelayed({
                        val newNodeInfo = rootInActiveWindow
                        if (newNodeInfo != null) {
                            val tagNodes = newNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kk")
                            for (tagNode in tagNodes) {
                                if (tagNode.text.toString() == "群聊") {
                                    tagNode.parent.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    Thread.sleep(500)
                                    break
                                }
                            }
                        }
                    }, 500L)
                }
            }
        }
    }

    //2.搜索群聊
    private fun searchGroup() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/f2v")
            var found=false
            for (node in nodes) {
                val infoNode = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bqu")
                if(infoNode.size>0)
                {
                    val info = infoNode[0]
                    val group = Hawk.get(Constant.GROUP_NAME,"").trim()
                    if (info.text.toString().equals(group)) {
                        found = true
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        break
                    }
                }
            }
            if(found==false){
                Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
            }
        }
    }

    //3.打开群聊设置
    private fun openGroupSetting() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eo")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
        else {
            performBackClick()
            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
        }

    }

    //4.滚动后点击添加按钮，打开添加成员页面
    private fun openSelectContact() {
        val friend_list_str = Hawk.get(Constant.FRIEND_LIST,"")
        if( friend_list_str!= "") {
            val members = friend_list_str.split("\n")
            if (members.size > 0) {
                val nodeInfo = rootInActiveWindow
                if (nodeInfo != null) {
                    val numText = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/text1")[0].text.toString()
                    val memberCount = numText.substring(numText.indexOf("(") + 1,numText.indexOf(")")).toInt()
                    val listNode = nodeInfo.findAccessibilityNodeInfosByViewId("android:id/list")[0]
                    if(memberCount > 100) {
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        listNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    }
                    val scrollNodeInfo = rootInActiveWindow
                    if (scrollNodeInfo != null) {
                        val nodes = scrollNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/iwc")
                        handler.postDelayed({
//                            for (info in nodes) {
//                                if (info.contentDescription.toString() == "添加成员") {
//                                    info.parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
//                                    break
//                                }
//                            }
                            nodes.get(memberCount).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            addMembers()
                        }, 1000L)
                    }
                }
            }
        } else {
            performBackClick()
            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
        }

    }

    //5.添加成员
    private fun addMembers() {
        val friend_list_str = Hawk.get(Constant.FRIEND_LIST,"")
        if( friend_list_str!= "") {
            val members = friend_list_str.split("\n")
            if (members.size > 0) {
                for (i in 0 until members.size) {
                    handler.postDelayed({
                        val nodeInfo = rootInActiveWindow
                        if (nodeInfo != null) {
                            val editNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/h7o")
                            if (editNodes != null && editNodes.size > 0) {
                                val editNode = editNodes[0]
                                val arguments = Bundle()
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, members[i].trim())
                                editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                            }
                        }
                    }, 500L * (i + 1))
                    handler.postDelayed({
                        val list = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j55")
                        val cbNodes = list[0].findAccessibilityNodeInfosByText(members[i].trim())
                        if (cbNodes != null) {
                            val cbNode: AccessibilityNodeInfo?
                            if (cbNodes.size > 0) {
                                cbNode = cbNodes[0]
                                cbNode?.parent?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                Thread.sleep(300)
                            }
                        }
                        //最后一次的时候清空记录，并且点击顶部确定按钮
                        if (i == members.size - 1) {
//                            Hawk.put(Constant.GROUP_NAME, "")
//                            Hawk.put(Constant.FRIEND_LIST, "")
                            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP, false)
                            val sureNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/e9s")
                            if (sureNodes != null && sureNodes.size > 0) {
                                if(sureNodes[0].text.toString().equals("完成")){
                                    performBackClick()
                                }
                                else{
                                    sureNodes[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }

                        }
                    }, 700L * (i + 1))
                }
            }
        }
    }

    //对话框自动点击
    private fun dialogClick() {
        val inviteNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b00")[0]
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    //自动点赞
    private fun autoZan() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            while (true) {
                val rootNode = rootInActiveWindow
                if (rootNode != null) {
                    val friend_squares = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jv8")
                    if (friend_squares != null && friend_squares.size > 0) {
                        for(i in 1..friend_squares.size){
                            val friend_square = friend_squares[i-1]
                            val zanNodes = friend_square.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nh")
                            for(zan in zanNodes) {
                                zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                Thread.sleep(300)
                                val zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jp")
                                if (zsNodes != null && zsNodes.size > 0) {
                                    val dianZan = zsNodes[0].findAccessibilityNodeInfosByText("赞")
                                    if (dianZan.size > 0) {
                                        dianZan[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                    }
                                }
                                Thread.sleep(500)
                            }
                            friend_square.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                        }
                    }
                } else {
                    break
                }
            }
        }
    }

    //遍历控件的方法
    fun recycle(info: AccessibilityNodeInfo) {
        if (info.childCount == 0) {
            Log.i(TAG, "child widget----------------------------" + info.className.toString())
            Log.i(TAG, "showDialog:" + info.canOpenPopup())
            Log.i(TAG, "Text：" + info.text)
            Log.i(TAG, "windowId:" + info.windowId)
            Log.i(TAG, "desc:" + info.contentDescription)
        } else {
            (0 until info.childCount)
                .filter { info.getChild(it) != null }
                .forEach { recycle(info.getChild(it)) }
        }
    }

    private fun performBackClick() {
        handler.postDelayed({ performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK) }, 1300L)
    }

    private fun groupCharge() {
        var nodeInfo = rootInActiveWindow
        var groupName = " "
        if (nodeInfo != null) {
            val groups = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ko4")
            if(groups==null || groups.size==0 || groups[0]==null) return
            groupName = groups[0].text.toString()
        }
        if(groupName.contains("(")) groupName = groupName.substringBeforeLast('(')
//        performBackClick()
//        Thread.sleep(10000)
        val map = mapOf("com.tencent.mm:id/g0" to 0,"com.tencent.mm:id/grs" to 0,"com.tencent.mm:id/iwc" to 3)
        for(pair in map) {
            nodeInfo = rootInActiveWindow
            if (nodeInfo != null) {
                println(pair)
                nodeInfo.findAccessibilityNodeInfosByViewId(pair.key)[pair.value].performAction(
                    AccessibilityNodeInfo.ACTION_CLICK
                )
            }
            Thread.sleep(1000)
        }

        Thread.sleep(1000)
        nodeInfo = rootInActiveWindow
        if(nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByText("群收款")
            if(nodes==null || nodes.size==0 || nodes[0]==null) return
            nodes[0].performAction(
                AccessibilityNodeInfo.ACTION_CLICK
            )
        }
        Thread.sleep(1000)
        nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/fis")
            if(nodes==null || nodes.size==0 || nodes[0]==null) return
            nodes[0].performAction(
                AccessibilityNodeInfo.ACTION_CLICK
            )
        }

        Thread.sleep(1000)
        nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/krs")
            if(nodes==null || nodes.size==0 || nodes[0]==null) return
            nodes[0].performAction(
                AccessibilityNodeInfo.ACTION_CLICK
            )
        }

        Thread.sleep(1000)
        nodeInfo = rootInActiveWindow
        if(nodeInfo!=null){
            val nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ev2")
            if(nodes==null || nodes.size==0 || nodes[0]==null) return
            nodes[0].performAction(
                AccessibilityNodeInfo.ACTION_CLICK
            )
        }
        Thread.sleep(1000)

//        if (nodeInfo != null) {
//            nodeInfo.findAccessibilityNodeInfosByText(groupName)[0].parent.performAction(
//                AccessibilityNodeInfo.ACTION_CLICK
//            )
//        }
        nodeInfo = rootInActiveWindow
        if(nodeInfo != null) {
            val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cd7")[0]
            val arguments = Bundle()
            arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,groupName)
            target.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,arguments)
        }
        Thread.sleep(1000)

        rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j9m")[0].getChild(3).performAction(AccessibilityNodeInfo.ACTION_CLICK)
        Thread.sleep(1000)

        Hawk.put(Constant.GROUP_CHARGE,false)
    }

    private fun repeatAction() {
        var nodeInfo = rootInActiveWindow
        val watcher = Hawk.get(Constant.WATCHER,arrayListOf<Pair<String?,String?>>())
        for(pair in watcher) {
            nodeInfo = rootInActiveWindow
            if(nodeInfo != null) {
//                if(pair.first==" " && pair.second==" ") {
//                    performBackClick()
//                    continue
//                }
                var node = nodeInfo.findAccessibilityNodeInfosByText(pair.second)
                if (pair.first == "com.tencent.mm:id/bth") {
                    println(pair)
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")[pair.second?.toInt()!!].performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                    )
                } else if (pair.first == "send") {
                    nodeInfo = rootInActiveWindow
                    val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b4a")[0]
                    target.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Thread.sleep(1000)
                    nodeInfo = rootInActiveWindow
                    if(findEditText(nodeInfo,pair.second.toString())) send()
                }else if((node==null || node.size==0) && pair.first!=null) {
                    node = nodeInfo.findAccessibilityNodeInfosByViewId(pair.first!!)
                }else if(node.size>0) {
                    while(node!=null && node[0]!=null && !node[0].isClickable) node[0] = node[0].parent
                    if(node!=null && node[0]!=null) node[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }else if(node!=null && nodeInfo.className!="android.widget.RelativeLayout" && nodeInfo.className!="android.widget.EditText"){
                    val back = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")
                    if(back!=null && back.size>0 && back[0]!=null)
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                Thread.sleep(1000)
            }
        }
        Hawk.put(Constant.WATCHER,arrayListOf<Pair<String?,String?>>())
    }

    private fun trr() {
        var nodeInfo = rootInActiveWindow
        nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            var target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
            val size = target.size
            for (i in 0 until size) {
//                nodeInfo = rootInActiveWindow
//                target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
                target[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(1000)
                nodeInfo = rootInActiveWindow
                nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(1000)
            }
            Thread.sleep(1000)
        }
    }

    /**
     * 执行遍历聊天框点击，消去红点
     * 问题：仍然无法只点击红点消息
     */
    private fun batchRead() {
        var nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            //红点聊天框
            val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kmv")
            //当前所有聊天框
            val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
            for (i in 0 until commu.size) {
//                for(j in 0 until target.size) {
//                    if(commu[i] == target[j].parent) {
                        commu[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
                        nodeInfo = rootInActiveWindow
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
//                    }
//                }
            }
            Thread.sleep(1000)
        }
    }

    private fun batchReply() {
        var nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            //红点聊天框
            val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kmv")
            //当前所有聊天框
            val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
            for (i in 0 until commu.size) {
//                for(j in 0 until target.size) {
//                    if(commu[i] == target[j].parent) {
                commu[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(100)
                val nodes = rootInActiveWindow
                findEditText(nodes,Hawk.get(Constant.BATCH_REPLY_CONTENT,"test"))
                Thread.sleep(1000)
                send()
                Thread.sleep(1000)
                nodeInfo = rootInActiveWindow
                nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(1000)
//                    }
//                }
            }
            Thread.sleep(1000)
        }
    }

    private fun fill() {
        val rootNode = rootInActiveWindow
        if (rootNode != null) {
            if(findEditText(rootNode, Hawk.get(Constant.AUTO_REPLY_CONTENT,"wait a minute"))){
                send()
            }
        }
    }

    /**
     * 根据传入的页面节点查找输入框并输入content内容
     */
    private fun findEditText(rootNode: AccessibilityNodeInfo, content: String): Boolean {
        val count = rootNode.childCount

        for (i in 0 until count) {
            val nodeInfo = rootNode.getChild(i)
            if (nodeInfo == null) continue

            if (nodeInfo.contentDescription != null) {
                val index = nodeInfo.contentDescription.toString().indexOf("com.tencent.mm")
                if (index != -1) {
                    //itemNodeinfo = nodeInfo
                }
            }
            if ("android.widget.EditText".equals(nodeInfo.className)) {
                val arguments = Bundle()
                arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                    AccessibilityNodeInfo.MOVEMENT_GRANULARITY_WORD)
                arguments.putBoolean(
                    AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                    true
                )
                nodeInfo.performAction(
                    AccessibilityNodeInfo.ACTION_PREVIOUS_AT_MOVEMENT_GRANULARITY,
                    arguments
                )
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_FOCUS)
                val clip = ClipData.newPlainText("label", content)
                val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(clip)
                nodeInfo.performAction(AccessibilityNodeInfo.ACTION_PASTE)
                return true
            }

            if (findEditText(nodeInfo, content)) {
                return true
            }
        }
        return false
    }

    private fun send() {
        Thread.sleep(50)
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val list = nodeInfo.findAccessibilityNodeInfosByText("发送")
            if (list != null && list.size > 0) {
                for (n in list) {
                    n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
            } else {
                val liste = nodeInfo.findAccessibilityNodeInfosByText("Send")
                if (liste != null && liste.size > 0) {
                    for (n in liste) {
                        n.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    }
                }
            }
        }
    }



}