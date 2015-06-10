package jp.co.cyberagent.android.gpuimage.util;

import java.util.ArrayList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.util.CameraHelper.CameraInfo2;
import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;

public class CameraLoader implements AutoFocusCallback {
	private Context mContext;
    private int mCurrentCameraId = 0;
    private Camera mCameraInstance;
    private GPUImage mGPUImage;
    private CameraHelper mCameraHelper;
    private AutoFocusCallback mAutoFocusCallback = null;
    
    public CameraLoader(Context context, GPUImage gpuImage, CameraHelper cameraHelper) {
    	mContext = context;
    	mGPUImage = gpuImage;
    	mCameraHelper = cameraHelper;
    }

    /**
     * 相机可见
     */
    public void onResume() {
        setUpCamera(mCurrentCameraId);
    }

    /**
     * 相机不可见
     */
    public void onPause() {
        releaseCamera();
    }

    /**
     * 切换照相机
     */
    public void switchCamera() {
        releaseCamera();
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        setUpCamera(mCurrentCameraId);
    }
    
    /**
     * 开始预览
     */
    public void startPreview() {
    	if (null != mCameraInstance) {
    		mCameraInstance.startPreview();
    	}
    }
    
    /**
     * 获取预览大小
     * @return
     */
    public Camera.Size getPreviewSize() {
    	if (null != mCameraInstance) {
    		Camera.Parameters params = mCameraInstance.getParameters(); 
    		return params.getPictureSize();
    	}
    	return null;
    }
    
    /**
     * 设置对焦模式
     * @param focusMode
     */
    public void setFocusMode(String focusMode) {
    	if (null != mCameraInstance) {
    		Camera.Parameters params = mCameraInstance.getParameters();  
            params.setFocusMode(focusMode);  
    	}
    }
    
    /**
     * 设置自动对焦回调
     * @param autoFocusCallback
     */
    public void setAutoFocusCallback(AutoFocusCallback autoFocusCallback) {
    	mAutoFocusCallback = autoFocusCallback;
    }
    
    /**
     * 设置对焦区域
     * @param focusArea
     * @param meteringArea
     */
    public void setFocusArea(Camera.Area focusArea, Camera.Area meteringArea) {
    	if (null == mCameraInstance) {
    		return;
    	}
    	
    	Camera.Parameters params = mCameraInstance.getParameters();
    	
    	int maxNumFocusAreas = params.getMaxNumFocusAreas();
        if (maxNumFocusAreas > 0) {  
            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();  
            focusAreas.add(focusArea);
          
            params.setFocusAreas(focusAreas);  
        }  
  
        int maxNumMeteringAreas = params.getMaxNumMeteringAreas();
        if (maxNumMeteringAreas > 0) {  
            List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();  
            meteringAreas.add(meteringArea);  
              
            params.setMeteringAreas(meteringAreas);
        }  
  
        mCameraInstance.setParameters(params);
		
		//设置自动对焦
        mCameraInstance.autoFocus(this);
    }
    
    /**
     * 拍照
     * @param shutter
     * @param raw
     * @param jpeg
     */
    public void takePicture(ShutterCallback shutter, PictureCallback raw, PictureCallback jpeg) {
    	if (mCameraInstance != null)
    	{
    		mCameraInstance.takePicture(shutter, raw, jpeg);    		
    	}
    }
    
    /**
     * 获取摄像头的方向
     * @return
     */
    public int getCameraDisplayOrientation()
    {
    	return mCameraHelper.getCameraDisplayOrientation((Activity)mContext, mCurrentCameraId);
    }
    
    /**
     * 设置摄像头旋转方向
     * @param nRotation
     */
    public void setRotation(int nRotation)
    {
    	if(mCameraInstance != null)
    	{
    		Parameters parameters = mCameraInstance.getParameters();
    		parameters.setRotation(nRotation);
    		mCameraInstance.setParameters(parameters);
    	}
    }
    
    /**
     * 是否为前置摄像头
     * @return
     */
	public boolean isFrontCamera()
	{
		if(mCameraInstance != null)
    	{
			// 获取相机信息
	        CameraInfo2 cameraInfo = new CameraInfo2();
	        mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
	        if(cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT)
	        {
	        	return true;
	        }
    	}
		return false;
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		if (null != mAutoFocusCallback) {
			mAutoFocusCallback.onAutoFocus(success, camera);
		}
	}

    private void setUpCamera(final int id) {
        mCameraInstance = getCameraInstance(id);
        Parameters parameters = mCameraInstance.getParameters();
        // adjust by getting supportedPreviewSizes and then choosing
        // the best one for screen size (best fill screen)
        Size previewSize = getBestSupportedSize(parameters.getSupportedPreviewSizes());
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        mCameraInstance.setParameters(parameters);

        int orientation = mCameraHelper.getCameraDisplayOrientation((Activity)mContext, mCurrentCameraId);
        CameraInfo2 cameraInfo = new CameraInfo2();
        mCameraHelper.getCameraInfo(mCurrentCameraId, cameraInfo);
        boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT ? true : false;
        mGPUImage.setUpCamera(mCameraInstance, orientation, flipHorizontal, false);
    }

    /** 
     * A safe way to get an instance of the Camera object. 
     **/
    private Camera getCameraInstance(final int id) {
        Camera c = null;
        try {
            c = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    private void releaseCamera() {
        mCameraInstance.setPreviewCallback(null);
        mCameraInstance.release();
        mCameraInstance = null;
    }
	
	/**
	 * 获取最佳的预览界面大小
	 * @param sizes
	 * @return
	 */
	private Size getBestSupportedSize(List<Size> sizes) {
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
}
