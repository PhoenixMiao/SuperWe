package com.example.superwe

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.orhanobut.hawk.Hawk

class ActionActivity : AppCompatActivity() {

    private var actions = ArrayList<Action>()
    private lateinit var adapter: ActionAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_action)
        val layoutManager = LinearLayoutManager(this)
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = layoutManager
        val acMap : MutableMap<Int,Action> = Hawk.get(Constant.ACTIONS)
        for(action in acMap.values) actions.add(action)
        adapter = ActionAdapter(actions,this)
        recyclerView.adapter = adapter
    }
}