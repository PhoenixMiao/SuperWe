package com.example.superwe

import android.app.Application
import com.google.android.material.shape.ShapePath
import com.orhanobut.hawk.Hawk
import kotlin.properties.Delegates

class SuperApp : Application() {
    companion object {
        var instance by Delegates.notNull<SuperApp>()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Hawk.init(this).build()
        Hawk.put(Constant.GROUP_NAME,"")
        Hawk.put(Constant.FRIEND_LIST,"")
        Hawk.put(Constant.GROUP_CHARGE,false)
        Hawk.put(Constant.RECORD_ACTION,false)
        Hawk.put(Constant.REPEAT_ACTION,false)
        Hawk.put(Constant.WATCHER,arrayListOf<Pair<String?,String?>>())
        Hawk.put(Constant.ADDING_FRIENDS_INTO_GROUP,false)
        Hawk.put(Constant.AUTO_RECEIVE_LUCKY_MONEY,false)
        Hawk.put(Constant.AUTO_ZAN,false)
        Hawk.put(Constant.AUTO_REPLY,false)
        Hawk.put(Constant.BATCH_REPLY,false)
        Hawk.put(Constant.BATCH_REPLY_CONTENT,"")
        Hawk.put(Constant.BATCH_READ,false)
        Hawk.put(Constant.AUTO_REPLY_CONTENT,"")
    }
}