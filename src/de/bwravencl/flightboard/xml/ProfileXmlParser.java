package de.bwravencl.flightboard.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import de.bwravencl.flightboard.model.ActionModel;
import de.bwravencl.flightboard.model.ItemModel;
import de.bwravencl.flightboard.model.MenuModel;

import android.util.Xml;

public class ProfileXmlParser {

	private List<ItemModel> allItems = new ArrayList<ItemModel>();
	private List<ItemModel> mainMenuItems = new ArrayList<ItemModel>();

	private int maxDepth = 0;
	private int maxItemsPerMenu = 0;
	private boolean emptyMenu = false;

	public List<ItemModel> getAllItems() {
		return allItems;
	}

	public List<ItemModel> getMainMenuItems() {
		return mainMenuItems;
	}

	public int getMaxConcurrentRows() {
		return maxDepth
				- 1
				+ (int) Math.ceil((float) maxItemsPerMenu
						/ MenuModel.MAX_ITEMS_PER_ROW) - (emptyMenu ? 0 : 1);
	}

	public void parseFile(InputStream in) throws XmlPullParserException,
			IOException {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(in, null);

			parser.next();

			if ("profile".equals(parser.getName()))
				readProfile(parser);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			in.close();
		}
	}

	public void readProfile(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;

			try {
				readItem(parser);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public ItemModel readItem(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String name = parser.getName();

		if (name == null)
			return null;

		if (name.equals("menu"))
			return readMenu(parser);
		else if (name.equals("action"))
			return readAction(parser);

		return null;
	}

	public MenuModel readMenu(XmlPullParser parser)
			throws XmlPullParserException, IOException {

		String title = readTitle(parser);
		MenuModel menu = new MenuModel(title);
		allItems.add(menu);

		if (parser.getDepth() == 2)
			mainMenuItems.add(menu);

		while (parser.next() != XmlPullParser.END_TAG) {
			if (parser.getEventType() != XmlPullParser.START_TAG)
				continue;

			ItemModel item = readItem(parser);

			if (item != null)
				menu.getItems().add(item);

			parser.next();
		}

		int numItems = menu.getItems().size();

		if (numItems == 0)
			emptyMenu = true;
		else if (maxItemsPerMenu < numItems)
			maxItemsPerMenu = numItems;

		return menu;
	}

	public ActionModel readAction(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		String title = readTitle(parser);
		String key = null;
		List<String> modifiers = null;

		for (int i = 0; i < parser.getAttributeCount(); i++) {
			String attributeName = parser.getAttributeName(i);
			String attributeValue = parser.getAttributeValue(i);

			if (attributeName.equals("key"))
				key = attributeValue.toUpperCase(Locale.US);
			else if (attributeName.equals("modifier")) {
				if (attributeValue.length() > 1)
					modifiers = Arrays.asList(attributeValue.split(","));
			}
		}

		ActionModel action = new ActionModel(title, key, modifiers);
		allItems.add(action);

		if (parser.getDepth() == 2)
			mainMenuItems.add(action);

		return action;
	}

	public String readTitle(XmlPullParser parser)
			throws XmlPullParserException, IOException {
		int depth = parser.getDepth();
		if (depth > maxDepth)
			maxDepth = depth;

		for (int i = 0; i < parser.getAttributeCount(); i++) {
			String attributeName = parser.getAttributeName(i);
			String attributeValue = parser.getAttributeValue(i);

			if (attributeName.equals("title"))
				return attributeValue;
		}

		return null;
	}
}
