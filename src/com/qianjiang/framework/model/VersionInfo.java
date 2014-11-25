package com.qianjiang.framework.model;

/**
 * 版本信息，用于判断是否要升级
 * 
 * @author cui.yp
 * @version 1.0
 */
public class VersionInfo {
	public static final int HAS_NEW_VERSION = 1;
	public static final int HAS_NO_NEW_VERSION = 0;

	/**
	 * 是否有新版本
	 */
	public int hasNew;
	/**
	 * 应用程序对外版本号
	 */
	public String appVersionNo;
	/**
	 * 新版本描述
	 */
	public String appLogs;
	/**
	 * 新版本下载地址
	 */
	public String appPath;
	/**
	 * 新版本大小，单位 M
	 */
	public String appSize;
	/**
	 * 新版本名字
	 */
	public String appName;
	/**
	 * 强制升级标志
	 */
	private int isForce;

	public boolean isForceUpdate() {
		return getIsForce() == 0 ? false : true;
	}

	public int getIsForce() {
		return isForce;
	}

	public void setIsForce(int isForce) {
		this.isForce = isForce;
	}
}