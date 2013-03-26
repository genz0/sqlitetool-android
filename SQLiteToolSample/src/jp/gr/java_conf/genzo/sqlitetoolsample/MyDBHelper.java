/**
 *
 */
package jp.gr.java_conf.genzo.sqlitetoolsample;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author matsuo
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {

	/**
	 * ヘルパークラス
	 *
	 * @param context コンテキスト
	 */
	public MyDBHelper(Context context) {
		super(context, "TestDB.db", null, 2);
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite
	 * .SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.beginTransaction();
		try {
			db.execSQL("CREATE TABLE TEST_TABLE(id INTEGER PRIMARY KEY, memo TEXT NOT NULL, latitude REAL, longitude REAL);");
			db.setTransactionSuccessful();
		} finally {
			db.endTransaction();
		}
	}

	/*
	 * (非 Javadoc)
	 *
	 * @see
	 * android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite
	 * .SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
