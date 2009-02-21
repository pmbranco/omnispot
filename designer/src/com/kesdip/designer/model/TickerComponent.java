package com.kesdip.designer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kesdip.designer.utils.DOMHelpers;

public class TickerComponent extends ComponentModelElement {
	/** A 16x16 pictogram of an elliptical shape. */
	private static final Image IMAGE_ICON = createImage("icons/alt_window_16.gif");
	
	private static final long serialVersionUID = 1L;

	/** 
	 * A static array of property descriptors.
	 * There is one IPropertyDescriptor entry per editable property.
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	private static IPropertyDescriptor[] descriptors;
	/** Property ID to use for the ticker source type property value. */
	public static final String TYPE_PROP = "Ticker.TickerTypeProp";
	public static final String STRING_TICKER_TYPE = "String Ticker Type";
	public static final String RSS_TICKER_TYPE = "RSS Ticker Type";
	/** Property ID to use for the string property value. */
	public static final String STRING_PROP = "Ticker.TickerStringProp";
	/** Property ID to use for the url property value. */
	public static final String URL_PROP = "Ticker.TickerURLProp";

	/* STATE */
	private String type;
	private String url;
	private String string;
	
	public TickerComponent() {
		type = STRING_TICKER_TYPE;
		url = "";
		string = "";
	}

	protected Element serialize(Document doc) {
		Element tickerElement = doc.createElement("bean");
		tickerElement.setAttribute("class", "com.kesdip.player.components.Ticker");
		super.serialize(doc, tickerElement);
		Element tickerSourcePropElement = DOMHelpers.addProperty(
				doc, tickerElement, "tickerSource");
		Element tickerSourceElement = doc.createElement("bean");
		if (type.equals(STRING_TICKER_TYPE)) {
			tickerSourceElement.setAttribute(
					"class", "com.kesdip.player.components.ticker.StringTickerSource");
			DOMHelpers.addProperty(doc, tickerSourceElement, "src", string);
		} else { /* assuming type is RSS_TICKER_TYPE */
			tickerSourceElement.setAttribute(
					"class", "com.kesdip.player.components.ticker.RssTickerSource");
			DOMHelpers.addProperty(doc, tickerSourceElement, "rssUrl", url);
		}
		tickerSourcePropElement.appendChild(tickerSourceElement);
		return tickerElement;
	}
	
	protected void deserialize(Document doc, Node componentNode) {
		super.deserialize(doc, componentNode);
		Node tickerSourcePropNode = DOMHelpers.getPropertyNode(componentNode, "tickerSource");
		NodeList children = tickerSourcePropNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE &&
					child.getNodeName().equals("bean")) {
				String className = child.getAttributes().
						getNamedItem("class").getNodeValue();
				if ("com.kesdip.player.components.ticker.StringTickerSource".equals(className)) {
					setPropertyValue(TYPE_PROP, getTickerType(STRING_TICKER_TYPE));
					setPropertyValue(STRING_PROP, DOMHelpers.getSimpleProperty(child, "src"));
				} else if ("com.kesdip.player.components.ticker.RssTickerSource".equals(className)) {
					setPropertyValue(TYPE_PROP, getTickerType(RSS_TICKER_TYPE));
					setPropertyValue(URL_PROP, DOMHelpers.getSimpleProperty(child, "rssUrl"));
				} else {
					throw new RuntimeException("Unexpected ticker source class: " + className);
				}
				break;
			}
		}
	}
	
	@Override
	void checkEquivalence(ComponentModelElement other) {
		super.checkEquivalence(other);
		assert(other instanceof TickerComponent);
		assert(type.equals(((TickerComponent) other).type));
		assert(url.equals(((TickerComponent) other).url));
		assert(string.equals(((TickerComponent) other).string));
	}
	
	/*
	 * Initializes the property descriptors array.
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	static {
		descriptors = new IPropertyDescriptor[] {
				new ComboBoxPropertyDescriptor(TYPE_PROP, "Ticker Type",
						new String[] { STRING_TICKER_TYPE, RSS_TICKER_TYPE }),
				new TextPropertyDescriptor(URL_PROP, "RSS URL"),
				new TextPropertyDescriptor(STRING_PROP, "String Source")
		};
		// use a custom cell editor validator for all three array entries
		for (int i = 0; i < descriptors.length; i++) {
			((PropertyDescriptor) descriptors[i]).setValidator(new ICellEditorValidator() {
				public String isValid(Object value) {
					return null;
				}
			});
		}
	} // static

	@Override
	public IPropertyDescriptor[] getPropertyDescriptors() {
		List<IPropertyDescriptor> superList = new ArrayList<IPropertyDescriptor>(
				Arrays.asList(super.getPropertyDescriptors()));
		superList.addAll(Arrays.asList(descriptors));
		IPropertyDescriptor[] retVal = new IPropertyDescriptor[superList.size()];
		int counter = 0;
		for (IPropertyDescriptor pd : superList) {
			retVal[counter++] = pd;
		}
		return retVal;
	}
	
	private int getTickerType(String t) {
		if (t.equals(STRING_TICKER_TYPE))
			return 0;
		else if (t.equals(RSS_TICKER_TYPE))
			return 1;
		else
			throw new RuntimeException("Unknown ticker type.");		
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (URL_PROP.equals(propertyId))
			return url;
		else if (STRING_PROP.equals(propertyId))
			return string;
		else if (TYPE_PROP.equals(propertyId)) {
			return getTickerType(type);
		} else
			return super.getPropertyValue(propertyId);
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (URL_PROP.equals(propertyId)) {
			String oldValue = url;
			url = (String) value;
			firePropertyChange(URL_PROP, oldValue, url);
		} else if (STRING_PROP.equals(propertyId)) {
			String oldValue = string;
			string = (String) value;
			firePropertyChange(STRING_PROP, oldValue, string);
		} else if (TYPE_PROP.equals(propertyId)) {
			int oldValue = getTickerType(type);
			int v = ((Integer) value).intValue();
			if (v == 0)
				type = STRING_TICKER_TYPE;
			else if (v == 1)
				type = RSS_TICKER_TYPE;
			else
				throw new RuntimeException("Unexpected ticker type.");
			firePropertyChange(TYPE_PROP, oldValue, value);
		} else
			super.setPropertyValue(propertyId, value);
	}

	@Override
	public Image getIcon() {
		return IMAGE_ICON;
	}
	
	public String toString() {
		return "Ticker(" + type + "," + url + "," + string + ")";
	}

}
