package com.example.superwe;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SuperWeDatabaseHelper extends SQLiteOpenHelper {

    private final String createGroupTable = "create table friend_group ("+
            "id integer primary key autoincrement,"+
            "group_name text,"+
            "friends_list text)"
            ;

    public SuperWeDatabaseHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createGroupTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


}
