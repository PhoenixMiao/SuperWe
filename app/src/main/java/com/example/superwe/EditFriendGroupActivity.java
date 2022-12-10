package com.example.superwe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class EditFriendGroupActivity extends AppCompatActivity {

    private final String TAG = "EditFriendGroupActivity";
    private Context context;
    private ListView tableListView;
    private List<FriendGroup> list;

    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend_group);
        context=this;

        //获取数据
        list = new ArrayList<>();
        Cursor cursor = SuperWeDatabaseUtils.query(this,"friend_group",null,null,null);
        while(cursor.moveToNext()){
            list.add(new FriendGroup(
                    cursor.getString(cursor.getColumnIndex("group_name")),
                    cursor.getString(cursor.getColumnIndex("friends_list"))
                    ));
        }

        tableListView = findViewById(R.id.friend_group_list);
        //渲染list
        FriendGroupTableAdapter adapter = new FriendGroupTableAdapter(this, list);
        tableListView.setAdapter(adapter);
        tableListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(EditFriendGroupActivity.this,EditFriendGroupDialogActivity.class);
            intent.putExtra("group_name",list.get(position).getGroupName());
            intent.putExtra("friend_list",list.get(position).getFriendList());
            startActivity(intent);
        });

        //清除所有分组
        Button clear = findViewById(R.id.clear_group_button);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list = new LinkedList<>();
                FriendGroupTableAdapter adapter = new FriendGroupTableAdapter(context, list);
                tableListView.setAdapter(adapter);
                SuperWeDatabaseUtils.delete(context,"friend_group",null,null);
            }
        });
    }

    @SuppressLint("Range")
    @Override
    protected void onResume() {
        super.onResume();
        list = new ArrayList<>();
        Cursor cursor = SuperWeDatabaseUtils.query(this, "friend_group", null, null, null);
        while (cursor.moveToNext()) {
            list.add(new FriendGroup(
                    cursor.getString(cursor.getColumnIndex("group_name")),
                    cursor.getString(cursor.getColumnIndex("friends_list"))
            ));
        }
        //渲染list
        FriendGroupTableAdapter adapter = new FriendGroupTableAdapter(this, list);
        tableListView.setAdapter(adapter);
        tableListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(EditFriendGroupActivity.this, EditFriendGroupDialogActivity.class);
            intent.putExtra("group_name", list.get(position).getGroupName());
            intent.putExtra("friend_list", list.get(position).getFriendList());
            startActivity(intent);
        });
    }
}