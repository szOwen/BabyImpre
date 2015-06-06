package com.owenhuang.babyimpre.image;

import com.owenhuang.babyimpre.util.BILog;
import com.owenhuang.babyimpre.util.CommonUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.util.LruCache;

public class ImageCache {
	private static final String TAG = ImageCache.class.getSimpleName();
	
	private static ImageCache mInstance = null;
	private LruCache<String, Bitmap> mLruCache;
	
	public static ImageCache getInstance() {
		if (null == mInstance) {
			mInstance = new ImageCache();
		}
		return mInstance;
	}
	
	/**
	 * 获取图片
	 * @param key
	 * @return
	 */
	public Bitmap get(String key, String fileName, boolean compress) {
		Bitmap bitmap = mLruCache.get(key);
		if (null == bitmap) {
			String filePath = CommonUtil.getSaveDir() + "/" + fileName;
			BILog.d(TAG, "get: filePath = " + filePath);

			BitmapFactory.Options options = new BitmapFactory.Options();
			if (compress) {
				//计算原始图片的长度和宽度
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(filePath, options);
				options.inSampleSize = computeSampleSize(options, -1, 128*128);
				options.inJustDecodeBounds = false;
			}
			
			//获取合适高宽的图片
			bitmap = BitmapFactory.decodeFile(filePath, options);
			if (null != bitmap) {
				mLruCache.put(key, bitmap);
			}
		}
		return bitmap;
	}
	
	private ImageCache() {
		//获取系统分配给每个应用程序的最大内存，每个应用系统分配32M，然后用它的八分之一作为缓存大小
        int maxMemory = (int) Runtime.getRuntime().maxMemory();    
        int cacheSize = maxMemory / 8;
        BILog.d(TAG, "ImageCache: cacheSize = " + cacheSize);
		mLruCache = new LruCache<String, Bitmap>(cacheSize) {
			//必须重写此方法，来测量Bitmap的大小  
            @Override  
            protected int sizeOf(String key, Bitmap value) {  
                return value.getRowBytes() * value.getHeight();  
            }  
		};
	}
	
	/**
	 * Compute the sample（样本） size as a function of minSideLength and maxNumOfPixels.
	 * 
     * minSideLength is used to specify that minimal width or height of a bitmap.
     * maxNumOfPixels is used to specify the maximal size in pixels that is tolerable（容许的） in terms of（依据） memory usage.
     *
     * The function returns a sample size based on the constraints（限制）.
     * Both size and minSideLength can be passed in as IImage.UNCONSTRAINED,
     * which indicates no care of the corresponding（相应的） constraint.
     * The functions prefers returning a sample size that
     * generates a smaller bitmap, unless minSideLength = IImage.UNCONSTRAINED.
     *
     * Also, the function rounds up the sample size to a power of 2 or multiple
     * of 8 because BitmapFactory only honors sample size this way.
     * For example, BitmapFactory downsamples an image by 2 even though the
     * request is 3. So we round up the sample size to avoid OOM.
     * 
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private int computeSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
	 
	    int roundedSize;
	    if (initialSize <= 8) {
	        roundedSize = 1;
	        while (roundedSize < initialSize) {
	            roundedSize <<= 1;
	        }
	    } else {
	        roundedSize = (initialSize + 7) / 8 * 8;
	    }
	 
	    return roundedSize;
	}
	
	/**
	 * 
	 * @param options
	 * @param minSideLength
	 * @param maxNumOfPixels
	 * @return
	 */
	private int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength, int maxNumOfPixels) {
	    double w = options.outWidth;
	    double h = options.outHeight;
	 
	    int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
	    int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));
	 
	    if (upperBound < lowerBound) {
	        return lowerBound;
	    }
	 
	    if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
	        return 1;
	    } else if (minSideLength == -1) {
	        return lowerBound;
	    } else {
	        return upperBound;
	    }
	}  
}
