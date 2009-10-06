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

import com.kesdip.designer.properties.CheckboxPropertyDescriptor;
import com.kesdip.designer.utils.DOMHelpers;

public class TunerVideoComponent extends ComponentModelElement {
	/** A 16x16 pictogram of an elliptical shape. */
	private static final Image IMAGE_ICON = createImage("icons/wireless.png");

	private static final long serialVersionUID = 1L;

	private static final String AnalogVideoType = "Analog TV";
	private static final String DigitalVideoType = "Digital TV";

	/**
	 * A static array of property descriptors. There is one IPropertyDescriptor
	 * entry per editable property.
	 * 
	 * @see #getPropertyDescriptors()
	 * @see #getPropertyValue(Object)
	 * @see #setPropertyValue(Object, Object)
	 */
	private static IPropertyDescriptor[] descriptors;
	/** Property ID to use for the type property value. */
	public static final String TYPE_PROP = "Tuner.TypeProp";
	/** Property ID to use for the video device property value. */
	public static final String VIDEO_DEVICE_PROP = "Tuner.VideoDeviceProp";
	/** Property ID to use for the audio device property value. */
	public static final String AUDIO_DEVICE_PROP = "Tuner.AudioDeviceProp";
	/** Property ID to use for the channel property value. */
	public static final String CHANNEL_PROP = "Tuner.ChannelProp";
	/** Property ID to use for the input property value. */
	public static final String INPUT_PROP = "Tuner.InputProp";
	public static final String FULL_SCREEN_PROP = "Tuner.FullScreen";
	public static final String VIDEO_PROVIDER_PROP = "Video.VideoProvider";

	private static final String STRING_VLC = "VLC";

	private static final String STRING_MPLAYER = "MPlayer";

	/* STATE */
	private String type;
	private String videoDevice;
	private String audioDevice;
	private int channel;
	private int input;
	private String provider;
	private boolean fullScreen;

	public TunerVideoComponent() {
		type = AnalogVideoType;
		videoDevice = "";
		audioDevice = "";
		channel = 0;
		input = 0;
		provider = STRING_MPLAYER;
		fullScreen = false;
	}

	protected Element serialize(Document doc, boolean isPublish) {
		Element videoElement = doc.createElement("bean");

		if (provider.equals(STRING_VLC)) {
			videoElement.setAttribute("class",
					"com.kesdip.player.components.TunerVideo");
		} else if (provider.equals(STRING_MPLAYER)) {
			videoElement.setAttribute("class",
					"com.kesdip.player.components.media.TunerVideo");
		}

		super.serialize(doc, videoElement);
		DOMHelpers.addProperty(doc, videoElement, "type", type
				.equals(AnalogVideoType) ? "1" : "2");
		DOMHelpers.addProperty(doc, videoElement, "videoDevice", videoDevice);
		DOMHelpers.addProperty(doc, videoElement, "audioDevice", audioDevice);
		DOMHelpers.addProperty(doc, videoElement, "channel", String
				.valueOf(channel));
		DOMHelpers.addProperty(doc, videoElement, "input", String
				.valueOf(input));
		DOMHelpers.addProperty(doc, videoElement, "fullScreen", Boolean
				.toString(fullScreen));
		return videoElement;
	}

	protected void deserialize(Document doc, Node componentNode) {
		String t = DOMHelpers.getSimpleProperty(componentNode, "type");

		String className = componentNode.getAttributes().getNamedItem("class")
				.getNodeValue();
		if (className.equals("com.kesdip.player.components.media.TunerVideo")) {
			setPropertyValue(VIDEO_PROVIDER_PROP,
					getProviderType(STRING_MPLAYER));
		} else if (className.equals("com.kesdip.player.components.TunerVideo")) {
			setPropertyValue(VIDEO_PROVIDER_PROP, getProviderType(STRING_VLC));
		}
		setPropertyValue(TYPE_PROP, Integer.parseInt(t) - 1);
		setPropertyValue(VIDEO_DEVICE_PROP, DOMHelpers.getSimpleProperty(
				componentNode, "videoDevice"));
		setPropertyValue(AUDIO_DEVICE_PROP, DOMHelpers.getSimpleProperty(
				componentNode, "audioDevice"));
		setPropertyValue(CHANNEL_PROP, DOMHelpers.getSimpleProperty(
				componentNode, "channel"));
		setPropertyValue(INPUT_PROP, DOMHelpers.getSimpleProperty(
				componentNode, "input"));
		String fullScreen = DOMHelpers.getSimpleProperty(componentNode,
				"fullScreen");
		setPropertyValue(FULL_SCREEN_PROP, fullScreen != null ? fullScreen
				: "false");
		super.deserialize(doc, componentNode);
	}

	public void save(IMemento memento) {
		super.save(memento);
		memento.putString(TAG_TUNER_TYPE, type);
		memento.putString(TAG_VIDEO_DEVICE, videoDevice);
		memento.putString(TAG_AUDIO_DEVICE, audioDevice);
		memento.putInteger(TAG_CHANNEL, channel);
		memento.putInteger(TAG_INPUT, input);
		memento.putString(TAG_TUNER_VIDEO_PROVIDER, provider);
		memento.putBoolean(TAG_TUNER_FULL_SCREEN, fullScreen);
	}

	public void load(IMemento memento) {
		super.load(memento);
		type = memento.getString(TAG_TUNER_TYPE);
		videoDevice = memento.getString(TAG_VIDEO_DEVICE);
		audioDevice = memento.getString(TAG_AUDIO_DEVICE);
		channel = memento.getInteger(TAG_CHANNEL);
		input = memento.getInteger(TAG_INPUT);
		provider = memento.getString(TAG_TUNER_VIDEO_PROVIDER);
		fullScreen = memento.getBoolean(TAG_TUNER_FULL_SCREEN);
	}

	@Override
	void checkEquivalence(ComponentModelElement other) {
		super.checkEquivalence(other);
		assert (other instanceof TunerVideoComponent);
		assert (type.equals(((TunerVideoComponent) other).type));
		assert (videoDevice.equals(((TunerVideoComponent) other).videoDevice));
		assert (audioDevice.equals(((TunerVideoComponent) other).audioDevice));
		assert (channel == ((TunerVideoComponent) other).channel);
		assert (input == ((TunerVideoComponent) other).input);
		assert (provider.equals(((TunerVideoComponent) other).provider));
		assert (fullScreen == ((TunerVideoComponent) other).fullScreen);
	}

	/*
	 * Initializes the property descriptors array.
	 * 
	 * @see #getPropertyDescriptors()
	 * 
	 * @see #getPropertyValue(Object)
	 * 
	 * @see #setPropertyValue(Object, Object)
	 */
	static {
		descriptors = new IPropertyDescriptor[] {
				new ComboBoxPropertyDescriptor(VIDEO_PROVIDER_PROP, "Provider",
						new String[] { STRING_MPLAYER, STRING_VLC }),
				new ComboBoxPropertyDescriptor(TYPE_PROP, "Type", new String[] {
						AnalogVideoType, DigitalVideoType }),
				new CheckboxPropertyDescriptor(FULL_SCREEN_PROP, "Full Screen"),
				new TextPropertyDescriptor(CHANNEL_PROP, "Channel"),
				new TextPropertyDescriptor(VIDEO_DEVICE_PROP,
						"Video Device (VLC only)"),
				new TextPropertyDescriptor(AUDIO_DEVICE_PROP,
						"Audio Device (VLC only)"),
				new TextPropertyDescriptor(INPUT_PROP, "Input Pin (VLC only)") };
		// use a custom cell editor validator for the array entries
		for (int i = 0; i < descriptors.length; i++) {
			((PropertyDescriptor) descriptors[i]).setCategory("Behaviour");
			if (CHANNEL_PROP.equals(descriptors[i].getId())
					|| INPUT_PROP.equals(descriptors[i].getId())) {
				((PropertyDescriptor) descriptors[i])
						.setValidator(new ICellEditorValidator() {
							public String isValid(Object value) {
								try {
									Integer.parseInt((String) value);
								} catch (NumberFormatException e) {
									return "'" + value
											+ "' is not a valid integer.";
								}
								return null;
							}
						});
			} else {
				((PropertyDescriptor) descriptors[i])
						.setValidator(new ICellEditorValidator() {
							public String isValid(Object value) {
								// No validation for the device
								return null;
							}
						});
			}
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

	private int getTunerType(String t) {
		if (t.equals(AnalogVideoType)) {
			return 0;
		} else if (t.equals(DigitalVideoType)) {
			return 1;
		} else {
			throw new RuntimeException("Unknown tuner type.");
		}
	}

	@Override
	public Object getPropertyValue(Object propertyId) {
		if (CHANNEL_PROP.equals(propertyId)) {
			return String.valueOf(channel);
		} else if (INPUT_PROP.equals(propertyId)) {
			return String.valueOf(input);
		} else if (VIDEO_DEVICE_PROP.equals(propertyId)) {
			return videoDevice;
		} else if (AUDIO_DEVICE_PROP.equals(propertyId)) {
			return audioDevice;
		} else if (TYPE_PROP.equals(propertyId)) {
			return getTunerType(type);
		} else if (VIDEO_PROVIDER_PROP.equals(propertyId)) {
			return getProviderType(provider);
		} else if (FULL_SCREEN_PROP.equals(propertyId)) {
			return fullScreen;
		} else {
			return super.getPropertyValue(propertyId);
		}
	}

	@Override
	public void setPropertyValue(Object propertyId, Object value) {
		if (CHANNEL_PROP.equals(propertyId)) {
			String oldValue = String.valueOf(channel);
			channel = Integer.parseInt((String) value);
			firePropertyChange(CHANNEL_PROP, oldValue, value);
		} else if (INPUT_PROP.equals(propertyId)) {
			String oldValue = String.valueOf(input);
			input = Integer.parseInt((String) value);
			firePropertyChange(INPUT_PROP, oldValue, value);
		} else if (VIDEO_DEVICE_PROP.equals(propertyId)) {
			String oldValue = videoDevice;
			videoDevice = (String) value;
			firePropertyChange(VIDEO_DEVICE_PROP, oldValue, videoDevice);
		} else if (AUDIO_DEVICE_PROP.equals(propertyId)) {
			String oldValue = audioDevice;
			audioDevice = (String) value;
			firePropertyChange(AUDIO_DEVICE_PROP, oldValue, audioDevice);
		} else if (TYPE_PROP.equals(propertyId)) {
			int oldValue = getTunerType(type);
			int v = ((Integer) value).intValue();
			if (v == 0) {
				type = AnalogVideoType;
			} else if (v == 1) {
				type = DigitalVideoType;
			} else {
				throw new RuntimeException("Unexpected tuner type.");
			}
			firePropertyChange(TYPE_PROP, oldValue, value);
		} else if (VIDEO_PROVIDER_PROP.equals(propertyId)) {
			if (value == null) {
				value = 0;
			}
			int oldValue = getProviderType(provider);
			int v = ((Integer) value).intValue();
			if (v == 0) {
				provider = STRING_MPLAYER;
			} else if (v == 1) {
				provider = STRING_VLC;
			} else {
				throw new RuntimeException("Unexpected provider type.");
			}
			firePropertyChange(VIDEO_PROVIDER_PROP, oldValue, provider);
		} else if (FULL_SCREEN_PROP.equals(propertyId)) {
			if (value instanceof String) {
				// We are being deserialized
				String oldValue = fullScreen ? "true" : "false";
				fullScreen = value.equals("true");
				firePropertyChange(FULL_SCREEN_PROP, oldValue, value);
				return;
			}
			Boolean oldValue = fullScreen;
			fullScreen = ((Boolean) value).booleanValue();
			firePropertyChange(FULL_SCREEN_PROP, oldValue, fullScreen);
		} else {
			super.setPropertyValue(propertyId, value);
		}
	}

	private int getProviderType(String t) {
		if (t.equals(STRING_MPLAYER)) {
			return 0;
		} else if (t.equals(STRING_VLC)) {
			return 1;
		} else {
			throw new RuntimeException("Unknown provider type.");
		}
	}

	public void relocateChildren(Point moveBy) {
		// Intentionally empty. Component not a container.
	}

	@Override
	public ModelElement deepCopy() {
		TunerVideoComponent retVal = new TunerVideoComponent();
		retVal.deepCopy(this);
		retVal.type = this.type;
		retVal.videoDevice = this.videoDevice;
		retVal.audioDevice = this.audioDevice;
		retVal.channel = this.channel;
		retVal.input = this.input;
		retVal.provider = this.provider;
		retVal.fullScreen = this.fullScreen;
		return retVal;
	}

	@Override
	public Image getIcon() {
		return IMAGE_ICON;
	}

	public String toString() {
		return "TunerVideo: (" + type + "," + videoDevice + "," + audioDevice
				+ "," + channel + "," + input + ")";
	}

}
