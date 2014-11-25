package com.qianjiang.framework.model;

public class BottomItem {
	private int mBottomIconRes;
	private String mBottomTabName;

	public BottomItem(int mBottomIconRes, String bottomTabName) {
		this.mBottomIconRes = mBottomIconRes;
		this.mBottomTabName = bottomTabName;
	}

	public BottomItem() {
	}

	public String getBottomTabName() {
		return mBottomTabName;
	}

	public void setBottomTabName(String bottomTabName) {
		this.mBottomTabName = bottomTabName;
	}

	public int getBottomIconRes() {
		return mBottomIconRes;
	}

	public void setBottomIconRes(int bottomIconRes) {
		mBottomIconRes = bottomIconRes;
	}
}
