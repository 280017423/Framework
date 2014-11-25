/**
 * @Project: Framework
 * @Title: HttpCacheConfigModel.java
 * @Package com.pdw.framework.model
 * @Description: TODO
 * @author tan.xx
 * @date 2013-5-11 下午1:37:04
 * @Copyright: 2013 www.paidui.cn Inc. All rights reserved.
 * @version V1.0
 */
package com.qianjiang.framework.model;

/**
 * HTTP网络请求参数配置封装类
 * 
 * @version 2013-5-11 下午1:37:04 tan.xx TODO
 */
public class HttpCacheConfigModel {

	/**
	 * HTTP网络请求ServiceName
	 * 例如ServerApiConstant.getAPIUrl(ServerApiConstant.GET_DISH_LIST);
	 */
	public String HttpServiceName;
	/**
	 * 是否优先取网络请求数据，false则优先取本地数据
	 */
	public boolean IsNetworkDataPriority = true;

	/**
	 * 构造方法
	 */
	public HttpCacheConfigModel() {

	}

	/**
	 * @param isNetworkDataPriority
	 *            是否网络数据优先
	 */
	public HttpCacheConfigModel(boolean isNetworkDataPriority) {
		this.IsNetworkDataPriority = isNetworkDataPriority;
	}

}
