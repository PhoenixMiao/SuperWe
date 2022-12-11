package com.example.superwe;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class EditFriendGroupDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_friend_group_dialog);

        //显示分组详情
        String groupName = getIntent().getStringExtra("group_name");
        String friendList = getIntent().getStringExtra("friend_list");
        TextView name = findViewById(R.id.group_name);
        EditText members = findViewById(R.id.group_members);
        name.setText("分组："+groupName);
        members.setText(friendList);

        //删除或修改此分组
        Button delete = findViewById(R.id.delete_group_button);
        Button update = findViewById(R.id.update_group_button);
        update.setOnClickListener(v -> {
            ContentValues contentValues = new ContentValues();
            contentValues.put("friends_list",members.getText().toString());
            SuperWeDatabaseUtils.update(this,"friend_group",contentValues,"group_name=?",new String[]{groupName});
            finish();
        });
        delete.setOnClickListener(v -> {
            SuperWeDatabaseUtils.delete(this,"friend_group","group_name=?",new String[]{groupName});
            Toast.makeText(this,"已删除分组"+groupName,Toast.LENGTH_LONG);
            finish();
        });
    }
}