package com.qianjiang.framework.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActivityGroup;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import com.qianjiang.framework.R;
import com.qianjiang.framework.util.EvtLog;
import com.qianjiang.framework.util.PackageUtil;
import com.qianjiang.framework.util.StringUtil;
import com.qianjiang.framework.widget.LoadingUpView;

/**
 * 所有应用Activity的基类
 * 
 * @author zou.sq
 * @version
 */
public abstract class QJActivityBase extends ActivityGroup {
	public static final String ACTION_DEFAULT_BROAD = "TestCustomBroadCast";
	public static final String ACTION_EXIT_APP = "EXIT_APP";
	public static final String ACTION_RESET_APP = "ACTION_RESET_APP";
	public static final String KEY_WORD_APP = "KEY_WORD_APP";
	private static final int DEFAUTL_COOLING_TIME = 3000;
	private static final String TAG = "PdwActivityBase";
	private static final String DEFAULT_ERROR_MSG = "error";
	private static final Object mSync = new Object();
	private static final List<String> ACTION_LIST = new ArrayList<String>();
	public static Context mContext;

	protected final IntentFilter mIntentFilter = new IntentFilter();
	protected BroadcastReceiver mGlobalReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Object data = intent.getSerializableExtra("data");
			String appIndentify = intent.getStringExtra(KEY_WORD_APP);
			// 过滤当前接收到的广播是否和上下文一致
			if (!StringUtil.isNullOrEmpty(appIndentify) && context.getPackageName().equals(appIndentify)) {
				if (action.equals(ACTION_DEFAULT_BROAD)) {
					action = intent.getStringExtra("action");
				}
				// 若收到退出程序的广播，则直接退出程序
				if (ACTION_EXIT_APP.equals(action) && !isFinishing()) {
					onFinish();
					finish();
					return;
				}
			}
			// 广播处理方法
			processBroadReceiver(action, data);
		}
	};

	/**
	 * 获取屏幕物理尺寸
	 * 
	 * @return double
	 * @throws
	 */
	private double getScreenSize() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		// 屏幕尺寸=屏幕对角线的像素值/（密度*160）= （长的平方+宽的平方）开2次根 / (密度*160）
		double diagonalPixels = Math.sqrt(Math.pow(dm.widthPixels, 2) + Math.pow(dm.heightPixels, 2));
		double scrrenSize = diagonalPixels / (160 * dm.density);
		return scrrenSize;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		EvtLog.d(TAG, "onCreate start... ");
		super.onCreate(savedInstanceState);
		mContext = this;
		// mAnalyticsGoogle = PDWGoogleAnalyticsImpl.getInstance(this);
		mIntentFilter.addAction(ACTION_DEFAULT_BROAD);
		mIntentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		addIntentFilter(mIntentFilter);
		registerReceiver(mGlobalReceiver, mIntentFilter);
		EvtLog.d(TAG, "onCreate end... ");

		if (QJApplicationBase.SCRRENSIZE <= 0d) {
			QJApplicationBase.SCRRENSIZE = getScreenSize();
			EvtLog.d(TAG, "获取屏幕物理尺寸---- " + QJApplicationBase.SCRRENSIZE);
		}
	}

	protected void processBroadReceiver(String action, Object data) {

	}

	/**
	 * 关闭时做的处理
	 */
	protected void onFinish() {

	}

	/**
	 * 手动添加广播频道
	 * 
	 * @param str
	 */
	protected void addAction(String... str) {
		String action;
		for (int i = 0; i < str.length; i++) {
			action = str[i];
			mIntentFilter.addAction(action);
		}
	}

	/**
	 * 添加广播过滤器
	 * 
	 * @param intentFilter
	 *            参数
	 */
	protected void addIntentFilter(IntentFilter intentFilter) {

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mGlobalReceiver);
	}

	/**
	 * 
	 * @param action
	 *            广播类型
	 * @param data
	 *            广播发送的数据
	 */
	public void sendBroadCastV(String action, String data) {
		Intent intent = new Intent(ACTION_DEFAULT_BROAD);
		intent.putExtra("action", action);
		intent.putExtra("data", data);
		intent.putExtra(KEY_WORD_APP, mContext.getPackageName());
		QJApplicationBase.CONTEXT.sendBroadcast(intent);
	}

	/**
	 * 
	 * @param action
	 *            广播类型
	 * @param data
	 *            广播发送的数据
	 */
	public void sendBroadCastMap(String action, HashMap<String, Object> data) {
		Intent intent = new Intent(ACTION_DEFAULT_BROAD);
		intent.putExtra("action", action);
		intent.putExtra("data", data);
		intent.putExtra(KEY_WORD_APP, this.getPackageName());
		QJApplicationBase.CONTEXT.sendBroadcast(intent);
	}

	/**
	 * 发送系统广播
	 */
	@Override
	public void sendBroadcast(Intent intent) {
		if (intent != null) {
			intent.putExtra(KEY_WORD_APP, mContext.getPackageName());
			QJApplicationBase.CONTEXT.sendBroadcast(intent);
		}
	}

	/**
	 * 默认的toast方法，该方法封装下面的两点特性：<br>
	 * 1、只有当前activity所属应用处于顶层时，才会弹出toast；<br>
	 * 2、默认弹出时间为 Toast.LENGTH_SHORT;
	 * 
	 * @param msg
	 *            弹出的信息内容
	 */
	public void toast(final String msg) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (!StringUtil.isNullOrEmpty(msg) && !DEFAULT_ERROR_MSG.equals(msg)) {
					Toast toast = Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT);
					TextView tv = (TextView) toast.getView().findViewById(android.R.id.message);
					// 用来防止某些系统自定义了消息框
					if (tv != null) {
						tv.setGravity(Gravity.CENTER);
					}
					toast.show();
				}
			}
		});
	}

	/**
	 * 弹出toast
	 * 
	 * @param e
	 *            出错的exception
	 */
	public void toast(final Throwable e) {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getBaseContext(),
						getBaseContext().getResources().getString(R.string.msg_operate_fail_try_again),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 限制执行频率的方法。如按钮需要在指定的3000ms时间后才能再次执行，使用方式如：<br>
	 * 
	 * @param id
	 *            方法的标识，可以使用按钮控件的id或者其他唯一标识方法的字符串
	 * @param actionListener
	 *            方法的回调函数
	 */
	public void doActionAgain(final String id, ActionListener actionListener) {
		if (StringUtil.isNullOrEmpty(id) || actionListener == null) {
			throw new NullPointerException();
		}

		synchronized (mSync) {
			if (ACTION_LIST.contains(id)) {
				return;
			} else {
				ACTION_LIST.add(id);

				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						removeAction(id);
					}
				}, DEFAUTL_COOLING_TIME);
			}
		}

		actionListener.doAction();
	}

	/**
	 * 限制执行频率的方法。如按钮需要在指定的时间后才能再次执行，使用方式如：<br>
	 * 
	 * @param id
	 *            方法的标识，可以使用按钮控件的id或者其他唯一标识方法的字符串
	 * @param delay
	 *            延迟时间，以毫秒为单位
	 * @param actionListener
	 *            方法的回调函数
	 */
	protected void doActionAgain(final String id, int delay, ActionListener actionListener) {
		synchronized (mSync) {
			if (ACTION_LIST.contains(id)) {
				return;
			} else {
				ACTION_LIST.add(id);

				Timer timer = new Timer();
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						removeAction(id);
					}
				}, delay);
			}
		}

		actionListener.doAction();
	}

	private void removeAction(String id) {
		synchronized (mSync) {
			ACTION_LIST.remove(id);
		}
	}

	protected void showLoadingUpWindow(LoadingUpView loadingUpView) {
		if (loadingUpView != null && !loadingUpView.isShowing()) {
			loadingUpView.showPopup();
		}
	}

	protected void dismissLoadingUpWindow(LoadingUpView loadingUpView) {
		if (loadingUpView != null && loadingUpView.isShowing()) {
			loadingUpView.dismiss();
		}
	}

	/**
	 * @author Administrator
	 */
	public interface ActionListener {
		/**
		 * 
		 */
		void doAction();
	}
}
