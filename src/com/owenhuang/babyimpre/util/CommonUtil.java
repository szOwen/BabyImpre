package com.owenhuang.babyimpre.util;

import java.io.File;

import android.os.Environment;

public class CommonUtil {
	public static final String COMMONUTIL_SAVEDIR_NAME = "BabyImpre";
	
	/**
	 * 获取图片保存路径
	 * @return
	 */
	public static String getSaveDir() {
		File pictureDirFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		String saveDir = pictureDirFile.getPath() + "/" + COMMONUTIL_SAVEDIR_NAME;
		File babyImpreDirFile = new File(saveDir);
		if (!babyImpreDirFile.exists()) {
			babyImpreDirFile.mkdirs();
		}
		return babyImpreDirFile.getPath();
	}
}
