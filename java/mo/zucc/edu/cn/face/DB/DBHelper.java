package mo.zucc.edu.cn.face.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.sql.Blob;

/**
 * Created by mo on 2017/10/30.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "dbface.db";
    private static final int DATABASE_VERSION = 2;
    private Context mContext;
    public DBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        //用户列表 id 名字 照片 信息
        db.execSQL("CREATE TABLE IF NOT EXISTS User" +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, facename TEXT, facepic Blob , faceinfo Blob)");


        Toast.makeText(mContext, "success", Toast.LENGTH_SHORT).show();

    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onCreate(db);
    }
}