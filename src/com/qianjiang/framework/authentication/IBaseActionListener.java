package com.qianjiang.framework.authentication;

/**
 * 动作回调接口
 * 
 * @author zeng.ww
 * @version 1.1.0
 * @param <T>
 */
public interface IBaseActionListener<T extends BaseActionResult> {
	/**
	 * 执行动作
	 * 
	 * @return 对象
	 */
	T onAsyncRun();

	/**
	 * 成功回调
	 * 
	 * @param result
	 *            返回结果
	 */
	void onSuccess(T result);

	/**
	 * 失败回调
	 * 
	 * @param result
	 *            返回结果
	 */
	void onError(T result);
}
