package jp.gr.java_conf.genzo.sqlitetoolsample;

import jp.gr.java_conf.sqlite_android.SQLiteServerService;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// アプリケーション生成のタイミングで、サービスを起動する。
		if (BuildConfig.DEBUG) {
			Log.d("SQLiteToolSample", "started SQLiteServer!!");
			startService(new Intent(this, SQLiteServerService.class));
		}

	}
}
