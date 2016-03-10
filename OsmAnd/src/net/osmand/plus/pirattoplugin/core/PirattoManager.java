package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Log;

import net.osmand.data.LatLon;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;

import java.util.List;
import java.util.Timer;

public class PirattoManager implements PointsRetrieverTask.OnRetrievingPointsCallback {

	public final static String PIRATTO_CAR_PLATE = "piratto_car_plate"; //$NON-NLS-1$
	private static final String TAG = "PirattoManager";

	public interface OnUpdatePointsListener {
		void updatePoints(List<DestinationPoint> points);
	}

	private static PirattoManager instance;

	private final int TIME_INTERVAL = 1*60*1000;

	private Timer timer;
	private PointsRetrieverTask pointsRetrieverTask;
	private OnUpdatePointsListener onUpdatePointsListener;

	private String carPlate;
	private DestinationPoints destinationPoints;

	// Target destination point
	private OsmandSettings.CommonPreference<String> targetPointAddressSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLatitudeSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLongitudeSettings;
	///////
	private OsmandSettings.CommonPreference<String> carPlateSettings;

	public static PirattoManager getInstance() {
		if (PirattoManager.instance == null) {
			PirattoManager.instance = new PirattoManager();
		}

		return PirattoManager.instance;
	}

	protected PirattoManager() {
		this.destinationPoints = new DestinationPoints();
		this.addTestDestinationPoints();
	}

	private void addTestDestinationPoints() {
		this.addTestDestinationPoint("Shoubra", 30.0906098, 31.2455468);
		this.addTestDestinationPoint("Qalioub1", 30.1736758, 31.2250333);
		this.addTestDestinationPoint("Qaliob2", 30.1796859, 31.2214284);
		this.addTestDestinationPoint("DownTown", 30.0165934, 31.417473);
	}

	private void addTestDestinationPoint(String address, double latitude, double longitude) {
		this.destinationPoints.addPoint(new DestinationPoint(address, latitude, longitude));
	}

	public void setCarPlate(OsmandApplication application, String newCarPlate) {
		if (!this.shouldChangeCarPlate(newCarPlate)) {
			return;
		}

		OsmandSettings settings = application.getSettings();
		this.carPlateSettings = settings.registerStringPreference(PIRATTO_CAR_PLATE, null).makeGlobal();
		this.carPlateSettings.set(newCarPlate);
		this.carPlate = newCarPlate;
		this.refresh();
	}

	public void setDestinationPoint(OsmandApplication application, DestinationPoint destinationPoint) {
		OsmandSettings settings = application.getSettings();
//		this.destinationPointSettings = settings.registerStringPreference(PIRATTO_CAR_PLATE, null).makeGlobal();
	}

	public List<DestinationPoint> getDestinationPoints() {
		if (this.destinationPoints == null) {
			return null;
		}
		return this.destinationPoints.getDestinationPoints();
	}

	public DestinationPoint getDestinationPoint(LatLon point) {
		if (this.destinationPoints == null
				|| this.destinationPoints.getDestinationPoints() == null
				|| this.destinationPoints.getDestinationPoints().isEmpty()) {
			return null;
		}

		for (DestinationPoint dstPoint : this.destinationPoints.getDestinationPoints()) {
			if (dstPoint.getPoint().equals(point)) {
				return dstPoint;
			}
		}

		return null;
	}

	public void setOnUpdatePointsListener(OnUpdatePointsListener listener) {
		this.onUpdatePointsListener = listener;
	}

	public void refresh() {
		this.cancelSchedule();
		this.pointsRetrieverTask = new PointsRetrieverTask(this.carPlate, this);
		this.timer = new Timer();
		this.timer.scheduleAtFixedRate(this.pointsRetrieverTask, 0, TIME_INTERVAL);
	}

	public void cancelSchedule() {
		if (this.pointsRetrieverTask != null) {
			this.pointsRetrieverTask.cancel();
		}
		if (this.timer != null) {
			this.timer.cancel();
			this.timer.purge();
		}
	}

	public void removeDestinationPoint(DestinationPoint destinationPoint) {
		this.destinationPoints.removePoint(destinationPoint);
		this.destinationPoints.commit();
	}

	@Override
	public void onSuccess(String carPlate, DestinationPoints points) {
		if (this.onUpdatePointsListener == null) {
			return;
		}

		boolean updated = this.destinationPoints.updatePoints(points);
		if (updated) {
			Log.d(TAG, "Points are updated");
			this.onUpdatePointsListener.updatePoints(this.destinationPoints.getDestinationPoints());
		}
		this.destinationPoints.commit();
	}

	@Override
	public void onFailure(String carPlate, String message) {
		Log.w(TAG, "Failed to retrieve destination points for car [" + carPlate + "] due to " + message);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.cancelSchedule();
		this.pointsRetrieverTask = null;
		this.timer = null;
	}

	private static boolean shouldChangeCarPlate(String carPlate) {
		return TextUtils.isEmpty(PirattoManager.instance.carPlate)
				|| (!TextUtils.isEmpty(carPlate)
				&& !PirattoManager.instance.carPlate.equals(carPlate));
	}
}
