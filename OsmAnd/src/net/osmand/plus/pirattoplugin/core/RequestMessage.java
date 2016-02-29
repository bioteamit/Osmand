package net.osmand.plus.pirattoplugin.core;

import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;

public class RequestMessage {

	private static final String TAG_SIMPLE_MESSAGE = "pl.kubryk.messanger.server.SimpleMessage";
	public static final String TAG_TYPE = "type";
	public static final String TAG_MESSAGE = "message";
	public static final String ATTR_CLASS = "class";
	public static final String ATTR_CLASS_VALUE = "pl.kubryk.pizzeria.message.RequestDestinationPoints";
	public static final String TAG_CAR_REGISTRATION_PLATE = "carRegistrationPlate";

	private int type;
	private String carPlate;

	public RequestMessage(int type, String carPlate) {
		this.type = type;
		this.carPlate = carPlate;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCarPlate() {
		return carPlate;
	}

	public void setCarPlate(String carPlate) {
		this.carPlate = carPlate;
	}

	@Override
	public String toString() {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", TAG_SIMPLE_MESSAGE);

			serializer.startTag("", TAG_TYPE);
			serializer.text(String.valueOf(this.type));
			serializer.endTag("", TAG_TYPE);

			serializer.startTag("", TAG_MESSAGE);
			serializer.attribute("", ATTR_CLASS, ATTR_CLASS_VALUE);

			serializer.startTag("", TAG_CAR_REGISTRATION_PLATE);
			serializer.text(this.carPlate);
			serializer.endTag("", TAG_CAR_REGISTRATION_PLATE);
			serializer.endTag("", TAG_MESSAGE);

			serializer.endTag("", TAG_SIMPLE_MESSAGE);
			serializer.endDocument();
			serializer.flush();
		} catch (Exception e) {
			return null;
		}
		return writer.toString();
	}
}
