package com.connexun.tracking.app.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;

/*......................

DB tables for activity app..

 */
public class SQLiteHelper extends SQLiteOpenHelper {
	
    static String DATABASE_NAME="TrackingDb";
    public static final String KEY_ID="id";
    public static final String TABLE_NAME="demoTable_tracking"; // to store user state
    public static final String TABLE_NAME2="tblMovingStates";  // table for static state in which location throws as per the matrix
    public static final String KEY_Name="name";
    public static final String KEY_DETECTIONTIME="detection_time"; // user state detection time
    public static final String KEY_Location="lat";
    public static final String KEY_Location_long="long";
    public static final String KEY_Battery="battery";
    public static final String KEY_Accuracy="accuracy";
    public static final String KEY_Provider="provider";
    public static final String KEY_Address="address";
    public static final String KEY_RAM="ram";
    ///////////////////////////////////////
  /*...............
    parameter for static table
   */
    public static final String KEY_ID_2="id";
    public static final String KEY_CSTATE="cstate";
    public static final String KEY_PSATE="pstate";

    public SQLiteHelper(Context context) {

        super(context, DATABASE_NAME, null, 3);
        
    }

    @Override
    public void onCreate(SQLiteDatabase database) {

        String CREATE_TABLE="CREATE TABLE IF NOT EXISTS "+TABLE_NAME+" ("+KEY_ID+" INTEGER PRIMARY KEY," +
                " "+KEY_Name+" VARCHAR, "+KEY_DETECTIONTIME+" VARCHAR,"+ KEY_Location+" VARCHAR,"+ KEY_Location_long+" VARCHAR,"+
              " "+KEY_Battery+" VARCHAR, " +KEY_Accuracy+" VARCHAR," +KEY_Provider+" VARCHAR,"+KEY_Address+" VARCHAR," +KEY_RAM+" VARCHAR)";

        String CREATE_TABLE2="CREATE TABLE IF NOT EXISTS "+TABLE_NAME2+" ("+KEY_ID_2+" INTEGER PRIMARY KEY," +
                " "+KEY_CSTATE+" VARCHAR, "+KEY_PSATE+" VARCHAR)";
        database.execSQL(CREATE_TABLE);
        database.execSQL(CREATE_TABLE2);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_NAME2);
        onCreate(db);

    }

    public ArrayList<HashMap<String,String>> getAllState()
    {

        ArrayList<HashMap<String,String>> array_list = new ArrayList<HashMap<String,String>>();
        SQLiteDatabase db = this.getReadableDatabase(); // Reading DATABASE
        Cursor res = db.rawQuery("SELECT * FROM demoTable_tracking ORDER BY id DESC ", null);
        res.moveToFirst();
        try {
            while(res.isAfterLast() == false){
                /*
                 * Inserting record in HashMap
                 * */
                HashMap<String,String> map=new HashMap<String,String>();
                map.put(KEY_ID,res.getString(res.getColumnIndex(KEY_ID)));
                map.put(KEY_Name,res.getString(res.getColumnIndex(KEY_Name)));
                map.put(KEY_DETECTIONTIME,res.getString(res.getColumnIndex(KEY_DETECTIONTIME)));
                map.put(KEY_Location,res.getString(res.getColumnIndex(KEY_Location)));
                map.put(KEY_Location_long,res.getString(res.getColumnIndex(KEY_Location_long)));
                map.put(KEY_Battery,res.getString(res.getColumnIndex(KEY_Battery)));
                map.put(KEY_Accuracy,res.getString(res.getColumnIndex(KEY_Accuracy)));
                map.put(KEY_Provider,res.getString(res.getColumnIndex(KEY_Provider)));
                map.put(KEY_Address,res.getString(res.getColumnIndex(KEY_Address)));
                map.put(KEY_RAM,res.getString(res.getColumnIndex(KEY_RAM)));


                array_list.add(map); // Inserting HashMap in ArrayList
                res.moveToNext();
            }
            return array_list; // return ArrayList
        }finally {
            res.close();
        }

    }


    public ArrayList<HashMap<String,String>> getRecentEntry()
    {

        ArrayList<HashMap<String,String>> array_list = new ArrayList<HashMap<String,String>>();
        SQLiteDatabase db = this.getReadableDatabase(); // Reading DATABASE
        Cursor res = db.rawQuery("SELECT * FROM demoTable_tracking ORDER BY id DESC LIMIT 1", null);
        res.moveToFirst();
        try {
            while(res.isAfterLast() == false){
                /*
                 * Inserting record in HashMap
                 * */
                HashMap<String,String> map=new HashMap<String,String>();
                map.put(KEY_ID,res.getString(res.getColumnIndex(KEY_ID)));
                map.put(KEY_Name,res.getString(res.getColumnIndex(KEY_Name)));
                map.put(KEY_DETECTIONTIME,res.getString(res.getColumnIndex(KEY_DETECTIONTIME)));
                map.put(KEY_Location,res.getString(res.getColumnIndex(KEY_Location)));
                map.put(KEY_Location_long,res.getString(res.getColumnIndex(KEY_Location_long)));
                map.put(KEY_Battery,res.getString(res.getColumnIndex(KEY_Battery)));
                map.put(KEY_Accuracy,res.getString(res.getColumnIndex(KEY_Accuracy)));
                map.put(KEY_Provider,res.getString(res.getColumnIndex(KEY_Provider)));
                map.put(KEY_Address,res.getString(res.getColumnIndex(KEY_Address)));
                map.put(KEY_RAM,res.getString(res.getColumnIndex(KEY_RAM)));


                array_list.add(map); // Inserting HashMap in ArrayList
                res.moveToNext();
            }
            return array_list; // return ArrayList
        }finally {
            res.close();
        }

    }

}