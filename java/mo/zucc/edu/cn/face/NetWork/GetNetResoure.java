package mo.zucc.edu.cn.face.NetWork;

import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import mo.zucc.edu.cn.face.item.FaceInfo;

/**
 * Created by Administrator on 2018/3/27.
 */

public class GetNetResoure {
    ArrayList<HashMap<String, Object>> human;
    ImageView name ;
    ArrayList<FaceInfo> faceInfos = new ArrayList<FaceInfo>();
    private static ArrayList<HashMap<String, Object>> Analysis(String jsonStr)throws JSONException {
        JSONArray jsonArray = null;
        // 初始化list数组对象
        ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
        jsonArray = new JSONArray(jsonStr);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            // 初始化map数组对象
            HashMap<String, Object> map = new HashMap<String, Object>();
            map.put("id", jsonObject.getString("id"));
            map.put("name", jsonObject.getString("name"));
            map.put("pic", jsonObject.getString("pic"));
            map.put("faceinfo",jsonObject.getString("faceinfo"));
            list.add(map);
        }
        return list;
    }
    public  ArrayList<FaceInfo> GetNet(){
        try {
            String url = "http://" + CurUrl.url;
            String c =  HttpUrlConnection.readParse(url);
            human = Analysis(c);
            faceInfos.clear();
            for(int i = 0 ; i< human.size() ; i++){
                byte[] srtbyte = null ;
                FaceInfo item = new FaceInfo();
                item.setNo(Integer.valueOf(human.get(i).get("id").toString()));
                String res = human.get(i).get("faceinfo").toString();
                srtbyte = res.getBytes("ISO-8859-1");
                item.setFaceinfo(srtbyte);
                faceInfos.add(item);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return faceInfos;
    }
}
