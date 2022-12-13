package com.example.superwe;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ListView;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

public class FriendInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_information);

        //初始化配置项数据
        List<FriendInformation> list = new ArrayList<>();
        Cursor cursor = SuperWeDatabaseUtils.query(this,"friend_info",null,null,null);
        while (cursor.moveToNext()){
            @SuppressLint("Range") FriendInformation friendInformation = new FriendInformation(cursor.getString(cursor.getColumnIndex("name")),
                    cursor.getString(cursor.getColumnIndex("nickname")),
                    cursor.getString(cursor.getColumnIndex("wx")),
                    cursor.getString(cursor.getColumnIndex("location")));
            list.add(friendInformation);
        }
        //渲染list
        ListView tableListView = findViewById(R.id.friend_info_list);
        FriendInformationTableAdapter adapter = new FriendInformationTableAdapter(this, list);
        tableListView.setAdapter(adapter);
        //渲染title
//        ViewGroup tableTitle = findViewById(R.id.configuration_table_title);
//        tableTitle.setBackgroundColor(Color.rgb(177, 173, 172));
    }
}