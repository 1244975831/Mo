package mo.zucc.edu.cn.face;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mo.zucc.edu.cn.face.DB.DBManager;
import mo.zucc.edu.cn.face.item.FaceInfo;
import mo.zucc.edu.cn.face.item.GridItem;

import static mo.zucc.edu.cn.face.MainActivity.getDataColumn;

public class ManagerDBActivity extends AppCompatActivity implements MultiChoiceModeListener {

	private GridView mGridView;
	private GridAdapter mGridAdapter;
	private TextView mActionText;
	private final String TAG = this.getClass().toString();
	private static final int MENU_SELECT_ALL = 0;
	private static final int MENU_UNSELECT_ALL = MENU_SELECT_ALL + 1;
	private static final int REQUEST_CODE_IMAGE_CAMERA = 1;
	private static final int REQUEST_CODE_IMAGE_OP = 2;
	private static final int REQUEST_CODE_OP = 3;
	private static final int REQUEST_CODE_IMAGE_Detecter = 4;
	private Map<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private DBManager dbManager;
	private Uri mPath;
	int w = 0 ;
	ArrayList<FaceInfo> faceInfos;
	private Bitmap[] mImgIds = new Bitmap[200];

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facedb);
		mGridView = (GridView) findViewById(R.id.gridview);
		mGridView.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);

		android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();

		if (supportActionBar != null) {
			supportActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM); //Enable自定义的View
			supportActionBar.setCustomView(R.layout.bar_layout_manger);//设置自定义的布局：actionbar_custom
			supportActionBar.setTitle("管理人库");
		}

		InitData();
		mGridAdapter = new GridAdapter(this);
		mGridView.setAdapter(mGridAdapter);
		mGridView.setMultiChoiceModeListener(this);
		mGridAdapter.notifyDataSetChanged();
	}

	public void InitData(){
		dbManager = new DBManager(getBaseContext());
		faceInfos =  dbManager.selectAllManagers();
		for(int m = 0 ; m<faceInfos.size() ; m++ ){
			byte[] face = faceInfos.get(m).getFacepic();
			Bitmap bitface = BitmapFactory.decodeByteArray(face, 0, face.length);
			mImgIds[m] = bitface;

		}
//		mGridAdapter.notifyDataSetChanged();
	}

	/** Override MultiChoiceModeListener start **/
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {

		View v = LayoutInflater.from(this).inflate(R.layout.actionbar_layout,
				null);
		mActionText = (TextView) v.findViewById(R.id.action_text);

		mActionText.setText(formatString(mGridView.getCheckedItemCount()));

		mode.setCustomView(v);
		getMenuInflater().inflate(R.menu.action_menu, menu);
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
//		menu.getItem(MENU_SELECT_ALL).setEnabled(
//				mGridView.getCheckedItemCount() != mGridView.getCount());
		return true;
	}

	@Override
	public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_delete:

				new SweetAlertDialog(this,SweetAlertDialog.WARNING_TYPE)
						.setTitleText("确认进行删除")
						.setContentText("删除的内容无法恢复，确认进行删除？")
						.setConfirmText("确定")
						.setConfirmClickListener( new SweetAlertDialog.OnSweetClickListener(){
							@Override
							public void onClick(SweetAlertDialog sweetAlertDialog) {
								dbManager.DeleteManagerByMap(mSelectMap);
								sweetAlertDialog.setTitleText("删除成功!")
										.setContentText("您选择的图片已经成功删除")
										.setConfirmText("确定")
										.showCancelButton(false)
										.setCancelClickListener(null)
										.setConfirmClickListener(null)
										.setEdname(false)
										.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
								InitData();
								mGridAdapter.notifyDataSetChanged();
								mode.finish();
							}
						})
						.setCancelText("取消")
						.show();

			break;

		}
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		mGridAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {

		mActionText.setText(formatString(mGridView.getCheckedItemCount()));
		mSelectMap.put(position, checked);
		mode.invalidate();
	}

	/** Override MultiChoiceModeListener end **/

	private String formatString(int count) {
		return String.format("选中%s个", count);
	}

	private class GridAdapter extends BaseAdapter {

		private Context mContext;

		public GridAdapter(Context ctx) {
			mContext = ctx;
		}

		@Override
		public int getCount() {
			
			return faceInfos.size();
		}

		@Override
		public Bitmap getItem(int position) {

			return mImgIds[position];
		}

		@Override
		public long getItemId(int position) {
			
			return position;
		}

		@SuppressWarnings("deprecation")
		@Override

		public View getView(int position, View convertView, ViewGroup parent) {
			GridItem item;
			if (convertView == null) {
				item = new GridItem(mContext);
				item.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
						LayoutParams.FILL_PARENT));
				w = 0 ;
			} else {
				item = (GridItem) convertView;
			}
			if(w<getCount()) {
				item.setImgResId(getItem(position));
				w++;
			}
			item.setChecked(mSelectMap.get(position) == null ? false
					: mSelectMap.get(position));
			return item;
		}
	}

	public void back(View view){
		this.finish();
	}

	public void add(View view){
		new SweetAlertDialog(this)
				.setTitleText("添加管理员")
				.setContentText("您确定要添加管理员吗")
				.setConfirmMissText("确定")
				.setCancelMissText("取消")
				.showConfirmButton(false)
				.showCancelButton(false)
				.setConfirmMissListener(new SweetAlertDialog.OnSweetClickListener(){
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
				.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_OP) {
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
			Intent it = new Intent(ManagerDBActivity.this, RegisterActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("imagePath", file);
			bundle.putByteArray("oldimage",null);
			bundle.putInt("registerclass",1);
			it.putExtras(bundle);
			startActivityForResult(it, REQUEST_CODE_OP);
		}
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

	@Override
	protected void onRestart() {
		super.onRestart();
		InitData();
		mGridAdapter.notifyDataSetChanged();
	}
}