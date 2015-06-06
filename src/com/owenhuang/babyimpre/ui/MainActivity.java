package com.owenhuang.babyimpre.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.ui.base.BaseActivity;
import com.owenhuang.babyimpre.ui.base.CameraView;

public class MainActivity extends BaseActivity {
	
	private CameraView mCameraView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		mCameraView = (CameraView)findViewById(R.id.main_cameraview);
		
		Button takePictureBtn = (Button)findViewById(R.id.main_takepicture);
		takePictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCameraView.takePicture();
			}			
		});
		
		Button showPictureBtn = (Button)findViewById(R.id.main_showpicture);
		showPictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainActivity.this, ImageGridActivity.class);
				startActivity(i);
			}			
		});
	}
}
