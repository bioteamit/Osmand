package net.osmand.plus.pirattoplugin;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;

import net.osmand.data.LatLon;
import net.osmand.plus.ApplicationMode;
import net.osmand.plus.ContextMenuAdapter;
import net.osmand.plus.OsmAndFormatter;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.mapcontextmenu.MapContextMenu;
import net.osmand.plus.views.AnimateDraggingMapThread;
import net.osmand.plus.views.MapInfoLayer;
import net.osmand.plus.views.OsmandMapLayer;
import net.osmand.plus.views.OsmandMapTileView;
import net.osmand.plus.views.mapwidgets.TextInfoWidget;

public class PirattoPlugin extends OsmandPlugin {

	public static final String ID = "osmand.oneteam.piratto";
	public static final String PIRATTO_PLUGIN_COMPONENT = "net.osmand.oneteam.pirattoPlugin"; //$NON-NLS-1$
	public final static String PIRATTO_POINT_LAT = "piratto_point_lat"; //$NON-NLS-1$
	public final static String PIRATTO_POINT_LON = "piratto_point_lon"; //$NON-NLS-1$
	public final static String KEY_PIRATTO_POINTS = "piratto_points"; //$NON-NLS-1$
	private LatLon destinationPoint;
	private OsmandApplication application;

	private PirattoPositionLayer pirattoLayer;
	private TextInfoWidget pirattoPlaceControl;
	private final OsmandSettings.CommonPreference<Float> pointLatitude;
	private final OsmandSettings.CommonPreference<Float> pointLongitude;

	public PirattoPlugin(OsmandApplication application) {
		this.application = application;
		OsmandSettings set = application.getSettings();
		ApplicationMode.regWidget(KEY_PIRATTO_POINTS, (ApplicationMode[]) null);
		this.pointLatitude = set.registerFloatPreference(PIRATTO_POINT_LAT, 0f).makeGlobal();
		this.pointLongitude = set.registerFloatPreference(PIRATTO_POINT_LON, 0f).makeGlobal();
		this.destinationPoint = constructDestinationPoint();
	}

	public LatLon getDestinationPoint() {
		return this.destinationPoint;
	}

	public LatLon constructDestinationPoint() {
		float lat = this.pointLatitude.get();
		float lon = this.pointLongitude.get();
		if (lat == 0 && lon == 0) {
			return null;
		}
		return new LatLon(lat, lon);
	}

	public boolean clearDestinationPoint() {
		this.pointLatitude.resetToDefault();
		this.pointLongitude.resetToDefault();
		this.destinationPoint = null;
		return true;
	}

	public boolean setDestinationPoint(double latitude, double longitude) {
		pointLatitude.set((float) latitude);
		pointLongitude.set((float) longitude);
		destinationPoint = constructDestinationPoint();
		return true;
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
		registerWidget(activity);
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
		if (isActive()) {
			if (this.pirattoLayer == null) {
				registerLayers(activity);
			}
			if (this.pirattoPlaceControl == null) {
				registerWidget(activity);
			}
		} else {
			if (this.pirattoLayer != null) {
				activity.getMapView().removeLayer(this.pirattoLayer);
				this.pirattoLayer = null;
			}
			MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
			if (mapInfoLayer != null && this.pirattoPlaceControl != null) {
				mapInfoLayer.removeSideWidget(this.pirattoPlaceControl);
				mapInfoLayer.recreateControls();
				this.pirattoPlaceControl = null;
			}
		}
	}

	private void registerWidget(MapActivity activity) {
		MapInfoLayer mapInfoLayer = activity.getMapLayers().getMapInfoLayer();
		if (mapInfoLayer != null) {
			this.pirattoPlaceControl = this.createPointInfoControl(activity);
			mapInfoLayer.registerSideWidget(this.pirattoPlaceControl,
					R.drawable.ic_action_piratto_dark,  R.string.map_widget_piratto, KEY_PIRATTO_POINTS, false, 8);
			mapInfoLayer.recreateControls();
		}
	}

	@Override
	public void registerMapContextMenuActions(final MapActivity mapActivity,
											  final double latitude, final double longitude,
											  ContextMenuAdapter adapter, Object selectedObj) {

		ContextMenuAdapter.OnContextMenuClick addListener = new ContextMenuAdapter.OnContextMenuClick() {
			@Override
			public boolean onContextMenuClick(ArrayAdapter<?> adapter, int resId,
											  int pos, boolean isChecked) {
				if (resId == R.string.context_menu_item_piratto_add_destination_point) {
					PirattoPlugin.this.addDestinationPointToMap(mapActivity, latitude, longitude);
				}
				return true;
			}
		};
		adapter.item(R.string.context_menu_item_piratto_add_destination_point)
				.iconColor( R.drawable.ic_action_piratto_dark).listen(addListener).reg();

	}

	private void addDestinationPointToMap(MapActivity mapActivity, double latitude, double longitude) {
		this.setDestinationPoint(mapActivity, latitude, longitude, false);
		this.showContextMenuIfNeeded(mapActivity);
		mapActivity.getMapView().refreshMap();
	}

	private void showContextMenuIfNeeded(final MapActivity mapActivity) {
		if (this.pirattoLayer != null) {
			MapContextMenu menu = mapActivity.getContextMenu();
			if (menu.isVisible()) {
				menu.show(new LatLon(this.destinationPoint.getLatitude(), this.destinationPoint.getLongitude()),
						this.pirattoLayer.getObjectName(this.destinationPoint), this.destinationPoint);
			}
		}
	}

	/**
	 * Method creates confirmation dialog for deletion of a destination point location.
	 */
	public AlertDialog showDeleteDialog(final Activity activity) {
		AlertDialog.Builder confirm = new AlertDialog.Builder(activity);
		confirm.setTitle(activity.getString(R.string.osmand_oneteam_piratto_delete));
		confirm.setMessage(activity.getString(R.string.osmand_oneteam_piratto_delete_confirm));
		confirm.setCancelable(true);
		confirm.setPositiveButton(R.string.shared_string_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelDestinationPoint();
				if (activity instanceof MapActivity) {
					((MapActivity) activity).getContextMenu().close();
				}
			}
		});
		confirm.setNegativeButton(R.string.shared_string_cancel, null);
		return confirm.show();
	}

	/**
	 * Method sets a destination point on a PirattoLayer.
	 * @param mapActivity
	 * @param latitude
	 * @param longitude
	 * @param isLimited
	 */
	private void setDestinationPoint(final MapActivity mapActivity, final double latitude, final double longitude, boolean isLimited) {
		this.setDestinationPoint(latitude, longitude);
		if (this.pirattoLayer != null) {
			this.pirattoLayer.refresh();
		}
	}

	private void cancelDestinationPoint() {
		if (this.pirattoLayer != null) {
			this.pirattoLayer.refresh();
		}
		clearDestinationPoint();
	}

	@Override
	public void registerOptionsMenuItems(final MapActivity mapActivity, ContextMenuAdapter helper) {
	}

	/**
	 * @return the control to be added on a MapInfoLayer
	 * that shows a distance between
	 * the current position on the map
	 * and the location of the destination point
	 */
	private TextInfoWidget createPointInfoControl(final MapActivity map) {
		TextInfoWidget pointInfoControl = new TextInfoWidget(map) {
			private float[] calculations = new float[1];
			private int cachedMeters = 0;

			@Override
			public boolean updateInfo(OsmandMapLayer.DrawSettings drawSettings) {
				LatLon destinationPoint = pirattoLayer.getDestinationPoint();
				if( destinationPoint != null && !map.getRoutingHelper().isFollowingMode()) {
					OsmandMapTileView view = map.getMapView();
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
							String ds = OsmAndFormatter.getFormattedDistance(cachedMeters, map.getMyApplication());
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
		};
		pointInfoControl.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				OsmandMapTileView mapView = map.getMapView();
				AnimateDraggingMapThread thread = mapView.getAnimatedDraggingThread();
				final LatLon destinationPoint = PirattoPlugin.this.destinationPoint;
				if (destinationPoint != null) {
					int fZoom = mapView.getZoom() < 15 ? 15 : mapView.getZoom();
					thread.startMoving(destinationPoint.getLatitude(), destinationPoint.getLongitude(), fZoom, true);
				}
			}
		});
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
}
