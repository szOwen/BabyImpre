package com.owenhuang.babyimpre.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesUtil {
	
	private static final String NAME_PICTURE_INFO = "PictureInfo";
	private static final String KEY_PICTURE_INDEX = "PictureIndex";
	
	/**
	 * 图片序号
	 */
	static public int getPictureIndex(Context context)
	{
		SharedPreferences preferences;
		preferences = context.getSharedPreferences(NAME_PICTURE_INFO, 0);
		int index = preferences.getInt(KEY_PICTURE_INDEX, 0);
		return index;
	}	
	static public void setPictureIndex(Context context, int index)
	{
		SharedPreferences preferences;
		SharedPreferences.Editor editor;
		preferences = context.getSharedPreferences(NAME_PICTURE_INFO, 0);
		editor = preferences.edit();
		editor.putInt(KEY_PICTURE_INDEX, index);
		editor.commit();
	}
}
