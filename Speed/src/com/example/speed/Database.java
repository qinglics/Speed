package com.example.speed;

import java.util.Date;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

@SuppressLint("NewApi")
public class Database extends SQLiteOpenHelper {

	private static final String TAG = "Database";

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "car_ges_info";
	private static final String TABLE_NAME = "ges_table";

	// col names
	private static final String DATETIME = "datetime";
	private static final String LONGTI = "longti";
	private static final String LATI = "lati";

	public Database(Context context) {
		super(context, Database.DATABASE_NAME, null, Database.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_APPS_TABLE = String.format(
				"CREATE TABLE %s(%s TEXT PRIMAY KEY, %s TEXT, %s TEXT)",
				TABLE_NAME, DATETIME, LONGTI, LATI);

		db.execSQL(CREATE_APPS_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "onUpgrade is called, do nothing");
	}

	public void addLocation(double x, double y) {
		String dateStamp = String.valueOf(new Date().getTime());
		SQLiteDatabase db = this.getWritableDatabase();
		Log.i(TAG, "values: " + dateStamp + " \t" + String.valueOf(x) + " \t"
				+ String.valueOf(y));
		ContentValues values = new ContentValues();
		values.put(DATETIME, dateStamp);
		values.put(LONGTI, String.valueOf(x));
		values.put(LATI, String.valueOf(y));
		db.insert(TABLE_NAME, null, values);

		this.cleanOldRecords(db);
		db.close();
	}

	public Position getLastLocation() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cur = db.query(TABLE_NAME,
				new String[] { DATETIME, LONGTI, LATI },
				DATETIME + ">" + this.getLastWeekStamp(), null, null, null,
				DATETIME + " DESC");
		Position ret = new Position();
		if (cur.moveToNext()) {
			ret.x = Double.parseDouble(cur.getString(1));
			ret.y = Double.parseDouble(cur.getString(2));
		}
		cur.close();
		db.close();

		return ret;
	}

	private void cleanOldRecords(SQLiteDatabase db) {
		String selectCount = String.format("SELECT COUNT(*) FROM %s",
				TABLE_NAME);
		Cursor cursor = db.rawQuery(selectCount, null);
		if (cursor.moveToNext()) {
			if (cursor.getLong(0) > 3000) {
				db.delete(TABLE_NAME, DATETIME + "<" + this.getLastWeekStamp(),
						null);
			}
		}
		cursor.close();
		db.close();
	}

	private String getLastWeekStamp() {
		long lastWeekStamp = new Date().getTime() - 7 * 24 * 3600;
		return String.valueOf(new Date(lastWeekStamp).getTime());
	}

}

class Position {
	double x = 0.0;
	double y = 0.0;
}
