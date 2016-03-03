package net.osmand.plus.pirattoplugin;

import android.graphics.drawable.Drawable;

import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.mapcontextmenu.MenuBuilder;
import net.osmand.plus.mapcontextmenu.MenuController;
import net.osmand.util.Algorithms;

public class PirattoPositionMenuController extends MenuController {

	private PirattoPlugin plugin;
	private String destinationPointDescription = "";

	public PirattoPositionMenuController(OsmandApplication app, MapActivity mapActivity, PointDescription pointDescription) {
		super(new MenuBuilder(app), pointDescription, mapActivity);
		plugin = OsmandPlugin.getPlugin(PirattoPlugin.class);
		if (plugin != null) {
			this.buildDestinationPointDescription(mapActivity);
		}
		leftTitleButtonController = new TitleButtonController() {
			@Override
			public void buttonPressed() {
				if (plugin != null) {
					plugin.showDeleteDialog(getMapActivity());
				}
			}
		};
		leftTitleButtonController.caption = getMapActivity().getString(R.string.shared_string_delete);
		leftTitleButtonController.leftIconId = R.drawable.ic_action_delete_dark;
	}

	private void buildDestinationPointDescription(MapActivity mapActivity) {
		// TODO: add destination point description 2
		StringBuilder sb = new StringBuilder();
		sb.append("destination point description 2");
		destinationPointDescription = sb.toString();
	}

	@Override
	protected void setObject(Object object) {
		if (plugin != null) {
			buildDestinationPointDescription(getMapActivity());
		}
	}

	@Override
	protected int getSupportedMenuStatesPortrait() {
		return MenuState.HEADER_ONLY | MenuState.HALF_SCREEN;
	}

	@Override
	public boolean needTypeStr() {
		return !Algorithms.isEmpty(this.destinationPointDescription);
	}

	@Override
	public boolean displayDistanceDirection() {
		return true;
	}

	@Override
	public Drawable getLeftIcon() {
		return getIcon(R.drawable.ic_action_piratto_dark, R.color.map_widget_blue);
	}

	@Override
	public String getTypeStr() {
		return this.destinationPointDescription;
	}

	@Override
	public boolean needStreetName() {
		return false;
	}
}
