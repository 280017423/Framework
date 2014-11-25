package com.qianjiang.framework.util;

import java.net.URLEncoder;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

/**
 * 
 * @ClassName: UrlUtil
 * @Description: 处理Url的工具类
 * @author zou.sq
 * 
 */
public class UrlUtil {
	private static final String TAG = "UrlUtil";

	/**
	 * 
	 * @Title: buildUrl
	 * @Description: 拼接Url和请求参数
	 * @param url
	 *            url地址
	 * @param getParams
	 *            <NameValuePair> getParams 请求参数
	 * @return url地址
	 */
	public static String buildUrl(String url, List<NameValuePair> getParams) {
		String returnUrl = url;
		if (getParams != null && !getParams.isEmpty()) {
			if (returnUrl.endsWith("/")) {
				returnUrl = returnUrl.substring(0, returnUrl.length() - 1);
			}
			if (returnUrl.indexOf("?") < 0) {
				returnUrl = returnUrl + "?";
			}
			returnUrl = returnUrl + buildContent(getParams);
			return returnUrl;
		}
		return returnUrl;
	}

	/**
	 * 
	 * @Title: buildContent
	 * @Description: 组装参数
	 * @param getParams
	 *            <NameValuePair> getParams 参数集合
	 * @return String 组装后的参数
	 * @throws
	 */
	public static String buildContent(List<NameValuePair> getParams) {
		String content = "";
		String tempParamters = "";
		for (int i = 0; i < getParams.size(); i++) {
			NameValuePair nameValuePair = getParams.get(i);
			if (nameValuePair != null) {
				String key = StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
				String value = StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
				tempParamters = tempParamters + "&" + key + "=" + URLEncoder.encode(value);
			}
		}
		if (tempParamters.length() > 1) {
			content = tempParamters.substring(1);
		} else {
			content = tempParamters;
		}
		return content;
	}

	/**
	 * 为参数列表加入sign字段，传入的参数列表需要调整好顺序 (添加进入参数列表时，key是sign)
	 * 
	 * @param getParams
	 *            请求参数列表
	 */
	public static void addSignToParams(List<NameValuePair> getParams) {
		String sign = getSignString(getParams);
		if (StringUtil.isNullOrEmpty(sign)) {
			EvtLog.w(TAG, "sign为null");
			return;
		}
		EvtLog.d(TAG, "sign:  " + sign);
		getParams.add(new BasicNameValuePair(ServerAPIConstant.Key_Sign, sign.toUpperCase(Locale.getDefault())));
	}

	public static String getSignString(List<NameValuePair> getParams) {
		if (getParams == null) {
			return "";
		}
		String content = "";
		String tempParamters = "";
		for (int i = 0; i < getParams.size(); i++) {
			NameValuePair nameValuePair = getParams.get(i);
			if (nameValuePair != null) {
				String key = StringUtil.isNullOrEmpty(nameValuePair.getName()) ? "" : nameValuePair.getName();
				String value = StringUtil.isNullOrEmpty(nameValuePair.getValue()) ? "" : nameValuePair.getValue();
				tempParamters = tempParamters + "&" + key + "=" + value;
			}
		}
		if (tempParamters.length() > 1) {
			content = tempParamters.substring(1);
		} else {
			content = tempParamters;
		}
		EvtLog.d(TAG, "content:  " + content);
		if (StringUtil.isNullOrEmpty(content)) {
			return "";
		}
		String sign = StringUtil.getMd5Hash(content, null);
		if (StringUtil.isNullOrEmpty(sign)) {
			return "";
		}
		return sign.toUpperCase(Locale.getDefault());
	}

	public static boolean isIntentAvailable(Context context, Intent intent) {
		boolean result = false;
		if (context != null && intent != null) {
			PackageManager packageManager = context.getPackageManager();
			List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY | PackageManager.GET_RESOLVED_FILTER);
			if (resolveInfos != null && resolveInfos.size() > 0) {
				result = true;
			}
		}
		return result;
	}
}
