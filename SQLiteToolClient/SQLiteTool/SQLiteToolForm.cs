using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Text;
using System.Diagnostics;
using System.Windows.Forms;
using System.Net.Sockets;
using System.Threading;
using System.IO;
using System.Xml.Serialization;

namespace SQLiteTool
{
    /// <summary>
    /// 通信するクラスのForm
    /// </summary>
    public partial class SQLiteToolForm : Form
    {
        /// <summary>
        /// TcpClient
        /// </summary>
        private TcpClient tcpClient = null;

        /// <summary>
        /// 接続中のネットワークストリーム
        /// </summary>
        private NetworkStream networkStream = null;

        /// <summary>
        /// 設定
        /// </summary>
        private Settings appSettings = new Settings();

        /// <summary>
        /// 設定ファイルパス
        /// </summary>
        private static readonly string SETTING_FILE = Directory.GetCurrentDirectory() + @"\settings.config";

        /// <summary>
        /// コマンド用のマップ
        /// </summary>
        private static Dictionary<Keys, EventHandler> commandMap = new Dictionary<Keys, EventHandler>();

        /// <summary>
        /// テキスト表示用デリゲート
        /// </summary>
        /// <param name="text">表示するテキスト</param>
        delegate void ReceiveText(string text);

        /// <summary>
        /// コンストラクタ
        /// </summary>
        public SQLiteToolForm()
        {
            InitializeComponent();

            //コマンドマップ初期化
            commandMap[Keys.S] = new EventHandler(btnConnect_Click);   //ctrl + S:接続
            commandMap[Keys.F] = new EventHandler(btnForward_Click);   //ctrl + F:ポートフォワード
            commandMap[Keys.E] = new EventHandler(ExecuteSQL);         //ctrl + E:実行
            commandMap[Keys.A] = new EventHandler(SelectAll);          //ctrl + A：全選択(フォーカスのあるテキスト)

        }

        /// <summary>
        /// Formロード.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SQLiteToolForm_Load(object sender, EventArgs e)
        {
            btnDisConnect.Enabled = false;
            txtHost.Text = "localhost";
            txtPort.Text = "1280";

            // ファイルがない場合処理終了
            if (!File.Exists(SETTING_FILE))
            {
                return;
            }

            XmlSerializer ser = new XmlSerializer(typeof(Settings));
            //ファイルを開く
            using (FileStream fs = new FileStream(SETTING_FILE, FileMode.Open))
            {
                //XMLファイルから読み込み、逆シリアル化する
                appSettings = (Settings)ser.Deserialize(fs);
                //閉じる
                fs.Close();
            }

            textInput.Text = appSettings.LastInputSQL.Replace("\n", Environment.NewLine);

            txtHost.Text = !string.IsNullOrEmpty(appSettings.LastSetHost) ? appSettings.LastSetHost : txtHost.Text;
            txtPort.Text = !string.IsNullOrEmpty(appSettings.LastSetHost) ? appSettings.LastSetPort : txtPort.Text;

        }

        /// <summary>
        /// Formクローズ.
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SQLiteToolForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            // 回線を閉じる
            disConnect();

            XmlSerializer ser = new XmlSerializer(typeof(Settings));
            //ファイルを開く
            using (FileStream fs = new FileStream(SETTING_FILE, FileMode.Create))
            {
                appSettings.LastInputSQL = textInput.Text;
                //シリアル化し、XMLファイルに保存する
                ser.Serialize(fs, appSettings);
                fs.Close();
            }
        }

        /// <summary>
        /// 接続ボタン押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnConnect_Click(object sender, EventArgs e)
        {

            // オープン済みなら処理しない
            if (IsConnected())
            {
                return;
            }

            String host = txtHost.Text;
            int port = Convert.ToInt32(txtPort.Text);
            try
            {
                txtStatus.Text = "connecting server [" + host + "] port[" + port + "]";
                tcpClient = new TcpClient(host, port);
                networkStream = tcpClient.GetStream();

                btnConnect.Enabled = false;
                btnDisConnect.Enabled = !btnConnect.Enabled;

                //受信待機スレッド起動
                Thread thread = new Thread(new ThreadStart(DataRecieve));
                thread.Start();


            }
            catch (SocketException se)
            {
                MessageBox.Show(se.Message + " code=" + se.ErrorCode, "Error!!",
                    MessageBoxButtons.OK, MessageBoxIcon.Stop);
                txtStatus.Text = string.Empty;
            }

        }

        /// <summary>
        /// 接続判定
        /// </summary>
        /// <returns>true:接続中 false:切断中</returns>
        private bool IsConnected()
        {
            return networkStream != null;
        }

        /// <summary>
        /// 切断ボタン押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnDisConnect_Click(object sender, EventArgs e)
        {
            disConnect();
        }

        /// <summary>
        /// 切断処理
        /// </summary>
        private void disConnect()
        {
            btnConnect.Enabled = true;
            btnDisConnect.Enabled = !btnConnect.Enabled;
            
            txtStatus.Text = "Disconnect Server!!";
            if (networkStream != null)
            {
                networkStream.Close();
                networkStream = null;
            }

            if (tcpClient != null)
            {
                tcpClient.Close();
                tcpClient = null;
            }
        }

        /// <summary>
        /// ポートフォワード
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnForward_Click(object sender, EventArgs e)
        {
            string port = txtPort.Text;

            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = "adb";
            startInfo.Arguments = "forward tcp:" + port + " tcp:" + port;
            startInfo.CreateNoWindow = true;
            startInfo.UseShellExecute = false;

            Process.Start(startInfo);

            txtStatus.Text = startInfo.FileName + " " + startInfo.Arguments;

        }


        /// <summary>
        /// クリアボタン押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnClear_Click(object sender, EventArgs e)
        {
            textResult.Text = string.Empty;
        }

        /// <summary>
        /// 実行ボタン押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnExecute_Click(object sender, EventArgs e)
        {
            string sendData = textInput.SelectedText;
            if (string.IsNullOrEmpty(sendData))
            {
                sendData = textInput.Text;
            }

            if (string.IsNullOrEmpty(sendData))
            {
                txtStatus.Text = "no Input!!";
                return;
            }

            //送信
            send(Encoding.UTF8.GetBytes(sendData));
        }

        /// <summary>
        /// コピーボタン押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnCopy_Click(object sender, EventArgs e)
        {
            if (!string.IsNullOrEmpty(textResult.Text))
            {
                Clipboard.SetText(textResult.Text);
            }
        }

        /// <summary>
        /// キー押下
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SQLiteToolForm_KeyDown(object sender, KeyEventArgs e)
        {
            //コントロール押してなければ終了
            if (!e.Control)
            {
                return;
            }

            //登録したデリゲートを実行
            if (commandMap.ContainsKey(e.KeyCode))
            {
                commandMap[e.KeyCode](sender, e);
            }

        }


        /// <summary>
        /// 全選択
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void SelectAll(object sender, EventArgs e)
        {
            if (sender.GetType() == typeof(TextBox))
            {
                ((TextBox)sender).SelectAll();
            }
        }


        /// <summary>
        /// クリア&実行
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void ExecuteSQL(object sender, EventArgs e)
        {
            KeyEventArgs keyEvent = (KeyEventArgs)e;

            if (keyEvent.Shift)
            {
                // ctrl + shift + eだったら結果をクリアする
                btnClear_Click(sender, keyEvent);
            }
            //実行する
            btnExecute_Click(sender, keyEvent);
        }

        /// <summary>
        /// データ送信
        /// </summary>
        /// <param name="data"></param>
        private void send(byte[] data)
        {
            // 未接続なら接続する
            if (!IsConnected())
            {
                btnConnect_Click(this, null);
                if (!IsConnected())
                {
                    // 接続に失敗したらあきらめる
                    return;
                }
            }

            try
            {
                // データ送信
                networkStream.Write(data, 0, data.Length);
                networkStream.Flush();
            }
            catch (IOException e)
            {
                MessageBox.Show(e.Message, "Error!!",
                    MessageBoxButtons.OK, MessageBoxIcon.Stop);
                disConnect();
            }
        }

        /// <summary>
        /// 結果受信
        /// </summary>
        /// <param name="text"></param>
        private void receive(string text)
        {
            textResult.AppendText(text);
            textResult.AppendText(Environment.NewLine);
        }

        /// <summary>
        /// 受信待機スレッドのdelegate
        /// </summary>
        public void DataRecieve()
        {

            try
            {
                while (true)
                {
                    StreamReader reader = new StreamReader(networkStream, Encoding.UTF8);
                    string line;
                    while ((line = reader.ReadLine()) != null)
                    {
                        // 一行ずつテキストに表示する
                        Invoke(new ReceiveText(receive), line);
                    }

                    if (line == null)
                    {
                        // nullで復帰する場合回線断が起こっている
                        Invoke(new EventHandler(btnDisConnect_Click), null, null);
                        break;
                    }

                }
            }
            catch (IOException ioe)
            {
                if (!IsDisconnection(ioe))
                {
                    // エラーメッセージを表示してループを抜ける
                    MessageBox.Show(ioe.Message, "Error!!",
                        MessageBoxButtons.OK, MessageBoxIcon.Stop);
                }
            }

        }

        /// <summary>
        /// 回線断判定
        /// </summary>
        /// <param name="ioe">元となる例外</param>
        /// <returns>true:回線断 false:それ以外</returns>
        private static bool IsDisconnection(IOException ioe)
        {
            if (ioe.InnerException == null){
                return false;
            }

            if (ioe.InnerException.GetType() != typeof(SocketException))
            {
                return false;
            }

            // 10004は回線断
            SocketException e = (SocketException)ioe.InnerException;
            return e.ErrorCode == 10004;
        }

    }
}
