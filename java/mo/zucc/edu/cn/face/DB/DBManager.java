package mo.zucc.edu.cn.face.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.renderscript.Sampler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.Map;

import mo.zucc.edu.cn.face.FaceDB;
import mo.zucc.edu.cn.face.R;
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
//            initnet(faceinfo,facename,faceimg);
            values.put("facename", facename);
            values.put("faceinfo", faceinfo);
            values.put("facepic", faceimg);
            byte[] srtbyte = faceinfo ;
            String str = null;
            String res = new String(srtbyte,"ISO-8859-1");
            srtbyte = res.getBytes("ISO-8859-1");
            db.insert("User", null, values);
            db.insert("Net",null,values);
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

    public void DeleteFaceByMap(Map<Integer, Boolean> map){
        ArrayList<FaceInfo> data = new ArrayList<>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        data = selectAllFaces();
        for(int i = 0 ; i< data.size() ; i++ ){
            if(null != map.get(i) && map.get(i) == true){
                dp.delete("User","_id = ?",new String[]{ data.get(i).getNo()+""});
            }
        }
    }

    public boolean ManagerIsEmpty(){
        boolean res = true;
        SQLiteDatabase dp=helper.getWritableDatabase();
        Cursor cursor = dp.query("Manager",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            res = false;
            break;
        }
        return res;
    }

    public  ArrayList<FaceInfo> selectAllManagers() {
        ArrayList<FaceInfo> data = new ArrayList<>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        Cursor cursor = dp.query("Manager",null,null,null,null,null,null);
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


    public void DeleteManagerByMap(Map<Integer, Boolean> map){
        ArrayList<FaceInfo> data = new ArrayList<>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        data = selectAllManagers();
        for(int i = 0 ; i< data.size() ; i++ ){
            if(null != map.get(i) && map.get(i) == true){
                dp.delete("Manager","_id = ?",new String[]{ data.get(i).getNo()+""});
            }
        }
    }

    public void addManager(String facename , byte[] faceinfo ,Bitmap facepic ,Bitmap oldfacepic ) {
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
            db.insert("Manager", null, values);
            values.clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] trans(byte[]face){
        byte[] srtbyte = face ;
        String str = null;
        try {
            String res = new String(srtbyte,"ISO-8859-1");
            srtbyte = res.getBytes("ISO-8859-1");
        }catch (Exception e ){

        }
        return  srtbyte;
    }

    public  void initnet(byte[] res,String name,byte[] faceimg){
        ContentValues values = new ContentValues();
        //存人脸
        try {
            values.put("name", name);
            values.put("faceinfo", res);
            values.put("facepic", faceimg);
            db.insert("Net", null, values);
            values.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public  ArrayList<FaceInfo> selectAllNet(){
        ArrayList<FaceInfo> faceInfos = new ArrayList<FaceInfo>();
        SQLiteDatabase dp=helper.getWritableDatabase();
        Cursor cursor = dp.query("Net",null,null,null,null,null,null);
        while (cursor.moveToNext()){
            FaceInfo datas = new FaceInfo();
            String name = cursor.getString(cursor.getColumnIndex("facename"));
            byte[] faceinfo = cursor.getBlob(cursor.getColumnIndex("faceinfo"));
            byte[] facepic = cursor.getBlob(cursor.getColumnIndex("facepic"));
            datas.setFacename(name);
            datas.setFaceinfo(faceinfo);
            datas.setFacepic(facepic);
            faceInfos.add(datas);
        }
        cursor.close();
        return faceInfos;
    }
}
