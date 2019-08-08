package com.example.anna.activityapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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






}