package com.example.final_project_s20.BBC;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpener extends SQLiteOpenHelper {

    protected final static String DATABASE_NAME = "BBCNewsDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "NewsLog";
    public final static String COL_TITLE = "Title";
    public final static String COL_DESCRIPTION = "Description";
    public final static String COL_LINK = "Link";
    public final static String COL_DATE = "Date";
    public final static String COL_ID = "_id";

    /**
     * @param ctx - the current context activity
     */
    public MyOpener(Context ctx)
    {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }


    /**
     * called if no database file exists
     * @param db - the SQLite database object
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " text,"
                + COL_DESCRIPTION + " text,"
                + COL_LINK + " text,"
                + COL_DATE  + " text);");  // add or remove columns
    }

    /**
     * called if the database version on your device is lower than VERSION_NUM
     * @param db - the database object
     * @param oldVersion - old version number
     * @param newVersion - new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }

    /**
     * called if the database version on your device is higher than VERSION_NUM
     * @param db - the database object
     * @param oldVersion - old version number
     * @param newVersion - new version number
     */
    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL( "DROP TABLE IF EXISTS " + TABLE_NAME);

        onCreate(db);
    }
}
