package com.owenhuang.babyimpre.ui;

import java.io.File;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.image.ImageCache;
import com.owenhuang.babyimpre.ui.base.BaseActivity;
import com.owenhuang.babyimpre.util.BILog;
import com.owenhuang.babyimpre.util.CommonUtil;

public class ImageGridActivity extends BaseActivity {
	private static final String TAG = ImageGridActivity.class.getSimpleName();
	
	/**
	 * GridView适配器
	 * @author XlOwen
	 *
	 */
	public class GridAdapter extends BaseAdapter {
		
		private String[] mPictureFileNameList = new String[0];
		
		public GridAdapter() {
			File pictureDir = new File(CommonUtil.getSaveDir());
			if (null != pictureDir && pictureDir.isDirectory()) {
				mPictureFileNameList = pictureDir.list();
				BILog.d(TAG, "GridAdapter: mPictureFileNameList.length = " + mPictureFileNameList.length);
			}
		}

		@Override
		public int getCount() {
			return mPictureFileNameList.length;
		}

		@Override
		public Object getItem(int position) {
			return mPictureFileNameList[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;
			if (null == convertView) {
				convertView = LayoutInflater.from(ImageGridActivity.this).inflate(R.layout.imagegrid_item, null);
				viewHolder = new ViewHolder();
				viewHolder.image = (ImageView)convertView.findViewById(R.id.imagegriditem_image);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder)convertView.getTag();
			}
			
			String fileName = (String)getItem(position);
			Bitmap image = ImageCache.getInstance().get(fileName + "_c", fileName, true);
			BILog.d(TAG, "getView: image.width = " + image.getWidth() + ", image.height = " + image.getHeight());
			viewHolder.image.setImageBitmap(image);
			
			return convertView;
		}
	    
	    class ViewHolder{
	        public ImageView image;
	    }
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_imagegrid);
		
		GridView gridView = (GridView)findViewById(R.id.imagegrid_gridview);
		gridView.setAdapter(new GridAdapter());	
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent i = new Intent(ImageGridActivity.this, ImageActivity.class);
				i.putExtra(ImageActivity.INTENT_KEY_FILENAME, (String)parent.getItemAtPosition(position));
				startActivity(i);
			}			
		});
	}
}
