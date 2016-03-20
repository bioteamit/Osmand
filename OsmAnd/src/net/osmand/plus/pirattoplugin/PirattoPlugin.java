package net.osmand.plus.pirattoplugin;

import android.app.Activity;
import android.util.Log;

import net.osmand.ValueHolder;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;
import net.osmand.plus.routing.RoutingHelper;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmandMapTileView;

import java.util.Observable;
import java.util.Observer;

public class PirattoPlugin extends OsmandPlugin implements Observer, RoutingHelper.IRouteInformationListener {

	private static final String TAG = "PirattoPlugin";

	public static final String ID = "osmand.oneteam.piratto";
	public static final String PIRATTO_PLUGIN_COMPONENT = "net.osmand.oneteam.pirattoPlugin"; //$NON-NLS-1$
	public final static String KEY_PIRATTO_POINTS = "piratto_points"; //$NON-NLS-1$

	private OsmandApplication application;

	private MapActivity mapActivity;

	private RoutingHelper routingHelper;

	private PirattoManager pirattoManager;

	private PirattoPositionLayer pirattoLayer;

	private PirattoTextInfoWidget targetDestinationPointWidget;

	public PirattoPlugin(OsmandApplication application) {
		this.application = application;
		ApplicationMode.regWidget(KEY_PIRATTO_POINTS, (ApplicationMode[]) null);
		this.pirattoManager = PirattoManager.initialize(this.application);

		this.pirattoManager.enable(this.mapActivity);
		this.routingHelper = this.application.getRoutingHelper();
	}

	@Override
	public boolean init(OsmandApplication app, Activity activity) {
		this.pirattoManager.enable(this.mapActivity);
		return super.init(app, activity);
	}

	@Override
	public void disable(OsmandApplication app) {
		super.disable(app);

		this.pirattoManager.disable();
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
		return SettingsPirattoActivity.class;
	}

	@Override
	public void registerLayers(MapActivity activity) {
		// remove old if existing after turn
		if(this.pirattoLayer != null) {
			activity.getMapView().removeLayer(this.pirattoLayer);
		}
		this.pirattoLayer = new PirattoPositionLayer(activity);
		activity.getMapView().addLayer(this.pirattoLayer, 5.5f);
		if (this.targetDestinationPointWidget == null) {
			this.registerWidgets(activity);
		}
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (this.isActive()) {
			if (this.pirattoLayer == null) {
				registerLayers(activity);
			}
			if (this.targetDestinationPointWidget == null) {
				this.registerWidgets(activity);
			}
		} else {
			if (this.pirattoLayer != null) {
				activity.getMapView().removeLayer(this.pirattoLayer);
				this.pirattoLayer = null;
			}
			MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
			if (mapInfoLayer != null
					&& this.targetDestinationPointWidget != null) {
				mapInfoLayer.removeSideWidget(this.targetDestinationPointWidget);
				mapInfoLayer.recreateControls();
				this.targetDestinationPointWidget = null;
			}
		}
	}

	private void registerWidgets(MapActivity activity) {
		MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
		if (!this.pirattoManager.isRoutingPoint()) {
			DestinationPoint destinationPoint = this.pirattoManager.getNextRoutingPoint();
			if (mapInfoLayer != null && destinationPoint != null) {
				Log.d(TAG, "create point widget for " + destinationPoint.getAddress());

				this.targetDestinationPointWidget = this.createPointInfoControl(activity, destinationPoint);
				mapInfoLayer.registerSideWidget(this.targetDestinationPointWidget,
						R.drawable.ic_action_piratto_dark, R.string.map_widget_piratto, KEY_PIRATTO_POINTS, false, 8);
				mapInfoLayer.recreateControls();
				this.pirattoManager.removeOldTargetPoint();
				this.pirattoManager.routeNextPoint(this.mapActivity);
			}
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

	@Override
	public DashFragmentData getCardFragment() {
		return DashPirattoFragment.FRAGMENT_DATA;
	}

	// Synchronize plugin lifecycle with Map Activity lifecycle

	@Override
	public void mapActivityCreate(MapActivity activity) {
		super.mapActivityCreate(activity);
		Log.d(TAG, "on activity created");

		this.mapActivity = activity;
		this.pirattoManager.addObserver(this);

		this.pirattoManager.refresh();
	}

	@Override
	public void mapActivityResume(MapActivity activity) {
		super.mapActivityResume(activity);
		Log.d(TAG, "on activity resumed");
		this.routingHelper.addListener(this);
	}

	@Override
	public void mapActivityPause(MapActivity activity) {
		super.mapActivityPause(activity);
		Log.d(TAG, "on activity paused");
		this.routingHelper.removeListener(this);
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
		this.pirattoManager.deleteObserver(this);
	}

	@Override
	public void update(Observable observable, Object data) {
		PirattoManager pirattoManager = PirattoManager.getInstance();
		if (!pirattoManager.isRoutingPoint()) {
			pirattoManager.removeOldTargetPoint();
			pirattoManager.routeNextPoint(this.mapActivity);
		}

		this.pirattoLayer.refresh();
		this.mapActivity.getMapView().refreshMap();
	}

	@Override
	public void newRouteIsCalculated(boolean newRoute, ValueHolder<Boolean> showToast) {
	}

	@Override
	public void routeWasCancelled() {
//		Log.d(TAG, "Routing is cancelled");
//		this.pirattoManager.setRoutingPoint(false);
//		this.pirattoManager.setRoutingPoint(null);
//		this.pirattoManager.removeOldTargetPoint();
//		this.pirattoManager.routeNextPoint(this.mapActivity);
	}

	@Override
	public void routeWasFinished() {
	}
}
