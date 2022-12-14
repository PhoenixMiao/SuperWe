package com.example.superwe;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SaveGroupDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_group_dialog);

        Button save = findViewById(R.id.save_button);
        Button notSave = findViewById(R.id.not_save_button);
        EditText groupName=findViewById(R.id.group_name_edit_text);
        TextView repeatWarning = findViewById(R.id.repeat_warn_text);
        TextView emptyWarning = findViewById(R.id.empty_warn_text);

        repeatWarning.setVisibility(View.GONE);
        save.setEnabled(false);

        notSave.setOnClickListener(v -> createIntent(false,""));

        save.setOnClickListener(v -> createIntent(true,groupName.getText().toString()));

        groupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if(name.equals("")) {
                    save.setEnabled(false);
                    emptyWarning.setVisibility(View.VISIBLE);
                    repeatWarning.setVisibility(View.GONE);
                }
                else{
                    emptyWarning.setVisibility(View.GONE);
                    Cursor cursor = SuperWeDatabaseUtils.query(SaveGroupDialogActivity.this,"friend_group",null,"group_name=?",new String[]{name});
                    if(cursor.moveToNext()){
                        save.setEnabled(false);
                        repeatWarning.setVisibility(View.VISIBLE);
                    }
                    else{
                        save.setEnabled(true);
                        repeatWarning.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onBackPressed(){
        createIntent(false,"");
    }

    private void createIntent(boolean save,String name){
        Intent intent = new Intent();
        intent.putExtra("save",save);
        intent.putExtra("name",name);
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}