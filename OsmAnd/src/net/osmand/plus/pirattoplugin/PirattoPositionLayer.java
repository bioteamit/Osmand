package net.osmand.plus.pirattoplugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.List;

public class PirattoPositionLayer extends OsmandMapLayer implements ContextMenuLayer.IContextMenuProvider {

	private static final String TAG = "PirattoPositionLayer";
	private static final int radius = 18;

	private DisplayMetrics dm;

	private final MapActivity mapActivity;
	private OsmandMapTileView mapTileView;

	private Paint bitmapPaint;

	private Bitmap pointIcon;

	private PirattoPlugin plugin;
	private PirattoManager pirattoManager;

	public PirattoPositionLayer(MapActivity mapActivity, PirattoPlugin plugin) {
		this.mapActivity = mapActivity;
		this.plugin = plugin;
		this.pirattoManager = PirattoManager.getInstance();
	}

	@Override
	public void initLayer(OsmandMapTileView mapTileView) {
		this.mapTileView = mapTileView;
		dm = new DisplayMetrics();
		WindowManager wmgr = (WindowManager) mapTileView.getContext().getSystemService(Context.WINDOW_SERVICE);
		wmgr.getDefaultDisplay().getMetrics(dm);

		bitmapPaint = new Paint();
		bitmapPaint.setDither(true);
		bitmapPaint.setAntiAlias(true);
		bitmapPaint.setFilterBitmap(true);
		pointIcon = BitmapFactory.decodeResource(mapTileView.getResources(), R.drawable.map_poi_piratto_pos);
	}

	@Override
	public void onDraw(Canvas canvas, RotatedTileBox tb, DrawSettings nightMode) {
		List<DestinationPoint> destinationPoints = this.pirattoManager.getDestinationPoints();
		if (destinationPoints == null || destinationPoints.isEmpty()) {
			return;
		}

		for (DestinationPoint destinationPoint : destinationPoints) {
			final Bitmap pointIcon = this.pointIcon;
			double latitude = destinationPoint.getLatitude();
			double longitude = destinationPoint.getLongitude();
			if (this.isLocationVisible(tb, latitude, longitude)) {
				int marginX = pointIcon.getWidth() / 2;
				int marginY = pointIcon.getHeight() / 2;
				int locationX = tb.getPixXFromLonNoRot(longitude);
				int locationY = tb.getPixYFromLatNoRot(latitude);
				canvas.rotate(-this.mapTileView.getRotate(), locationX, locationY);
				canvas.drawBitmap(pointIcon, locationX - marginX, locationY - marginY, this.bitmapPaint);
			}
		}
	}

	@Override
	public void destroyLayer() {
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public boolean disableSingleTap() {
		return false;
	}

	@Override
	public boolean disableLongPressOnMap() {
		return false;
	}

	@Override
	public void collectObjectsFromPoint(PointF point, RotatedTileBox tileBox, List<Object> o) {
		this.getDestinationPointFromPoint(tileBox, point, o);
	}

	@Override
	public LatLon getObjectLocation(Object o) {
		Log.d(TAG, "getObjectLocation: " + o != null ? o.toString() : "null");
		if (o != null && o instanceof DestinationPoint) {
			return ((DestinationPoint) o).getPoint();
		}
		return null;
	}

	@Override
	public String getObjectDescription(Object o) {
		return plugin.getDestinationPointDescription(this.mapActivity);
	}

	@Override
	public PointDescription getObjectName(Object o) {
		return new PointDescription(PointDescription.POINT_TYPE_PIRATTO_MARKER,
				mapTileView.getContext().getString(R.string.osmand_oneteam_piratto_point_name));
	}

	public void refresh() {
		if (mapTileView != null) {
			mapTileView.refreshMap();
		}
	}

	/**
	 * @param latitude
	 * @param longitude
	 * @return true if there is no destination points is located on a visible part of map
	 */
	private boolean isLocationVisible(RotatedTileBox tb, double latitude, double longitude){
		if(this.pirattoManager.getDestinationPoints() == null
				|| this.pirattoManager.getDestinationPoints().isEmpty()
				|| mapTileView == null){
			return false;
		}
		return tb.containsLatLon(latitude, longitude);
	}

	/**
	 * @param point
	 * @param points
	 *            is in this case not necessarily has to be a list, but it's also used in method
	 *            <link>collectObjectsFromPoint(PointF point, List<Object> o)</link>
	 */
	private void getDestinationPointFromPoint(RotatedTileBox tb, PointF point, List<? super LatLon> points) {
		List<DestinationPoint> destinationPoints = this.pirattoManager.getDestinationPoints();
		if (destinationPoints == null
				|| destinationPoints.isEmpty()
				|| this.mapTileView == null) {
			return;
		}

		int ex = (int) point.x;
		int ey = (int) point.y;
		for (DestinationPoint destinationPoint : destinationPoints) {
			LatLon position = destinationPoint.getPoint();
			int x = (int) tb.getPixXFromLatLon(position.getLatitude(), position.getLongitude());
			int y = (int) tb.getPixYFromLatLon(position.getLatitude(), position.getLongitude());
			// the width of an image is 40 px, the height is 60 px -> radius = 20,
			// the position of a destination point relatively to the icon is at the center of the bottom line of the image
			int rad = (int) (radius * tb.getDensity());
			if (Math.abs(x - ex) <= rad && (ey - y) <= rad && (y - ey) <= 2.5 * rad) {
				points.add(position);
			}
		}
	}
}
