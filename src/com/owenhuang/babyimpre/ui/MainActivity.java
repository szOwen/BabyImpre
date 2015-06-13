package com.owenhuang.babyimpre.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.ui.base.BaseActivity;
import com.owenhuang.babyimpre.ui.base.CameraViewGroup;
import com.owenhuang.tcptransfer.download.DownloadMgr;
import com.owenhuang.tcptransfer.upload.UploadMgr;

public class MainActivity extends BaseActivity {
	private CameraViewGroup mCameraView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mCameraView = (CameraViewGroup)findViewById(R.id.main_cameraview);
		
		Button takePictureBtn = (Button)findViewById(R.id.main_takepicture);
		takePictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//mCameraView.takePicture();
				DownloadMgr.getInstance().init(MainActivity.this);
			}			
		});
		
		Button showPictureBtn = (Button)findViewById(R.id.main_showpicture);
		showPictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				/*Intent i = new Intent(MainActivity.this, ImageGridActivity.class);
				startActivity(i);*/
				UploadMgr.getInstance().init(MainActivity.this);
			}			
		});
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mCameraView.onRusume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mCameraView.onPause();
	}
}
