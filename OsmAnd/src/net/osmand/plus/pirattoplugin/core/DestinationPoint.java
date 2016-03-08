package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;

import java.io.Serializable;

public class DestinationPoint implements Comparable<DestinationPoint>, Serializable {

	public static final String TAG_ADDRESS = "address";
	public static final String TAG_LATITUDE = "latitude";
	public static final String TAG_LONGITUDE = "longitude";

	private String address;
	private LatLon latLon;

	public DestinationPoint() {
	}

	public DestinationPoint(PointDescription pointDescription) {
		if (pointDescription == null) {
			return;
		}

		this.latLon = new LatLon(pointDescription.getLat(), pointDescription.getLon());
	}

	public DestinationPoint(String address, double latitude, double longitude) {
		this.address = address;
		this.latLon = new LatLon(latitude, longitude);
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setLatLong(double latitude, double longitude) {
		this.latLon = new LatLon(latitude, longitude);
	}

	public String getAddress() {
		return address;
	}

	public double getLatitude() {
		return this.latLon.getLatitude();
	}

	public double getLongitude() {
		return this.latLon.getLongitude();
	}

	public LatLon getPoint() {
		return this.latLon;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DestinationPoint)) return false;
		DestinationPoint another = (DestinationPoint) obj;
		if (TextUtils.isEmpty(this.address) && !TextUtils.isEmpty(another.address)) return false;
		if (!TextUtils.isEmpty(this.address) && !this.address.equalsIgnoreCase(another.address)) return false;
		if (this.latLon.getLatitude() != another.getLatitude()) return false;
		if (this.latLon.getLongitude() != another.getLongitude()) return false;
		return true;
	}

	@Override
	public int compareTo(DestinationPoint another) {
		if (another == null) return 1;
		if (!this.address.equalsIgnoreCase(another.address)) return 1;
		if (this.latLon.getLatitude() != another.getLatitude()) return 1;
		if (this.latLon.getLongitude() != another.getLongitude()) return 1;
		return 0;
	}
}
