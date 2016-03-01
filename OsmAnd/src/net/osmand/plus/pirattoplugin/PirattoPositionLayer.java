package net.osmand.plus.pirattoplugin;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.List;

public class PirattoPositionLayer extends OsmandMapLayer implements ContextMenuLayer.IContextMenuProvider {

	private static final int radius = 18;

	private DisplayMetrics dm;

	private final MapActivity mapActivity;
	private OsmandMapTileView mapTileView;

	private Paint bitmapPaint;

	private Bitmap parkingLimitIcon;

	private PirattoPlugin plugin;

	public PirattoPositionLayer(MapActivity mapActivity, PirattoPlugin plugin) {
		this.mapActivity = mapActivity;
		this.plugin = plugin;
	}

	public LatLon getDestinationPoint() {
		return plugin.getDestinationPoint();
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
		parkingLimitIcon = BitmapFactory.decodeResource(mapTileView.getResources(), R.drawable.map_poi_parking_pos_limit);
	}

	@Override
	public void onDraw(Canvas canvas, RotatedTileBox tb, DrawSettings nightMode) {
		LatLon parkingPoint = getDestinationPoint();
		if (parkingPoint == null)
			return;

		Bitmap parkingIcon = parkingLimitIcon;
		double latitude = parkingPoint.getLatitude();
		double longitude = parkingPoint.getLongitude();
		if (isLocationVisible(tb, latitude, longitude)) {
			int marginX = parkingLimitIcon.getWidth() / 2;
			int marginY = parkingLimitIcon.getHeight() / 2;
			int locationX = tb.getPixXFromLonNoRot(longitude);
			int locationY = tb.getPixYFromLatNoRot(latitude);
			canvas.rotate(-mapTileView.getRotate(), locationX, locationY);
			canvas.drawBitmap(parkingIcon, locationX - marginX, locationY - marginY, bitmapPaint);
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
		getParkingFromPoint(tileBox, point, o);
	}

	@Override
	public LatLon getObjectLocation(Object o) {
		if(o == getDestinationPoint()) {
			return getDestinationPoint();
		}
		return null;
	}

	@Override
	public String getObjectDescription(Object o) {
		return plugin.getParkingDescription(this.mapActivity);

	}

	@Override
	public PointDescription getObjectName(Object o) {
		return new PointDescription(PointDescription.POINT_TYPE_PARKING_MARKER,
				mapTileView.getContext().getString(R.string.osmand_parking_position_name));
	}

	public void refresh() {
		if (mapTileView != null) {
			mapTileView.refreshMap();
		}
	}

	/**
	 * @param latitude
	 * @param longitude
	 * @return true if the parking point is located on a visible part of map
	 */
	private boolean isLocationVisible(RotatedTileBox tb, double latitude, double longitude){
		if(getDestinationPoint() == null || mapTileView == null){
			return false;
		}
		return tb.containsLatLon(latitude, longitude);
	}

	/**
	 * @param point
	 * @param parkingPosition
	 *            is in this case not necessarily has to be a list, but it's also used in method
	 *            <link>collectObjectsFromPoint(PointF point, List<Object> o)</link>
	 */
	private void getParkingFromPoint(RotatedTileBox tb, PointF point, List<? super LatLon> parkingPosition) {
		LatLon parkingPoint = getDestinationPoint();
		if (parkingPoint != null && mapTileView != null) {
			int ex = (int) point.x;
			int ey = (int) point.y;
			LatLon position = plugin.getDestinationPoint();
			int x = (int) tb.getPixXFromLatLon(position.getLatitude(), position.getLongitude());
			int y = (int) tb.getPixYFromLatLon(position.getLatitude(), position.getLongitude());
			// the width of an image is 40 px, the height is 60 px -> radius = 20,
			// the position of a parking point relatively to the icon is at the center of the bottom line of the image
			int rad = (int) (radius * tb.getDensity());
			if (Math.abs(x - ex) <= rad && (ey - y) <= rad && (y - ey) <= 2.5 * rad) {
				parkingPosition.add(parkingPoint);
			}
		}
	}
}
