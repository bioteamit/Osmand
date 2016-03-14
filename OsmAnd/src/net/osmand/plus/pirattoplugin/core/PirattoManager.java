package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Log;

import net.osmand.data.LatLon;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

public class PirattoManager extends Observable implements PointsRetrieverTask.OnRetrievingPointsCallback {

	public final static String PIRATTO_CAR_PLATE = "pref_piratto_car_plate"; //$NON-NLS-1$
	public final static String PIRATTO_UPDATE_INTERVAL = "pref_piratto_update_interval"; //$NON-NLS-1$
	public final static String PIRATTO_TARGET_POINT_ADDRESS = "piratto_target_point_address"; //$NON-NLS-1$
	public final static String PIRATTO_TARGET_POINT_LATITUDE = "piratto_target_point_latitude"; //$NON-NLS-1$
	public final static String PIRATTO_TARGET_POINT_LONGITUDE = "piratto_target_point_longitude"; //$NON-NLS-1$
	private static final String TAG = "PirattoManager";

	public interface OnUpdatePointsListener {
		void updatePoints(List<DestinationPoint> points);
	}

	private static PirattoManager instance;

	private OsmandApplication application;
	private OsmandSettings settings;

	private Timer timer;
	private PointsRetrieverTask pointsRetrieverTask;

	private DestinationPoints destinationPoints;

	// Target destination point
	private OsmandSettings.CommonPreference<String> targetPointAddressSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLatitudeSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLongitudeSettings;
	///////
	private OsmandSettings.CommonPreference<String> carPlateSettings;
	private OsmandSettings.CommonPreference<Integer> updateIntervalSettings;

	public static PirattoManager initialize(OsmandApplication application) {
		PirattoManager.instance = new PirattoManager(application);
		return PirattoManager.instance;
	}

	public static PirattoManager getInstance() throws IllegalStateException {
		if (PirattoManager.instance == null) {
			throw new IllegalStateException("Piratto manager is not initialized yet");
		}

		return PirattoManager.instance;
	}

	protected PirattoManager(OsmandApplication application) {
		this.application = application;
		this.settings = application.getSettings();
		this.destinationPoints = new DestinationPoints();
		this.addTestDestinationPoints();

		this.carPlateSettings = this.settings.registerStringPreference(PIRATTO_CAR_PLATE, null).makeGlobal();
		this.updateIntervalSettings = this.settings.registerIntPreference(PIRATTO_UPDATE_INTERVAL, 2).makeGlobal();
	}

	private void addTestDestinationPoints() {
		this.addTestDestinationPoint("Downtown", 30.0165934, 31.417473);
		this.addTestDestinationPoint("19 Marca 19811", 53.1247962, 18.0043512);
		this.addTestDestinationPoint("Aleksandrowska2", 53.0987161, 18.0235117);
		this.addTestDestinationPoint("16 Pułku Ułanów Wielkopolskich2", 53.1192529, 17.9720179);
	}

	private void addTestDestinationPoint(String address, double latitude, double longitude) {
		this.destinationPoints.addPoint(new DestinationPoint(address, latitude, longitude));
	}

	public void setCarPlate(String newCarPlate) {
		if (!this.shouldChangeCarPlate(newCarPlate)) {
			return;
		}
		this.carPlateSettings.set(newCarPlate);

		this.refresh();
	}

	public void setUpdateTimeInterval(int timeInMinutes) {
		if (timeInMinutes <= 0) {
			return;
		}
		this.updateIntervalSettings.set(timeInMinutes);

		this.refresh();
	}

	public void setTargetDestinationPoint(DestinationPoint destinationPoint) {
		if (destinationPoint == null) {
			Log.w(TAG, "Destination point is valid to be set as target point");
			return;
		}

		if (this.targetPointAddressSettings == null) {
			this.targetPointAddressSettings = this.settings.registerStringPreference(PIRATTO_TARGET_POINT_ADDRESS, null).makeGlobal();
		}
		if (this.targetPointLatitudeSettings == null) {
			this.targetPointLatitudeSettings = this.settings.registerFloatPreference(PIRATTO_TARGET_POINT_LATITUDE, 0f).makeGlobal();
		}
		if (this.targetPointLongitudeSettings == null) {
			this.targetPointLongitudeSettings = this.settings.registerFloatPreference(PIRATTO_TARGET_POINT_LONGITUDE, 0f).makeGlobal();
		}

		this.targetPointAddressSettings.set(destinationPoint.getAddress());
		this.targetPointLatitudeSettings.set((float) destinationPoint.getLatitude());
		this.targetPointLongitudeSettings.set((float) destinationPoint.getLongitude());
	}

	public DestinationPoint getTargetDestinationPoints() {
		if (this.targetPointAddressSettings == null
				|| this.targetPointLatitudeSettings == null
				|| this.targetPointLongitudeSettings == null) {
			return null;
		}

		DestinationPoint targetPoint = new DestinationPoint(this.targetPointAddressSettings.get(), this.targetPointLatitudeSettings.get(), this.targetPointLongitudeSettings.get());
		return targetPoint;
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

	public void addObserver(Observer observer) {
		if (observer == null) {
			return;
		}
		super.addObserver(observer);
	}

	public void deleteObserver(Observer observer) {
		if (observer == null) {
			return;
		}
		super.deleteObserver(observer);
	}

	public synchronized void disable() {
		this.cancelSchedule();
		this.deleteObservers();
		this.destinationPoints = new DestinationPoints();
	}

	public void refresh() {
		this.cancelSchedule();
		this.pointsRetrieverTask = new PointsRetrieverTask(this.carPlateSettings.get(), this);
		this.timer = new Timer();
		int period = this.updateIntervalSettings.get() * 60 * 1000;
		this.timer.scheduleAtFixedRate(this.pointsRetrieverTask, 0, period);
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
		boolean updated = this.destinationPoints.updatePoints(points);
		this.setChanged();
		if (updated) {
			Log.d(TAG, "Points are updated");
			this.notifyObservers(this.destinationPoints.getDestinationPoints());
			this.destinationPoints.commit();
		} else {
			this.notifyObservers();
		}
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
		return TextUtils.isEmpty(PirattoManager.instance.carPlateSettings.get())
				|| (!TextUtils.isEmpty(carPlate)
				&& !PirattoManager.instance.carPlateSettings.get().equals(carPlate));
	}
}
