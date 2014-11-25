package com.qianjiang.framework.authentication;

/**
 * 动作执行结果封装基类
 * 
 * @author tan.xx
 * @version 2013-3-12 下午2:12:05 tan.xx
 */
public class BaseActionResult {

	public static final String DEFAULT_ERROR_MSG = "error";
	/**
	 * 链接已被释放
	 */
	public static final String RESULT_CODE_IS_RELEASE = "-1";
	/**
	 * 运行成功
	 */
	public static final String RESULT_CODE_SUCCESS = "1";
	/**
	 * 账号未授权
	 */
	public static final String RESULT_CODE_ACCESS_ERROR = "2";
	/**
	 * 运行失败
	 */
	public static final String RESULT_CODE_ERROR = "0";
	/**
	 * 未登录
	 */
	public static final String RESULT_CODE_NOLOGIN = "3";
	/**
	 * 是否进行下一步操作状态
	 */
	public static final String RESULT_CODE_NEXT_ACTION = "4";
	/**
	 * 未知异常
	 */
	public static final String RESULT_CODE_UNKNOW = "5";
	/**
	 * 加密狗验证未通过
	 */
	public static final String RESULT_CODE_NO_VERIFY = "98";
	/**
	 * 网络异常
	 */
	public static final String RESULT_STATE_CODE_NET_ERROR = "100";
	/**
	 * 未返回数据
	 */
	public static final String RESULT_STATE_CODE_NOT_RETURN_DATA = "101";
	/**
	 * 本地参数错误
	 */
	public static final String RESULT_STATE_CODE_PARAM_ERROR = "102";
	/**
	 * 服务器返回参数异常
	 */
	public static final String RESULT_STATE_CODE_RETURN_STATE_ERROR = "103";
	/**
	 * 服务器地址错误
	 */
	public static final String RESULT_STATE_CODE_SERVICE_ADDRESS_ERROR = "104";
	/**
	 * 重单
	 */
	public static final String RESULT_STATE_CODE_REPEAT_ORDER = "99";

	/**
	 * 结果状态
	 */
	public String ResultCode = "0";
	/**
	 * 结果状态码
	 */
	public String ResultStateCode;
	/**
	 * 结果对象
	 */
	public Object ResultObject;

	/**
	 * 动作执行结果
	 */
	public BaseActionResult() {
	}
}
