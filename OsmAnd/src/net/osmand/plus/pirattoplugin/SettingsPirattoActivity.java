package net.osmand.plus.pirattoplugin;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.SettingsBaseActivity;
import net.osmand.plus.pirattoplugin.core.PirattoManager;

public class SettingsPirattoActivity extends SettingsBaseActivity {

	private OsmandSettings.CommonPreference<String> carPlateSettings;
	private OsmandSettings.CommonPreference<Integer> updateIntervalSettings;
	private PirattoManager pirattoManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		((OsmandApplication) getApplication()).applyTheme(this);
		super.onCreate(savedInstanceState);

		getToolbar().setTitle(R.string.osmand_oneteam_piratto_plugin_name);
		addPreferencesFromResource(R.xml.piratto_settings);

		try {
			this.pirattoManager = PirattoManager.getInstance();
		} catch (Exception e) {
			this.pirattoManager = PirattoManager.initialize(this.getMyApplication());
		}

		this.carPlateSettings = this.settings.registerStringPreference(PirattoManager.PIRATTO_CAR_PLATE, null).makeGlobal();
		this.updateIntervalSettings = this.settings.registerIntPreference(PirattoManager.PIRATTO_UPDATE_INTERVAL, 2).makeGlobal();

		final EditTextPreference carPlatePref = (EditTextPreference) findPreference(this.carPlateSettings.getId());
		final EditTextPreference updateIntervalPref = (EditTextPreference) findPreference(this.updateIntervalSettings.getId());

		// Registered cars
		// "CB 763AU" "CB 060EG" "CB 077CN" "CB 201AX" "CB 8627W"
		carPlatePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				carPlateSettings.set((String) newValue);
				pirattoManager.refresh();
				return true;
			}
		});

		updateIntervalPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				updateIntervalSettings.set((Integer) newValue);
				pirattoManager.refresh();
				return true;
			}
		});
	}
}
