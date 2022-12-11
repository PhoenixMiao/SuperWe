package com.example.superwe

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import androidx.core.content.ContextCompat
import com.orhanobut.hawk.Hawk
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class TimeAsyncTask(val action : Action,val context : Context) : AsyncTask<Unit, Int, Boolean>() {

    override fun doInBackground(vararg params: Unit?): Boolean {
        while(true) {
            while(SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Calendar.getInstance().time)!=action.createTime.substringAfterLast(" ")) {
                Thread.sleep(TimeUnit.SECONDS.toMillis(abs(SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Calendar.getInstance().time).substringBefore(":").toLong()
                         - action.createTime.substringAfterLast(" ").substringBefore(":").toLong()).times(60*60)
                        + TimeUnit.SECONDS.toMillis(abs(SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Calendar.getInstance().time).substringAfter(":").substringBefore(":").toLong()
                         - action.createTime.substringAfter(":").substringBefore(":").toLong()).times(60))))
            }
            val actions : MutableMap<Int,Action>  = Hawk.get(Constant.ACTIONS)
            Hawk.put(Constant.READY,actions[action.id])
            Hawk.put(Constant.REPEAT_ACTION,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            val intent = Intent("com.example.superwe.gap")
            ContextCompat.startActivity(context, intent, null)
        }
    }

}