package de.bwravencl.flightboard;

import android.content.Context;
import android.inputmethodservice.KeyboardView;
import android.util.AttributeSet;

public class PopupKeyboardView extends KeyboardView {

	private Flightboard flightboard;

	public PopupKeyboardView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PopupKeyboardView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public Flightboard getFlightboard() {
		return flightboard;
	}

	public void setFlightboard(Flightboard flightboard) {
		this.flightboard = flightboard;
	}

	@Override
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		flightboard.showPopupWindow();
		setMeasuredDimension(0, 0);
	}
}
