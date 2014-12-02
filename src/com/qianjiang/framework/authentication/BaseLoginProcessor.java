package com.qianjiang.framework.authentication;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;

import com.qianjiang.framework.model.ActionModel;
import com.qianjiang.framework.util.EvtLog;
import com.qianjiang.framework.util.StringUtil;

/**
 * 登录验证器基类（登录界面设置等在使用者中实现）
 * 
 * @author tan.xx
 * @version 2013-12-11 上午10:23:57 tan.xx
 * @param <T>
 *            动作执行结果
 */
public abstract class BaseLoginProcessor<T extends BaseActionResult> implements IBaseLoginListener {
	// 登录动作标识传递key
	public static final String IDENTIFY = "Identify";
	// 登录类型传递
	public static final String KEY_LOGIN_TYPE = "LOGIN_TYPE";
	private static final String TAG = BaseLoginProcessor.class.getSimpleName();
	// 已登录标记
	private static final int ISLOGIN = 1;
	// 未登录标记
	private static final int NOLOGIN = 0;
	// 记录登录状态
	private static int LOGIN_STATUS_CODE;
	// 缓存记录动作 key: identify,value: ActionObject
	@SuppressWarnings("rawtypes")
	private static Map<String, ActionModel> ACTION_MAP;
	// 登录界面类
	private Class<? extends Activity> mLoginActivityClass;

	/**
	 * 登录类型，可扩展分支
	 * 
	 * @author tan.xx
	 * @version 2013-12-12 上午11:35:43 tan.xx
	 */
	public enum LOGIN_TYPE {
		From_ActivityGroup_Tab, // 从ActivityGroup_Tab验证登录
		From_UserInfo_Type, // 从个人中心进入登录
		From_UserInfo_Type_And_Cancel_Finish_Type, // 从个人中心进入登录
		From_Jump_Activity_Type, // 跳转界面进入登录
		From_Jump_Activity_Type_And_Cancel_ReturnMain_Type, // 跳转界面进入登录,取消需要返回首页
		From_GetData_Type, // 获取数据进入登录
		From_GetData_And_Cancel_ReturnMain_Type, // 获取数据进入登录,取消需要返回首页
		From_GetData_And_Cancel_Finish_Type, // 获取数据进入登录,取消需要关闭进入前的界面
		Exit_To_Cancel_Apk// 如果返回登录，按返回键就要退出时要进入的界面
	};

	/**
	 * 构造方法
	 * 
	 * @param loginActivityClass
	 *            登录界面
	 */
	protected BaseLoginProcessor(Class<? extends Activity> loginActivityClass) {
		this.mLoginActivityClass = loginActivityClass;
	}

	/**
	 * 是否本地已经登录
	 * 
	 * @return boolean
	 */
	protected abstract boolean hasUserLogin();

	/**
	 * jumpToUserInfo 跳转至用户中心界面，无From_UserInfo_Type类型登录时可不实现此方法
	 * 
	 * @return void
	 */
	protected abstract void jumpToUserInfo();

	/**
	 * 执行操作(登录成功或者登录取消后回调)
	 * 
	 * @param action
	 *            动作
	 * @param isCancelLogin
	 *            是否是取消登录
	 */
	protected abstract void doAction(final ActionModel<?> action, boolean isCancelLogin);

	/**
	 * 执行自动登录
	 * 
	 * @param action
	 *            动作
	 */
	public void executeAutoLoginTask(ActionModel<?> action) {
	}

	/**
	 * 返回首页
	 * 
	 * @return void
	 */
	protected void jumpToHomePage() {
	}

	/**
	 * @return 返回登陆状态码
	 */
	public int getLoginStatus() {
		return LOGIN_STATUS_CODE;
	}

	/**
	 * 
	 * @return 返回本地登陆状态
	 */
	public boolean isLogin() {
		return LOGIN_STATUS_CODE == ISLOGIN;
	}

	/**
	 * 跳转到登录界面（主动跳转使用）
	 * 
	 * @param activity
	 *            Activity
	 * @param loginType
	 *            登录类型
	 */
	public void jumpToLoginActivity(Activity activity, Intent intent, LOGIN_TYPE loginType) {
		if (activity == null || activity.isFinishing()) {
			return;
		}
		@SuppressWarnings({ "rawtypes", "unchecked" })
		ActionModel action = new ActionModel(activity, null, null, false);
		action.setLoginType(loginType);
		if (intent != null) {
			action.setIntent(intent);
		}
		processorToLogin(action);
	}

	/**
	 * 登录验证跳转界面
	 * 
	 * @param action
	 *            ActionModel
	 */
	@SuppressWarnings("rawtypes")
	public void startActivity(ActionModel action) {
		if (action == null) {
			return;
		}
		Activity activity = action.getActivity();
		if (activity == null || activity.isFinishing()) {
			return;
		}
		Intent intent = action.getIntent();
		if (intent == null) {
			return;
		}
		if (hasUserLogin()) {
			action.getActivity().startActivity(intent);
		} else {
			// 进入登录
			processorToLogin(action);
		}
	}

	/**
	 * 添加动作数据缓存 （未登录时才需要）
	 * 
	 * @param action
	 * @return void
	 */
	@SuppressWarnings("rawtypes")
	private synchronized void addActionObject(ActionModel action) {
		if (action == null) {
			return;
		}
		if (ACTION_MAP == null) {
			ACTION_MAP = new HashMap<String, ActionModel>();
		}
		String identify = action.getIdentify();
		if (StringUtil.isNullOrEmpty(identify)) {
			return;
		}
		if (ACTION_MAP.containsKey(identify)) {
			// 存在相同的移除，现在的机制应该不会出现相同的
			ACTION_MAP.remove(identify);
		}
		ACTION_MAP.put(identify, action);
	}

	/**
	 * 从缓存中获取动作记录
	 * 
	 * @param identify
	 *            动作标识
	 * @return ActionObject
	 */
	@SuppressWarnings("rawtypes")
	private synchronized ActionModel getActionObject(String identify) {
		EvtLog.d(TAG, "getActionObject identify:" + identify);
		if (StringUtil.isNullOrEmpty(identify) || ACTION_MAP == null) {
			return null;
		}
		return ACTION_MAP.get(identify);
	}

	/**
	 * 清除动作缓存记录
	 * 
	 * @param action
	 *            ActionModel
	 */
	@SuppressWarnings("rawtypes")
	public synchronized void removeActionObject(ActionModel action) {
		if (action == null) {
			return;
		}
		String key = action.getIdentify();
		if (StringUtil.isNullOrEmpty(key) || ACTION_MAP == null || !ACTION_MAP.containsKey(key)) {
			return;
		}
		EvtLog.d(TAG, "removeActionObject:" + key);
		ACTION_MAP.remove(key);
	}

	/**
	 * 跳转到登录界面处理
	 * 
	 * @param action
	 *            ActionModel
	 */
	@SuppressWarnings("rawtypes")
	public void processorToLogin(ActionModel action) {
		if (action == null) {
			return;
		}
		Activity activity = action.getActivity();
		if (activity == null || activity.isFinishing()) {
			return;
		}
		// 记录动作
		addActionObject(action);
		// 暂时认为跳转到登录界面后都为未登录状态
		setLoginStatus(false);
		startToLoginActivity(activity, action.getIdentify(), action.getLoginType());

	}

	/**
	 * 获取进入登录界面Intent （ActivityGroup等跳转中使用,需要传递其它参数时使用）
	 * 
	 * @param activity
	 *            Activity
	 * @param loginType
	 *            登录类型
	 * @return Intent
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Intent getLoginIntent(Activity activity, LOGIN_TYPE loginType) {
		if (activity == null || activity.isFinishing()) {
			return null;
		}
		ActionModel action = new ActionModel(activity, null, null, false);
		action.setLoginType(loginType);
		Intent mIntent = new Intent(activity, mLoginActivityClass);
		if (!StringUtil.isNullOrEmpty(action.getIdentify())) {
			mIntent.putExtra(IDENTIFY, action.getIdentify());
		}
		// 记录动作
		addActionObject(action);
		// 暂时认为跳转到登录界面后都为未登录状态
		setLoginStatus(false);
		return mIntent;
	}

	/**
	 * 跳转至登录界面
	 * 
	 * @param activity
	 * @param actionIdentify
	 *            动作标记
	 */
	private void startToLoginActivity(Activity activity, String actionIdentify, LOGIN_TYPE loginType) {
		if (mLoginActivityClass == null) {
			return;
		}
		Intent mIntent = new Intent(activity, mLoginActivityClass);
		if (!StringUtil.isNullOrEmpty(actionIdentify)) {
			mIntent.putExtra(IDENTIFY, actionIdentify);
		}
		if (loginType != null) {
			mIntent.putExtra(KEY_LOGIN_TYPE, loginType);
			// // 如果是退出登录,可以直接右向返回
			// if (loginType.equals(LOGIN_TYPE.Exit_To_Cancel_Apk)) {
			// activity.overridePendingTransition(R.anim.push_right_in,
			// R.anim.push_right_out);
			// }
		}
		activity.startActivity(mIntent);
	}

	/**
	 * 设置登录状态
	 * 
	 * @param isLogin
	 *            是否登录
	 */
	public void setLoginStatus(boolean isLogin) {
		if (isLogin) {
			LOGIN_STATUS_CODE = ISLOGIN;
		} else {
			LOGIN_STATUS_CODE = NOLOGIN;
		}
	}

	/**
	 * onLoginSuccess 登录成功
	 * 
	 * @param loginActivity
	 *            登录上下文
	 * @param actionIdentify
	 *            动作标识ID
	 * @see com.qianjiang.framework.authentication.IBaseLoginListener#onLoginSuccess(android.app.Activity,
	 *      java.lang.String)
	 */
	@Override
	public void onLoginSuccess(Activity loginActivity, String actionIdentify) {
		// 设置为登录状态
		setLoginStatus(true);
		if (StringUtil.isNullOrEmpty(actionIdentify)) {
			return;
		}

		@SuppressWarnings("rawtypes")
		ActionModel actionObject = getActionObject(actionIdentify);
		if (actionObject == null) {
			return;
		}

		if (actionObject.getLoginType() == null) {
			return;
		}
		// 回调按分支执行
		switch (actionObject.getLoginType()) {
		// 数据请求类型
			case From_GetData_Type:
			case From_GetData_And_Cancel_ReturnMain_Type:
			case From_GetData_And_Cancel_Finish_Type:
			case From_UserInfo_Type_And_Cancel_Finish_Type:
				// 继续网络请求数据
				doAction(actionObject, false);
				loginActivity.finish();
				// 清除动作记录
				removeActionObject(actionObject);
				break;

			case From_Jump_Activity_Type: // 跳转界面进入登录
			case From_Jump_Activity_Type_And_Cancel_ReturnMain_Type: // 跳转界面进入登录,取消需要返回首页
			case Exit_To_Cancel_Apk:
				Intent intent = actionObject.getIntent();
				// 为空时暂不处理
				if (intent != null) {
					// 继续跳转界面
					loginActivity.startActivity(intent);
					loginActivity.finish();
				}
				// 清除动作记录
				removeActionObject(actionObject);
				break;
			case From_UserInfo_Type: // 个人中心进入
				// 跳转至用户中心界面,暂时不清除动作记录，因为首页中未重新生成登录界面
				jumpToUserInfo();
				break;
			default:
				// 清除动作记录
				removeActionObject(actionObject);
				break;
		}
	}

	/**
	 * onLoginError 登录失败
	 * 
	 * @param loginActivity
	 *            动作上下文
	 * @param actionIdentify
	 *            动作标识
	 * @see com.qianjiang.framework.authentication.IBaseLoginListener#onLoginError(android.app.Activity,
	 *      java.lang.String)
	 */
	@Override
	public void onLoginError(Activity loginActivity, String actionIdentify) {
		// 暂无特殊处理

	};

	/**
	 * 取消登录
	 * 
	 * @param loginActivity
	 *            登录上下文
	 * @param actionIdentify
	 *            动作标识
	 * @param mainActivityClass
	 *            主页
	 * @see com.qianjiang.framework.authentication.IBaseLoginListener#onLoginCancel(android.app.Activity,
	 *      java.lang.String, java.lang.Class)
	 */
	@Override
	public void onLoginCancel(Activity loginActivity, String actionIdentify, Class<? extends Activity> mainActivityClass) {
		if (StringUtil.isNullOrEmpty(actionIdentify)) {
			return;
		}

		@SuppressWarnings("rawtypes")
		ActionModel actionObject = getActionObject(actionIdentify);
		if (actionObject == null) {
			return;
		}
		// 清除动作记录
		removeActionObject(actionObject);
		if (actionObject.getLoginType() == null) {
			return;
		}
		// 回调按分支执行
		switch (actionObject.getLoginType()) {
			case From_Jump_Activity_Type_And_Cancel_ReturnMain_Type: // 跳转界面进入登录,取消需要返回首页
			case From_GetData_And_Cancel_ReturnMain_Type: // 获取数据进入登录,取消需要返回首页
				// 跳转至首页(主页为一个Activity时适用)
				// Intent mIntent = new Intent(loginActivity,
				// mainActivityClass);
				// mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				// loginActivity.startActivity(mIntent);
				// // 设置跳转动画
				// loginActivity.overridePendingTransition(R.anim.push_right_in,
				// R.anim.push_right_out);

				// 跳转至首页
				jumpToHomePage();
				break;
			case From_GetData_And_Cancel_Finish_Type: // 获取数据进入登录,取消需要关闭进入前的界面
			case From_UserInfo_Type_And_Cancel_Finish_Type: // 获取数据进入登录,取消需要关闭进入前的界面
				Activity fromActivity = actionObject.getActivity();
				if (fromActivity != null) {
					// 关闭之前界面
					fromActivity.finish();
				}
				break;
			case From_GetData_Type: // 获取数据进入登录
				// 执行取消回调
				doAction(actionObject, true);
				break;
			default:
				break;
		}
		// 关闭登录界面
		loginActivity.finish();
	}
}
