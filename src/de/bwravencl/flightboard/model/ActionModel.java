package de.bwravencl.flightboard.model;

import java.util.List;
import java.util.Locale;


import android.content.Context;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class ActionModel extends ItemModel {

	private String key;
	private List<String> modifiers;

	public ActionModel(String title, String key, List<String> modifiers) {
		super(title);

		this.key = key;
		this.modifiers = modifiers;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public List<String> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<String> modifiers) {
		this.modifiers = modifiers;
	}

	@Override
	public Button toButton(Context context) {
		Button button = super.toButton(context);
		button.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_DOWN)
					flightboard.handleActionBegin(ActionModel.this);
				else if(event.getAction() == MotionEvent.ACTION_UP)
					flightboard.handleActionEnd(ActionModel.this);
				
				return true;
			}
		});

		return button;
	}

	public int[] getKeyEventCodes() {
		int keyCode = KeyEvent.keyCodeFromString("KEYCODE_"
				+ key.toUpperCase(Locale.US));

		int ctrlModifier = 0;
		int altModifier = 0;
		int shiftModifier = 0;

		if (modifiers != null) {
			for (String s : modifiers) {
				if (s.equals("CTRL"))
					ctrlModifier = 1;
				if (s.equals("ALT"))
					altModifier = 1;
				if (s.equals("SHIFT"))
					shiftModifier = 1;
			}
		}

		int[] retval = { keyCode, ctrlModifier, altModifier, shiftModifier };
		return retval;
	}
}
