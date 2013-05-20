package com.zhan_dui.dictionary.custom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Path;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class VerticalTextView extends TextView {

	private int mToward = 4;
	public final static int TOWARD_LEFT = 3;
	public final static int TOWARD_RIGHT = 4;

	public VerticalTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public VerticalTextView(Context context) {
		super(context);
	}

	public void setToward(int toward) {
		if (toward <= TOWARD_RIGHT && toward >= TOWARD_LEFT) {
			this.mToward = toward;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		final ColorStateList csl = getTextColors();
		final int color = csl.getDefaultColor();
		final int paddingBottom = getPaddingBottom();
		final int paddingTop = getPaddingTop();
		final int viewWidth = getWidth();
		final int viewHeight = getHeight();
		final TextPaint paint = getPaint();
		paint.setColor(color);
		float bottom = 0;

		Path p = new Path();
		switch (mToward) {
			case TOWARD_LEFT :
				bottom = viewWidth / 2 - getTextSize() / 2;
				p.moveTo(bottom, paddingTop);
				p.lineTo(bottom, viewHeight - paddingBottom - paddingTop);
				break;
			case TOWARD_RIGHT :
				bottom = viewWidth / 2 + getTextSize() / 2;
				p.moveTo(bottom, viewHeight - paddingBottom - paddingTop);
				p.lineTo(bottom, paddingTop);
				break;
			default :
				break;
		}

		canvas.drawTextOnPath(getText().toString(), p, 0, 0, paint);

	}
}