package com.example.superwe

import java.sql.Date
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Action (val id : Int,val name : String,val value : ArrayList<Pair<String,String>>,val createTime : String){
    companion object {
        @JvmField
        public var id : Int = 0
    }

    constructor(name : String, value : ArrayList<Pair<String,String>>) :
       this(Action.id++,name,value, SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).format(Calendar.getInstance().time))

}