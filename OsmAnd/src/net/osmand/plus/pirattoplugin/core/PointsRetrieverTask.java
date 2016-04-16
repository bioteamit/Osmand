package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.TimerTask;

public class PointsRetrieverTask extends TimerTask {

	private static final String TAG = "PointsRetrieverTask";

	public interface OnRetrievingPointsCallback {
		void onSuccess(String hostName, String carPlate, DestinationPoints points);
		void onFailure(String hostName, String carPlate, String message);
	}

	private OnRetrievingPointsCallback onRetrievingPointsCallback;
	private String hostName;
	private String carPlate;

	public PointsRetrieverTask(String hostName, String carPlate, OnRetrievingPointsCallback onRetrievingPointsCallback) {
		this.hostName = hostName;
		this.carPlate = carPlate;
		this.onRetrievingPointsCallback = onRetrievingPointsCallback;
	}

	@Override
	public void run() {
		this.retrievePoints(this.hostName, this.carPlate);
	}

	private void retrievePoints(String hostName, String carPlate) {
		Log.d(TAG, "Request destination points");

		if (TextUtils.isEmpty(hostName)) {
			Log.w(TAG, "Host name is not defined");
			return;
		}

		if (TextUtils.isEmpty(carPlate)) {
			Log.w(TAG, "Car plate is not defined");
			return;
		}

		if (this.onRetrievingPointsCallback == null) {
			Log.e(TAG, "Callback is not assigned");
			return;
		}

		DestinationPointsRetriever retriever = new DestinationPointsRetriever();
		try {
			DestinationPoints points = retriever.retrievePoints(this.hostName, this.carPlate);
			this.handleRetrievedPoints(points);
		} catch (IOException e) {
			Log.e(TAG, "Failed to parse destination points", e);
			this.sendFailure(this.hostName, this.carPlate, e);
		} catch (CarNotDefinedException e) {
			Log.e(TAG, "Car plate is not valid", e);
			this.sendFailure(this.hostName, this.carPlate, e);
		} catch (HostNameNotDefinedException e) {
			Log.e(TAG, "Host name is not valid", e);
			this.sendFailure(this.hostName, this.carPlate, e);
		} catch (InvalidFormatException e) {
			Log.e(TAG, "Response format is not valid", e);
			this.sendFailure(this.hostName, this.carPlate, e);
		}
	}

	private void handleRetrievedPoints(DestinationPoints points) {
		if (points == null) {
			Log.d(TAG, "Failed to retrieve destination points");
			this.onRetrievingPointsCallback.onFailure(this.hostName, this.carPlate, "Failed to retrieve destination points");
			return;
		}

		Log.d(TAG, "Destination points size: " + points.getDestinationPoints().size());
		this.onRetrievingPointsCallback.onSuccess(this.hostName, this.carPlate, points);
		return;
	}

	private void sendFailure(String hostName, String carPlate, Exception e) {
		String message = e.getMessage();
		if (TextUtils.isEmpty(message)) {
			message = e.getClass().getSimpleName();
		}
		this.onRetrievingPointsCallback.onFailure(hostName, carPlate, message);
	}
}
