package com.example.superwe

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import com.google.android.material.shape.ShapePath
import com.orhanobut.hawk.Hawk
import kotlin.properties.Delegates

class SuperApp : Application() {

    val TAG = "SuperApp"

    companion object {
        var instance by Delegates.notNull<SuperApp>()
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG,"SuperApp is created")
        instance = this
        Hawk.init(this).build()
        Hawk.put(Constant.DISPOSABLE_ACTION,0)
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
        context = applicationContext
    }
}