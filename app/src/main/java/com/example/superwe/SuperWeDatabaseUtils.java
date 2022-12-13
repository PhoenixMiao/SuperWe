package com.example.superwe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

//辅助实现数据库的增删改查
public class SuperWeDatabaseUtils {
    private static int version=3;

    private static String TAG="SuperWeDatabaseUtils";

    public static long add(Context context,String tableName,ContentValues values){
        SuperWeDatabaseHelper dbHelper = new SuperWeDatabaseHelper(context,"SuperWe.db",version);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(tableName,null,values);

    }

    public static long delete(Context context,String tableName,String whereClause, String[] whereArgs){
        SuperWeDatabaseHelper dbHelper = new SuperWeDatabaseHelper(context,"SuperWe.db",version);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(tableName,whereClause,whereArgs);
    }

    public static long update(Context context,String tableName,ContentValues newValues,String whereClause, String[] whereArgs){
        SuperWeDatabaseHelper dbHelper = new SuperWeDatabaseHelper(context,"SuperWe.db",version);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.update(tableName,newValues,whereClause,whereArgs);
    }

    //返回匹配查询条件的第一个数据
    public static Cursor query(Context context,String tableName,String[] columns,String whereClause,String[] whereArgs){
        SuperWeDatabaseHelper dbHelper = new SuperWeDatabaseHelper(context,"SuperWe.db",version);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.query(tableName,columns,whereClause,whereArgs,null,null,null);
    }

}
