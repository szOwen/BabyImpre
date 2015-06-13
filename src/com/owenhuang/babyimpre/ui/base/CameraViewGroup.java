package com.owenhuang.babyimpre.ui.base;

import com.owehuang.babyimpre.R;
import com.owenhuang.babyimpre.BabyImpreApplication;
import com.owenhuang.babyimpre.util.BILog;
import com.owenhuang.babyimpre.util.CommonUtil;
import com.owenhuang.babyimpre.util.SharedPreferencesUtil;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.util.CameraHelper;
import jp.co.cyberagent.android.gpuimage.util.CameraLoader;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class CameraViewGroup extends ViewGroup {
	private static final String TAG = CameraViewGroup.class.getSimpleName();
	
	private static final int VIEW_ID_SURFACEVIEW = 1;
	private static final int VIEW_ID_FOCUSICON = 2;
	
	private static final int FOCUS_AREA_SIZE = 300;
	
	private Context mContext;
	
	private GLSurfaceView mGLSurfaceView;
	private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private CameraLoader mCameraLoader;
    private GPUImageFilter mFilter;
    private int mDeviceRotation = 0;
    
    private OrientationEventListener mOrientationEventListener;

	private ImageView mFocusIcon;
	private Animation mFocusIconAni = null;
	private int mFocusIconX = 0, mFocusIconY = 0;
	private int mFocusIconWidth, mFocusIconHeight;
	
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
			int pictureIndex = SharedPreferencesUtil.getPictureIndex(BabyImpreApplication.getInstance());
			SharedPreferencesUtil.setPictureIndex(BabyImpreApplication.getInstance(), ++pictureIndex);
			/*String filePath = CommonUtil.getSaveDir() + "/BI" + pictureIndex + ".jpg";
			final File pictureFile = new File(filePath);

			try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
            	BILog.e(TAG, "mJpegPictureCallback: File not found: " + e.getMessage());
            } catch (IOException e) {
            	BILog.e(TAG, "mJpegPictureCallback: Error accessing file: " + e.getMessage());
            }*/
            
            mCameraLoader.startPreview();

            final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);	// RENDERMODE_WHEN_DIRTY-有改变时重绘-需调用requestRender()
            mGPUImage.saveToPictures(bitmap, CommonUtil.COMMONUTIL_SAVEDIR_NAME, "BI" + pictureIndex + ".jpg", new OnPictureSavedListener() {
                @Override
                public void onPictureSaved(final Uri uri) {
                    mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);	// RENDERMODE_CONTINUOUSLY -自动连续重绘（默认
                    bitmap.recycle();
                }
            });
		}		
	};

	/**
	 * 构造函数
	 * @param context
	 */
	public CameraViewGroup(Context context) {
		this(context, null);
	}    
    public CameraViewGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public CameraViewGroup(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        
        mContext = context;
        
        // 监听手机方向
        startOrientationChangeListener();
        
        //创建SurfaceView  
        mGLSurfaceView = new GLSurfaceView(mContext);
        mGLSurfaceView.setId(VIEW_ID_SURFACEVIEW);
        MarginLayoutParams surfaceViewParams = new MarginLayoutParams(MarginLayoutParams.MATCH_PARENT, MarginLayoutParams.MATCH_PARENT);
		addView(mGLSurfaceView, surfaceViewParams);
        
        mGPUImage = new GPUImage(mContext);
        mGPUImage.setGLSurfaceView(mGLSurfaceView);
        mCameraHelper = new CameraHelper(mContext);
        mCameraLoader = new CameraLoader(mContext, mGPUImage, mCameraHelper);
        mCameraLoader.setAutoFocusCallback(new AutoFocusCallback() {
			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				mFocusIcon.setVisibility(View.INVISIBLE);
			}        	
        });
		
		//创建FocusIcon
		mFocusIcon = new ImageView(mContext);
		mFocusIcon.setId(VIEW_ID_FOCUSICON);
		mFocusIcon.setImageResource(R.drawable.ico_camera_focus);
		mFocusIcon.setVisibility(View.GONE);
		mFocusIcon.setTag(false);
		int focusIconWidth = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);		//计算图片宽
		int focusIconHeight = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);	//计算图片高
		mFocusIcon.measure(focusIconWidth, focusIconHeight);  
		mFocusIconHeight = mFocusIcon.getMeasuredHeight();  
		mFocusIconWidth = mFocusIcon.getMeasuredWidth(); 		
		MarginLayoutParams focusIconParams = new MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		addView(mFocusIcon, focusIconParams);
		mFocusIconAni = AnimationUtils.loadAnimation(mContext, R.anim.camerasurfaceview_ani_focus);
		mFocusIconAni.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				BILog.d(TAG, "onAnimationStart: Enter");
			}
			@Override
			public void onAnimationEnd(Animation animation) {
				BILog.d(TAG, "onAnimationEnd: Enter");
				mFocusIcon.setImageResource(R.drawable.ico_camera_focused);
			}
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				BILog.d(TAG, "onAnimationRepeat: Enter");				
			}					
		});
    }
    
    /**
     * 视图展示时调用
     */
    public void onRusume() {
    	mCameraLoader.onResume();
    	mOrientationEventListener.enable();
    }
    
    /**
     * 视图隐藏时调用
     */
    public void onPause() {
    	mCameraLoader.onPause();
    	mOrientationEventListener.disable();
    }
    
    /**
     * 拍照
     */
    public void takePicture() {
    	// 设置旋转角度
		if(mCameraLoader.isFrontCamera())
		{
			if(mDeviceRotation == 90)
			{
				mDeviceRotation = 270;
			}
			else if(mDeviceRotation == 270)
			{
				mDeviceRotation = 90;
			}
			mCameraLoader.setRotation((mCameraLoader.getCameraDisplayOrientation() + mDeviceRotation)%360);
		}
		else
		{
			mCameraLoader.setRotation((mCameraLoader.getCameraDisplayOrientation() + mDeviceRotation)%360);
		}
		
		//照相
    	mCameraLoader.takePicture(mShutterCallback, mRawPictureCallback, mJpegPictureCallback);
    }
    
    /**
     * 设置对焦区域
     * @param rect
     */
    public void setFocusArea(MotionEvent event) {
		
    	Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f, mGLSurfaceView.getWidth(), mGLSurfaceView.getHeight(), mCameraLoader.getPreviewSize().width, mCameraLoader.getPreviewSize().height);  
    	Rect meteringRect = calculateTapArea(event.getX(), event.getY(), 1.5f, mGLSurfaceView.getWidth(), mGLSurfaceView.getHeight(), mCameraLoader.getPreviewSize().width, mCameraLoader.getPreviewSize().height);  
    	
        mCameraLoader.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);  
        mCameraLoader.setFocusArea(new Camera.Area(focusRect, 1000), new Camera.Area(meteringRect, 1000));
		
		//对焦动画
		setFocusIconPos((int)event.getX(), (int)event.getY());
    }

    /**
     * 切换过滤
     * @param filter
     */
    public void switchFilterTo(final GPUImageFilter filter) {
        if (mFilter == null || (filter != null && !mFilter.getClass().equals(filter.getClass()))) {
            mFilter = filter;
            mGPUImage.setFilter(mFilter);
        }
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
	 * 计算所有ChildView的宽度和高度 然后根据ChildView的计算结果，设置自己的宽和高 
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		BILog.d(TAG, "onMeasure: widthMeasureSpec = " + widthMeasureSpec + ", heightMeasureSpec = " + heightMeasureSpec);

		//获得此ViewGroup上级容器为其推荐的宽和高，以及计算模式 
		/*int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);*/
		int width = MeasureSpec.getSize(widthMeasureSpec);
		int height = MeasureSpec.getSize(heightMeasureSpec);
		
		//计算出所有的childView的宽和高
		measureChildren(widthMeasureSpec, heightMeasureSpec);
		
		setMeasuredDimension(width, height);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		BILog.d(TAG, "onLayout: changed = " + changed + ", l = " + l + ", t = " + t + ", r = " + r + ", b = " + b);
		
		int childCount = getChildCount();
		int childWidth = 0, childHeight = 0;
		MarginLayoutParams childParams = null;
		
		//遍历所有childView根据其宽和高，以及margin进行布局
		for (int i = 0; i < childCount; i ++) {
			View childView = getChildAt(i);
			
			childWidth = childView.getMeasuredWidth();
			childHeight = childView.getMeasuredHeight();
			childParams = (MarginLayoutParams)childView.getLayoutParams();
			
			int childLeft = 0, childTop = 0, childRight = 0, childBottom = 0;
			
			if (childView.getId() == VIEW_ID_FOCUSICON) {
				childLeft = mFocusIconX;
				childTop = mFocusIconY;
			} else {
				childLeft = childParams.leftMargin;
				childTop = childParams.topMargin;
			}
			childRight = childLeft + childWidth;
			childBottom = childTop + childHeight;
			
			childView.layout(childLeft, childTop, childRight, childBottom);
			
			if (childView.getId() == VIEW_ID_FOCUSICON && ((Boolean)childView.getTag()).booleanValue()) {
				childView.setTag(false);
				mFocusIcon.setImageResource(R.drawable.ico_camera_focus);
				childView.setVisibility(View.VISIBLE);
				childView.startAnimation(mFocusIconAni);
			}
		}
	}
	
	// 监听手机旋转角度
	private void startOrientationChangeListener() 
	{  
		mOrientationEventListener = new OrientationEventListener(mContext) {  
			@Override  
			public void onOrientationChanged(int rotation) { 
				BILog.d(TAG, "onOrientationChanged: rotation = " + rotation);
				if((rotation>=0 && rotation <= 45) || (rotation >= 315 && rotation <= 360))
				{
					mDeviceRotation = 0;
				}
				else if(rotation >= 45 && rotation <= 135)
				{
					mDeviceRotation = 90;					
				}
				else if(rotation >=135 && rotation <=225)
				{
					mDeviceRotation = 180;
				}
				else if(rotation >= 225 && rotation <= 315)
				{
					mDeviceRotation = 270;
				}
			}  
		};
	}
	
	/**
	 * 设置对焦图标位置
	 * @param x
	 * @param y
	 */
	private void setFocusIconPos(int x, int y) {
		BILog.d(TAG, "setFocusIconPos: x = " + x + ", y = " + y);
		
		mFocusIconX = x - mFocusIconWidth / 2;
		mFocusIconY = y - mFocusIconHeight / 2;
		mFocusIcon.setTag(true);
		
		//更新布局
		requestLayout();
	}
    
	/**
	 * 计算对焦区域 
	 * @param x 点击屏幕的X坐标
	 * @param y 点击屏蔽的Y坐标
	 * @param coefficient 对焦区域比例
	 * @param viewWidth 预览显示视图宽度
	 * @param viewHeight 预览显示视图高度
	 * @param previewWidth 相机预览视图宽度
	 * @param previewHeight 相机预览视图调试
	 * @return
	 */
    private Rect calculateTapArea(float x, float y, float coefficient, int viewWidth, int viewHeight, int previewWidth, int previewHeight) {  
    	int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue(); 
    	
    	// 三个坐标系：
    	// 1、显示视图坐标系
    	// 2、预览视图映射到显示视图的坐标系
    	// 3、对焦坐标系（对焦坐标的范围从 -1000:-1000 到 1000:1000）
    	
    	// 修正坐标点：选择显示视图(GLSurfaceView)与相机预览视图(Preview)比值最大的边作为新的边长，然后按相机预览视图宽高的比例算出新的宽（高）
    	int newViewWidth, newViewHeight;
    	if(viewHeight/(previewWidth*1.0f) > viewWidth/(previewHeight*1.0f)) // 这块没有错的，previewWidth, previewHeight是反的
    	{
    		newViewHeight = viewHeight;
    		newViewWidth = (int) (viewHeight*(previewHeight/(previewWidth*1.0f)) + 0.5);
    	}
    	else
    	{
    		newViewWidth = viewWidth;
    		newViewHeight = (int) (viewWidth*(previewWidth/(previewHeight*1.0f)) + 0.5);
    	}
    	
    	// 把x,y（x,y还属于显示视图坐标系下的坐标）映射到新的坐标体系中（由2000X2000确定的）
    	// (x-oldCenterX)及(y-oldCenterY)为显示视图坐标系下的，这里取它们距离原点的长度，这个长度和在(newViewWidth,newViewHeight)坐标系下的长度是相等的
    	// 最后计算出在(2000X2000)坐标系下的位置
    	int oldCenterX = viewWidth / 2;
    	int oldCenterY = viewHeight / 2;  	
    	int newX = (int) ((x-oldCenterX)/(newViewWidth/2.0)*1000);
    	int newY = (int) ((y-oldCenterY)/(newViewHeight/2.0)*1000);
    	
    	// 顺时针旋转90度
    	int tempX, tempY;
    	tempX = newY;
    	tempY = -newX;
		newX = tempX;
		newY = tempY;
    	
		// 取焦点框
    	int left = clamp(newX - areaSize / 2, -1000, 1000);  
    	int right = clamp(left + areaSize, -1000, 1000);  
    	int top = clamp(newY - areaSize / 2, -1000, 1000);  
    	int bottom = clamp(top + areaSize, -1000, 1000);  
    	return new Rect(left, top, right, bottom);  
	}  
    
    private int clamp(int nValue, int nMin, int nMax)
    {
    	if(nValue < nMin)
    	{
    		return nMin;
    	}
    	if(nValue > nMax)
    	{
    		return nMax;
    	}
    	return nValue;
    }
}
