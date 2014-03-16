package de.bwravencl.flightboard.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MenuModel extends ItemModel {

	public static final int MAX_ITEMS_PER_ROW = 8;

	private List<ItemModel> items = new ArrayList<ItemModel>();

	private LinearLayout verticalLinearLayout;

	private Random random = new Random();

	public MenuModel(String title) {
		super(title);
	}

	public List<ItemModel> getItems() {
		return items;
	}

	public void setItems(List<ItemModel> items) {
		this.items = items;
	}

	public LinearLayout getLinearLayout() {
		return verticalLinearLayout;
	}

	@Override
	public Button toButton(Context context) {
		Button button = super.toButton(context);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flightboard.handleMenu(MenuModel.this);
			}
		});

		return button;
	}

	public LinearLayout toLinearLayout(Context context) {

		if (verticalLinearLayout == null) {
			verticalLinearLayout = new LinearLayout(context);
			verticalLinearLayout.setId(random.nextInt(Integer.MAX_VALUE) + 1);
			verticalLinearLayout.setOrientation(LinearLayout.VERTICAL);

			int numRowsRequired = (int) Math.ceil((float) (items.size() + 1)
					/ MAX_ITEMS_PER_ROW); // + 1 for the close button!

			for (int i = 0; i < numRowsRequired; i++) {
				LinearLayout horizontalLinearLayout = new LinearLayout(context);
				horizontalLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
				verticalLinearLayout.addView(horizontalLinearLayout);

				for (int j = 0; j < MAX_ITEMS_PER_ROW; j++) {
					int n = i * MAX_ITEMS_PER_ROW + j;

					if (items.size() > n) {
						ItemModel item = items.get(n);
						horizontalLinearLayout.addView(item.toButton(context));
					} else
						break;
				}

				if (i == numRowsRequired - 1) {
					Button closeButton = new Button(context);
					closeButton.setText("Close");
					closeButton.setMinimumWidth(BUTTON_WITH);
					closeButton.setMinimumHeight(BUTTON_HEIGHT);
					closeButton.setMaxWidth(BUTTON_WITH);
					closeButton.setMaxHeight(BUTTON_HEIGHT);
					closeButton.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							flightboard.handleCloseButton(MenuModel.this);
						}
					});
					horizontalLinearLayout.addView(closeButton);
				}

			}
		}

		return verticalLinearLayout;
	}
}
