package mo.zucc.edu.cn.face.Fragmnet;


import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import mo.zucc.edu.cn.face.R;
import mo.zucc.edu.cn.face.item.FaceInfo;

public class CompareFragment extends Fragment {
    private List<FaceInfo> datas;
    private ImageView zhu;
    private ImageView bei;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v2 = inflater.inflate(R.layout.fragment_compare, container, false);
        zhu = (ImageView)v2.findViewById(R.id.zhu);
        bei = (ImageView)v2.findViewById(R.id.bei);
        initdata();
        return v2;
    }
    public void initdata(){
        Bundle bundle = getArguments();
        byte[] getimage = bundle.getByteArray("zhu");
        Bitmap zhuimage =  BitmapFactory.decodeByteArray(getimage, 0, getimage.length);
        getimage = bundle.getByteArray("bei");
        Bitmap beiimage =  BitmapFactory.decodeByteArray(getimage, 0, getimage.length);
        zhu.setImageBitmap(zhuimage);
        bei.setImageBitmap(beiimage);
    }

}
