package com.example.superwe

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.hawk.Hawk

class ActionAdapter (private val actions:List<Action>,private val context : Context) : RecyclerView.Adapter<ActionAdapter.ViewHolder>() {

    inner class ViewHolder(view : View) : RecyclerView.ViewHolder(view) {
        val actionName : TextView = view.findViewById(R.id.action_name)
        val createTime : TextView = view.findViewById(R.id.create_time)
        val btnRepeat : Button = view.findViewById(R.id.repeat_action)
        val btnSchedule : Button = view.findViewById(R.id.schedule_action)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.action_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val action = actions[position]
        holder.actionName.text = action.name
        holder.createTime.text = action.createTime
        holder.btnRepeat.setOnClickListener {
            val actions : MutableMap<Int,Action>  = Hawk.get(Constant.ACTIONS)
            Hawk.put(Constant.READY,actions[action.id])
            println("actionId :" + action.id)
            Hawk.put(Constant.REPEAT_ACTION,true)
            Hawk.put(Constant.DISPOSABLE_ACTION,true)
            val intent = Intent("com.example.superwe.gap")
            startActivity(context,intent,null)
        }
        holder.btnSchedule.setOnClickListener {
            shortToast("该行为将在每天的" + action.createTime.substringAfterLast(" ") + "执行")
            TimeAsyncTask(action,context).execute()
        }
    }

    override fun getItemCount(): Int {
        return actions.size
    }
}