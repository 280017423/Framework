package com.qianjiang.framework.widget;

import java.util.List;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qianjiang.framework.R;
import com.qianjiang.framework.model.BottomItem;

/**
 * 底部导航栏控件
 * 
 * @author xu.xb
 * 
 */
public abstract class BottomTab extends LinearLayout {
	private Context mContext;
	private OnBottomCheckedListener mBottomCheckedListener;
	private List<BottomItem> mBottomItems;
	private int mSelectedIndex = -1;

	public enum BottomTabModeFlag {
		FLAG_CLEAR, FLAG_SHOW
	}

	public BottomTab(Context context) {
		super(context);
		mContext = context;
		initItem();
	}

	public BottomTab(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initItem();
	}

	private void initItem() {
		mBottomItems = initBottomItems();
		if (mBottomItems != null) {
			initView();
		}
	}

	private void initView() {
		setBackgroundResource(getBackGroupRes());
		if (mBottomItems != null && mBottomItems.size() != 0) {
			// 设置当前的权重总数
			setWeightSum(mBottomItems.size());
			// 循环初始化每一块的布局，并添加入这里
			for (int i = 0; i < mBottomItems.size(); i++) {
				BottomItem bottomItem = mBottomItems.get(i);
				View bottomView = View.inflate(mContext, R.layout.bottom_item_view, null);
				bottomView.setLayoutParams(new LayoutParams(
						android.view.ViewGroup.LayoutParams.MATCH_PARENT,
						android.view.ViewGroup.LayoutParams.MATCH_PARENT, 1));
				ImageView iconView = (ImageView) bottomView.findViewById(R.id.view_bottom_icon);
				TextView tvTabName = (TextView) bottomView.findViewById(R.id.tv_bottom_tap_name);
				tvTabName.setTextColor(mContext.getResources().getColorStateList(getNormalTextColorRes()));
				tvTabName.setText(bottomItem.getBottomTabName());
				iconView.setImageResource(bottomItem.getBottomIconRes());
				bottomView.setTag(i);
				bottomView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Integer viewIndex = (Integer) v.getTag();
						viewIndex = viewIndex == null ? 0 : viewIndex;
						setSelectedIndex(viewIndex);
					}
				});
				addView(bottomView);
			}
		}
	}

	public void setSelectedIndex(int index) {
		if (index == mSelectedIndex || index < 0) {
			return;
		}
		// 二级保险，当selectedIndex < 0 时，不还原
		if (mSelectedIndex >= 0) {
			View lastSelectedView = getChildAt(mSelectedIndex);
			lastSelectedView.setBackgroundResource(getBackGroupRes());
			lastSelectedView.setEnabled(true);
			TextView tvTabName = (TextView) lastSelectedView.findViewById(R.id.tv_bottom_tap_name);
			tvTabName.setTextColor(mContext.getResources().getColorStateList(getNormalTextColorRes()));
		}
		// 将选中置为选中
		View selectedView = getChildAt(index);
		selectedView.setBackgroundResource(getSelectedViewRes());
		selectedView.setEnabled(false);
		TextView tvTabName = (TextView) selectedView.findViewById(R.id.tv_bottom_tap_name);
		tvTabName.setTextColor(mContext.getResources().getColorStateList(getSelectedTextColorRes()));
		// 将之前选中设置为还原
		mSelectedIndex = index;
		if (mBottomCheckedListener != null) {
			mBottomCheckedListener.onBottomCheckedListener(index);
		}
	}

	public void setNewMsgDisplay(int index, BottomTabModeFlag flag) {
		if (index < 0) {
			return;
		}
		// 获取选中那一块View
		View selectedView = getChildAt(index);
		View newMsgFlagView = selectedView.findViewById(R.id.view_bottom_msg_flag);
		if (flag == BottomTabModeFlag.FLAG_CLEAR && newMsgFlagView.getVisibility() == VISIBLE) {
			newMsgFlagView.setVisibility(View.GONE);
		} else if (flag == BottomTabModeFlag.FLAG_SHOW && newMsgFlagView.getVisibility() != VISIBLE) {
			newMsgFlagView.setVisibility(View.VISIBLE);
		}
	}

	public void setOnBottomCheckedListener(OnBottomCheckedListener bottomCheckedListener) {
		mBottomCheckedListener = bottomCheckedListener;
	}

	public interface OnBottomCheckedListener {
		void onBottomCheckedListener(int checkedPos);
	}

	/**
	 * 由子类去传入内容
	 * 
	 */
	protected abstract List<BottomItem> initBottomItems();

	/**
	 * 由子类去传入背景
	 * 
	 */
	protected abstract int getBackGroupRes();

	/**
	 * 子类传入选中样式
	 * 
	 * @return
	 */
	protected abstract int getSelectedViewRes();

	protected abstract int getSelectedTextColorRes();

	protected abstract int getNormalTextColorRes();
}
