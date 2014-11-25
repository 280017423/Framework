package com.qianjiang.framework.authentication;

import android.app.Activity;

/**
 * 登录动作回调接口
 * 
 * @author tan.xx
 * @version 2013-12-13 下午4:19:52 tan.xx TODO
 */
public interface IBaseLoginListener {

	/**
	 * 成功登录回调
	 * 
	 * @param loginActivity
	 *            登录上下文
	 * @param actionIdentify
	 *            动作标识
	 */
	void onLoginSuccess(Activity loginActivity, String actionIdentify);

	/**
	 * 失败登录回调
	 * 
	 * @param loginActivity
	 *            登录上下文
	 * @param actionIdentify
	 *            动作标识
	 */
	void onLoginError(Activity loginActivity, String actionIdentify);

	/**
	 * 取消登录回调
	 * 
	 * @param loginActivity
	 *            登录上下文
	 * @param actionIdentify
	 *            动作标识
	 * @param mainActivityClass
	 *            首页（返回首页使用）
	 */
	void onLoginCancel(Activity loginActivity, String actionIdentify, Class<? extends Activity> mainActivityClass);
}
