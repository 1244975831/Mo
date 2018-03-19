package mo.zucc.edu.cn.face;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.ByteArrayOutputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mo.zucc.edu.cn.face.DB.DBManager;
import mo.zucc.edu.cn.face.item.FaceInfo;

/**
 *Created by mo on 2017/10/11.
 */
public class MainActivity extends Activity implements OnClickListener {
	private final String TAG = this.getClass().toString();

	private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
	private static final int REQUEST_CODE_IMAGE_OP = 2;
	private static final int REQUEST_CODE_OP = 3;
	private static final int REQUEST_CODE_IMAGE_Detecter = 4;
    private int RegisterClass ;
	private Uri mPath;
	private DBManager dbManager ;
	private ViewFlipper viewFlipper;
	float x1 =0;
	float y1 =0;
	float x = 0;
	float y = 0;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.main_test);
		dbManager = new DBManager(getBaseContext());
		viewFlipper = (ViewFlipper)findViewById(R.id.ViewFlippers);
		viewFlipper.setFlipInterval(4000);
		viewFlipper.setAutoStart(true);
		//注册人脸
		View v = this.findViewById(R.id.button1);
		v.setOnClickListener(this);
		//检测人脸
		v = this.findViewById(R.id.button2);
		v.setOnClickListener(this);
        //人脸库
		v = this.findViewById(R.id.button3);
		v.setOnClickListener(this);
        //权限管理
        v = this.findViewById(R.id.button4);
        v.setOnClickListener(this);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
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
					Toast.makeText(MainActivity.this, "上传图片格式不正确，请选择jpg或png格式图片", Toast.LENGTH_SHORT).show();
				else {
					if (bmp == null || bmp.getWidth() <= 0 || bmp.getHeight() <= 0) {
						Log.e(TAG, "error");
					} else {
						Log.i(TAG, "bmp [" + bmp.getWidth() + "," + bmp.getHeight());
					}
					if (requestCode == REQUEST_CODE_IMAGE_Detecter) {
						startImageDetector(bmp, file);
					} else {
						startRegister(bmp, file);
					}

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
		} else if (requestCode == REQUEST_CODE_IMAGE_CAMERA && resultCode == RESULT_OK) {
			String file = getPath(mPath);
			Bitmap bmp = Application.decodeImage(file);
			startRegister(bmp, file);
		}
	}

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		switch (paramView.getId()) {
            case R.id.button4:
                if(dbManager.ManagerIsEmpty()==true){
                    new SweetAlertDialog(this)
                            .setTitleText("检测到您尚未设置任何管理员")
							.setContentText("请尽快设置以保证资料的安全性")
                            .setConfirmText("立即设置")
							.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    RegisterClass = 1;
									sweetAlertDialog.setTitleText("请选择注册方式")
											.setOpenImageListener(new SweetAlertDialog.OnSweetClickListener(){
												@Override
												public void onClick(SweetAlertDialog sweetAlertDialog) {
													Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
													getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
													getImageByalbum.setType("image/jpeg");
													startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
												}
											})
											.setOpenCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
												@Override
												public void onClick(SweetAlertDialog sweetAialog) {
													Intent getImageByCamera = new Intent(
															"android.media.action.IMAGE_CAPTURE");
													ContentValues values = new ContentValues(1);

													values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
													mPath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
													getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, mPath);

													startActivityForResult(getImageByCamera, REQUEST_CODE_IMAGE_CAMERA);
												}
											})
											.setOpenCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
												@Override
												public void onClick(SweetAlertDialog sweetAlertDialog) {
													startDetector(0);
												}
											})
											.setCancelText("取消")
											.showCancelMissButton(false)
											.showConfirmButton(false)
											.showConfirmMissButton(false)
											.setLiebiao(true)
											.setEdname(false)
											.isfront(false)
											.show();
								}
							})
							.setCancelMissListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
//									sweetAlertDialog
//											.setTitleText("请进行身份验证")
//											.setContentText("为确保您的资料安全请先进行身份验证")
//											.setCancelText("取消")
//											.setConfirmMissText("确定")
//											.showConfirmButton(false)
//											.setConfirmMissListener(new SweetAlertDialog.OnSweetClickListener( ){
//												@Override
//												public void onClick(SweetAlertDialog sweetAlertDialog) {
//													startRecognition(1,1);
//												}
//											})
//											.show();
									Intent intent = new Intent(MainActivity.this,ManagerDBActivity.class);
									startActivity(intent);
								}
							})
                            .setCancelMissText("稍后设置")
                            .show();
					break;
                }
                else {
                    new SweetAlertDialog(this)
                            .setTitleText("请进行身份验证")
                            .setContentText("为确保您的资料安全请先进行身份验证")
                            .setCancelText("取消")
							.setConfirmMissText("确定")
							.showConfirmButton(false)
							.showConfirmMissButton(true)
							.setConfirmMissListener(new SweetAlertDialog.OnSweetClickListener( ){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									startRecognition(1,1);
								}
							})
                            .show();
//					Intent intent = new Intent(MainActivity.this,ManagerDBActivity.class);
//					startActivity(intent);
					break;
				}
			case R.id.button3:
				if(dbManager.ManagerIsEmpty()==false){
					new SweetAlertDialog(this)
							.setTitleText("请进行身份验证")
							.setContentText("为确保您的资料安全请先进行身份验证")
							.setCancelText("取消")
							.setConfirmMissText("确定")
							.showConfirmButton(false)
							.setConfirmMissListener(new SweetAlertDialog.OnSweetClickListener( ){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									startRecognition(1,0);
								}
							})
							.show();
					break;
				}
				Intent intent = new Intent(MainActivity.this,FaceDBActivity.class);
				startActivity(intent);
				break;
			case R.id.button2:
				if( ((Application)getApplicationContext()).mFaceDB.mRegister.isEmpty() ) {
					new SweetAlertDialog(MainActivity.this, SweetAlertDialog.ERROR_TYPE)
							.setTitleText("错误")
							.setContentText("尚未有注册过人脸，请先前往注册")
							.setConfirmText("确定")
							.show();
				} else {
					//用sweetAlert 制作感觉较好的选择弹出框
					new SweetAlertDialog(this)
							.setTitleText("请选择检测方式")
							.setOpenImageListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
									getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
									getImageByalbum.setType("image/jpeg");
									startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_Detecter);
								}
							})
							.setOpenCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									startDetector(0);
								}
							})
							.setOpenFrontCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
								@Override
								public void onClick(SweetAlertDialog sweetAlertDialog) {
									startDetector(1);
								}
							})
							.setCancelText("取消")
							.isfront(true)
                            .setLiebiao(true)
							.setEdname(false)
							.showCancelButton(true)
							.show();
					break;
				}
				break;
			case R.id.button1:
			    RegisterClass = 0 ;
				new SweetAlertDialog(this)
						.setTitleText("请选择注册方式")
						.setOpenImageListener(new SweetAlertDialog.OnSweetClickListener(){
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog) {
								Intent getImageByalbum = new Intent(Intent.ACTION_GET_CONTENT);
								getImageByalbum.addCategory(Intent.CATEGORY_OPENABLE);
								getImageByalbum.setType("image/jpeg");
								startActivityForResult(getImageByalbum, REQUEST_CODE_IMAGE_OP);
							}
						})
						.setOpenCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog) {
								Intent getImageByCamera = new Intent(
										"android.media.action.IMAGE_CAPTURE");
								ContentValues values = new ContentValues(1);

								values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
								mPath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
								getImageByCamera.putExtra(MediaStore.EXTRA_OUTPUT, mPath);

								startActivityForResult(getImageByCamera, REQUEST_CODE_IMAGE_CAMERA);
							}
						})
						.setOpenFrontCameraClickListener(new SweetAlertDialog.OnSweetClickListener(){
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog) {
								startDetector(1);
							}
						})
						.setCancelText("取消")
                        .setLiebiao(true)
						.setEdname(false)
						.isfront(false)
						.show();
				break;
			default:;
		}
	}

	/**
	 * @param uri
	 * @return
	 */
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

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
									   String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {
				column
		};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
					null);
			if (cursor != null && cursor.moveToFirst()) {
				final int index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}

	/**
	 * @param mBitmap
	 */
	//注册方法
	private void startRegister(Bitmap mBitmap, String file) {
		Intent it = new Intent(MainActivity.this, RegisterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		byte[] oldimage = baos.toByteArray();
		bundle.putByteArray("oldimage",null);
        bundle.putInt("registerclass",RegisterClass);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);

//		face.setImageBitmap(mBitmap);

	}
	private void startImageDetector(Bitmap mBitmap, String file) {
		Intent it = new Intent(MainActivity.this, ImageDetecterActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("imagePath", file);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);
	}
	//摄像头识别
	private void startDetector(int camera) {
		Intent it = new Intent(MainActivity.this, DetecterActivity.class);
		it.putExtra("Camera", camera);
		startActivityForResult(it, REQUEST_CODE_OP);
	}
	//摄像头验证
	private void startRecognition(int camera,int flag) {
		Intent it = new Intent(MainActivity.this, RecognitionActivity.class);
		it.putExtra("Camera", camera);
		Bundle bundle = new Bundle();
		bundle.putInt("flag",flag);
		it.putExtras(bundle);
		startActivityForResult(it, REQUEST_CODE_OP);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			x=event.getX();
			y=event.getY();
			viewFlipper.stopFlipping();
			viewFlipper.setAutoStart(false);
		}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			x1=event.getX();
			y1=event.getY();

			if(x1-x>20){
				viewFlipper.showPrevious();
			}
			if(x-x1>20){
				viewFlipper.showNext();
			}
			viewFlipper.startFlipping();
			viewFlipper.setAutoStart(true);
		}
		return super.onTouchEvent(event);
	}
}

