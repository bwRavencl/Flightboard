package de.bwravencl.flightboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import de.bwravencl.flightboard.model.ActionModel;
import de.bwravencl.flightboard.model.ItemModel;
import de.bwravencl.flightboard.model.MenuModel;
import de.bwravencl.flightboard.xml.ProfileXmlParser;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.KeyboardView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class Flightboard extends InputMethodService implements
		KeyboardView.OnKeyboardActionListener {

	private PopupKeyboardView popupKeyboardView;
	private PopupWindow popupWindow;
	private RelativeLayout popupRelativeLayout;

	private List<ItemModel> mainMenuItems;
	private List<ItemModel> allItems;
	private int maxConcurrentRows;
	private int numRowsRequired;

	private List<LinearLayout> menuBarStack = new ArrayList<LinearLayout>();

	private Random random = new Random();

	@Override
	public void onCreate() {
		super.onCreate();

		ProfileXmlParser profileXmlParser = new ProfileXmlParser();
		try {
			profileXmlParser.parseFile(this.getResources().openRawResource(
					R.raw.fltsim5));

			allItems = profileXmlParser.getAllItems();
			mainMenuItems = profileXmlParser.getMainMenuItems();
			numRowsRequired = (int) Math.ceil((float) mainMenuItems.size()
					/ MenuModel.MAX_ITEMS_PER_ROW);
			maxConcurrentRows = profileXmlParser.getMaxConcurrentRows()
					+ numRowsRequired - 1;

			for (ItemModel i : allItems)
				i.setFlightboard(this);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private RelativeLayout getPopupRelativeLayout(List<ItemModel> mainMenuItems) {
		popupRelativeLayout = new RelativeLayout(this);

		LinearLayout mainMenuVertialLinearLayout = new LinearLayout(this);
		mainMenuVertialLinearLayout
				.setId(random.nextInt(Integer.MAX_VALUE) + 1);
		mainMenuVertialLinearLayout.setOrientation(LinearLayout.VERTICAL);
		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
				RelativeLayout.TRUE);
		mainMenuVertialLinearLayout.setLayoutParams(layoutParams);

		for (int i = 0; i < numRowsRequired; i++) {
			LinearLayout mainMenuHorizontalLinearLayout = new LinearLayout(this);
			mainMenuHorizontalLinearLayout
					.setOrientation(LinearLayout.HORIZONTAL);
			mainMenuHorizontalLinearLayout.setId(random
					.nextInt(Integer.MAX_VALUE) + 1);
			mainMenuVertialLinearLayout.addView(mainMenuHorizontalLinearLayout);

			for (int j = 0; j < MenuModel.MAX_ITEMS_PER_ROW; j++) {
				int n = i * MenuModel.MAX_ITEMS_PER_ROW + j;

				if (mainMenuItems.size() > n) {
					ItemModel item = mainMenuItems.get(n);
					mainMenuHorizontalLinearLayout.addView(item.toButton(this));
				} else
					break;
			}
		}

		popupRelativeLayout.addView(mainMenuVertialLinearLayout);
		menuBarStack.add(mainMenuVertialLinearLayout);

		return popupRelativeLayout;
	}

	@Override
	public View onCreateInputView() {
		popupKeyboardView = new PopupKeyboardView(this, null);
		popupKeyboardView.setFlightboard(this);
		popupKeyboardView.setOnKeyboardActionListener(this);

		return popupKeyboardView;
	}

	public void showPopupWindow() {
		try {
			WindowManager wm = (WindowManager) this
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wm.getDefaultDisplay();
			DisplayMetrics dm = new DisplayMetrics();
			display.getMetrics(dm);

			int popupHeight = maxConcurrentRows * ItemModel.BUTTON_HEIGHT;
			int popupWidth = dm.widthPixels;

			if (popupWindow == null)
				popupWindow = new PopupWindow(
						getPopupRelativeLayout(mainMenuItems), popupWidth,
						popupHeight, false);

			popupWindow.showAtLocation(popupKeyboardView,
					Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 0);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void handleMenu(MenuModel menu) {
		LinearLayout menuLinearLayout = menu.toLinearLayout(this);
		LinearLayout layoutAbove = menuBarStack.get(menuBarStack.size() - 1);

		RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
				RelativeLayout.TRUE);
		layoutParams.addRule(RelativeLayout.BELOW, layoutAbove.getId());
		menuLinearLayout.setLayoutParams(layoutParams);

		for (int i = 0; i < layoutAbove.getChildCount(); i++) {
			View child = layoutAbove.getChildAt(i);

			if (child instanceof LinearLayout) {
				LinearLayout linearLayoutChild = (LinearLayout) child;

				for (int j = 0; j < linearLayoutChild.getChildCount(); j++) {
					View subchild = linearLayoutChild.getChildAt(j);
					subchild.setEnabled(false);
				}
			}
		}

		popupRelativeLayout.addView(menuLinearLayout);
		menuBarStack.add(menuLinearLayout);
	}

	public void handleCloseButton(MenuModel menu) {
		int n = menuBarStack.indexOf(menu.getLinearLayout());

		for (int i = menuBarStack.size() - 1; i >= n; i--) {
			View v = menuBarStack.get(i);

			popupRelativeLayout.removeView(v);
			menuBarStack.remove(v);
		}

		LinearLayout layoutAbove = menuBarStack.get(menuBarStack.size() - 1);

		for (int i = 0; i < layoutAbove.getChildCount(); i++) {
			View child = layoutAbove.getChildAt(i);

			if (child instanceof LinearLayout) {
				LinearLayout linearLayoutChild = (LinearLayout) child;

				for (int j = 0; j < linearLayoutChild.getChildCount(); j++) {
					View subchild = linearLayoutChild.getChildAt(j);
					subchild.setEnabled(true);
				}
			}
		}
	}

	public void handleActionBegin(ActionModel action) {
		int[] keyCodes = action.getKeyEventCodes();

		if (keyCodes[1] == 1) {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_CTRL_LEFT));
		}
		if (keyCodes[2] == 1) {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_ALT_LEFT));
		}
		if (keyCodes[3] == 1) {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_DOWN,
							KeyEvent.KEYCODE_SHIFT_LEFT));
		}

		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_DOWN, keyCodes[0]));
	}

	public void handleActionEnd(ActionModel action) {
		int[] keyCodes = action.getKeyEventCodes();

		getCurrentInputConnection().sendKeyEvent(
				new KeyEvent(KeyEvent.ACTION_UP, keyCodes[0]));

		if (keyCodes[1] == 1) {
			getCurrentInputConnection()
					.sendKeyEvent(
							new KeyEvent(KeyEvent.ACTION_UP,
									KeyEvent.KEYCODE_CTRL_LEFT));
		}
		if (keyCodes[2] == 1) {
			getCurrentInputConnection()
					.sendKeyEvent(
							new KeyEvent(KeyEvent.ACTION_UP,
									KeyEvent.KEYCODE_ALT_LEFT));
		}
		if (keyCodes[3] == 1) {
			getCurrentInputConnection().sendKeyEvent(
					new KeyEvent(KeyEvent.ACTION_UP,
							KeyEvent.KEYCODE_SHIFT_LEFT));
		}
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
	}

	@Override
	public void onPress(int primaryCode) {
	}

	@Override
	public void onRelease(int primaryCode) {
	}

	@Override
	public void onText(CharSequence text) {
	}

	@Override
	public void swipeDown() {
	}

	@Override
	public void swipeLeft() {
	}

	@Override
	public void swipeRight() {
	}

	@Override
	public void swipeUp() {
	}
}
