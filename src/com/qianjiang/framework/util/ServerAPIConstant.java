package com.qianjiang.framework.util;

import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

import com.qianjiang.framework.app.QJApplicationBase;

/**
 * 定义与服务器端的接口交互需要用到的常量
 * 
 * @author cui.yp
 * @version 时间，作者，更改内容 <br>
 *          2012-7-25 cui.yp 服务器修改了获取基础数据的接口地址<br>
 * 
 */
public class ServerAPIConstant {
	// 下面是排队网 优惠券API 的地址定义，按字母序往下排，不然不好找
	public static final String COUPON_GETLIST = "Coupon/GetList";
	public static final String COUPON_GetAllInfo = "Coupon/GetAllInfo";
	public static final String COUPON_SHOW_INFO = "Shop/GetCouponShowInfo";
	public static final String MERCHANT_GETLIST = "Merchant/GetList";
	public static final String SYS_GETBASEDATA = "Sys/GetBaseData3";
	public static final String SYSTEM_GETAPPINFO = "Sys/GetAppInfo";
	public static final String SYSTEM_CHECKVERSION = "Sys/CheckVersion";
	public static final String SYSTEM_GETAPPLIST = "Sys/GetAppList";
	public static final String SYSTEM_RECORD_FEEDBACK = "Sys/RecordFeedback";
	public static final String SYSTEM_RECORD_DEVICEINFO = "Sys/RecordDeviceInfo";

	// 接口文档中的json key，按字母序往下排，不然不好找
	public static final String Key_AppInfo = "AppInfo";
	public static final String Key_AppList = "AppList";
	public static final String Key_AppSign = "AppSign";
	public static final String Key_AreaID = "AreaID";
	public static final String Key_AreaList = "AreaList";
	public static final String Key_Content = "Content";
	public static final String Key_CouponList = "CouponList";
	public static final String Key_ContactInfo = "ContactInfo";
	public static final String Key_DeviceType = "DeviceType";
	public static final String Key_LastTime = "LastTime";
	public static final String Key_MerchantID = "MerchantID";
	public static final String Key_MerchantList = "MerchantList";
	public static final String Key_OSVersion = "OSVersion";
	public static final String Key_ServerCurrentTime = "ServerCurrentTime";
	public static final String Key_MobileType = "MobileType";
	public static final String Key_ScopeList = "ScopeList";
	public static final String Key_Sign = "Sign";
	public static final String Key_TerminalSign = "TerminalSign";
	public static final String Key_TradeList = "TradeList";
	public static final String Key_VersionNo = "VersionNo";
	public static final String KEY_VERSION_NAME_FLAG = "VersionNameFlag";

	private static final String SYSTEM_CONFIG = "com.qianjiang.system.config";
	private static final String API_ROOT_URL = "api_root_url";
	private static final String SYSTEMCONFIG_VERSION_NAME = "system_config_version_name";

	/**
	 * 获取后端的 api URL地址
	 * 
	 * @param appSign
	 *            应用类型
	 * @param actions
	 *            方法的子路径
	 * @return 返回后端的 api URL地址
	 */
	public static String getAPIUrl(String appSign, String actions) {
		// PackageUtil.getConfigString("api_root_url") + appSign + "/" +
		// actions;
		return getApiRootUrl() + appSign + "/" + actions;
	}

	private static String getApiRootUrl() {
		SharedPreferences settings = QJApplicationBase.CONTEXT
				.getSharedPreferences(SYSTEM_CONFIG, 0);
		String apiRootUrl = settings.getString(API_ROOT_URL, "");
		String versionName = settings.getString(SYSTEMCONFIG_VERSION_NAME, "");
		String currentVersionName = "";
		try {
			currentVersionName = PackageUtil.getVersionName();
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		// 三种情况使用原始的api：新api为空，版本号为空，当前版本名和保存的版本名不同
		if ("".equals(apiRootUrl) || "".equals(versionName)
				|| !currentVersionName.equals(versionName)) {
			return PackageUtil.getConfigString(API_ROOT_URL);
		}
		return "".equals(apiRootUrl) ? PackageUtil
				.getConfigString(API_ROOT_URL) : apiRootUrl;
	}
}
