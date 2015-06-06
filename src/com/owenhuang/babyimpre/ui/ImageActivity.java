package com.owenhuang.babyimpre.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.image.ImageCache;
import com.owenhuang.babyimpre.ui.base.BaseActivity;

public class ImageActivity extends BaseActivity {
	
	public static final String INTENT_KEY_FILENAME = "Intent_Key_FileName";
	
	private String mFileName;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_image);
		
		Intent i = getIntent();
		mFileName = i.getStringExtra(INTENT_KEY_FILENAME);
		
		ImageView image = (ImageView)findViewById(R.id.image_image);
		image.setImageBitmap(ImageCache.getInstance().get(mFileName, mFileName, false));
	}
}
