package net.osmand.plus.pirattoplugin.core;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class DestinationPointsRetriever {

	private OkHttpClient client;

	public DestinationPointsRetriever() {
		this.client = new OkHttpClient();
	}

	public DestinationPoints retrievePoints(File pointsFile) throws IOException {
		if (pointsFile == null
				|| !pointsFile.exists()) {
			// TODO: report error
			return null;
		}
		return this.toDestinationPoints(pointsFile);
	}

	public DestinationPoints retrievePoints(String carPlate) throws IOException, CarNotDefinedException {

		if (TextUtils.isEmpty(carPlate)) {
			throw new CarNotDefinedException("No car plate is defined");
		}

		String requestBody = this.createDestinationPointsBody(carPlate);

		Response response = this.execute(Api.SERVER_URL, Api.RequestDestinationPoints.CONTENT_TYPE, requestBody);
		if (response.isSuccessful()) {
			return this.toDestinationPoints(response.body());
		}

		// TODO: return error and put response message as reference
		return null;
	}

	private synchronized Response execute(String url, String contentType, String body) throws IOException {
		MediaType mediaType = MediaType.parse(contentType);
		RequestBody requestBody = RequestBody.create(mediaType, body);

		Request request = new Request.Builder()
				.url(url)
				.post(requestBody)
				.addHeader("content-type", contentType)
				.addHeader("cache-control", "no-cache")
				.build();

		return this.client.newCall(request).execute();
	}

	private DestinationPoints toDestinationPoints(ResponseBody body) throws IOException {
		return DestinationPoints.parse(body.byteStream());
	}

	private DestinationPoints toDestinationPoints(File pointsFile) throws IOException {
		BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(pointsFile), 5120);
		return DestinationPoints.parse(inputStream);
	}

	private String createDestinationPointsBody(String carPlate) {
		RequestMessage requestMessage = new RequestMessage(12, carPlate);
		return "REQUEST_DESTINATION_POINTS=" + requestMessage.toString();
	}
}
