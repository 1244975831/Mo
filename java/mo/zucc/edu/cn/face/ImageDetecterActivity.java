package mo.zucc.edu.cn.face;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.widget.ExtImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mo.zucc.edu.cn.face.Animation.CustomView;
import mo.zucc.edu.cn.face.DB.DBManager;
import mo.zucc.edu.cn.face.Fragmnet.CompareFragment;
import mo.zucc.edu.cn.face.item.FaceInfo;

import static mo.zucc.edu.cn.face.MainActivity.getDataColumn;

public class ImageDetecterActivity extends Activity implements SurfaceHolder.Callback {
    private final static int MSG_CODE = 0x1000;
    private final static int MSG_EVENT_REG = 0x1001;
    private final static int MSG_EVENT_NO_FACE = 0x1002;
    private final static int MSG_EVENT_NO_FEATURE = 0x1003;
    private final static int MSG_EVENT_FD_ERROR = 0x1004;
    private final static int MSG_EVENT_FR_ERROR = 0x1005;
    private final static int MSG_Image_NO_Match = 0x1006;
    private final static int MSG_Image_Match = 0x1007;
    private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
    private static final int REQUEST_CODE_IMAGE_OP = 2;
    private static final int REQUEST_CODE_OP = 3;
    private static final int REQUEST_CODE_IMAGE_Detecter = 4;
    private Uri mPath;
    private UIHandler mUIHandler;
    private String mFilePath;
    private final String TAG = this.getClass().toString();
    private Bitmap mBitmap;
    private Rect src = new Rect();
    private Rect dst = new Rect();
    private DBManager dbManager;
    private Thread view;
    private SurfaceHolder mSurfaceHolder;
    private AFR_FSDKFace mAFR_FSDKFace;
    private SurfaceView mSurfaceView;
    FRAbsLoop mFRAbsLoop = null;
    Handler mHandler;
    byte[] mImageNV21 = null;
    private boolean Find = false;
    private int mWidth, mHeight, mFormat;
    private CustomView customView;
    AFT_FSDKFace mAFT_FSDKFace = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detecter);

        if (!getIntentData(getIntent().getExtras())) {
            Log.e(TAG, "getIntentData fail!");
            this.finish() ;
        }
        mUIHandler = new UIHandler();
        mBitmap = Application.decodeImage(mFilePath);
        src.set(0,0,mBitmap.getWidth(),mBitmap.getHeight());
        customView = (CustomView)findViewById(R.id.customview);
        mSurfaceView = (SurfaceView)this.findViewById(R.id.surfaceView);
        mSurfaceView.getHolder().addCallback(this);
        view = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mSurfaceHolder == null) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight() * 3 / 2];
                ImageConverter convert = new ImageConverter();
                convert.initial(mBitmap.getWidth(), mBitmap.getHeight(), ImageConverter.CP_PAF_NV21);
                if (convert.convert(mBitmap, data)) {
                    Log.d(TAG, "convert ok!");
                }
                convert.destroy();

                AFD_FSDKEngine engine = new AFD_FSDKEngine();
                AFD_FSDKVersion version = new AFD_FSDKVersion();
                List<AFD_FSDKFace> result = new ArrayList<AFD_FSDKFace>();
                AFD_FSDKError err = engine.AFD_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.fd_key, AFD_FSDKEngine.AFD_OPF_0_HIGHER_EXT, 16, 5);
                Log.d(TAG, "AFD_FSDK_InitialFaceEngine = " + err.getCode());
                if (err.getCode() != AFD_FSDKError.MOK) {
                    Message reg = Message.obtain();
                    reg.what = MSG_CODE;
                    reg.arg1 = MSG_EVENT_FD_ERROR;
                    reg.arg2 = err.getCode();
                    mUIHandler.sendMessage(reg);
                }
                err = engine.AFD_FSDK_GetVersion(version);
                Log.d(TAG, "AFD_FSDK_GetVersion =" + version.toString() + ", " + err.getCode());
                err  = engine.AFD_FSDK_StillImageFaceDetection(data, mBitmap.getWidth(), mBitmap.getHeight(), AFD_FSDKEngine.CP_PAF_NV21, result);
                Log.d(TAG, "AFD_FSDK_StillImageFaceDetection =" + err.getCode() + "<" + result.size());
                while (mSurfaceHolder != null) {
                    Canvas canvas = mSurfaceHolder.lockCanvas();
                    if (canvas != null) {
                        Paint mPaint = new Paint();
                        boolean fit_horizontal = canvas.getWidth() / (float)src.width() < canvas.getHeight() / (float)src.height() ? true : false;
                        float scale = 1.0f;
                        if (fit_horizontal) {
                            scale = canvas.getWidth() / (float)src.width();
                            dst.left = 0;
                            dst.top = (canvas.getHeight() - (int)(src.height() * scale)) / 2;
                            dst.right = dst.left + canvas.getWidth();
                            dst.bottom = dst.top + (int)(src.height() * scale);
                        } else {
                            scale = canvas.getHeight() / (float)src.height();
                            dst.left = (canvas.getWidth() - (int)(src.width() * scale)) / 2;
                            dst.top = 0;
                            dst.right = dst.left + (int)(src.width() * scale);
                            dst.bottom = dst.top + canvas.getHeight();
                        }
                        canvas.drawBitmap(mBitmap, src, dst, mPaint);
                        canvas.save();
                        canvas.scale((float) dst.width() / (float) src.width(), (float) dst.height() / (float) src.height());
                        canvas.translate(dst.left / scale, dst.top / scale);
                        for (AFD_FSDKFace face : result) {
                            mPaint.setColor(Color.RED);
                            mPaint.setStrokeWidth(10.0f);
                            mPaint.setStyle(Paint.Style.STROKE);
//                            canvas.drawRect(face.getRect(), mPaint);
                            customView.Customgetdata(face.getRect(),src,dst,scale,mBitmap.getWidth());
                            new Thread(customView).start();
                        }
                        canvas.restore();
                        mSurfaceHolder.unlockCanvasAndPost(canvas);
                        break;
                    }
                }

                if (!result.isEmpty()) {
                    AFR_FSDKVersion version1 = new AFR_FSDKVersion();
                    AFR_FSDKEngine engine1 = new AFR_FSDKEngine();
                    AFR_FSDKFace result1 = new AFR_FSDKFace();
                    AFR_FSDKError error1 = engine1.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
                    Log.d("com.arcsoft", "AFR_FSDK_InitialEngine = " + error1.getCode());
                    if (error1.getCode() != AFD_FSDKError.MOK) {
                        Message reg = Message.obtain();
                        reg.what = MSG_CODE;
                        reg.arg1 = MSG_EVENT_FR_ERROR;
                        reg.arg2 = error1.getCode();
                        mUIHandler.sendMessage(reg);
                    }
                    error1 = engine1.AFR_FSDK_GetVersion(version1);
                    Log.d("com.arcsoft", "FR=" + version.toString() + "," + error1.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
                    error1 = engine1.AFR_FSDK_ExtractFRFeature(data, mBitmap.getWidth(), mBitmap.getHeight(), AFR_FSDKEngine.CP_PAF_NV21, new Rect(result.get(0).getRect()), result.get(0).getDegree(), result1);
                    Log.d("com.arcsoft", "Face=" + result1.getFeatureData()[0] + "," + result1.getFeatureData()[1] + "," + result1.getFeatureData()[2] + "," + error1.getCode());
                    if(error1.getCode() == error1.MOK) {
                        int i = 0;
                        //多张人脸注册
                        while(i<result.size()){
                            mAFR_FSDKFace = result1.clone();
                            int width = result.get(0).getRect().width();
                            int height = result.get(0).getRect().height();
                            Bitmap face_bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
                            Canvas face_canvas = new Canvas(face_bitmap);
                            face_canvas.drawBitmap(mBitmap, result.get(i).getRect(), new Rect(0, 0, width, height), null);
                            Message reg = Message.obtain();
                            reg.what = MSG_CODE;
                            reg.arg1 = MSG_EVENT_REG;
                            reg.obj = face_bitmap;
                            mUIHandler.sendMessage(reg);
                            i++;
                        }


                    } else {
                        Message reg = Message.obtain();
                        reg.what = MSG_CODE;
                        reg.arg1 = MSG_EVENT_NO_FEATURE;
                        mUIHandler.sendMessage(reg);
                    }
                    error1 = engine1.AFR_FSDK_UninitialEngine();
                    Log.d("com.arcsoft", "AFR_FSDK_UninitialEngine : " + error1.getCode());
                } else {
                    Message reg = Message.obtain();
                    reg.what = MSG_CODE;
                    reg.arg1 = MSG_EVENT_NO_FACE;
                    mUIHandler.sendMessage(reg);
                }
                err = engine.AFD_FSDK_UninitialFaceEngine();
                Log.d(TAG, "AFD_FSDK_UninitialFaceEngine =" + err.getCode());
            }
        });
        view.start();

    }

    class UIHandler extends android.os.Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_CODE) {
                if (msg.arg1 == MSG_EVENT_REG) {
                    if (msg.arg1 == MSG_EVENT_REG) {
                        final Bitmap face = (Bitmap) msg.obj;
//                        dbManager= new DBManager(getBaseContext());
//                        dbManager.addFace("识别图像",mAFR_FSDKFace.getFeatureData(),face);
//                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//                        face.compress(Bitmap.CompressFormat.PNG, 100, baos);

//                        mImageNV21 = baos.toByteArray();
                        mFRAbsLoop = new FRAbsLoop();
                        mFRAbsLoop.start();
                    }
                }else if(msg.arg1 == MSG_EVENT_NO_FEATURE ){
                    Toast.makeText(ImageDetecterActivity.this, "人脸特征无法检测，请换一张图片", Toast.LENGTH_SHORT).show();
                } else if(msg.arg1 == MSG_EVENT_NO_FACE ){
                    new SweetAlertDialog(ImageDetecterActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("错误")
                            .setContentText("没有检测到人脸，请换一张图片")
                            .setCancelText("返回")
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    finish();
                                }
                            })
                            .setConfirmMissText("重新选择")
                            .setConfirmMissListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
                                    getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
                                    getImageByalbum.setType("image/jpeg");
                                    startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
                                    finish();
                                }
                            })
                            .show();
                }else if(msg.arg1 == MSG_Image_Match) {
                    Toast.makeText(ImageDetecterActivity.this, "hey", Toast.LENGTH_SHORT).show();
                }else if(msg.arg1 == MSG_EVENT_FD_ERROR ){
                    Toast.makeText(ImageDetecterActivity.this, "FD初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
                } else if(msg.arg1 == MSG_EVENT_FR_ERROR){
                    Toast.makeText(ImageDetecterActivity.this, "FR初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
                }
            }
            if (msg.arg1 == MSG_Image_NO_Match) {
                if(Find == false){
                    new SweetAlertDialog(ImageDetecterActivity.this, SweetAlertDialog.ERROR_TYPE)
                            .setConfirmText("确定")
                            .setCancelText("取消")
                            .setTitleText("没有找到匹配的人脸")
                            .setContentText("点击确定注册该人脸，取消返回主页")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent it = new Intent(ImageDetecterActivity.this, RegisterActivity.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString("imagePath", mFilePath);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] oldimage = baos.toByteArray();
                                    bundle.putByteArray("oldimage",null);
                                    it.putExtras(bundle);
                                    startActivityForResult(it, 3);
                                    finish();
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener(){
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Intent intent = new Intent(ImageDetecterActivity.this,MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            })
                            .show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_IMAGE_OP ||requestCode == REQUEST_CODE_IMAGE_Detecter && resultCode == RESULT_OK ) {
            //防止用户未选择图片关闭相册出错
            if(data != null) {
                mPath = data.getData();
                String file = getPath(mPath);
                Bitmap bmp = Application.decodeImage(file);
                if (file == null)
                    Toast.makeText(ImageDetecterActivity.this, "上传图片格式不正确，请选择jpg或png格式图片", Toast.LENGTH_SHORT).show();
                else {
                    if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
                        Log.e(TAG, "error");
                    } else {
                        Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
                    }
                    Intent it = new Intent(ImageDetecterActivity.this, ImageDetecterActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("imagePath", file);
                    it.putExtras(bundle);
                    startActivityForResult(it, REQUEST_CODE_OP);
                    this.finish();
                }
            }
        } else if (requestCode == REQUEST_CODE_OP) {
            Log.i(TAG, "RESULT =" + resultCode);
            if (data == null) {
                return;
            }
            Bundle bundle = data.getExtras();
            String path = bundle.getString("imagePath");
            Log.i(TAG, "path="+path);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSurfaceHolder = null;
        try {
            view.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    class FRAbsLoop extends AbsLoop {

        AFR_FSDKVersion version = new AFR_FSDKVersion();
        AFR_FSDKEngine engine = new AFR_FSDKEngine();
        AFR_FSDKFace result = new AFR_FSDKFace();
        List<FaceDB.FaceRegist> mResgist = ((Application)ImageDetecterActivity.this.getApplicationContext()).mFaceDB.mRegister;
        @Override
        public void setup() {
            AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
            Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());
            error = engine.AFR_FSDK_GetVersion(version);
            Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
        }

        @Override
        public void loop() {
            if (mImageNV21 != null||mImageNV21 == null) {
                result.setFeatureData(mAFR_FSDKFace.getFeatureData());
//                AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, dst.width(), dst.height(), AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
                AFR_FSDKMatching score = new AFR_FSDKMatching();
                float max = 0.0f;
                String name = null;
                FaceInfo matchface = new FaceInfo();
                matchface.setFacename(null);
                AFR_FSDKFace face = new AFR_FSDKFace();
                dbManager= new DBManager(getBaseContext());
                ArrayList<FaceInfo> faceInfo = dbManager.selectAllFaces();
                for(int i = 0 ; i<faceInfo.size() ; i++){
                    face.setFeatureData(faceInfo.get(i).getFaceinfo());
                    engine.AFR_FSDK_FacePairMatching(result, face, score);
                    if ( max < score.getScore()){
                        max = score.getScore();
                        matchface = faceInfo.get(i);
                    }
                }
                //crop
                if(max>0.6){
                    if(max >= 0.8 &&max<0.9 ){
                        max = max+(float)0.1;
                    }else if(max > 0.9&& max <0.95){
                        max = max + 0.05f;
                    }else if(max <0.8 && max >0.7){
                        max = max +0.15f;
                    }else if(max <0.7){
                        max = max+0.2f;
                    }
                    Find = true;
                    CompareFragment compareFragment = new CompareFragment();

                    Bundle bundle = new Bundle();
                    //主动比较的图，即用户选的图

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] zhu = baos.toByteArray();
                    bundle.putByteArray("zhu",zhu);
                    //数据库内被比较得出的最匹配的图
                    bundle.putByteArray("bei",matchface.getFacepic());
                    compareFragment.setArguments(bundle);
                    bundle.putFloat("soure",max);
                    bundle.putString("beimatchname",matchface.getFacename());

                    FragmentManager fragmentManager = getFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.compare,compareFragment);
                    fragmentTransaction.commit();
                    this.break_loop();

                }else{
                    Message reg = Message.obtain();
                    reg.arg1 = MSG_Image_NO_Match;
                    mUIHandler.sendMessage(reg);
                    Find = false;
                    this.break_loop();
                }

                mImageNV21 = null;
            }
        }

        @Override
        public void over() {
            AFR_FSDKError error = engine.AFR_FSDK_UninitialEngine();
            Log.d(TAG, "AFR_FSDK_UninitialEngine : " + error.getCode());
        }
    }
    private boolean getIntentData(Bundle bundle) {
        try {
            mFilePath = bundle.getString("imagePath");
            if (mFilePath == null || mFilePath.isEmpty()) {
                return false;
            }
            Log.i(TAG, "getIntentData:" + mFilePath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (DocumentsContract.isDocumentUri(this, uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    return null;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    return null;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(this, contentUri, selection, selectionArgs);
            }
        }
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor actualimagecursor = managedQuery(uri, proj,null,null,null);
        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor.getString(actual_image_column_index);
        String end = img_path.substring(img_path.length() - 4);
        if (0 != end.compareToIgnoreCase(".jpg") && 0 != end.compareToIgnoreCase(".png")) {
            return null;
        }
        return img_path;
    }
}
