package net.osmand.plus.pirattoplugin.core;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.TargetPointsHelper;
import net.osmand.plus.dialogs.DirectionsDialogs;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;

public class PirattoManager extends Observable implements PointsRetrieverTask.OnRetrievingPointsCallback {

	public final static String PIRATTO_HOST_NAME = "pref_piratto_hostname"; //$NON-NLS-1$
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

	private TargetPointsHelper targetPointsHelper;
	private DestinationPoints destinationPoints;
	private boolean isRoutingPoint;

	// Target destination point
	private OsmandSettings.CommonPreference<String> targetPointAddressSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLatitudeSettings;
	private OsmandSettings.CommonPreference<Float> targetPointLongitudeSettings;
	///////
	private OsmandSettings.CommonPreference<String> hostNameSettings;
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
		this.targetPointsHelper = application.getTargetPointsHelper();
		this.destinationPoints = new DestinationPoints();

		this.hostNameSettings = this.settings.registerStringPreference(PIRATTO_HOST_NAME, null).makeGlobal();
		this.carPlateSettings = this.settings.registerStringPreference(PIRATTO_CAR_PLATE, null).makeGlobal();
		this.updateIntervalSettings = this.settings.registerIntPreference(PIRATTO_UPDATE_INTERVAL, 2).makeGlobal();
	}

	public boolean isRoutingPoint() {
		return this.isRoutingPoint;
	}

	public DestinationPoint getNextRoutingPoint() {
		if (this.destinationPoints == null
				|| this.destinationPoints.getDestinationPoints() == null
				|| this.destinationPoints.getDestinationPoints().isEmpty()) {
			return null;
		}

		List<DestinationPoint> destinationPoints = this.destinationPoints.getDestinationPoints();
		return destinationPoints.get(0);
	}

	public synchronized void routeNextPoint(Context context) {
		DestinationPoint targetPoint = this.getNextRoutingPoint();
		if (targetPoint != null) {
			this.navigateTo(context, targetPoint);
		}
	}

	public void removeOldTargetPoint() {
		if (this.destinationPoints == null
				|| this.destinationPoints.getDestinationPoints() == null
				|| this.destinationPoints.getDestinationPoints().isEmpty()) {
			return;
		}

		DestinationPoint destinationPoint = this.getRoutingPoint();
		if (destinationPoint == null) {
			return;
		}

		if (this.removeDestinationPoint(destinationPoint)) {
			this.isRoutingPoint = false;
			this.setRoutingPoint(null);
			this.targetPointsHelper.clearPointToNavigate(false);
			Log.d(TAG, "Old target point is removed: " + destinationPoint.getAddress() + " @ " + destinationPoint.getPoint().toString());
		}
	}

	public void navigateTo(final Context context, final DestinationPoint destinationPoint) {
		if (destinationPoint == null) {
			return;
		}

		final PointDescription description = new PointDescription(PointDescription.POINT_TYPE_PIRATTO_MARKER, destinationPoint.getAddress());
		description.setLat(destinationPoint.getLatitude());
		description.setLon(destinationPoint.getLongitude());

		this.application.getSettings().navigateDialog();

		if (context instanceof Activity) {
			final Activity activity = (Activity) context;
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					DirectionsDialogs.directionsToDialogAndLaunchMap(activity, destinationPoint.getLatitude(), destinationPoint.getLongitude(), description);
					PirattoManager.this.setRoutingPoint(destinationPoint);
					PirattoManager.this.isRoutingPoint = true;
					Log.d(TAG, "Navigate to: " + destinationPoint.getAddress() + " @ " + destinationPoint.getPoint().toString());
				}
			});
		}
	}

	public void setRoutingPoint(DestinationPoint destinationPoint) {
		if (this.targetPointAddressSettings == null) {
			this.targetPointAddressSettings = this.settings.registerStringPreference(PIRATTO_TARGET_POINT_ADDRESS, null).makeGlobal();
		}
		if (this.targetPointLatitudeSettings == null) {
			this.targetPointLatitudeSettings = this.settings.registerFloatPreference(PIRATTO_TARGET_POINT_LATITUDE, 0f).makeGlobal();
		}
		if (this.targetPointLongitudeSettings == null) {
			this.targetPointLongitudeSettings = this.settings.registerFloatPreference(PIRATTO_TARGET_POINT_LONGITUDE, 0f).makeGlobal();
		}

		this.targetPointAddressSettings.set(destinationPoint != null ? destinationPoint.getAddress() : null);
		this.targetPointLatitudeSettings.set(destinationPoint != null ? (float) destinationPoint.getLatitude() : 0f);
		this.targetPointLongitudeSettings.set(destinationPoint != null ? (float) destinationPoint.getLongitude() : 0f);
	}

	public DestinationPoint getRoutingPoint() {
		if (this.targetPointAddressSettings == null
				|| this.targetPointLatitudeSettings == null
				|| this.targetPointLongitudeSettings == null) {
			return null;
		}

		DestinationPoint targetPoint = new DestinationPoint(this.targetPointAddressSettings.get(), Double.parseDouble(this.targetPointLatitudeSettings.get().toString()), Double.parseDouble(this.targetPointLongitudeSettings.get().toString()));

		if (targetPoint.equals(new DestinationPoint(null, 0, 0))) {
			return null;
		}
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

	public synchronized void enable(final Context context) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				PirattoManager.this.refresh();
				PirattoManager.this.targetPointsHelper.clearPointToNavigate(false);
				PirattoManager.this.routeNextPoint(context);
				return null;
			}
		}.execute();
	}

	public synchronized void disable() {
		this.cancelSchedule();
		this.deleteObservers();
		this.destinationPoints = new DestinationPoints();
		this.targetPointsHelper.clearPointToNavigate(false);
	}

	public void refresh() {
		this.cancelSchedule();
		String hostName = this.hostNameSettings.get();
		String carPlate = this.carPlateSettings.get();
		if (TextUtils.isEmpty(hostName) || TextUtils.isEmpty(carPlate)) {
			Log.w(TAG, "Invalid refresh piratto points request as hostname or car plate is not defined");
			return;
		}

		this.pointsRetrieverTask = new PointsRetrieverTask(hostName, carPlate, this);
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

	public boolean removeDestinationPoint(DestinationPoint destinationPoint) {
		return this.destinationPoints.removePoint(destinationPoint);
	}

	@Override
	public void onSuccess(String hostName, String carPlate, DestinationPoints points) {
		if (points == null) {
			this.destinationPoints = new DestinationPoints();
		} else {
			this.destinationPoints = points;
		}

		this.setChanged();
		Log.d(TAG, "Points are updated");
		this.notifyObservers(this.destinationPoints.getDestinationPoints());
	}

	@Override
	public void onFailure(String hostName, String carPlate, String message) {
		Log.w(TAG, "Failed to retrieve destination points for host name [" + hostName + "] and car [" + carPlate + "] due to " + message);

		this.setChanged();
		this.notifyObservers(new String[] { hostName, carPlate, message });
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.cancelSchedule();
		this.deleteObservers();
		this.pointsRetrieverTask = null;
		this.timer = null;
	}

	public String getHostName() {
		return this.hostNameSettings.get();
	}

	public String getCarPlate() {
		return this.carPlateSettings.get();
	}

	public int getUpdateInterval() {
		return this.updateIntervalSettings.get();
	}
}
