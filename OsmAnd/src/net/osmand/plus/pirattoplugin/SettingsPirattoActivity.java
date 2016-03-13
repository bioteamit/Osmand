package net.osmand.plus.pirattoplugin;

import android.os.Bundle;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.activities.SettingsBaseActivity;

public class SettingsPirattoActivity extends SettingsBaseActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);

		getToolbar().setTitle(R.string.osmand_oneteam_piratto_plugin_name);
	}
}
