package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Log;

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

	private final int TIME_INTERVAL = 3*60*1000;

	private Timer timer;
	private PointsRetrieverTask pointsRetrieverTask;
	private OnUpdatePointsListener onUpdatePointsListener;

	private String carPlate;
	private DestinationPoints destinationPoints;

	private OsmandSettings.CommonPreference<String> carPlateSettings;

	public static PirattoManager getInstance() {
		if (PirattoManager.instance == null) {
			PirattoManager.instance = new PirattoManager();
		}

		return PirattoManager.instance;
	}

	protected PirattoManager() {
		this.timer = new Timer();
		this.destinationPoints = new DestinationPoints();
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

	public List<DestinationPoint> getDestinationPoints() {
		if (this.destinationPoints == null) {
			return null;
		}
		return this.destinationPoints.getDestinationPoints();
	}

	public void setOnUpdatePointsListener(OnUpdatePointsListener listener) {
		this.onUpdatePointsListener = listener;
	}

	public void refresh() {
		this.cancelSchedule();
		this.pointsRetrieverTask = new PointsRetrieverTask(this.carPlate, this);
		this.timer.scheduleAtFixedRate(this.pointsRetrieverTask, 0, TIME_INTERVAL);
	}

	public void cancelSchedule() {
		this.pointsRetrieverTask.cancel();
		this.timer.cancel();
		this.timer.purge();
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
