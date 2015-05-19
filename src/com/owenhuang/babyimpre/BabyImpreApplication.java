package com.owenhuang.babyimpre;

import java.util.Stack;

import com.owenhuang.babyimpre.util.CrashHandler;
import com.owenhuang.babyimpre.util.BILog;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

public class BabyImpreApplication extends Application{
	private static BabyImpreApplication INSTANCE;
	
	private static Stack<Activity> mActivityStack;
	public static final boolean DEBUG = true;
	public  static boolean isAlive=false;
	
	static public BabyImpreApplication getInstance()
	{
		return INSTANCE;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		INSTANCE = this;
		
		// 做一些全局初始化
		init(INSTANCE);
	}
	
	@Override
	public void onTerminate() {}
	
	/**
	 * Activity栈管理：添加activity
	 * @param activity
	 */
	public void addActivity(Activity activity) {
		if (null == mActivityStack) {
			mActivityStack = new Stack<Activity>();
		}
		
		mActivityStack.add(activity);
	}
	
	/**
	 * Activity栈管理：结束activity
	 * @param activity
	 */
	public void finishActivity(Activity activity) {
		if (null != activity) {
			mActivityStack.remove(activity);
			activity.finish();
			activity = null;
		}
	}
	
	/**
	 * Activity栈管理：结束activity
	 */
	public void finishActivity() {
		finishActivity(mActivityStack.lastElement());
	}
	
	/**
	 * Activity栈管理：结束所有activity
	 */	
	public void finishAllActivity() {
		for (int i = 0, size = mActivityStack.size(); i < size; i ++) {
			if (null != mActivityStack.get(i)) {
				mActivityStack.get(i).finish();
			}
		}
		mActivityStack.clear();
	}
	
	private void init(Context context)
	{		
		// 友盟统计发送策略
		MobclickAgent.updateOnlineConfig(context);
		//供统计测试，先让统计开启调试模式，测试完后可以注释掉，勿删，供后续测试使用
		//MobclickAgent.setDebugMode( true );
		//统计数据加密
		AnalyticsConfig.enableEncrypt(true);	
		
		// 初始化崩溃上报工具
		//CrashHandler.getInstance().init(context);
		
		// 初始化日志工具
		BILog.init(context);
	}
}
