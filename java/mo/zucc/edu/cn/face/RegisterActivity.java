package mo.zucc.edu.cn.face;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.arcsoft.facedetection.AFD_FSDKEngine;
import com.arcsoft.facedetection.AFD_FSDKError;
import com.arcsoft.facedetection.AFD_FSDKFace;
import com.arcsoft.facedetection.AFD_FSDKVersion;
import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.image.ImageConverter;
import com.guo.android_extend.widget.ExtImageView;
import com.guo.android_extend.widget.HListView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mo.zucc.edu.cn.face.Animation.CustomView;
import mo.zucc.edu.cn.face.DB.DBManager;

/**
 *Created by mo on 2017/10/11.
 */

public class RegisterActivity extends Activity implements SurfaceHolder.Callback {
	private final String TAG = this.getClass().toString();
	private final static int MSG_CODE = 0x1000;
	private final static int MSG_EVENT_REG = 0x1001;
	private final static int MSG_EVENT_NO_FACE = 0x1002;
	private final static int MSG_EVENT_NO_FEATURE = 0x1003;
	private final static int MSG_EVENT_FD_ERROR = 0x1004;
	private final static int MSG_EVENT_FR_ERROR = 0x1005;
	private int Face_Size;
	private int click_times;
	private UIHandler mUIHandler;
	// Intent data.
	private String 	mFilePath;
	private DBManager dbManager;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Bitmap mBitmap;
	private Bitmap oldmap;
	byte[] getimage;
	private Rect src = new Rect();
	private Rect dst = new Rect();
	private Thread view;
	private int RegisterClass;
	private EditText mEditText;
	private ExtImageView mExtImageView;
//	private HListView mHListView;
	private RegisterViewAdapter mRegisterViewAdapter;
	private AFR_FSDKFace mAFR_FSDKFace;

	private CustomView customView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_register);
		//initial data.
		if (!getIntentData(getIntent().getExtras())) {
			Log.e(TAG, "getIntentData fail!");
			this.finish() ;
		}

		mRegisterViewAdapter = new RegisterViewAdapter(this);
//		mHListView = (HListView)findViewById(R.id.hlistView);
//		mHListView.setAdapter(mRegisterViewAdapter);
//		mHListView.setOnItemClickListener(mRegisterViewAdapter);
		customView = (CustomView)findViewById(R.id.customView);
		mUIHandler = new UIHandler();
		mBitmap = Application.decodeImage(mFilePath);
		src.set(0,0,mBitmap.getWidth(),mBitmap.getHeight());
		mSurfaceView = (SurfaceView)this.findViewById(R.id.surfaceView);
		mSurfaceView.getHolder().addCallback(this);
		view = new Thread(new Runnable() {
			@Override
			public void run() {
				while (mSurfaceHolder == null) {
					try {
						// 每执行一次暂停80毫秒
						Thread.sleep(80);
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
					Face_Size = result.size();
					click_times = 1;
					if (canvas != null&&result.size()==1) {
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
//							canvas.drawRect(face.getRect(), mPaint);
							customView.Customgetdata(face.getRect(),src,dst,scale, mBitmap.getWidth());
							new Thread(customView).start();
						}
						canvas.restore();
						mSurfaceHolder.unlockCanvasAndPost(canvas);
						break;
					}
					else if(canvas != null&&result.size()>1){
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
							canvas.drawRect(face.getRect(), mPaint);
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

	/**
	 * @note bundle data :
	 * String imagePath
	 *
	 * @param bundle
	 */
	private boolean getIntentData(Bundle bundle) {
		try {
			mFilePath = bundle.getString("imagePath");
			RegisterClass = bundle.getInt("registerclass");
//			getimage = bundle.getByteArray("oldimage");
//			oldmap =  BitmapFactory.decodeByteArray(getimage, 0, getimage.length);
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

	class UIHandler extends android.os.Handler {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == MSG_CODE) {
				if (msg.arg1 == MSG_EVENT_REG) {
					final Bitmap face = (Bitmap) msg.obj;
					new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.CUSTOM_IMAGE_TYPE)
							.setTitleText("请输入注册名!")
							.setCustomImage((Bitmap) msg.obj)
							.setConfirmText("确定")
							.setCancelText("取消")
							.setEdname(true)
							.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									String edname = sweetAlertDialog.getEdtext();
									if(edname.equals(null) || edname.equals("")){
										Toast.makeText(RegisterActivity.this, "名字不能为空！", Toast.LENGTH_SHORT).show();
									}else{
										((Application)RegisterActivity.this.getApplicationContext()).mFaceDB.addFace(edname, mAFR_FSDKFace,1);
										dbManager= new DBManager(getBaseContext());
										if(RegisterClass == 0){
											dbManager.addFace(edname,mAFR_FSDKFace.getFeatureData(),face,null);
										}else{
											dbManager.addManager(edname,mAFR_FSDKFace.getFeatureData(),face,null);
										}
										mRegisterViewAdapter.notifyDataSetChanged();
										if(Face_Size == 1){
											sweetAlertDialog.setTitleText("注册成功!")
													.setContentText("您上传的信息已经成功保存至识别库内!")
													.setConfirmText("确定")
													.showCancelButton(false)
													.setCancelClickListener(null)
													.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener(){
														@Override
														public void onClick(SweetAlertDialog sweetAlertDialog) {
															finish();
														}
													})
													.setEdname(false)
													.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
										}else{
											if(click_times<Face_Size){
												sweetAlertDialog.setTitleText("注册成功!")
														.setContentText("您上传的信息已经成功保存至识别库内!")
														.setConfirmText("确定")
														.showConfirmButton(false)
														.showCancelButton(false)
														.setCancelClickListener(null)
														.setConfirmClickListener(null)
														.setEdname(false)
														.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
												click_times++;
											}else{
												sweetAlertDialog.setTitleText("注册成功!")
														.setContentText("您上传的信息已经成功保存至识别库内!")
														.setConfirmText("确定")
														.showCancelButton(false)
														.setCancelClickListener(null)
														.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener(){
															@Override
															public void onClick(SweetAlertDialog sweetAlertDialog) {
																finish();
															}
														})
														.setEdname(false)
														.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
											}
										}

									}
								}
							})
							.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									if(Face_Size == 1) {
										sweetAlertDialog.setTitleText("取消注册!")
												.setContentText("您的注册操作已取消")
												.setConfirmText("确定")
												.showCancelButton(false)
												.setCancelClickListener(null)
												.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
													@Override
													public void onClick(SweetAlertDialog sweetAlertDialog) {
														finish();
													}
												})
												.setEdname(false)
												.changeAlertType(SweetAlertDialog.ERROR_TYPE);
									}else {
										if(click_times<Face_Size) {
											sweetAlertDialog.setTitleText("取消注册!")
													.setContentText("您的注册操作已取消")
													.setConfirmText("下一个")
													.setCancelText("返回主页")
													.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
														@Override
														public void onClick(SweetAlertDialog sweetAlertDialog) {
															finish();
														}
													})
													.setConfirmClickListener(null)
													.setEdname(false)
													.changeAlertType(SweetAlertDialog.ERROR_TYPE);
											click_times++;
										}else {
											sweetAlertDialog.setTitleText("取消注册!")
													.setContentText("您的注册操作已取消")
													.setConfirmText("确定")
													.showCancelButton(false)
													.setCancelClickListener(null)
													.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
														@Override
														public void onClick(SweetAlertDialog sweetAlertDialog) {
															finish();
														}
													})
													.setEdname(false)
													.changeAlertType(SweetAlertDialog.ERROR_TYPE);
										}
									}
								}
							})
							.show();

				} else if(msg.arg1 == MSG_EVENT_NO_FEATURE ){
					Toast.makeText(RegisterActivity.this, "人脸特征无法检测，请换一张图片", Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_NO_FACE ){
					Toast.makeText(RegisterActivity.this, "没有检测到人脸，请换一张图片", Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_FD_ERROR ){
					Toast.makeText(RegisterActivity.this, "FD初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
				} else if(msg.arg1 == MSG_EVENT_FR_ERROR){
					Toast.makeText(RegisterActivity.this, "FR初始化失败，错误码：" + msg.arg2, Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

	class Holder {
		ExtImageView siv;
		TextView tv;
	}

	class RegisterViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
		Context mContext;
		LayoutInflater mLInflater;

		public RegisterViewAdapter(Context c) {
			// TODO Auto-generated constructor stub
			mContext = c;
			mLInflater = LayoutInflater.from(mContext);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			Holder holder = null;
			if (convertView != null) {
				holder = (Holder) convertView.getTag();
			} else {
				convertView = mLInflater.inflate(R.layout.item_sample, null);
				holder = new Holder();
				holder.siv = (ExtImageView) convertView.findViewById(R.id.imageView1);
				holder.tv = (TextView) convertView.findViewById(R.id.textView1);
				convertView.setTag(holder);
			}

			if (!((Application)mContext.getApplicationContext()).mFaceDB.mRegister.isEmpty()) {
				FaceDB.FaceRegist face = ((Application) mContext.getApplicationContext()).mFaceDB.mRegister.get(position);
				holder.tv.setText(face.mName);
//				byte[] m = face.mFaceList.get(0).getFeatureData();
//				Bitmap bm = BitmapFactory.decodeByteArray(m, 0, m.length);

//                InputStream input = new ByteArrayInputStream(m);
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inSampleSize = 8;
//                SoftReference softRef = new SoftReference(BitmapFactory.decodeStream(
//                        input , null, options));
//                Bitmap bm = (Bitmap) softRef.get();
//				Bitmap bm = BitmapFactory.decodeByteArray(m, 0, m.length);
//				holder.siv.setImageBitmap(mBitmap);
				convertView.setWillNotDraw(false);
			}

			return convertView;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//			Log.d("onItemClick", "onItemClick = " + position + "pos=" + mHListView.getScroll());
			final String name = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.get(position).mName;
			final int count = ((Application)mContext.getApplicationContext()).mFaceDB.mRegister.get(position).mFaceList.size();
			new AlertDialog.Builder(RegisterActivity.this)
					.setTitle("删除注册名:" + name)
					.setMessage("包含:" + count + "个注册人脸特征信息")
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							((Application)mContext.getApplicationContext()).mFaceDB.delete(name);
							mRegisterViewAdapter.notifyDataSetChanged();
							dialog.dismiss();
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
		}
	}

}
