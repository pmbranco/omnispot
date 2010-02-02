package com.kesdip.designer.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.kesdip.designer.properties.FileChooserPropertyDescriptor;
import com.kesdip.designer.utils.DOMHelpers;

public class FlashWeatherComponent extends ComponentModelElement {
	/** A 16x16 pictogram of an elliptical shape. */
	private static final Image IMAGE_ICON = createImage("icons/cloud_sun.png");
	
	private static final long serialVersionUID = 1L;

	/** 
	 * A static array of property descriptors.
	 * There is one IPropertyDescriptor entry per editable property.
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	private static IPropertyDescriptor[] descriptors;
	public static final String SOURCE_PROP = "Weather.SourceProp";
	/** Property ID to use for the weather source type property value. */
	public static final String TYPE_PROP = "Weather.WeatherSourceTypeProp";
	public static final String URL_XML_WEATHER_TYPE = "URL XML Weather Source Type";
	public static final String RSS_WEATHER_TYPE = "RSS Weather Source Type";
	public static final String SCRIPTED_PUSH_WEATHER_TYPE = "Scripted Push Weather Source Type";
	/** Property ID to use for the xml property value. */
	public static final String RSS_PROP = "Weather.WeatherRSSProp";
	/** Property ID to use for the url property value. */
	public static final String URL_PROP = "Weather.WeatherURLProp";
	/** Property ID to use for the script file property value. */
	public static final String SCRIPT_FILE_PROP = "Weather.ScriptFileProp";

	/* STATE */
	private String source;
	private String type;
	private String url;
	private String rss;
	private String scriptFile;
	
	public FlashWeatherComponent() {
		source = "";
		type = SCRIPTED_PUSH_WEATHER_TYPE;
		url = "";
		rss = "";
		scriptFile = "";
	}

	protected Element serialize(Document doc, boolean isPublish) {
		Element tickerElement = doc.createElement("bean");
		tickerElement.setAttribute("class", "com.kesdip.player.components.weather.FlashWeatherComponent");
		super.serialize(doc, tickerElement);
		Element sourcePropElement = DOMHelpers.addProperty(doc, tickerElement, "source");
		Resource r = new Resource(source, "");
		sourcePropElement.appendChild(r.serialize(doc, isPublish));
		Element weatherSourcePropElement = DOMHelpers.addProperty(
				doc, tickerElement, "weatherDataSource");
		Element weatherSourceElement = doc.createElement("bean");
		
		boolean legacyAddWeatherDataProcessor = true;
		if (type.equals(URL_XML_WEATHER_TYPE)) {
			weatherSourceElement.setAttribute(
					"class", "com.kesdip.player.components.weather.URLXmlSource");
			DOMHelpers.addProperty(doc, weatherSourceElement, "url", url);
		} else if (type.equals(RSS_WEATHER_TYPE)){ /* assuming type is RSS_WEATHER_TYPE */
			weatherSourceElement.setAttribute(
					"class", "com.kesdip.player.components.weather.RssWeatherSource");
			DOMHelpers.addProperty(doc, weatherSourceElement, "rssUrl", rss);
		} else if (type.equals(SCRIPTED_PUSH_WEATHER_TYPE)) {
			legacyAddWeatherDataProcessor = false;
			weatherSourceElement.setAttribute(
					"class", "com.kesdip.player.components.weather.ScriptedPushDataSource");
			Element scriptFilePropElement = DOMHelpers.addProperty(doc, weatherSourceElement, "scriptFile");
			Resource resource = new Resource(scriptFile, "");
			Element resourceElement = resource.serialize(doc, isPublish);
			scriptFilePropElement.appendChild(resourceElement);
		}
		
		weatherSourcePropElement.appendChild(weatherSourceElement);
		
		if (legacyAddWeatherDataProcessor) {
			Element processorPropElement = DOMHelpers.addProperty(doc, weatherSourceElement, "weatherDataProcessor");
			Element processorElement = doc.createElement("bean");
			processorElement.setAttribute("class", "com.kesdip.player.components.weather.ScriptingDataProcessor");
			processorPropElement.appendChild(processorElement);
			Element scriptFilePropElement = DOMHelpers.addProperty(doc, processorElement, "scriptFile");
			Resource resource = new Resource(scriptFile, "");
			Element resourceElement = resource.serialize(doc, isPublish);
			scriptFilePropElement.appendChild(resourceElement);
		}
		
		return tickerElement;
	}
	
	protected void deserialize(Document doc, Node componentNode) {
		super.deserialize(doc, componentNode);
		Node sourcePropNode = DOMHelpers.getPropertyNode(componentNode, "source");
		NodeList children = sourcePropNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE &&
					child.getNodeName().equals("bean")) {
				Resource r = new Resource("", "");
				r.deserialize(doc, child);
				setPropertyValue(SOURCE_PROP, r.getResource());
			}
		}
		Node tickerSourcePropNode = DOMHelpers.getPropertyNode(componentNode, "weatherDataSource");
		children = tickerSourcePropNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE &&
					child.getNodeName().equals("bean")) {
				String className = child.getAttributes().
						getNamedItem("class").getNodeValue();
				if ("com.kesdip.player.components.weather.URLXmlSource".equals(className)) {
					setPropertyValue(TYPE_PROP, getWeatherSourceType(URL_XML_WEATHER_TYPE));
					setPropertyValue(URL_PROP, DOMHelpers.getSimpleProperty(child, "url"));
					Node processorPropNode = DOMHelpers.getPropertyNode(child, "weatherDataProcessor");
					NodeList processorChildren = processorPropNode.getChildNodes();
					for (int j = 0; j < processorChildren.getLength(); j++) {
						Node processorChild = processorChildren.item(j);
						if (processorChild.getNodeType() == Node.ELEMENT_NODE &&
								processorChild.getNodeName().equals("bean")) {
							Node scriptFilePropNode = DOMHelpers.getPropertyNode(processorChild, "scriptFile");
							NodeList nl = scriptFilePropNode.getChildNodes();
							for (int k = 0; k < nl.getLength(); k++) {
								Node nc = nl.item(k);
								if (nc.getNodeType() == Node.ELEMENT_NODE &&
										nc.getNodeName().equals("bean")) {
									Resource r = new Resource("", "");
									r.deserialize(doc, nc);
									setPropertyValue(SCRIPT_FILE_PROP, r.getResource());
								}
							}
						}
					}
				} else if ("com.kesdip.player.components.weather.RssWeatherSource".equals(className)) {
					setPropertyValue(TYPE_PROP, getWeatherSourceType(RSS_WEATHER_TYPE));
					setPropertyValue(RSS_PROP, DOMHelpers.getSimpleProperty(child, "rssUrl"));
				} else {
					throw new RuntimeException("Unexpected weather source class: " + className);
				}
				break;
			}
		}
	}
	
	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_SOURCE, source);
		memento.putString(TAG_WEATHER_TYPE, type);
		memento.putString(TAG_WEATHER_URL, url);
		memento.putString(TAG_WEATHER_RSS, rss);
		memento.putString(TAG_WEATHER_SCRIPT, scriptFile);
	}
	
	public void load(IMemento memento) {
		super.load(memento);
		source = memento.getString(TAG_SOURCE);
		type = memento.getString(TAG_WEATHER_TYPE);
		url = memento.getString(TAG_WEATHER_URL);
		rss = memento.getString(TAG_WEATHER_RSS);
		scriptFile = memento.getString(TAG_WEATHER_SCRIPT);
	}
	
	@Override
	void checkEquivalence(ComponentModelElement other) {
		super.checkEquivalence(other);
		assert(other instanceof FlashWeatherComponent);
		assert(source.equals(((FlashWeatherComponent) other).source));
		assert(type.equals(((FlashWeatherComponent) other).type));
		assert(url.equals(((FlashWeatherComponent) other).url));
		assert(rss.equals(((FlashWeatherComponent) other).rss));
		assert(scriptFile.equals(((FlashWeatherComponent) other).scriptFile));
	}
	
	/*
	 * Initializes the property descriptors array.
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	static {
		descriptors = new IPropertyDescriptor[] {
				new FileChooserPropertyDescriptor(SOURCE_PROP, "Source"),
				new ComboBoxPropertyDescriptor(TYPE_PROP, "Weather Source Type",
						new String[] { SCRIPTED_PUSH_WEATHER_TYPE, URL_XML_WEATHER_TYPE, RSS_WEATHER_TYPE }),
				new TextPropertyDescriptor(URL_PROP, "XML URL"),
				new TextPropertyDescriptor(RSS_PROP, "RSS URL"),
				new FileChooserPropertyDescriptor(SCRIPT_FILE_PROP, "Script File")
		};
		// use a custom cell editor validator for all three array entries
		for (int i = 0; i < descriptors.length; i++) {
			((PropertyDescriptor) descriptors[i]).setCategory("Behaviour");
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

	private int getWeatherSourceType(String t) {
		if (t.equals(URL_XML_WEATHER_TYPE)) {
			return 0;
		} else if (t.equals(RSS_WEATHER_TYPE)) {
			return 1;
		} else if (t.equals(SCRIPTED_PUSH_WEATHER_TYPE)) {
			return 2;
		} else {
			throw new RuntimeException("Unknown weather source type.");
		}
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (SOURCE_PROP.equals(propertyId))
			return source;
		else if (URL_PROP.equals(propertyId))
			return url;
		else if (RSS_PROP.equals(propertyId))
			return rss;
		else if (SCRIPT_FILE_PROP.equals(propertyId))
			return scriptFile;
		else if (TYPE_PROP.equals(propertyId))
			return getWeatherSourceType(type);
		else
			return super.getPropertyValue(propertyId);
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (SOURCE_PROP.equals(propertyId)) {
			String oldValue = source;
			source = (String) value;
			firePropertyChange(SOURCE_PROP, oldValue, source);
		} else if (URL_PROP.equals(propertyId)) {
			String oldValue = url;
			url = (String) value;
			firePropertyChange(URL_PROP, oldValue, url);
		} else if (RSS_PROP.equals(propertyId)) {
			String oldValue = rss;
			rss = (String) value;
			firePropertyChange(RSS_PROP, oldValue, rss);
		} else if (SCRIPT_FILE_PROP.equals(propertyId)) {
			String oldValue = scriptFile;
			scriptFile = (String) value;
			firePropertyChange(SCRIPT_FILE_PROP, oldValue, scriptFile);
		} else if (TYPE_PROP.equals(propertyId)) {
			int oldValue = getWeatherSourceType(type);
			int v = ((Integer) value).intValue();
			if (v == 0)
				type = URL_XML_WEATHER_TYPE;
			else if (v == 1)
				type = RSS_WEATHER_TYPE;
			else if (v == 2)
				type = SCRIPTED_PUSH_WEATHER_TYPE;
			else
				throw new RuntimeException("Unexpected weather source type.");
			firePropertyChange(TYPE_PROP, oldValue, value);
		} else
			super.setPropertyValue(propertyId, value);
	}
	
	public void relocateChildren(Point moveBy) {
		// Intentionally empty. Component not a container.
	}
	
	public ModelElement deepCopy() {
		FlashWeatherComponent retVal = new FlashWeatherComponent();
		retVal.deepCopy(this);
		retVal.source = this.source;
		retVal.type = this.type;
		retVal.rss = this.rss;
		retVal.url = this.url;
		retVal.scriptFile = this.scriptFile;
		return retVal;
	}

	@Override
	public Image getIcon() {
		return IMAGE_ICON;
	}
	
	public String toString() {
		return "FlashWeather(" + source + "," + type + "," + url + "," + rss +
			"," + scriptFile + ")";
	}

}
