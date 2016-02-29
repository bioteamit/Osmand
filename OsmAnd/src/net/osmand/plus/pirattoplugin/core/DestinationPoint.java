package net.osmand.plus.pirattoplugin.core;

public class DestinationPoint implements Comparable<DestinationPoint> {

	public static final String TAG_ADDRESS = "address";
	public static final String TAG_LATITUDE = "latitude";
	public static final String TAG_LONGITUDE = "longitude";

	private String address;
	private double latitude;
	private double longitude;

	public DestinationPoint() {
	}

	public DestinationPoint(String address, double latitude, double longitude) {
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DestinationPoint)) return false;
		DestinationPoint another = (DestinationPoint) obj;
		if (!this.address.equalsIgnoreCase(another.address)) return false;
		if (this.latitude != another.latitude) return false;
		if (this.longitude != another.longitude) return false;
		return true;
	}

	@Override
	public int compareTo(DestinationPoint another) {
		if (another == null) return 1;
		if (!this.address.equalsIgnoreCase(another.address)) return 1;
		if (this.latitude != another.latitude) return 1;
		if (this.longitude != another.longitude) return 1;
		return 0;
	}
}
