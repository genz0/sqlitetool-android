package jp.gr.java_conf.genzo.sqlitetoolsample;

import java.util.Date;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	// 参照ボタン
	public void onButtonClick1(View view) {

		MyDBHelper helper = new MyDBHelper(this);
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = null;
		String sql = "SELECT id, memo FROM TEST_TABLE";

		try {
			cursor = db.rawQuery(sql, null);
			boolean eof = cursor.moveToFirst();
			StringBuilder sb = new StringBuilder();
			while (eof) {
				sb.append(cursor.getInt(0)).append(":")
						.append(cursor.getString(1)).append("\n");
				eof = cursor.moveToNext();
			}

			TextView tv = (TextView) findViewById(R.id.textView1);
			tv.setText(new String(sb));

		} finally {
			if (cursor != null) {
				cursor.close();
			}

			if (db != null) {
				db.close();
			}

			if (helper != null) {
				helper.close();
			}
		}
	}

	// 登録ボタン
	public void onButtonClick2(View view) {
		MyDBHelper helper = new MyDBHelper(this);
		SQLiteDatabase db = helper.getWritableDatabase();
		String ins_sql = "INSERT INTO TEST_TABLE(memo) VALUES (?);";

		db.beginTransaction();
		try {
			db.execSQL(ins_sql, new Object[] { new Date().toString() });
			db.setTransactionSuccessful();

		} finally {
			db.endTransaction();

			if (db != null) {
				db.close();
			}

			if (helper != null) {
				helper.close();
			}
		}
	}

}
