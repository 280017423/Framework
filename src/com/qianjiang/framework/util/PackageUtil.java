package com.qianjiang.framework.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.pdw.gson.JsonObject;
import com.qianjiang.framework.app.QJApplicationBase;

/**
 * 
 * @author Zeng.hh
 * @version 王先佑 2012-12-06 增加getPackageName方法，返回应用程序的包名<br>
 *          2013-03-20 xu.xb <br>
 *          1.修改获取macAddress，若获取WifiInfo对象为空时，返回空字符串<br>
 *          2.增加获取渠道信息的方法<br>
 * 
 */
public class PackageUtil {

	private static final String TAG = "PackageUtil";
	private static final String DEVICE_ID = "Unknow";

	/**
	 * 获取应用程序的包名
	 * 
	 * @return 应用程序的包名
	 */
	public static String getPackageName() {
		return QJApplicationBase.CONTEXT.getPackageName();
	}

	/**
	 * 获取应用程序的版本号
	 * 
	 * @return 版本号
	 * @throws NameNotFoundException
	 *             找不到改版本号的异常信息
	 */
	public static int getVersionCode() throws NameNotFoundException {
		int verCode = QJApplicationBase.CONTEXT.getPackageManager().getPackageInfo(
				QJApplicationBase.CONTEXT.getPackageName(), 0).versionCode;
		return verCode;
	}

	/**
	 * 获取应用程序的外部版本号
	 * 
	 * @return 外部版本号
	 * @throws NameNotFoundException
	 *             找不到信息的异常
	 */
	public static String getVersionName() throws NameNotFoundException {
		String versionName = QJApplicationBase.CONTEXT.getPackageManager().getPackageInfo(
				QJApplicationBase.CONTEXT.getPackageName(), 0).versionName;

		return versionName;
	}

	/**
	 * 获取MAC地址
	 * 
	 * @return 返回MAC地址
	 */
	public static String getLocalMacAddress() {
		WifiManager wifi = (WifiManager) QJApplicationBase.CONTEXT.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		if (info == null) {
			return "";
		}
		return info.getMacAddress();
	}

	/**
	 * 获取 string.xml 文件定义的字符串
	 * 
	 * @param resourceId
	 *            资源id
	 * @return 返回 string.xml 文件定义的字符串
	 */
	public static String getString(int resourceId) {
		Resources res = QJApplicationBase.CONTEXT.getResources();
		return res.getString(resourceId);
	}

	/**
	 * 
	 * @return 获得手机端终端标识
	 */
	public static String getTerminalSign() {
		String tvDevice = null;
		TelephonyManager tm = (TelephonyManager) QJApplicationBase.CONTEXT.getSystemService(Context.TELEPHONY_SERVICE);
		tvDevice = tm.getDeviceId();
		if (tvDevice == null) {
			tvDevice = getLocalMacAddress();
		}

		if (tvDevice == null) {
			tvDevice = DEVICE_ID;
		}

		EvtLog.d(TAG, "唯一终端标识号：" + tvDevice);
		return tvDevice;
	}

	/**
	 * 
	 * @return 获得手机型号
	 */
	public static String getDeviceType() {
		String deviceType = android.os.Build.MODEL;
		return deviceType;
	}

	/**
	 * 
	 * @return 获得手机deviceId
	 */
	public static String getDeviceId() {
		String deviceId = null;
		deviceId = ((TelephonyManager) QJApplicationBase.CONTEXT.getSystemService(Context.TELEPHONY_SERVICE))
				.getDeviceId();
		if (StringUtil.isNullOrEmpty(deviceId)) {
			deviceId = Secure.getString(QJApplicationBase.CONTEXT.getContentResolver(), Secure.ANDROID_ID);
		}
		return deviceId;
	}

	/**
	 * 
	 * @return 获得操作系统版本号
	 */

	public static String getSysVersion() {
		String sysVersion = android.os.Build.VERSION.RELEASE;
		return sysVersion;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项，如果不存在，则返回空字符串
	 * 
	 * @param key
	 *            键名
	 * @return 返回字符串
	 */
	public static String getConfigString(String key) {
		String val = "";
		try {
			ApplicationInfo appInfo = QJApplicationBase.CONTEXT.getPackageManager().getApplicationInfo(
					QJApplicationBase.CONTEXT.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getString(key);
			if (val == null) {
				EvtLog.e(TAG, "please set config value for " + key + " in manifest.xml first");
			}
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		return val;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项
	 * 
	 * @param key
	 *            键名
	 * @return 返回字符串
	 */
	public static int getConfigInt(String key) {
		int val = 0;
		try {
			ApplicationInfo appInfo = QJApplicationBase.CONTEXT.getPackageManager().getApplicationInfo(
					QJApplicationBase.CONTEXT.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getInt(key);
		} catch (NameNotFoundException e) {
			EvtLog.e(TAG, e);
		}
		return val;
	}

	/**
	 * 读取manifest.xml中application标签下的配置项
	 * 
	 * @param key
	 *            键名
	 * @return 返回字符串
	 */
	public static boolean getConfigBoolean(String key) {
		boolean val = false;
		try {
			ApplicationInfo appInfo = QJApplicationBase.CONTEXT.getPackageManager().getApplicationInfo(
					QJApplicationBase.CONTEXT.getPackageName(), PackageManager.GET_META_DATA);
			val = appInfo.metaData.getBoolean(key);
		} catch (NameNotFoundException e) {
			EvtLog.e(TAG, e);
		}
		return val;
	}

	/**
	 * 指定的activity所属的应用，是否是当前手机的顶级
	 * 
	 * @param context
	 *            activity界面或者application
	 * @return 如果是，返回true；否则返回false
	 */
	public static boolean isTopApplication(Context context) {
		if (context == null) {
			return false;
		}

		try {
			String packageName = context.getPackageName();
			ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasksInfo = activityManager.getRunningTasks(1);
			if (tasksInfo.size() > 0) {
				// 应用程序位于堆栈的顶层
				if (packageName.equals(tasksInfo.get(0).topActivity.getPackageName())) {
					return true;
				}
			}
		} catch (Exception e) {
			// 什么都不做
			EvtLog.w(TAG, e);
		}
		return false;
	}

	/**
	 * 判断APP是否已经打开
	 * 
	 * @param context
	 *            activity界面或者application
	 * @return true表示已经打开 false表示没有打开
	 */
	public static boolean isAppOpen(Context context) {
		return isAppOpen(context, "com.qianjiang.pmh");
	}

	/**
	 * 判断指定进程是否已经打开
	 * 
	 * @param context
	 *            activity界面或者application
	 * @param process
	 *            指定进程 ；
	 * @return true表示已经打开 false表示没有打开
	 */
	public static boolean isAppOpen(Context context, String process) {
		ActivityManager mManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> mRunningApp = mManager.getRunningAppProcesses();
		int size = mRunningApp.size();
		for (int i = 0; i < size; i++) {
			if (process.equals(mRunningApp.get(i).processName)) {
				EvtLog.d(TAG, "找到进程");
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param context
	 *            上下文对象
	 * 
	 * @return 渠道号字符串格式为序列化的json
	 */
	public static String getChanelStr(Context context) {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo appinfo;
		String result = "";
		try {
			appinfo = packageManager.getApplicationInfo(getPackageName(), 0);
			EvtLog.d(TAG, "packageName" + getPackageName());
			String chanelStr = getChanelStrFromFile(appinfo.sourceDir);
			EvtLog.d(TAG, "获取的渠道信息为:  " + chanelStr);
			if (!StringUtil.isNullOrEmpty(chanelStr)) {
				int allianceNo = Integer.valueOf(chanelStr.substring(0, chanelStr.indexOf("_")));
				String siteNo = chanelStr.substring(chanelStr.indexOf("_") + 1, chanelStr.lastIndexOf("_"));
				int businessType = Integer.valueOf(chanelStr.substring(chanelStr.lastIndexOf("_") + 1,
						chanelStr.length()));
				JsonObject jsonChannel = new JsonObject();
				jsonChannel.addProperty("CooperationType", 1);
				jsonChannel.addProperty("AllianceNo", allianceNo);
				jsonChannel.addProperty("SiteNo", siteNo);
				jsonChannel.addProperty("BusinessType", businessType);
				result = jsonChannel.toString();
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取渠道号
	 * 
	 * @param context
	 *            上下文对象
	 * @return String 渠道号
	 */
	public static String getChanelNo(Context context) {
		PackageManager packageManager = context.getPackageManager();
		ApplicationInfo appinfo;
		String result = "";
		try {
			appinfo = packageManager.getApplicationInfo(getPackageName(), 0);
			EvtLog.d(TAG, "packageName" + getPackageName());
			result = getChanelStrFromFile(appinfo.sourceDir);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getChanelStrFromFile(String file) {
		String fromStr = "META-INF/paidui_";
		Enumeration<?> entries = null;
		ZipFile zipfile = null;
		String id = "";
		try {
			zipfile = new ZipFile(file);
			entries = zipfile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
				if (entry.getName().startsWith(fromStr)) {
					EvtLog.d(TAG, "渠道号为:  " + entry.getName());
					id = entry.getName().substring(fromStr.length());
					break;
				}
			}
			zipfile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return id;
	}

	/**
	 * 检测是否安装对应的应用程序
	 * 
	 * @param context
	 *            上下文对象
	 * @param packageName
	 *            包名
	 * @return 是否安装对应的应用程序
	 */
	public static boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 获取应用的VersionName
	 * 
	 * @param context
	 *            上下文
	 * @param packageName
	 *            包名
	 * @return 获取应用的VersionName
	 */
	public static String getApkVersion(Context context, String packageName) {
		String version = "0.0.0";
		if (packageName == null || "".equals(packageName)) {
			return version;
		}
		List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			if (packageInfo.packageName.equals(packageName)) {
				return packageInfo.versionName;
			}
		}
		return version;
	}
}
