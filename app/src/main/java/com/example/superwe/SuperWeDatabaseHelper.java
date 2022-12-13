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
    private final String createFriendTable = "create table friend_info ("+
            "id integer primary key autoincrement,"+
            "name text,"+
            "nickname text,"+
            "wx text,"+
            "location text)"
            ;

    public SuperWeDatabaseHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createGroupTable);
        db.execSQL(createFriendTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>=2){
            db.execSQL(createFriendTable);
        }
    }


}
