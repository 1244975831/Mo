package mo.zucc.edu.cn.face;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import mo.zucc.edu.cn.face.DB.DBManager;
import mo.zucc.edu.cn.face.item.FaceInfo;
import mo.zucc.edu.cn.face.item.GridItem;

public class FaceDBActivity extends AppCompatActivity implements MultiChoiceModeListener {

	private GridView mGridView;
	private GridAdapter mGridAdapter;
	private TextView mActionText;
	private static final int MENU_SELECT_ALL = 0;
	private static final int MENU_UNSELECT_ALL = MENU_SELECT_ALL + 1;
	private Map<Integer, Boolean> mSelectMap = new HashMap<Integer, Boolean>();
	private DBManager dbManager;
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
			supportActionBar.setCustomView(R.layout.bar_layout);//设置自定义的布局：actionbar_custom
			supportActionBar.setTitle("照片库");
		}

		InitData();
		mGridAdapter = new GridAdapter(this);
		mGridView.setAdapter(mGridAdapter);
		mGridView.setMultiChoiceModeListener(this);

	}

	public void InitData(){
		dbManager = new DBManager(getBaseContext());
		faceInfos =  dbManager.selectAllFaces();
		for(int m = 0 ; m<faceInfos.size() ; m++ ){
			byte[] face = faceInfos.get(m).getFacepic();
			Bitmap bitface = BitmapFactory.decodeByteArray(face, 0, face.length);
			mImgIds[m] = bitface;

		}

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
								dbManager.DeleteFaceByMap(mSelectMap);
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

//			for (int i = 0; i < mGridView.getCount(); i++) {
//				mGridView.setItemChecked(i, true);
//				mSelectMap.put(i, true);
//			}
			break;
//		case R.id.menu_unselect:
//			for (int i = 0; i < mGridView.getCount(); i++) {
//				mGridView.setItemChecked(i, false);
//				mSelectMap.clear();
//			}
//			break;
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

}