package com.eahm.drinkwaterapp.DB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.eahm.drinkwaterapp.DB.RecordsContract.RecordsEntry;
import com.eahm.drinkwaterapp.Models.RecordModel;

public class RecordsDbHelper extends SQLiteOpenHelper {

    //region VARIABLES
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Records.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RecordsEntry.TABLE_NAME +
                    "(" +
                    RecordsEntry._ID + " INTEGER PRIMARY KEY," +
                    RecordsEntry.COLUMN_NAME_TIMESTAMP + " TEXT," +
                    RecordsEntry.COLUMN_NAME_DRINK_AMOUNT + " TEXT," +
                    RecordsEntry.COLUMN_NAME_STATUS_CODE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RecordsEntry.TABLE_NAME;

    //endregion VARIABLES

    //region IMPLEMENT METHODS
    public RecordsDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
    //endregion IMPLEMENT METHODS

    //region QUERYS
    public Cursor getAllRecords() {
        return getReadableDatabase()
                .query(
                        RecordsEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    public Cursor getRecordById(String recordId) {
        Cursor c = getReadableDatabase().query(
                RecordsEntry.TABLE_NAME,
                null,
                RecordsEntry._ID + " LIKE ?",
                new String[]{recordId},
                null,
                null,
                null);
        return c;
    }

    //endregion QUERYS


    public long saveRecord(RecordModel newRecord) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        return sqLiteDatabase.insert(RecordsEntry.TABLE_NAME, null, newRecord.toContentValues());

    }

    public int deleteRecord(String recordId) {
        return getWritableDatabase().delete(
                RecordsEntry.TABLE_NAME,
                RecordsEntry._ID + " LIKE ?",
                new String[]{recordId});
    }

    public int updateRecord(RecordModel record, String recordId) {
        return getWritableDatabase().update(
                RecordsEntry.TABLE_NAME,
                record.toContentValues(),
                RecordsEntry._ID + " LIKE ?",
                new String[]{recordId}
        );
    }

}
