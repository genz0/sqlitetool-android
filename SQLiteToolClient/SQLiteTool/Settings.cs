using System;
using System.Collections.Generic;
using System.Text;

namespace SQLiteTool
{
    /// <summary>
    /// 状態を保存するクラス
    /// </summary>
    public class Settings
    {
        public string LastInputSQL
        {
            get;
            set;
        }
        public string LastSetHost
        {
            get;
            set;
        }

        public string LastSetPort
        {
            get;
            set;
        }
    }
}
