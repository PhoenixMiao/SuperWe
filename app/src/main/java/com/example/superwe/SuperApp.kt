package com.example.superwe

import android.app.Application
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
    }
}