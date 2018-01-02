package mo.zucc.edu.cn.face.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.ArrayList;

import mo.zucc.edu.cn.face.FaceDB;
import mo.zucc.edu.cn.face.item.FaceInfo;

/**
 * Created by a on 2017/10/30.
 */

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;
    private Context mContext;
    private int TEXT_CONTENT = 0;//纯文本
    private int LINK_CONTENT = 1;//连接内容
    private int IMAGE_CONTENT = 2;//图片内容

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
        mContext = context;
    }

    public void initData() {
        ContentValues values = new ContentValues();
        //写入User表
        try {
            String facename = "路人甲";
            values.put("facename", facename);
            db.insert("User", null, values);
            values.clear();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFace(String facename , byte[] faceinfo ,Bitmap facepic ,Bitmap oldfacepic ) {
        ContentValues values = new ContentValues();
        //存人脸
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        facepic.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] faceimg = baos.toByteArray();
        //存原照
//        ByteArrayOutputStream ba = new ByteArrayOutputStream();
//        oldfacepic.compress(Bitmap.CompressFormat.PNG, 100, ba);
//        byte[] oldfaceimg = ba.toByteArray();
        //写入User表
        try {
            values.put("facename", facename);
            values.put("faceinfo", faceinfo);
            values.put("facepic", faceimg);
//            values.put("oldfacepic", oldfaceimg);
            db.insert("User", null, values);
            values.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public FaceInfo selectFaces(String name) {
        ArrayList<FaceInfo> data = new ArrayList<>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        FaceInfo result = new FaceInfo();
        Cursor cursor = dp.query("User",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            FaceInfo datas = new FaceInfo();
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String facename = cursor.getString(cursor.getColumnIndex("facename"));
            byte[] facepic = cursor.getBlob(cursor.getColumnIndex("facepic"));
            byte[] faceinfo = cursor.getBlob(cursor.getColumnIndex("faceinfo"));
            byte[] oldfacepic = cursor.getBlob(cursor.getColumnIndex("oldfacepic"));
            if(facename.equals(name)){
                datas.setNo(id);
                datas.setFacename(facename);
                datas.setFacepic(facepic);
                datas.setFaceinfo(faceinfo);
                datas.setOldfacepic(oldfacepic);
                result = datas;
                data.add(datas);
            }
        }

        cursor.close();

        return result;
    }

    public  ArrayList<FaceInfo> selectAllFaces() {
        ArrayList<FaceInfo> data = new ArrayList<>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        Cursor cursor = dp.query("User",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            FaceInfo datas = new FaceInfo();
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String facename = cursor.getString(cursor.getColumnIndex("facename"));
            byte[] facepic = cursor.getBlob(cursor.getColumnIndex("facepic"));
            byte[] faceinfo = cursor.getBlob(cursor.getColumnIndex("faceinfo"));
            byte[] oldfacepic = cursor.getBlob(cursor.getColumnIndex("oldfacepic"));
            datas.setNo(id);
            datas.setFacename(facename);
            datas.setFacepic(facepic);
            datas.setFaceinfo(faceinfo);
            datas.setOldfacepic(oldfacepic);
            data.add(datas);
        }
        cursor.close();
        return data;
    }
}
