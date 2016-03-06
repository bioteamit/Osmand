package net.osmand.plus.pirattoplugin;

import android.app.Activity;
import android.util.Log;

import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.List;

public class PirattoPlugin extends OsmandPlugin implements PirattoManager.OnUpdatePointsListener {

	private static final String TAG = "PirattoPlugin";

	public static final String ID = "osmand.oneteam.piratto";
	public static final String PIRATTO_PLUGIN_COMPONENT = "net.osmand.oneteam.pirattoPlugin"; //$NON-NLS-1$
	public final static String KEY_PIRATTO_POINTS = "piratto_points"; //$NON-NLS-1$

	private OsmandApplication application;

	private MapActivity mapActivity;

	private PirattoManager pirattoManager;

	private PirattoPositionLayer pirattoLayer;

	private List<PirattoTextInfoWidget> pirattoTextInfoWidgetList;

	public PirattoPlugin(OsmandApplication application) {
		this.application = application;
		ApplicationMode.regWidget(KEY_PIRATTO_POINTS, (ApplicationMode[]) null);
		this.pirattoManager = PirattoManager.getInstance();

		// Registered cars
		// "CB 763AU" "CB 060EG" "CB 077CN" "CB 201AX" "CB 8627W"
		this.pirattoManager.setCarPlate(this.application, "CB 763AU");

	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getDescription() {
		return this.application.getString(R.string.osmand_oneteam_piratto_plugin_description);
	}

	@Override
	public String getName() {
		return this.application.getString(R.string.osmand_oneteam_piratto_plugin_name);
	}

	@Override
	public int getLogoResourceId() {
		return R.drawable.ic_plugin_piratto;
	}

	@Override
	public int getAssetResourceName() {
		return R.drawable.piratto_map;
	}

	@Override
	public Class<? extends Activity> getSettingsActivity() {
		return null;
	}

	@Override
	public void registerLayers(MapActivity activity) {
		// remove old if existing after turn
		if(this.pirattoLayer != null) {
			activity.getMapView().removeLayer(this.pirattoLayer);
		}
		this.pirattoLayer = new PirattoPositionLayer(activity, this);
		activity.getMapView().addLayer(this.pirattoLayer, 5.5f);
		registerWidgets(activity);
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (this.isActive()) {
			if (this.pirattoLayer == null) {
				registerLayers(activity);
			}
			if (this.pirattoTextInfoWidgetList == null
					|| this.pirattoTextInfoWidgetList.isEmpty()) {
				this.registerWidgets(activity);
			}
		} else {
			if (this.pirattoLayer != null) {
				activity.getMapView().removeLayer(this.pirattoLayer);
				this.pirattoLayer = null;
			}
			MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
			if (mapInfoLayer != null
					&& this.pirattoTextInfoWidgetList != null) {
				for (PirattoTextInfoWidget textInfoWidget : this.pirattoTextInfoWidgetList) {
					mapInfoLayer.removeSideWidget(textInfoWidget);
					mapInfoLayer.recreateControls();
					this.pirattoTextInfoWidgetList.remove(textInfoWidget);
				}
				this.pirattoTextInfoWidgetList = null;
			}
		}
	}

	private void registerWidgets(MapActivity activity) {
		MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
		List<DestinationPoint> destinationPoints = this.pirattoManager.getDestinationPoints();
		if (mapInfoLayer != null && destinationPoints != null) {
			for (DestinationPoint destinationPoint : destinationPoints) {
				PirattoTextInfoWidget textInfoWidget = this.createPointInfoControl(activity, destinationPoint);
				mapInfoLayer.registerSideWidget(textInfoWidget,
						R.drawable.ic_action_piratto_dark, R.string.map_widget_piratto, KEY_PIRATTO_POINTS, false, 8);
			}
			mapInfoLayer.recreateControls();
		}
	}

	/**
	 * @return the control to be added on a MapInfoLayer
	 * that shows a distance between
	 * the current position on the map
	 * and the location of the destination point
	 */
	private PirattoTextInfoWidget createPointInfoControl(final MapActivity map, final DestinationPoint destinationPoint) {
		PirattoTextInfoWidget pointInfoControl = new PirattoTextInfoWidget(map, destinationPoint);
		pointInfoControl.setText(null, null);
		pointInfoControl.setIcons(R.drawable.widget_piratto_day, R.drawable.widget_piratto_night);
		return pointInfoControl;
	}

	public String getDestinationPointDescription(Activity ctx) {
		// TODO: Set the description of selected destination point 1
		StringBuilder description = new StringBuilder();
		description.append("destination point description 1");
		return description.toString();
	}

	@Override
	public DashFragmentData getCardFragment() {
		return DashPirattoFragment.FRAGMENT_DATA;
	}

	@Override
	public void updatePoints(List<DestinationPoint> points) {
		this.pirattoLayer.refresh();
		this.mapActivity.getMapView().refreshMap();
	}

	// Synchronize plugin lifecycle with Map Activity lifecycle

	@Override
	public void mapActivityCreate(MapActivity activity) {
		super.mapActivityCreate(activity);
		Log.d(TAG, "on activity created");

		this.mapActivity = activity;
	}

	@Override
	public void mapActivityResume(MapActivity activity) {
		super.mapActivityResume(activity);
		Log.d(TAG, "on activity resumed");
	}

	@Override
	public void mapActivityPause(MapActivity activity) {
		super.mapActivityPause(activity);
		Log.d(TAG, "on activity paused");
	}

	@Override
	public void mapActivityScreenOff(MapActivity activity) {
		super.mapActivityScreenOff(activity);
		Log.d(TAG, "on activity screen off");
	}

	@Override
	public void mapActivityDestroy(MapActivity activity) {
		super.mapActivityDestroy(activity);
		Log.d(TAG, "on activity destroy");

		this.pirattoManager.cancelSchedule();
	}
}
