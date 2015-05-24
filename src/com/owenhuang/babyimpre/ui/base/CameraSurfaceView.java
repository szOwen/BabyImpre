package com.owenhuang.babyimpre.ui.base;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.util.BILog;
import com.owenhuang.babyimpre.util.CommonUtil;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;

/**
 * Surface对象也有生命周期：SurfaceView出现在屏幕上时，会创建Surface；SurfaceView从屏幕上消失时，Surface随即被销毁。
 * Surface不存在时，必须保证没有任务内容要在它上面绘制
 * @author huangowen
 *
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, AutoFocusCallback {
	private static final String TAG = CameraSurfaceView.class.getSimpleName();
	
	private Context mContext;
	private ImageView mFocusIcon;
	private int mFocusIconWidth, mFocusIconHeight;
	
	//SurfaceHolder是我们与Surface对象联系的纽带
	private SurfaceHolder mSurfaceHolder = null;
	private Camera mCamera = null;
	//ShutterCallback回调方法会在相机捕获图件时调用，但此时，图像数据还未处理完成
	private ShutterCallback mShutterCallback = new ShutterCallback() {
		@Override
		public void onShutter() {
			// TODO Auto-generated method stub
			
		}		
	};
	//此PictureCallback回调方法在原始图像数据可用时调用，通常来说，是在加工处理原始图像数据且没有存储之前
	private PictureCallback mRawPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub
			
		}		
	};
	//此PictureCallback回调方法是在JPEG版本的图像可用时调用
	private PictureCallback mJpegPictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			//Create a filename
			String filePath = CommonUtil.getSaveDir() + "/" + UUID.randomUUID().toString() + ".jpg";
			File pictureFile = new File(filePath);
			//Save the jpeg to disk
			FileOutputStream out = null;
			boolean success = true;
			
			try {
				//out = mContext.openFileOutput(filePath, Context.MODE_PRIVATE);
				pictureFile.createNewFile();
				out = new FileOutputStream(pictureFile);
				out.write(data);
			} catch (Exception e) {
				e.printStackTrace();
				success = false;
			} finally {
				try {
					if (null != out) {
						out.close();
					}
				} catch (Exception e) {
					e.printStackTrace();
					success = false;
				}
			}
		}		
	};

	public CameraSurfaceView(Context context) {
		super(context);
		
		init(context);
	}
    
    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
		
		init(context);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		
		init(context);
    }
    
    /**
     * 拍照
     */
    public void takePicture() {
    	//mCamera.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
    	Camera.Parameters params = mCamera.getParameters(); 
    	List<Camera.Area> focusAreaList = params.getFocusAreas();
    	for (Camera.Area area : focusAreaList) {
        	BILog.d(TAG, "takePicture: area.rect.left = " + area.rect.left + ", area.rect.top = " + area.rect.top + ", area.rect.right = " + area.rect.right + ", area.rect.bottom = " + area.rect.bottom);
    	}
    }
    
    /**
     * 设置对焦区域
     * @param rect
     */
    public void setFocusArea(MotionEvent event) {
		
    	//BILog.d(TAG, "setFocusArea: event.getRawX = " + event.getRawX() + ", event.getRawY = " + event.getRawY());
    	Rect focusRect = calculateTapArea(event.getRawX(), event.getRawY(), 1f);  
    	BILog.d(TAG, "setFocusArea: focusRect.left = " + focusRect.left + ", focusRect.top = " + focusRect.top + ", focusRect.right = " + focusRect.right + ", focusRect.bottom = " + focusRect.bottom);
        Rect meteringRect = calculateTapArea(event.getRawX(), event.getRawY(), 1.5f);  
    	//BILog.d(TAG, "setFocusArea: meteringRect.left = " + meteringRect.left + ", meteringRect.top = " + meteringRect.top + ", meteringRect.right = " + meteringRect.right + ", meteringRect.bottom = " + meteringRect.bottom);
  
        Camera.Parameters params = mCamera.getParameters();  
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);  
        
        int maxNumFocusAreas = params.getMaxNumFocusAreas();
        if (maxNumFocusAreas > 0) {  
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();  
            focusAreas.add(new Camera.Area(focusRect, 1000));
          
            params.setFocusAreas(focusAreas);  
        }  
  
        int maxNumMeteringAreas = params.getMaxNumMeteringAreas();
        if (maxNumMeteringAreas > 0) {  
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();  
            meteringAreas.add(new Camera.Area(meteringRect, 1000));  
              
            params.setMeteringAreas(meteringAreas);
        }  
  
        mCamera.setParameters(params);
		
		//设置自动对焦
		mCamera.autoFocus(this);
		
		//对焦动画
		setFocusIconPos((int)event.getX(), (int)event.getY());
    }
    
    /**
     * 触摸事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
    	
    	if (MotionEvent.ACTION_DOWN == ev.getAction()) {
    		setFocusArea(ev);
    		return true;
    	} else {
    		return false;
    	}
    }

    /**
     * 包含SurfaceView的视图层级结构被放到屏蔽上时调用该方法。这里也是Surface与其客户端进行关联的地方。
     */
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

	/**
	 * Surface首次显示在屏幕上时调用该方法。通过传入的参数，可以知道Surface的像素格式以及它的宽度和高度。该方法内可以通知Surface的客户端，有多大的绘制区域可以使用。
	 */
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		BILog.d(TAG, "surfaceChanged: Enter");
		
		try {
			//开始绘制帧
			mCamera.startPreview();
		} catch (Exception e) {
			e.printStackTrace();
			mCamera.release();
			mCamera = null;
		}
		
		//获取Camera Parameters
		Camera.Parameters params = mCamera.getParameters();
		//设置聚焦模式

		Size previewSize = getBestSupportedSize(params.getSupportedPreviewSizes(), width, height);
		//BILog.d(TAG, "surfaceChanged: previewSize.width = " + previewSize.width + ", previewSize.height = " + previewSize.height);
		params.setPreviewSize(previewSize.width, previewSize.height); 
		
		Size pictureSize = getBestSupportedSize(params.getSupportedPictureSizes(), width, height);
		//BILog.d(TAG, "surfaceChanged: pictureSize.width = " + pictureSize.width + ", pictureSize.height = " + pictureSize.height);
		params.setPictureSize(pictureSize.width, pictureSize.height);
		
		params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
		params.setPictureFormat(PixelFormat.JPEG);
		
		mCamera.setParameters(params);
		
		//设置自动对焦
		mCamera.autoFocus(this);
	}

	/**
	 * SurfaceView从屏幕上移除时，Surface也随即被销毁。通过该方法，可以通知Surface的客户端停止使用Surface。
	 */
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		BILog.d(TAG, "surfaceDestroyed: Enter");
		
		//停止绘制帧
		mCamera.stopPreview();
		
		if (null != mCamera) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (success) {
			
		}
	}
	
	/**
	 * 初始化
	 */
	private void init(Context context) {
		mContext = context;
		
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	/**
	 * 设置对焦图标位置
	 * @param x
	 * @param y
	 */
	private void setFocusIconPos(int x, int y) {
		BILog.d(TAG, "setFocusIconPos: x = " + x + ", y = " + y);
		
    	//创建对焦图标并绑定到视图中
    	if (null == mFocusIcon) {
			ViewGroup viewGroup = (ViewGroup)getParent();
			mFocusIcon = new ImageView(mContext);
			mFocusIcon.setImageResource(R.drawable.ico_camera_focus);
			
			//计算图片宽高
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);  
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);  
			mFocusIcon.measure(w, h);  
			mFocusIconHeight = mFocusIcon.getMeasuredHeight();  
			mFocusIconWidth = mFocusIcon.getMeasuredWidth(); 
			
			AbsoluteLayout.LayoutParams focusIconParams = new AbsoluteLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, x - mFocusIconWidth / 2, y - mFocusIconHeight / 2);
			viewGroup.addView(mFocusIcon, focusIconParams);
    	} else {
    		AbsoluteLayout.LayoutParams focusIconParams = new AbsoluteLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, x - mFocusIconWidth / 2, y - mFocusIconHeight / 2);
    		mFocusIcon.setLayoutParams(focusIconParams);
    	}
	}
	
	/**
	 * 获取最佳的预览界面大小
	 * @param sizes
	 * @param width
	 * @param height
	 * @return
	 */
	private Size getBestSupportedSize(List<Size> sizes, int width, int height) {
		Size bestSize = sizes.get(0);
		int largestArea = bestSize.width * bestSize.height;
		for (Size s : sizes) {
			int area = s.width * s.height;
			if (area > largestArea) {
				bestSize = s;
				largestArea = area;
			}
		}
		
		return bestSize;
	}
	
	/** 
     * Convert touch position x:y to {@link Camera.Area} position -1000:-1000 to 1000:1000. 
     */  
    private Rect calculateTapArea(float x, float y, float coefficient) {  
        float focusAreaSize = 300;  
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();  
  
        int centerX = (int) (x / getResolution().width * 2000 - 1000);  
        int centerY = (int) (y / getResolution().height * 2000 - 1000);  
  
        int left = clamp(centerX - areaSize / 2, -1000, 1000);  
        int right = clamp(left + areaSize, -1000, 1000);  
        int top = clamp(centerY - areaSize / 2, -1000, 1000);  
        int bottom = clamp(top + areaSize, -1000, 1000);  
  
        return new Rect(left, top, right, bottom);  
    }
    
    private int clamp(int x, int min, int max) {  
        if (x > max) {  
            return max;  
        }  
        if (x < min) {  
            return min;  
        }  
        return x;  
    } 
    
    private Camera.Size getResolution() {  
        Camera.Parameters params = mCamera.getParameters();   
        Camera.Size previewSize = params.getPreviewSize();  
        //BILog.d(TAG, "getResolution: previewSize.width = " + previewSize.width + ", previewSize.height = " + previewSize.height);
        return previewSize;  
    } 
}
