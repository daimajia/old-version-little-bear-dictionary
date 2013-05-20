package com.zhan_dui.dictionary.listeners;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.EditText;
/**
 * 
 * @Description:实现了点击右侧Drawable的点击事件，用来做删除EditText文本时候的监听器
 * @date 2012-11-16 下午10:56:42
 */
public abstract class RightDrawableOnTouchListener implements OnTouchListener {
	Drawable drawable;
	private int fuzz = 10;

	/**
	 * @param keyword
	 */
	public RightDrawableOnTouchListener(EditText view) {
		super();
		final Drawable[] drawables = view.getCompoundDrawables();
		if (drawables != null && drawables.length == 4)
			this.drawable = drawables[2];
	}

	@Override
	public boolean onTouch(final View v, final MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN && drawable != null) {
			final int x = (int) event.getX();
			final int y = (int) event.getY();
			final Rect bounds = drawable.getBounds();
			if (x >= (v.getRight() - bounds.width() - fuzz)
					&& x <= (v.getRight() - v.getPaddingRight() + fuzz)
					&& y >= (v.getPaddingTop() - fuzz)
					&& y <= (v.getHeight() - v.getPaddingBottom()) + fuzz) {
				return onDrawableTouch(event);
			}
		}
		return false;
	}

	public abstract boolean onDrawableTouch(final MotionEvent event);

}