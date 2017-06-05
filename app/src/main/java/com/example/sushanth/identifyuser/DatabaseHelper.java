package com.example.sushanth.identifyuser;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sushanth on 4/26/2017.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "name.db";
    private static final int DATABASE_VERSION = 1;
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase nameDb) {
        nameDb.execSQL("CREATE TABLE IF NOT EXISTS usersInDb (_ID INTEGER PRIMARY KEY, NAME TEXT, COUNTRY TEXT, STATE  TEXT, CITY  TEXT, YEAR  INTEGER, LAT  REAL, LONG  REAL);");


    }
    public void onUpgrade(SQLiteDatabase nameDb, int oldVersion, int newVersion) {


    }
    public int getMaxID(String query ){
        //reuturn the maximum ID in the sqlDatabase
        SQLiteDatabase nameDb = this.getWritableDatabase();
        Cursor result = nameDb.rawQuery(query, null);
        int rowCount = result.getCount();
        if (rowCount > 0) {
            result.moveToFirst();
            nameDb.close();
            return result.getInt(0);

        }
        else{
            nameDb.close();
            return -1;
        }


    }
    public void clearTheDb(){
        SQLiteDatabase nameDb = this.getWritableDatabase();
        nameDb.delete("usersInDb", null,null);
        nameDb.close();

    }
}
