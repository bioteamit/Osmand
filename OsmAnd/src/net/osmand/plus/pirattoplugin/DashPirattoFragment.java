package net.osmand.plus.pirattoplugin;

import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandPlugin;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.helpers.FontCache;

/**
 * Created by Denis on
 * 26.01.2015.
 */
public class DashPirattoFragment extends DashLocationFragment {
	private static final String TAG = "DASH_PIRATTO_FRAGMENT";
	private static final int TITLE_ID = R.string.osmand_oneteam_piratto_plugin_name;
	PirattoPlugin plugin;

	private static final DashFragmentData.ShouldShowFunction SHOULD_SHOW_FUNCTION =
			new DashboardOnMap.DefaultShouldShow() {
				@Override
				public int getTitleId() {
					return TITLE_ID;
				}
			};
	static final DashFragmentData FRAGMENT_DATA = new DashFragmentData(
			DashPirattoFragment.TAG, DashPirattoFragment.class,
			SHOULD_SHOW_FUNCTION, 50, null);

	@Override
	public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_parking_fragment, container, false);
		Typeface typeface = FontCache.getRobotoMedium(getActivity());
		Button remove = (Button) view.findViewById(R.id.remove_tag);
		remove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog dialog = plugin.showDeleteDialog(getActivity());
				dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface dialog) {
						updateParkingPosition();
					}
				});
			}
		});
		remove.setTypeface(typeface);

		view.findViewById(R.id.parking_header).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				LatLon point = plugin.getDestinationPoint();
				getMyApplication().getSettings().setMapLocationToShow(point.getLatitude(), point.getLongitude(),
						15, new PointDescription(PointDescription.POINT_TYPE_PARKING_MARKER, getString(R.string.osmand_parking_position_name)), false,
						point); //$NON-NLS-1$
				MapActivity.launchMapActivityMoveToTop(getActivity());
			}
		});
		return view;
	}

	@Override
	public void onOpenDash() {
		plugin = OsmandPlugin.getEnabledPlugin(PirattoPlugin.class);
		updateParkingPosition();
	}

	private void updateParkingPosition() {
		View mainView = getView();
		if (mainView == null) {
			return;
		}
		if (plugin == null || plugin.getDestinationPoint() == null) {
			mainView.setVisibility(View.GONE);
			return;
		} else {
			mainView.setVisibility(View.VISIBLE);
		}

		LatLon loc = getDefaultLocation();
		LatLon position = plugin.getDestinationPoint();

		TextView timeLeft = (TextView) mainView.findViewById(R.id.time_left);
		String description = getString(R.string.parking_place);
		timeLeft.setText("");
		timeLeft.setVisibility(View.GONE);
		((TextView) mainView.findViewById(R.id.name)).setText(description);
		ImageView direction = (ImageView) mainView.findViewById(R.id.direction_icon);
		if (loc != null) {
			DashLocationView dv = new DashLocationView(direction, (TextView) mainView.findViewById(R.id.distance), position);
			dv.paint = false;
			dv.arrowResId = R.drawable.ic_action_start_navigation; 
			distances.add(dv);
		}


	}

	String getFormattedTime(long timeInMillis) {
		if (timeInMillis < 0) {
			timeInMillis *= -1;
		}
		StringBuilder timeStringBuilder = new StringBuilder();
		int hours = (int) timeInMillis / (1000 * 60 * 60);
		int minMills = (int) timeInMillis % (1000 * 60 * 60);
		int minutes = minMills / (1000 * 60);
		if (hours > 0) {
			timeStringBuilder.append(hours);
			timeStringBuilder.append(" ");
			timeStringBuilder.append(getResources().getString(R.string.osmand_parking_hour));
		}

		timeStringBuilder.append(" ");
		timeStringBuilder.append(minutes);
		timeStringBuilder.append(" ");
		timeStringBuilder.append(getResources().getString(R.string.osmand_parking_minute));


		return timeStringBuilder.toString();
	}
}
