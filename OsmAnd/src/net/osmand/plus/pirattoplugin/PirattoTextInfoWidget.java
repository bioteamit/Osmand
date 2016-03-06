package net.osmand.plus.pirattoplugin;

import android.view.View;

import net.osmand.data.LatLon;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.views.AnimateDraggingMapThread;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;

public class PirattoTextInfoWidget extends TextInfoWidget implements View.OnClickListener {

	private MapActivity mapActivity;

	private DestinationPoint destinationPoint;

	private float[] calculations;
	private int cachedMeters;

	public PirattoTextInfoWidget(MapActivity activity, DestinationPoint destinationPoint) {
		super(activity);

		this.mapActivity = activity;
		this.destinationPoint = destinationPoint;

		this.calculations = new float[1];
		this.cachedMeters = 0;

		this.setOnClickListener(this);
	}

	@Override
	public boolean updateInfo(OsmandMapLayer.DrawSettings drawSettings) {
		LatLon destinationPoint = this.destinationPoint.getPoint();
		if( destinationPoint != null && !this.mapActivity.getRoutingHelper().isFollowingMode()) {
			OsmandMapTileView view = this.mapActivity.getMapView();
			int d = 0;
			if (d == 0) {
				net.osmand.Location.distanceBetween(view.getLatitude(), view.getLongitude(), destinationPoint.getLatitude(), destinationPoint.getLongitude(), calculations);
				d = (int) calculations[0];
			}
			if (distChanged(cachedMeters, d)) {
				cachedMeters = d;
				if (cachedMeters <= 20) {
					cachedMeters = 0;
					setText(null, null);
				} else {
					String ds = OsmAndFormatter.getFormattedDistance(cachedMeters, this.mapActivity.getMyApplication());
					int ls = ds.lastIndexOf(' ');
					if (ls == -1) {
						setText(ds, null);
					} else {
						setText(ds.substring(0, ls), ds.substring(ls + 1));
					}
				}
				return true;
			}
		} else if (cachedMeters != 0) {
			cachedMeters = 0;
			setText(null, null);
			return true;
		}
		return false;
	}

	/**
	 * Utility method.
	 * @param oldDist
	 * @param dist
	 * @return
	 */
	private boolean distChanged(int oldDist, int dist){
		if(oldDist != 0 && Math.abs(oldDist - dist) < 30){
			return false;
		}
		return true;
	}

	@Override
	public void onClick(View view) {
		if (this.mapActivity == null || this.destinationPoint == null) {
			return;
		}

		OsmandMapTileView mapView = this.mapActivity.getMapView();
		AnimateDraggingMapThread thread = mapView.getAnimatedDraggingThread();
		final LatLon position = this.destinationPoint.getPoint();
		if (position != null) {
			int fZoom = mapView.getZoom() < 15 ? 15 : mapView.getZoom();
			thread.startMoving(position.getLatitude(), position.getLongitude(), fZoom, true);
		}
	}
}
