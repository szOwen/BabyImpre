package com.owenhuang.babyimpre.ui.base;

import com.owenhuang.babyimpre.util.BILog;

import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, AutoFocusCallback {
	private static final String TAG = CameraSurfaceView.class.getSimpleName();
	
	private SurfaceHolder mSurfaceHolder = null;
	private Camera mCamera = null;
	//快门按下的回调，在这里我们可以设置类似播放“咔嚓”声之类的操作。默认的就是咔嚓。
	private ShutterCallback mShutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			
		}		
	};
	//拍摄的未压缩原始数据的回调,可以为null
	private PictureCallback mRawPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}		
	};
	//拍摄的未压缩JPEG数据的回调,可以为null
	private PictureCallback mJpegPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}		
	};

	public CameraSurfaceView(Context context) {
		super(context);
		
		init();
	}
    
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
		
		init();
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		
		init();
    }
    
    /**
     * 拍照
     */
    public void takePicture() {
    	mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
    }

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		BILog.d(TAG, "surfaceCreated: Enter");
		if (null == mCamera) {
			//开启相机
			mCamera = Camera.open();
			
			try {
				mCamera.setPreviewDisplay(mSurfaceHolder);
				mCamera.setDisplayOrientation(90);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		BILog.d(TAG, "surfaceChanged: Enter");
		
		//开始预览
		mCamera.startPreview();
		
		//获取Camera Parameters
		Camera.Parameters params = mCamera.getParameters();
		//设置聚焦模式

		//params.setPictureSize(960, 720);  
		params.setPreviewSize(480, 320); 
		params.setFocusMode(Camera.Parameters.FLASH_MODE_AUTO);
		params.setPictureFormat(PixelFormat.JPEG);
		mCamera.setParameters(params);
		
		//设置自动对焦
		mCamera.autoFocus(this);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		BILog.d(TAG, "surfaceDestroyed: Enter");
		
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}
	
	/**
	 * 初始化
	 */
	private void init() {
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			
		}
	}
}
