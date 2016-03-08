package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.TimerTask;

public class PointsRetrieverTask extends TimerTask {

	private static final String TAG = "PointsRetrieverTask";

	public interface OnRetrievingPointsCallback {
		void onSuccess(String carPlate, DestinationPoints points);
		void onFailure(String carPlate, String message);
	}

	private OnRetrievingPointsCallback onRetrievingPointsCallback;
	private String carPlate;

	public PointsRetrieverTask(String carPlate, OnRetrievingPointsCallback onRetrievingPointsCallback) {
		this.carPlate = carPlate;
		this.onRetrievingPointsCallback = onRetrievingPointsCallback;
	}

	@Override
	public void run() {
		this.retrievePoints(this.carPlate);
	}

	private void retrievePoints(String carPlate) {
		Log.d(TAG, "Request destination points");

		if (TextUtils.isEmpty(carPlate)) {
			Log.w(TAG, "No car plat is defined");
			return;
		}

		if (this.onRetrievingPointsCallback == null) {
			Log.e(TAG, "No callback is assigned");
			return;
		}

		DestinationPointsRetriever retriever = new DestinationPointsRetriever();
		try {
//			DestinationPoints points = retriever.retrievePoints(this.carPlate);
//			this.handleRetrievedPoints(points);

			// FIXME: START INTEGRATION TEST
			DestinationPoints destinationPoints1 = retriever.retrievePoints("CB 763AU");
			DestinationPoints destinationPoints2 = retriever.retrievePoints("CB 060EG");
			DestinationPoints destinationPoints3 = retriever.retrievePoints("CB 077CN");
			DestinationPoints destinationPoints4 = retriever.retrievePoints("CB 201AX");
			DestinationPoints destinationPoints5 = retriever.retrievePoints("CB 8627W");

			DestinationPoints points = new DestinationPoints();
			points.updatePoints(destinationPoints1);
			points.updatePoints(destinationPoints2);
			points.updatePoints(destinationPoints3);
			points.updatePoints(destinationPoints4);
			points.updatePoints(destinationPoints5);
			this.handleRetrievedPoints(points);
			// END INTEGRATION TEST
		} catch (IOException e) {
			Log.e(TAG, "Failed to parse destination points", e);
		} catch (CarNotDefinedException e) {
			Log.e(TAG, "Car plate is not valid", e);
		}
		return;
	}

	private void handleRetrievedPoints(DestinationPoints points) {
		if (points == null) {
			Log.d(TAG, "Failed to retrieve destination points");
			this.onRetrievingPointsCallback.onFailure(this.carPlate, "Failed to retrieve destination points");
			return;
		}

		Log.d(TAG, "Destination points size: " + points.getDestinationPoints().size());
		this.onRetrievingPointsCallback.onSuccess(this.carPlate, points);
		return;
	}
}
