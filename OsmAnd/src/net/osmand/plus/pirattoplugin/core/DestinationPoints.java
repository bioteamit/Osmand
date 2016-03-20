package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class DestinationPoints {

	private static final String TAG_DESTINATION_POINTS = "pl.kubryk.gps.message.DestinationPoints";
	private static final String TAG_LIST = "list";
	private static final String TAG_DEFAULT = "default";
	private static final String TAG_SIZE = "size";
	private static final String TAG_DESTINATION_POINT = "pl.kubryk.gps.message.DestinationPoint";

	private List<DestinationPoint> destinationPoints;

	private String destinationPointsFilePath;

	public DestinationPoints() {
		this.destinationPoints = new ArrayList<>();
	}

	public void setDestinationPointsFilePath(String filePath) {
		this.destinationPointsFilePath = filePath;
	}

	public boolean updatePoints(DestinationPoints destinationPoints) {
		if (destinationPoints == null
				|| destinationPoints.getDestinationPoints().isEmpty()) {
			return false;
		}

		int size = this.destinationPoints.size();
		for (DestinationPoint point : destinationPoints.getDestinationPoints()) {
			if (!this.destinationPoints.contains(point)) {
				this.addPoint(point);
			}
		}

		return (this.destinationPoints.size() > size);
	}

	public List<DestinationPoint> getDestinationPoints() {
		return this.destinationPoints;
	}

	public void addPoint(DestinationPoint point) {
		this.destinationPoints.add(point);
	}

	public boolean removePoint(DestinationPoint point) {
		return this.destinationPoints.remove(point);
	}

	protected void removePoint(int index) {
		this.destinationPoints.remove(index);
	}

	public static DestinationPoints parse(InputStream inputStream) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			parser.setInput(inputStream, "UTF-8");
			return parse(parser);
		} catch (Exception e) {
			return null;
		}
	}

	public static DestinationPoints parse(String rawPoints) {
		XmlPullParser parser = Xml.newPullParser();
		try {
			StringReader reader = new StringReader(rawPoints);
			parser.setInput(reader);
			return parse(parser);
		} catch (Exception e) {
			return null;
		}
	}

	private static DestinationPoints parse(XmlPullParser parser) {
		DestinationPoints destinationPoints = new DestinationPoints();
		try {
			int eventType = parser.getEventType();
			DestinationPoint destinationPoint = null;
			double latitude = -1;
			double longitude = -1;
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				String name = null;
				switch (eventType) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(TAG_DESTINATION_POINT)) {
							destinationPoint = new DestinationPoint();
							latitude = -1;
							longitude = -1;
						} else if (destinationPoint != null) {
							if (name.equalsIgnoreCase(DestinationPoint.TAG_ADDRESS)) {
								destinationPoint.setAddress(parser.nextText());
							} else if (name.equalsIgnoreCase(DestinationPoint.TAG_LATITUDE)) {
								latitude = Double.parseDouble(parser.nextText());
							} else if (name.equalsIgnoreCase(DestinationPoint.TAG_LONGITUDE)){
								longitude = Double.parseDouble(parser.nextText());
							}
						}
						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(TAG_DESTINATION_POINT)
								&& destinationPoint != null) {
							if (latitude > 0 && longitude > 0) {
								destinationPoint.setLatLong(latitude, longitude);
								destinationPoints.addPoint(destinationPoint);
							}
						}
						break;
				}

				// skip "OK"
				try {
					eventType = parser.next();
				} catch (Exception e) {
					eventType = parser.next();
				}
			}
		} catch (Exception e) {
			return null;
		}

		return destinationPoints;
	}

	public boolean commit() {
		if (TextUtils.isEmpty(this.destinationPointsFilePath)) {
			return false;
		}

		XmlSerializer serializer = Xml.newSerializer();
		try {
			FileWriter fileWriter = new FileWriter(this.destinationPointsFilePath);
			serializer.setOutput(fileWriter);
			serializer.startDocument("UTF-8", true);

			serializer.startTag("", TAG_DESTINATION_POINTS);
			serializer.startTag("", TAG_LIST);
			serializer.startTag("", TAG_DEFAULT);
			serializer.startTag("", TAG_SIZE);
			serializer.text(String.valueOf(this.destinationPoints.size()));
			serializer.endTag("", TAG_SIZE);
			serializer.endTag("", TAG_DEFAULT);

			for (DestinationPoint point: this.destinationPoints) {
				serializer.startTag("", TAG_DESTINATION_POINT);
				serializer.startTag("", DestinationPoint.TAG_ADDRESS);
				serializer.text(point.getAddress());
				serializer.endTag("", DestinationPoint.TAG_ADDRESS);
				serializer.startTag("", DestinationPoint.TAG_LATITUDE);
				serializer.text(String.valueOf(point.getLatitude()));
				serializer.endTag("", DestinationPoint.TAG_LATITUDE);
				serializer.startTag("", DestinationPoint.TAG_LONGITUDE);
				serializer.text(String.valueOf(point.getLongitude()));
				serializer.endTag("", DestinationPoint.TAG_LONGITUDE);
				serializer.endTag("", TAG_DESTINATION_POINT);
			}
			serializer.endTag("", TAG_LIST);
			serializer.endTag("", TAG_DESTINATION_POINTS);
			serializer.endDocument();
			serializer.flush();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
