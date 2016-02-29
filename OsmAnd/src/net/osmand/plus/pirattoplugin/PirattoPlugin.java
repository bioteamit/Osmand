package net.osmand.plus.pirattoplugin;

import android.app.Activity;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.views.OsmandMapTileView;

public class PirattoPlugin extends OsmandPlugin {

	public static final String ID = "osmand.oneteam.piratto";
	public static final String PIRATTO_PLUGIN_COMPONENT = "net.osmand.oneteam.pirattoPlugin"; //$NON-NLS-1$

	private OsmandApplication app;

	public PirattoPlugin(OsmandApplication app) {
		this.app = app;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String getDescription() {
		return app.getString(R.string.osmand_oneteam_piratto_plugin_description);
	}

	@Override
	public String getName() {
		return app.getString(R.string.osmand_oneteam_piratto_plugin_name);
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
	}

	@Override
	public void updateLayers(OsmandMapTileView mapView, MapActivity activity) {
	}
}
