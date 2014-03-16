package de.bwravencl.flightboard.model;

import de.bwravencl.flightboard.Flightboard;
import android.content.Context;
import android.widget.Button;

public class ItemModel {

	public static final int BUTTON_WITH = 150;
	public static final int BUTTON_HEIGHT = 65;

	Flightboard flightboard;
	protected String title;

	public ItemModel(String title) {
		this.title = title;
	}
	
	public Flightboard getFlightboard() {
		return flightboard;
	}
	
	public void setFlightboard(Flightboard flightboard) {
		this.flightboard = flightboard;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Button toButton(Context context) {
		Button button = new Button(context);
		button.setText(title);
		button.setMinimumWidth(BUTTON_WITH);
		button.setMinimumHeight(BUTTON_HEIGHT);
		button.setMaxWidth(BUTTON_WITH);
		button.setMaxHeight(BUTTON_HEIGHT);

		return button;
	}
}
