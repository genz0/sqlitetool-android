package jp.gr.java_conf.genzo.sqlitetoolsample;

import java.io.IOException;

import jp.gr.java_conf.sqlite_android.SQLiteServer;
import android.app.Application;

public class MainApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		// アプリケーション生成のタイミングで、サービスを起動する。
		if (BuildConfig.DEBUG) {
			try {
				SQLiteServer server = new SQLiteServer();
				server.initialize(1280);
				server.setDBName(this, "TestDB.db");
				server.startServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
