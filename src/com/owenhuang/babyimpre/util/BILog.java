package com.owenhuang.babyimpre.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 日志工具，在发布版重会去掉正文
 */
public class BILog
{   
    /**建议使用下面的接口*/  
    public static final String SD_FILE_PATH = "/babyimpre/log";	// log到文件时，文件的目录 
    private static final String LOG_NAME = "/log";		    // log到文件时，文件名

    public static final int LOG_NO = 0;
    public static final int LOG_LOGCAT = 1;
    public static final int LOG_FILE = 2;
    public static final int LOG_BOTH = 3;
    
    private static String TAG = "babyimpre";

    private static int mLogLevel = LOG_NO;

    private static LogHandler mLogHandler = null;

    private static SimpleDateFormat mSdf = null;

    private BILog() {}
    
    public static void init(Context context) {
    	String appVersion;
        try {
        	PackageManager pm = context.getPackageManager();    
	        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
	        appVersion = pi.versionName;
        } catch (Exception e) {
        	appVersion = "0.0.0.0";
        }
        
        String appLastFigure = appVersion.substring(appVersion.length() - 1, appVersion.length());
        
        int appLastFigureTemp = Integer.parseInt(appLastFigure);
        if (appLastFigureTemp % 2 == 0) {
        	mLogLevel = LOG_NO;
        } else {
        	mLogLevel = LOG_BOTH;
        	initFileHandler();
        }
    }
    
    public static void i(String msg) {
    	i(TAG, msg);
    }

    public static void i(String tag, String msg) {

        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            if (tag != null && msg != null) {
                Log.i(tag, msg);
            }
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
            initFileHandler();
            if (tag != null && msg != null) {
                sendHandlerMsg("Info", tag, msg);
            }
        }
    }
    
    public static void d(String msg) {
    	d(TAG, msg);
    }

    public static void d(String tag, String msg) {
        
        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            if (tag != null && msg != null) {
                Log.d(tag, msg);
            }
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
            initFileHandler();
            if (tag != null && msg != null) {
                sendHandlerMsg("Debug", tag, msg);
            }
        }
    }
    
    public static void v(String msg) {
    	v(TAG, msg);
    }

    public static void v(String tag, String msg) {
        
        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            if (tag != null && msg != null) {
                Log.v(tag, msg);
            }
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
            initFileHandler();
            if (tag != null && msg != null) {
                sendHandlerMsg("Verbose", tag, msg);
            }
        }
    }
    
    public static void w(String msg) {
    	w(TAG, msg);
    }

    public static void w(String tag, String msg) {
        
        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            if (tag != null && msg != null) {
                Log.w(tag, msg);
            }
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
            initFileHandler();
            if (tag != null && msg != null) {
                sendHandlerMsg("Warn", tag, msg);
            }
        }
    }
    
    public static void e(String msg) {
    	e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            if (tag != null && msg != null) {
                Log.e(tag, msg);
            }
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
            initFileHandler();
            if (tag != null && msg != null) {
                sendHandlerMsg("Error", tag, msg);
            }
        }
    }
    
    public static void e(Throwable throwable) {
    	e(TAG, throwable);
    }

    public static void e(String tag, Throwable throwable) {
        if (throwable == null) {
            return;
        }

        if (mLogLevel == LOG_LOGCAT || mLogLevel == LOG_BOTH) {
            throwable.printStackTrace();
        }

        if (mLogLevel == LOG_FILE || mLogLevel == LOG_BOTH) {
        	
        	 initFileHandler();
             StackTraceElement[] stacks = new Throwable().getStackTrace();
             for (StackTraceElement stack : stacks) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("class:").append(stack.getClassName())
                         .append(";line:").append(stack.getLineNumber());
                 Log.e(tag, sb.toString());
             }
        }     
    }
    
    /**
     * 打印内存信息，包括：堆大小、内存占用、系统可用内存等
     * @param tag
     * @param context
     */
    public static void logHeapStats(String tag, Context context) {
        ActivityManager.MemoryInfo sysMemInfo = new ActivityManager.MemoryInfo();
        ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryInfo(sysMemInfo);
        
        Debug.MemoryInfo proMemInfo = new Debug.MemoryInfo();
        Debug.getMemoryInfo(proMemInfo);
        
        long heapTotalSize = Debug.getNativeHeapSize();
        long heapAllocatedSize = Debug.getNativeHeapAllocatedSize();
        long heapFreeSize = Debug.getNativeHeapFreeSize();
        
        DecimalFormat df = new DecimalFormat("0.000");
        
        Log.d(tag, "heap_stats " + "heap_size="
            + df.format(heapTotalSize / (1024 * 1024f)) + "M allocated="
            + df.format(heapAllocatedSize / (1024 * 1024f)) + "M free="
            + df.format(heapFreeSize / (1024 * 1024f)) + "M "
            + "memory_stats " + "memory_usage="
            + df.format(proMemInfo.getTotalPss() / 1024f) + "M dalvik_usage="
            + df.format(proMemInfo.dalvikPss / 1024f) + "M native_usage="
            + df.format(proMemInfo.nativePss / 1024f) + "M other_usage="
            + df.format(proMemInfo.otherPss / 1024f) + "M "
            + "system_stats " + "system_available="
            + df.format(sysMemInfo.availMem / (1024 * 1024f)) + "M");
    }
    
    /**
     * 打印当前线程的堆栈信息
     * @param tag
     */
    public static void logStackTrace(String tag) {
        Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();
        StackTraceElement[] ste = ts.get(Thread.currentThread());
        for (StackTraceElement s : ste) {
            Log.d(tag, s.toString());
        }
    }

    private static void initFileHandler() {
        if (mLogHandler == null) {
            mLogHandler = new LogHandler();
        }
    }

    private static void sendHandlerMsg(String level, String tag, String content) {
        if (mSdf == null) {
            mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        }

        StringBuilder buf = new StringBuilder();
        buf.append(mSdf.format(Calendar.getInstance().getTime())).append(" [");
        buf.append("Thread-").append(Thread.currentThread().getId())
                .append("] ");
        buf.append(level.toUpperCase()).append(" ");
        buf.append(tag).append(" : ").append(content);

        Message msg = mLogHandler.obtainMessage();
        msg.obj = buf.toString();
        mLogHandler.sendMessage(msg);
    }

    private static class LogHandler extends Handler {

        private FileOutputStream mLogOutput = null;

        private File mLogFile = null;

        public LogHandler() {
            super();
            createLogFile();
        }

        @Override
        public void handleMessage(Message msg) {
            if (mLogFile == null) {
                return;
            }

            try {
                if (mLogOutput == null) {
                    mLogOutput = new FileOutputStream(mLogFile, true);
                }
            } catch (FileNotFoundException ex) {
                if (!createLogFile()) {
                    return;
                }
            }

            if (mLogOutput != null) {
                String content = (String) msg.obj + "\n\n";
                if (content != null) {
                    byte[] logData = content.getBytes();
                    try {
                        mLogOutput.write(logData, 0, logData.length);
                    } catch (IOException e) {
                        mLogOutput = null;
                    }
                }
            }
        }

        private boolean createLogFile() {
            if (isExternalStorageAvailable()) {
                File dir = getLogFileDirectory();
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                
                File tempFile = new File(dir.getAbsoluteFile() + LOG_NAME + "5.txt");
                if (tempFile.exists()) {
                	for (int i = 1; i <= 5; i ++) {
                		tempFile = new File(dir.getAbsoluteFile() + LOG_NAME + i + ".txt");
                		if (tempFile.exists()) {
                			if (1 == i) {
                				tempFile.delete();
                			} else {
                				tempFile.renameTo(new File(dir.getAbsoluteFile() + LOG_NAME + (i - 1) + ".txt"));
                			}
                		}
                	}
                	mLogFile = new File(dir.getAbsoluteFile() + LOG_NAME + "5.txt");
                } else {
                	for (int i = 1; i <= 5; i ++) {
                		tempFile = new File(dir.getAbsoluteFile() + LOG_NAME + i + ".txt");
                		if (!tempFile.exists()) {
                			mLogFile = new File(dir.getAbsoluteFile() + LOG_NAME + i + ".txt");
                			break;
                		}
                	}
                }

                //mLogFile = new File(dir.getAbsolutePath() + LOG_NAME);
                if (!mLogFile.exists()) {
                    try {
                        mLogFile.createNewFile();
                    } catch (IOException e) {
                        mLogFile = null;
                        return false;
                    }
                }
            } else {
                mLogFile = null;
                return false;
            }
            return true;
        }

    }
    
    /**
     * 检查SD卡是否可用
     * 
     * @return boolean
     */
    private static boolean isExternalStorageAvailable() {
        if (Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())) {
            return true;
        }
        return false;
    }
    
    /**
     * sdcard上的log文件
     * 
     * @return
     */
    private static File getLogFileDirectory() {
        String storageDirectory = Environment.getExternalStorageDirectory()
                .getAbsolutePath() + SD_FILE_PATH;
        File file = new File(storageDirectory);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
