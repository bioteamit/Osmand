package net.osmand.plus.pirattoplugin;

import android.graphics.Canvas;
import android.graphics.PointF;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.data.RotatedTileBox;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.ContextMenuLayer;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.List;

public class PirattoPositionLayer extends OsmandMapLayer implements ContextMenuLayer.IContextMenuProvider {

	private final MapActivity mapActivity;
	private OsmandMapTileView mapTileView;

	public PirattoPositionLayer(MapActivity mapActivity) {
		this.mapActivity = mapActivity;
	}

	@Override
	public void initLayer(OsmandMapTileView mapTileView) {
		this.mapTileView = mapTileView;
	}

	@Override
	public void collectObjectsFromPoint(PointF point, RotatedTileBox tileBox, List<Object> o) {
	}

	@Override
	public LatLon getObjectLocation(Object o) {
		return null;
	}

	@Override
	public String getObjectDescription(Object o) {
		return null;
	}

	@Override
	public PointDescription getObjectName(Object o) {
		return null;
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
	public void onDraw(Canvas canvas, RotatedTileBox tileBox, DrawSettings settings) {
	}

	@Override
	public boolean drawInScreenPixels() {
		return false;
	}

	@Override
	public void destroyLayer() {
	}
}
