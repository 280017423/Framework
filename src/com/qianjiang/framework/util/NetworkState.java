/**
 * 网络辅助类
 */
package com.qianjiang.framework.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.qianjiang.framework.app.QJApplicationBase;

/**
 * @author wang.xy
 * 
 */
public class NetworkState implements INetworkState {

	private static final String TAG = "NetworkState";

	/**
	 * 该方法不要在业务方法中调用，这里临时解决单元测试中，设置网络状态的方法
	 */
	public static void setUnitTest() {

	}

	/**
	 * 检查网络是否可用
	 * 
	 * @return 如果可用，返回true，不可用则返回false
	 */
	@Override
	public boolean isNetworkAvailable() {
		Context context = QJApplicationBase.CONTEXT;
		if (context == null) {
			return false;
		}

		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (cm == null) {
			return false;
		}
		NetworkInfo[] info = null;
		try {
			info = cm.getAllNetworkInfo();
		} catch (Exception e) {
			EvtLog.w(TAG, e);
		}
		if (info != null) {
			for (int i = 0; i < info.length; i++) {
				if (info[i].getState() == NetworkInfo.State.CONNECTED) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * 检查网络是否可用
	 * 
	 * @return 如果可用，返回true，不可用则返回false
	 */
	@Override
	public boolean isGPSAvailable() {
		boolean result;
		LocationManager locationManager = (LocationManager) QJApplicationBase.CONTEXT
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			result = true;
		} else {
			result = false;
		}
		EvtLog.d(TAG, "result:" + result);

		return result;
	}

	/**
	 * @return 检查wifi网络是否可用
	 */
	@Override
	public boolean isWifiAvailable() {
		WifiManager wm = (WifiManager) QJApplicationBase.CONTEXT.getSystemService(Context.WIFI_SERVICE);
		if (wm.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			return true;
		}
		return false;
	}

	/**
	 * 获取当前连接wifi ssid名称
	 * 
	 * @return 当前连接wifi的SSID
	 */
	@Override
	public String getConnectionWifiSSID() {
		String ssid = "";
		WifiManager wifiManager = (WifiManager) QJApplicationBase.CONTEXT.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			ssid = wifiInfo.getSSID();
		}
		return ssid;
	}

}
