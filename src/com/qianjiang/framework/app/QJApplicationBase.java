package com.qianjiang.framework.app;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.qianjiang.framework.model.HttpCacheConfigModel;
import com.qianjiang.framework.util.CrashHandler;
import com.qianjiang.framework.util.EvtLog;
import com.qianjiang.framework.util.PackageUtil;
import com.qianjiang.framework.util.StringUtil;

/**
 * 全局应用程序
 * 
 * @author cui.yp
 * @version <br>
 *          2013-04-18，tan.xx，HTTP Header增加移动终端信息方法 2013-05-11，tan.xx，增加设置
 * 
 */
public abstract class QJApplicationBase extends Application {

	// http header KEY
	public static final String KEY_APPSIGN = "AppSign";

	public static final String KEY_APPVERSION = "AppVersion";
	public static final String KEY_DEVICETYPE = "DeviceType";
	public static final String KEY_PACKAGE = "Package";
	public static final String KEY_CLIENT_INFO = "Paidui-Header";
	public static final String KEY_DEVICE_ID = "DeviceId";
	public static final String KEY_CLIENT_TOKEN = "ClientToken";
	public static final String CONNECT_EQUAL_FLAG = "=";
	public static final String CONNECT_END_FLAG = ";";

	// 物理尺寸
	public static double SCRRENSIZE = 0d;

	public static Context CONTEXT;
	public static double APPCOORDERLATITUDE;
	public static double APPCOORDERLONGITUDE;
	public static String APPCOORDERADDRESS = "";

	// 应用标识
	protected String APP_SIGN;
	// 设备类型编号
	protected int CLIENT_TYPE = -1;
	// 应用的版本号，带上内部版本号
	protected String APP_VERSION;
	// 设备类型
	protected String DEVICE_TYPE;

	private Map<String, HttpCacheConfigModel> HTTP_CACHE_MAP = new HashMap<String, HttpCacheConfigModel>();
	private static final String TAG = "PDWApplicationBase";
	private static final int IS_PAD_FLAG = 7;
	private static QJApplicationBase instance;

	@Override
	public void onCreate() {
		super.onCreate();
		CONTEXT = getApplicationContext();
		instance = this;
		setAppSign();
		String crashPath = Environment.getExternalStorageDirectory().getPath()
				+ File.separator + "qianjiang/"
				+ CONTEXT.getApplicationInfo().packageName + "/crash/";
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(CONTEXT, crashPath);
	}

	public static QJApplicationBase getInstance() {
		return instance;
	}

	/**
	 * 设置应用标记
	 */
	protected abstract void setAppSign();

	/**
	 * 设置终端类型
	 */
	protected abstract void setClientType();

	/**
	 * 获取需要缓存http 接口路径数据列表
	 * 
	 * @return Map<String, HttpCacheConfigModel> key 接口路径
	 *         例如ServerApiConstant.getAPIUrl(ServerApiConstant.GET_DISH_LIST)
	 *         value HttpCacheConfigModel
	 */
	public Map<String, HttpCacheConfigModel> getHttpCacheConfigMap() {
		if (HTTP_CACHE_MAP.isEmpty()) {
			List<HttpCacheConfigModel> cacheConfigList = initHttpCacheConfigList();
			if (cacheConfigList != null && !cacheConfigList.isEmpty()) {
				for (HttpCacheConfigModel HttpCacheConfigModel : cacheConfigList) {
					HTTP_CACHE_MAP.put(HttpCacheConfigModel.HttpServiceName,
							HttpCacheConfigModel);
				}
			}
		}
		return HTTP_CACHE_MAP;
	}

	/**
	 * 配置需要缓存的http 接口 路径 及其参数列表
	 * 
	 * @return List<HttpCacheConfigModel>
	 * @throws
	 */
	protected List<HttpCacheConfigModel> initHttpCacheConfigList() {
		return null;
	}

	public String getAppSign() {
		if (StringUtil.isNullOrEmpty(APP_SIGN)) {
			setAppSign();
		}
		return APP_SIGN;
	}

	/**
	 * 得到 百度定位验证码（子类可实现）
	 */
	public String getAK() {
		return "";
	}

	public int getClientType() {
		if (CLIENT_TYPE == -1) {
			setClientType();
		}
		return CLIENT_TYPE;
	}

	public String getAppVersion() {
		if (StringUtil.isNullOrEmpty(APP_VERSION)) {
			try {
				APP_VERSION = "" + PackageUtil.getVersionCode();
			} catch (NameNotFoundException e) {
				EvtLog.w(TAG, "getAppVersion error : " + e);
			}
		}
		return APP_VERSION;
	}

	public String getDeviceType() {
		if (StringUtil.isNullOrEmpty(DEVICE_TYPE)) {
			// 平板
			if (SCRRENSIZE >= IS_PAD_FLAG) {
				DEVICE_TYPE = "1.2";
			} else if (SCRRENSIZE > 0) {
				DEVICE_TYPE = "1.1";
			} else {
				DEVICE_TYPE = "1.0";
			}
		}
		return DEVICE_TYPE;
	}

	/**
	 * 获取设备标记信息
	 * 
	 * @return String
	 */
	public String getClientInfo() {
		// String info = "";
		// JsonObject jsonObject = new JsonObject();
		// jsonObject.addProperty(KEY_APPSIGN, getAppSign());
		// jsonObject.addProperty(KEY_APPVERSION, getAppVersion());
		// jsonObject.addProperty(KEY_DEVICETYPE, getDeviceType());
		// String deviceId = PackageUtil.getDeviceId();
		// jsonObject.addProperty(KEY_DEVICE_ID,
		// StringUtil.isNullOrEmpty(deviceId) ? "" : deviceId);
		// info = jsonObject.toString();
		// EvtLog.d(TAG, "getClientInfo---- " + info);

		// StringBuffer sb = new StringBuffer();
		// String deviceId = PackageUtil.getDeviceId();
		// AppSign=PMH;AppVersion=8888;DeviceType=1.0;DeviceId=005056C00008;Package=com.qianjiang.pmh
		// sb.append(KEY_APPSIGN);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(getAppSign());
		// sb.append(CONNECT_END_FLAG);
		// sb.append(KEY_APPVERSION);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(getAppVersion());
		// sb.append(CONNECT_END_FLAG);
		// sb.append(KEY_DEVICETYPE);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(getDeviceType());
		// sb.append(CONNECT_END_FLAG);
		// sb.append(KEY_CLIENT_TOKEN);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(StringUtil.isNullOrEmpty(deviceId) ? "" : deviceId);
		// sb.append(CONNECT_END_FLAG);
		// sb.append(KEY_DEVICE_ID);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(StringUtil.isNullOrEmpty(deviceId) ? "" : deviceId);
		// sb.append(CONNECT_END_FLAG);
		// sb.append(KEY_PACKAGE);
		// sb.append(CONNECT_EQUAL_FLAG);
		// sb.append(PackageUtil.getPackageName());
		// EvtLog.d(TAG, "getClientInfo---- " + sb.toString());
		// return sb.toString();
		return "";
	}
}
