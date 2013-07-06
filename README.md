#sqlitetool-android

sqlitetool-androidは、Androidアプリケーションで利用しているデータベースを操作するためのライブラリと、ライブラリと通信して制御するツールのセットです。

## 経緯
一般的なAndroid端末では、アプリケーション固有のディレクトリ(/data/data)にアクセスすることはできません。
単純にファイルを取りだせるのは、ROOTを取得しているマシン又はエミュレータです。
データベースの出力先フォルダをsdcardに。という方式もありますが、これにはソースコードの修正が必要になります。。。いつもそういった状態に苛立ちを覚えていました。


ある日、Android端末でServerSocketを動作させるための検証を行っていました。確認していく中で「通信経路を作り、SQLを実行する方式でデータベースの中を覗けたら。。。」というアイデアを思い立ちこのツールを開発しました。
同じような思いをしている方々に使っていただければ幸いです。

## 前提条件
### Android
- Android2.3/4.2のマシンで動作確認しています。おそらく他の版でも動作すると思います
- Socketで通信およびログファイル出力により、いくつかのパーミッションが必要になります
- アプリケーションにライブラリを組み込み、サービスを起動する必要が有ります

### クライアント

- C#のクライアントしかありません。。。その為Windows環境のみの動作となります。他のプラットフォームの方々は申し訳ありません。マルチプラットフォームなクライアントをそのうち書きますので、しばらくお待ちください
- .NET Framework 2.0以降。もう2.0にこだわる必要もなくなってきたので、そのうち4.0以降に変えようと思います

### クライアントで使えるコマンド

- [接]接続(ctrl + S)
- [切]切断
- [転]ポートフォワード。adb forward相当(ctrl + F)
- [実]SQL実行。選択があればその範囲を、なければ全体をクエリーとして送信します(ctrl + E)
- [消]結果をクリアします
- [コ]クリップボードに結果をコピーします

使い方については、ヘルプを用意します。 ** しばらくお待ちください。 ** 

## Androidアプリケーションへの組み込み
### 組み込むの方法は以下のいづれかです。

- SQLiteToolLibをライブラリプロジェクトとして参照する
- AndroidアプリケーションのlibsフォルダにSQLiteToolLibのjarをコピーする

### サービスの起動
任意のタイミングでサービスを起動する必要が有ります。

サービスを起動する場合の例

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


### AndroidManifest.xmlの設定
パーミッションを指定します。

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

サービスの定義を追加します。

    <service android:name="jp.gr.java_conf.sqlite_android.SQLiteServerService" >
        <meta-data
            android:name="port_no"
            android:value="1280" />
        <meta-data
            android:name="db_file_name"
            android:value="TestDB.db" />
        <meta-data
            android:name="debug"
            android:value="true" />
    </service>


- port_no：ポート番号。必須
- db_file_name：データベースファイル名。必須
- debug：デバッグ出力の有無。任意

## テスト・および不具合
簡単なSQLの実行試験は行っていますが、今は自動テストはありません。**今後充実する予定です。**
不具合や問題点お気づきの点ありましたらご連絡ください。可能な限り対応します。

## ライセンス
こだわりがあるわけではありません、一番自由度の高い(と思っています）Apache License, Version 2.0で配布します。

## 謝辞
twitterやblogなどで技術情報を発信してくださっている方々および、github及びコミュニティの皆さんに感謝します。
