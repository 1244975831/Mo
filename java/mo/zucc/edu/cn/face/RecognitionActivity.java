package mo.zucc.edu.cn.face;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKMatching;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.arcsoft.facetracking.AFT_FSDKEngine;
import com.arcsoft.facetracking.AFT_FSDKError;
import com.arcsoft.facetracking.AFT_FSDKFace;
import com.arcsoft.facetracking.AFT_FSDKVersion;
import com.guo.android_extend.java.AbsLoop;
import com.guo.android_extend.java.ExtByteArrayOutputStream;
import com.guo.android_extend.tools.CameraHelper;
import com.guo.android_extend.widget.CameraFrameData;
import com.guo.android_extend.widget.CameraGLSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView;
import com.guo.android_extend.widget.CameraSurfaceView.OnCameraListener;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import mo.zucc.edu.cn.face.DB.DBManager;
import mo.zucc.edu.cn.face.item.FaceInfo;

/**
 *Created by mo on 2017/10/11.
 */

public class RecognitionActivity extends Activity implements OnCameraListener, View.OnTouchListener, Camera.AutoFocusCallback {
	private final String TAG = this.getClass().getSimpleName();

	private int mWidth, mHeight, mFormat;
	private CameraSurfaceView mSurfaceView;
	private CameraGLSurfaceView mGLSurfaceView;
	private Camera mCamera;

	AFT_FSDKVersion version = new AFT_FSDKVersion();
	AFT_FSDKEngine engine = new AFT_FSDKEngine();
	List<AFT_FSDKFace> result = new ArrayList<>();

	int mCameraID;
	int mCameraRotate;
	int mFlag;
	boolean mCameraMirror;
	byte[] mImageNV21 = null;
	FRAbsLoop mFRAbsLoop = null;
	AFT_FSDKFace mAFT_FSDKFace = null;
	Handler mHandler;
    private DBManager dbManager;



	class FRAbsLoop extends AbsLoop {

		AFR_FSDKVersion version = new AFR_FSDKVersion();
		AFR_FSDKEngine engine = new AFR_FSDKEngine();
		AFR_FSDKFace result = new AFR_FSDKFace();
		List<FaceDB.FaceRegist> mResgist = ((Application)RecognitionActivity.this.getApplicationContext()).mFaceDB.mRegister;

		@Override
		public void setup() {
			AFR_FSDKError error = engine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
			Log.d(TAG, "AFR_FSDK_InitialEngine = " + error.getCode());
			error = engine.AFR_FSDK_GetVersion(version);
			Log.d(TAG, "FR=" + version.toString() + "," + error.getCode()); //(210, 178 - 478, 446), degree = 1　780, 2208 - 1942, 3370
		}

		@Override
		public void loop() {
			if (mImageNV21 != null) {
				AFR_FSDKError error = engine.AFR_FSDK_ExtractFRFeature(mImageNV21, mWidth, mHeight, AFR_FSDKEngine.CP_PAF_NV21, mAFT_FSDKFace.getRect(), mAFT_FSDKFace.getDegree(), result);
				Log.d(TAG, "Face=" + result.getFeatureData()[0] + "," + result.getFeatureData()[1] + "," + result.getFeatureData()[2] + "," + error.getCode());
				AFR_FSDKMatching score = new AFR_FSDKMatching();
				float max = 0.0f;
				String name = null;
//				for (FaceDB.FaceRegist fr : mResgist) {
//					for (AFR_FSDKFace face : fr.mFaceList) {
//						//人脸匹配 face为人脸库数据
//						error = engine.AFR_FSDK_FacePairMatching(result, face, score);
//						Log.d(TAG,  "Score:" + score.getScore() + ", AFR_FSDK_FacePairMatching=" + error.getCode());
//						if (max < score.getScore()) {
//							max = score.getScore();
//							name = fr.mName;
//						}
//					}
//				}
                FaceInfo matchface = new FaceInfo();
                AFR_FSDKFace face = new AFR_FSDKFace();
                dbManager= new DBManager(getBaseContext());
                ArrayList<FaceInfo> faceInfo = dbManager.selectAllManagers();
                for(int i = 0 ; i<faceInfo.size() ; i++){
                    face.setFeatureData(faceInfo.get(i).getFaceinfo());
                    engine.AFR_FSDK_FacePairMatching(result, face, score);
                    if ( max < score.getScore()){
                        max = score.getScore();
                        matchface = faceInfo.get(i);
                        name = matchface.getFacename();
                    }
                }
				//crop
				byte[] data = mImageNV21;
				YuvImage yuv = new YuvImage(data, ImageFormat.NV21, mWidth, mHeight, null);
				ExtByteArrayOutputStream ops = new ExtByteArrayOutputStream();
				yuv.compressToJpeg(mAFT_FSDKFace.getRect(), 80, ops);
				final Bitmap bmp = BitmapFactory.decodeByteArray(ops.getByteArray(), 0, ops.getByteArray().length);
				try {
					ops.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (max > 0.6f) {
					if(mFlag == 1){
						Intent intent = new Intent(RecognitionActivity.this,ManagerDBActivity.class);
						startActivity(intent);
						finish();
					}else{
						Intent intent = new Intent(RecognitionActivity.this,FaceDBActivity.class);
						startActivity(intent);
						finish();
					}

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


	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mCameraID = getIntent().getIntExtra("Camera", 0) == 0 ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT;
		mCameraRotate = getIntent().getIntExtra("Camera", 0) == 0 ? 90 : 270;
		mCameraMirror = getIntent().getIntExtra("Camera", 0) == 0 ? false : true;
		if (!getIntentData(getIntent().getExtras())) {
			Log.e(TAG, "getIntentData fail!");
			this.finish() ;
		}

		mWidth = 1280;
		mHeight = 960;
		mFormat = ImageFormat.NV21;
		mHandler = new Handler();

		setContentView(R.layout.activity_recognition);
		mGLSurfaceView = (CameraGLSurfaceView) findViewById(R.id.glsurfaceView);
		mGLSurfaceView.setOnTouchListener(this);
		mSurfaceView = (CameraSurfaceView) findViewById(R.id.surfaceView);
		mSurfaceView.setOnCameraListener(this);
		mSurfaceView.setupGLSurafceView(mGLSurfaceView, true, mCameraMirror, mCameraRotate);
		mSurfaceView.debug_print_fps(true, false);

		//snap


		AFT_FSDKError err = engine.AFT_FSDK_InitialFaceEngine(FaceDB.appid, FaceDB.ft_key, AFT_FSDKEngine.AFT_OPF_0_HIGHER_EXT, 16, 5);
		Log.d(TAG, "AFT_FSDK_InitialFaceEngine =" + err.getCode());
		err = engine.AFT_FSDK_GetVersion(version);
		Log.d(TAG, "AFT_FSDK_GetVersion:" + version.toString() + "," + err.getCode());

		mFRAbsLoop = new FRAbsLoop();
		mFRAbsLoop.start();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mFRAbsLoop.shutdown();
		AFT_FSDKError err = engine.AFT_FSDK_UninitialFaceEngine();
		Log.d(TAG, "AFT_FSDK_UninitialFaceEngine =" + err.getCode());
	}

	@Override
	public Camera setupCamera() {
		// TODO Auto-generated method stub
		mCamera = Camera.open(mCameraID);
		try {
			Camera.Parameters parameters = mCamera.getParameters();
			parameters.setPreviewSize(mWidth, mHeight);
			parameters.setPreviewFormat(mFormat);

			for( Camera.Size size : parameters.getSupportedPreviewSizes()) {
				Log.d(TAG, "SIZE:" + size.width + "x" + size.height);
			}
			for( Integer format : parameters.getSupportedPreviewFormats()) {
				Log.d(TAG, "FORMAT:" + format);
			}

			List<int[]> fps = parameters.getSupportedPreviewFpsRange();
			for(int[] count : fps) {
				Log.d(TAG, "T:");
				for (int data : count) {
					Log.d(TAG, "V=" + data);
				}
			}
			//parameters.setPreviewFpsRange(15000, 30000);
			//parameters.setExposureCompensation(parameters.getMaxExposureCompensation());
			//parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
			//parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
			//parmeters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			//parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			//parameters.setColorEffect(Camera.Parameters.EFFECT_NONE);
			mCamera.setParameters(parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mCamera != null) {
			mWidth = mCamera.getParameters().getPreviewSize().width;
			mHeight = mCamera.getParameters().getPreviewSize().height;
		}
		return mCamera;
	}

	@Override
	public void setupChanged(int format, int width, int height) {

	}

	@Override
	public boolean startPreviewLater() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object onPreview(byte[] data, int width, int height, int format, long timestamp) {
		AFT_FSDKError err = engine.AFT_FSDK_FaceFeatureDetect(data, width, height, AFT_FSDKEngine.CP_PAF_NV21, result);
		Log.d(TAG, "AFT_FSDK_FaceFeatureDetect =" + err.getCode());
		Log.d(TAG, "Face=" + result.size());
		for (AFT_FSDKFace face : result) {
			Log.d(TAG, "Face:" + face.toString());
		}
		if (mImageNV21 == null) {
			if (!result.isEmpty()) {
				mAFT_FSDKFace = result.get(0).clone();
				mImageNV21 = data.clone();
			} else {

			}
		}
		//copy rects
		Rect[] rects = new Rect[result.size()];
		for (int i = 0; i < result.size(); i++) {
			rects[i] = new Rect(result.get(i).getRect());
		}
		//clear result.
		result.clear();
		//return the rects for render.
		return rects;
	}

	@Override
	public void onBeforeRender(CameraFrameData data) {

	}

	@Override
	public void onAfterRender(CameraFrameData data) {
		mGLSurfaceView.getGLES2Render().draw_rect((Rect[])data.getParams(), Color.GREEN, 2);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		CameraHelper.touchFocus(mCamera, event, v, this);
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			Log.d(TAG, "Camera Focus SUCCESS!");
		}
	}

	private boolean getIntentData(Bundle bundle) {
		try {
			mFlag = bundle.getInt("flag");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}
