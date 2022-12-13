package com.example.superwe

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.Notification
import android.app.PendingIntent
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.graphics.Rect
import android.os.*
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.orhanobut.hawk.Hawk
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream


class SuperService : AccessibilityService() {

    private val TAG = "SuperService"
    private val handler = Handler()
    private var watcher = arrayListOf<Pair<String?,String?>>()
    private val disposableActionInterruptedHint = "正在进行的操作已被中断"

    companion object{
        @JvmField
        var isAuto = false
        @Volatile @JvmField
        var lastEnterpriseUI = listOf<AccessibilityNodeInfo>()
        @Volatile @JvmField
        var lastLauncherUI = listOf<AccessibilityNodeInfo>()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val config = AccessibilityServiceInfo();
        //配置监听的事件类型为界面变化|点击事件
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED.or(AccessibilityEvent.TYPE_VIEW_CLICKED).or(AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED).or(AccessibilityEvent.TYPE_VIEW_SCROLLED)
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
        var nodeInfo = event.source //当前界面的可访问界点
//        Log.d(TAG, eventTypeToString(eventType))
        Log.d(TAG,className)
        println(event)
        when (eventType) {
            //点击事件
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                //记录动作功能 one off
                if(Hawk.get(Constant.RECORD_ACTION,false)) {
                    if(event.recordCount==1){
                        var flag = false
                        for(i in lastLauncherUI.indices) {
                            if(nodeInfo == lastLauncherUI[i]) {
                                watcher.add(Pair<String?,String?>("com.tencent.mm:id/bth",i.toString()))
                                flag = true
                                break
                            }
                        }
                        if(!flag) {
                            for(i in lastEnterpriseUI.indices) {
                                println(i)
                                if(nodeInfo == lastEnterpriseUI[i]) {
                                    watcher.add(Pair<String?,String?>("com.tencent.mm:id/btg",i.toString()))
                                    break
                                }
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
                                    flag = false
                                }else {
                                    list[list.size - i] = Pair<String?,String?>("com.tencent.mm:id/bth", ((list[list.size - i].second?.toInt()?:1) - 1).toString())
                                }
                            }
                        }
                        watcher = list
                        watcher.add(Pair<String?,String?>("send",messages[messages.size-1].text.toString()))
                    }
                    else if(nodeInfo!=null && nodeInfo.className=="android.widget.EditText") watcher.add(Pair<String?,String?>("input",""))
                    else if(nodeInfo!=null && nodeInfo.viewIdResourceName!=null) watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,""))
                    else if(nodeInfo!=null && nodeInfo.text!=null) watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,nodeInfo.text.toString()))
                    else {
                        var ch = ""
                        while(ch == "" && nodeInfo!=null && nodeInfo.childCount>0 && nodeInfo.getChild(0).text!=null) {
                            ch = nodeInfo.getChild(0).text.toString()
                            nodeInfo = nodeInfo.getChild(0)
                        }
                        watcher.add(Pair<String?,String?>(nodeInfo.viewIdResourceName,ch))
                    }
                    Hawk.put(Constant.WATCHER,watcher)
                }
            }
            //窗口改变事件
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if(className == "com.tencent.mm.ui.LauncherUI") {
                    val nodes = rootInActiveWindow
                    if(nodes!=null) lastLauncherUI = nodes.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
                    //获取通讯录信息 one off
                    if(Hawk.get(Constant.GET_CONTACTS,false)){
                        Hawk.put(Constant.GET_CONTACTS,false)
                        contactToFile()
                        Hawk.put(Constant.DISPOSABLE_ACTION,false)
                    }
                }
                if(className == "com.tencent.mm.ui.conversation.EnterpriseConversationUI") {
                    Thread.sleep(500)
                    val nodes = rootInActiveWindow
                    if(nodes!=null) lastEnterpriseUI = nodes.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/btg")
                }
                //批量回复 one off
                if(Hawk.get(Constant.BATCH_REPLY,false)) {
                    Hawk.put(Constant.BATCH_REPLY,false)
                    batchReply()
                    Hawk.put(Constant.DISPOSABLE_ACTION,false)
                }
                //批量清除未读消息 one off
                if(Hawk.get(Constant.BATCH_READ,false)) {
                    Hawk.put(Constant.BATCH_READ, false)
                    GlobalScope.launch {
                        batchRead()
                    }
                    Hawk.put(Constant.DISPOSABLE_ACTION,false)
                }
                //重复行为 one off
                if(Hawk.get(Constant.REPEAT_ACTION,false)) {
                    Hawk.put(Constant.REPEAT_ACTION,false)
                    Hawk.put(Constant.RECORD_ACTION,false)
                    GlobalScope.launch {
                        repeatAction()
                    }
                    Hawk.put(Constant.DISPOSABLE_ACTION,false)
                }
                //群收款 one off
                if(Hawk.get(Constant.GROUP_CHARGE,false)) {
                    Hawk.put(Constant.GROUP_CHARGE,false)
                    val groups = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ko4")
                    if(groups==null || groups.size==0 || groups[0]==null) {
                        Hawk.put(Constant.DISPOSABLE_ACTION,false)
                        shortToast("请进入群聊界面后点击群收款")
                    } else {
                        GlobalScope.launch {
                            groupCharge()
                            Hawk.put(Constant.DISPOSABLE_ACTION,false)
                        }
                    }
                }
                //邀请好友进入群聊 one off
                val adding = Hawk.get(Constant.ADDING_FRIENDS_INTO_GROUP, false)
                if (adding) {
                    Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
                    val groups = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ko4")
                    if(groups==null || groups.size==0 || groups[0]==null) {
                        Hawk.put(Constant.DISPOSABLE_ACTION,false)
                        shortToast("请进入群聊界面后再进行批量好友邀请")
                    } else {
                        GlobalScope.launch {
                            openGroupSetting()
                            Hawk.put(Constant.DISPOSABLE_ACTION,false)
                        }
                    }
//                    if(className=="com.tencent.mm.ui.LauncherUI") openGroup()
//                    else if(className == "com.tencent.mm.ui.contact.ChatroomContactUI") searchGroup()
//                    else if(className == "com.tencent.mm.ui.chatting.ChattingUI") openGroupSetting()
//                    else if(className == "com.tencent.mm.chatroom.ui.ChatroomInfoUI") openSelectContact()
//                    else if(className == "com.tencent.mm.ui.mvvm.MvvmSelectContactUI") addMembers()
//                    else performBackClick()
                }
                else if(className == "com.tencent.mm.ui.LauncherUI"){
                    //自动回复
                    if(Hawk.get(Constant.AUTO_REPLY,false)) {
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            Hawk.put(Constant.DISPOSABLE_ACTION,false)
                            fill()
                            Hawk.put(Constant.DISPOSABLE_ACTION,true)
                        }
                    }
                    //自动点开红包
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            val rootNode = rootInActiveWindow
                            val hongBaoList = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b47")
                            if(hongBaoList.size>0){
                                Thread.sleep(300)
                                hongBaoList.get(hongBaoList.size-1).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
                else if(className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI"){
                    //自动按红包的开按钮
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            Thread.sleep(800)
                            val source = event.source
                            recycle(source)
                            val openButton = source.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gip")
                            if(openButton.size>0){
                                openButton[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                    }
                }
                else if(className == "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI"){
                    //从红包界面退回
                    if(Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)){
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            performBackClick()
                        }
                    }
                }
                //朋友圈自动点赞
                if (Hawk.get(Constant.AUTO_ZAN, false)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            autoZan()
                        }
                    }
                }
                if (Hawk.get(Constant.ANOTHER_AUTO_ZAN, false)) {
                    if (className == "com.tencent.mm.plugin.sns.ui.SnsTimeLineUI") {
                        if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                            anotherAutoZan()
                        }
                    }
                }
            }
            //滚动事件
            AccessibilityEvent.TYPE_VIEW_SCROLLED -> {
                if (Hawk.get(Constant.ANOTHER_AUTO_ZAN, false)) {
                    if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                        anotherAutoZan()
                    }
                }
            }
            //通知栏状态改变
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                if (event.parcelableData != null && event.parcelableData is Notification) {
                    if(!Hawk.get(Constant.DISPOSABLE_ACTION,false)){
                        val notification = event.parcelableData as Notification
                        val content = notification.tickerText.toString()
                        if ((Hawk.get(Constant.AUTO_RECEIVE_LUCKY_MONEY,false) && content.contains("[微信红包]")) || Hawk.get(Constant.AUTO_REPLY,false)) {
                            val pendingIntent = notification.contentIntent
                            try {
                                pendingIntent.send()
                            } catch (e: PendingIntent.CanceledException) {
                                e.printStackTrace()
                                Hawk.put(Constant.DISPOSABLE_ACTION,false)
                            }finally {
                            }
                        }
                    }

                }
            }
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
            val group = Hawk.get(Constant.GROUP_NAME,"").trim()
            for (node in nodes) {
                val infoNode = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bqu")
                if(infoNode.size>0)
                {
                    val info = infoNode[0]
                    val groupName = info.text.toString()
                    if (groupName.equals(group)) {
                        found = true
                        node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(800)
                        break
                    }
                }
            }
            if(found==false){
                Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
                Hawk.put(Constant.DISPOSABLE_ACTION, false)

            }
        }
    }

    //3.打开群聊设置
    private fun openGroupSetting() {
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/eo")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            Thread.sleep(500)
            openSelectContact()
        }
        else {
            performBackClick()
            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
            Hawk.put(Constant.DISPOSABLE_ACTION, false)

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
                        nodes.get(memberCount).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(700)
                        addMembers()
                    }
                }
            }
            addMembers()
        } else {
            performBackClick()
            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
            Hawk.put(Constant.DISPOSABLE_ACTION, false)
        }

    }

    //5.添加成员
    private fun addMembers() {
        val friend_list_str = Hawk.get(Constant.FRIEND_LIST,"")
        if( friend_list_str!= "") {
            val members = friend_list_str.split("\n")
            if (members.size > 0) {
                for (i in 0 until members.size) {
                        val nodeInfo = rootInActiveWindow
                        if (nodeInfo != null) {
                            val editNodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/h7o")
                            if (editNodes != null && editNodes.size > 0) {
                                val editNode = editNodes[0]
                                val arguments = Bundle()
                                arguments.putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, members[i].trim())
                                editNode.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, arguments)
                                Thread.sleep(300)
                            }
                        }
                        val list = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j55")
                        if(list.size>0){
                            val cbNodes = list[0].findAccessibilityNodeInfosByText(members[i].trim())
                            val checkbox = list[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/j9g")
                            if (cbNodes != null) {
                                var cbNode: AccessibilityNodeInfo?
                                if (cbNodes.size > 0) {
                                    cbNode = cbNodes[0]
                                    if(cbNode.text.toString().equals(members[i].trim())){
                                        checkbox[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                        while(cbNode!=null){
                                            cbNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                            cbNode=cbNode.parent
                                        }
                                        Thread.sleep(500)
                                    }
                                }
                            }
                        }
                        //最后一次的时候清空记录，并且点击顶部确定按钮
                        if (i == members.size - 1) {
                            Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP, false)
                            Hawk.put(Constant.DISPOSABLE_ACTION, false)
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
                }
            }
        }
    }

    //对话框自动点击
    private fun dialogClick() {
        val inviteNode = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/b00")[0]
        inviteNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
    }

    private fun anotherAutoZan(){
        val nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            val friend_squares = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jtr")
            if (friend_squares != null && friend_squares.size > 0) {
                for(i in 1..friend_squares.size){
                    val friend_square = friend_squares[i-1]
                    val zanNodes = friend_square.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nh")
                    for(zan in zanNodes) {
                        zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(300)
                        val zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jp")
                        if (zsNodes != null && zsNodes.size > 0) {
                            val dianZan = zsNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/n4")
                            if (dianZan.size > 0 && dianZan[0].text.equals("赞")) {
                                dianZan[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                Thread.sleep(300)
                            }
                            else{
                                zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            }
                        }
                        Thread.sleep(500)
                    }
                }
            }
        }
    }

    //自动点赞
    private fun autoZan() {
        while (true) {
            val rootNode = rootInActiveWindow
            if (rootNode != null) {
                val friend_squares = rootNode.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jtr")
                if (friend_squares != null && friend_squares.size > 0) {
                    for(i in 1..friend_squares.size){
                        val friend_square = friend_squares[i-1]
                        val zanNodes = friend_square.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/nh")
                        if(zanNodes.size>0) {
                            val zan = zanNodes[0]
                            zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(500)
                            val zsNodes = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jp")
                            if (zsNodes != null && zsNodes.size > 0) {
                                val dianZan = zsNodes[0].findAccessibilityNodeInfosByViewId("com.tencent.mm:id/n4")
                                if (dianZan.size > 0 && dianZan[0].text.equals("赞")) {
                                    dianZan[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                                else{
                                    zan.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                                }
                            }
                            Thread.sleep(500)
                        }
                    }
                    val screen = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/jv8")
                    if(screen.size<=0){
                        shortToast(disposableActionInterruptedHint)
                        return
                    }
                    screen[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                    Thread.sleep(700)
                }
            } else {
                break
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
        val util : Action = Hawk.get(Constant.READY)
        val watcher = util.value
        for(pair in watcher) {
            Thread.sleep(500)
            nodeInfo = rootInActiveWindow
            if(nodeInfo != null) {
                var node = nodeInfo.findAccessibilityNodeInfosByText(pair.second)
                println(pair)
                if (pair.first == "com.tencent.mm:id/bth") {
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")[pair.second?.toInt()!!].performAction(
                        AccessibilityNodeInfo.ACTION_CLICK
                    )
                } else if(pair.first == "com.tencent.mm:id/btg") {
                    nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/btg")[pair.second?.toInt()!!].performAction(
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
            val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
            val size = target.size
            for (i in 0 until size) {
                target[i].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(1000)
                nodeInfo = rootInActiveWindow
                nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(1000)
            }
            Thread.sleep(1000)
        }
    }

    /*
     * 计算聊天框间距
     */
    private fun getGap():Int {
        val nodeInfo = rootInActiveWindow
        val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
        if (commu.size <= 1) return 1
        val nodeRect1 = Rect()
        val nodeRect2 = Rect()
        commu[0].getBoundsInScreen(nodeRect1)
        commu[1].getBoundsInScreen(nodeRect2)
        return nodeRect2.centerY() - nodeRect1.centerY()
    }
    /*
     * 得到未读消息索引列表
     */
    private fun getNotRead():ArrayList<Int>{
        val nodeInfo = rootInActiveWindow
        val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kmv")
        val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
        //获取聊天框间距
        val gap = getGap()
        //计算红点索引队列
        val nodeRect1 = Rect()
        val nodeRect2 = Rect()
        target[0].getBoundsInScreen(nodeRect1)
        commu[0].getBoundsInScreen(nodeRect2)
        var cur = (nodeRect1.centerY() - nodeRect2.centerY()) / gap
        var redCommu = arrayListOf(cur)
        for (i in 1 until target.size) {
            target[i].getBoundsInScreen(nodeRect1)
            target[i-1].getBoundsInScreen(nodeRect2)
            val y = (nodeRect1.centerY() - nodeRect2.centerY()) / gap + cur
            redCommu.add(y)
            cur = y
        }
        return redCommu
    }
    /**
     * 执行遍历聊天框点击，消去红点
     */
    private fun batchRead() {
        Thread.sleep(1000)
        var nodeInfo = rootInActiveWindow
        var scroll = true
        var lastNode = nodeInfo
        //打开置顶聊天
        var top = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ebm")
        if(top.size>0){
            if(!top[0].text.equals("折叠置顶聊天")){
                top[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(800)
            }
        }
        nodeInfo = rootInActiveWindow
        do{
            Log.d(TAG,"can scroll?"+scroll.toString())
            if (nodeInfo != null) {
                //找到“折叠置顶聊天”节点
                top = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ebm")
                if(top.size<=0 || !top[0].text.equals("折叠置顶聊天")){top=null}
                //红点聊天框
                val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kmv")
                //当前所有聊天框
                val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
                //翻页到底部则停止点击
                if(commu[commu.size-1]==lastNode){

                    return
                }
                else{
                    lastNode = commu[commu.size-1]
                }
                if (target.size > 0){
                    //获取未读消息索引
                    val redCommu = getNotRead()
                    //点击红点聊天框
                    for (i in 0 until redCommu.size) {

                        if(top==null || commu[redCommu[i]] != top[0].parent)
                        {
                            commu[redCommu[i]].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(800)
                            nodeInfo = rootInActiveWindow
                            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(1500)
                        }
                    }
                }

                //翻页
                nodeInfo = rootInActiveWindow
                val scrollNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gkp")[0]
                scroll = scrollNode.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                Thread.sleep(800)
                nodeInfo = rootInActiveWindow
            }
        }while (scroll)
    }
    /*
     * 批量回复未读消息
     */
    private fun batchReply() {
        var nodeInfo = rootInActiveWindow
        var scroll = true
        var lastNode = nodeInfo
        //打开置顶聊天
        val top = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ebm")
        if(top.size>0){
            if(!top[0].text.equals("折叠置顶聊天")){
                top[0].parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Thread.sleep(800)
            }
        }
        nodeInfo = rootInActiveWindow
        do{
            Log.d(TAG,scroll.toString())
            if (nodeInfo != null) {
                //当前所有聊天框
                val commu = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bth")
                //当前未读消息红点
                val target = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kmv")
                if(commu.size<=0) {

                    return
                }
                //翻页到底部则停止点击
                if(commu[commu.size-1]==lastNode){

                    return
                }
                else{
                    lastNode = commu[commu.size-1]
                }
                if (target.size > 0) {
                    //获取未读消息索引
                    val redCommu = getNotRead()
                    for (i in 0 until redCommu.size) {
                        commu[redCommu[i]].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
                        val nodes = rootInActiveWindow
                        findEditText(nodes,Hawk.get(Constant.BATCH_REPLY_CONTENT,"test"))
                        Thread.sleep(500)
                        send()
                        Thread.sleep(1000)
                        nodeInfo = rootInActiveWindow
                        if(nodeInfo.className!="android.widget.FrameLayout")
                            shortToast("当前不处于聊天界面")
                        nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
                    }
                }
                //翻页
                nodeInfo = rootInActiveWindow
                val scrollNode = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gkp")
                if(scrollNode.size>0)
                    scroll = scrollNode[0].performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
                Thread.sleep(800)
                nodeInfo = rootInActiveWindow
            }
        }while (scroll)

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
                if(Looper.myLooper() == null) Looper.prepare()
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
        Thread.sleep(500)
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
                else{
                    shortToast("当前页面不能发送信息")
                }
            }

        }
    }

    /*
     * 统计通讯录
     */
    private fun contactToFile() {

        var nodeInfo = rootInActiveWindow
        if (nodeInfo != null) {
            //返回首页
            nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
            nodeInfo = rootInActiveWindow
            // 查找底边导航按钮
            var nodes = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/kd_")
            if (nodes != null && nodes.size > 1) {
                // 点击“通讯录”按钮
                nodes[1].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                // 再次点击到达“通讯录”首先应当滑动置顶
                nodes[1].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                var end = false
                var contactPerson = mutableSetOf<String>()
                //新建文件
                createNewFile()

                //遍历联系人
                while (!end) {
                    nodeInfo = rootInActiveWindow
                    var page = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/js")
                    var contact = page[0].childCount
                    for (i in 0 until contact){
                        page[0].getChild(i).performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
                        var node = rootInActiveWindow
                        if (node == nodeInfo) continue
                        val nickname = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bq0")
                        val name = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bq1")
                        val number = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bq9")
                        val position = node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bpz")
                        // 非联系人，直接退出
                        if (name.size == 0) {
                            node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(1000)
                            continue
                        }
                        // 重复联系人，直接退出
                        if (contactPerson.contains(name[0].text.toString())) {
                            node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Thread.sleep(1000)
                            continue
                        } else {
                            contactPerson.add(name[0].text.toString())
                        }
                        val values = ContentValues()
                        values.put("name",name[0].text.toString())
                        writeTextToFile(name[0].text.toString())
                        if (nickname.size == 0) {
                            val str = "昵称:  " + name[0].text.toString()
                            writeTextToFile(str)
                            values.put("nickname",name[0].text.toString())
                        } else {
                            writeTextToFile(nickname[0].text.toString())
                            values.put("nickname",nickname[0].text.toString())
                        }
                        writeTextToFile(number[0].text.toString())
                        values.put("wx",number[0].text.toString())
                        if (position.size == 0) {
                            writeTextToFile("地区:  暂无")
                            values.put("location","暂无")
                        } else {
                            writeTextToFile(position[0].text.toString())
                            values.put("location",position[0].text.toString())
                        }
                        writeTextToFile(" ")
                        SuperWeDatabaseUtils.add(this,"friend_info",values)
                        node.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/g0")[0].performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Thread.sleep(1000)
                    }
                    //下滑
                    page[0].performAction((AccessibilityNodeInfo.ACTION_SCROLL_FORWARD))
                    Thread.sleep(2000)
                    nodeInfo = rootInActiveWindow
                    var newPage = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/js")
                    // 到底结束下滑
                    if (page[0].getChild(0) == newPage[0].getChild(0)) end = true
                }
                shortToast("通讯录导出完成")
            }
        }
    }
    /*
     * 写入文件
     */
    private fun writeTextToFile(content: String) {
        val path = "/data/data/com.example.superwe/contacts.txt"
        val fos = FileOutputStream(path, true)
        val info = content + "\n"
        fos.write(info.toByteArray())
        fos.flush()
        fos.close()
    }


    /*
     * 新建文件
     */
    private fun createNewFile() {
        var file = File("/data/data/com.example.superwe/contacts.txt")

        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()

    }


}