package com.example.owen.voicememos;
/*
Owen Brown - 4838488
Chang Ding - 5275821
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class MyDatabaseHelper extends SQLiteOpenHelper {
    //create the recordings table
    public static final String CREATE_RECORDING = "CREATE TABLE RECORDINGS ("
            +"id INTEGER PRIMARY KEY AUTOINCREMENT, "
            +"date TEXT, "
            +"filename TEXT,"
            +"note TEXT )";

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }

    public void onCreate(SQLiteDatabase db){
        //create tables
        db.execSQL(CREATE_RECORDING);
        Toast.makeText(mContext, "Database Create succeeded", Toast.LENGTH_SHORT).show();
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("drop table if exists RECORDINGS");
        onCreate(db);
    }


}
