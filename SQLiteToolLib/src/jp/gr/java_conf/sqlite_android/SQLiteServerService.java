/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android;

import java.io.IOException;
import java.util.logging.Level;

import jp.gr.java_conf.sqlite_android.util.LogUtils;
import jp.gr.java_conf.sqlite_android.util.StringUtils;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.os.IBinder;
import android.util.Log;

/**
 * SQLiteServerService.
 * 
 * SQLiteServerをAndroidのサービスとして実装したクラス。
 * 
 */
public class SQLiteServerService extends Service {

    /** TAG. */
    private static final String TAG = SQLiteServerService.class.getSimpleName();

    /** サーバのインスタンス. */
    private SQLiteServer mServer;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent arg0) {
        // バインド系のインタフェースは提供しない
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onStartCommand(android.content.Intent, int, int)
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mServer == null) {

            mServer = new SQLiteServer();
            try {
                ServiceInfo info = getPackageManager().getServiceInfo(
                        new ComponentName(this, SQLiteServerService.class),
                        PackageManager.GET_META_DATA);
                int port = info.metaData.getInt("port_no", 1280);
                String dbName = info.metaData.getString("db_file_name");
                boolean debug = info.metaData.getBoolean("debug", false);
                LogUtils.getLogger().setLevel(debug ? Level.ALL : Level.OFF);

                LogUtils.getLogger().info(
                        "SQLiteServerService dbName=" + dbName + " portNo="
                                + port);

                if (StringUtils.isEmpty(dbName)) {
                    Log.e(TAG, "SQLiteServerService no set \"db_file_name\"");
                    return super.onStartCommand(intent, flags, startId);
                }

                mServer.initialize(port);
                mServer.setDBName(this, dbName);
                mServer.startServer();
            } catch (IOException e) {
                Log.e(TAG, "server start error !!", e);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "server start error !!", e);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {

        mServer.stopServer();
        mServer = null;

        super.onDestroy();
    }

}
