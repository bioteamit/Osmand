package net.osmand.plus.pirattoplugin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.osmand.plus.R;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;

import java.util.List;

public class DashPirattoFragment extends DashLocationFragment implements PirattoDeleteDialog.PirattoDeleteCallback {

	private static final String TAG = "DASH_PIRATTO_FRAGMENT";
	private static final int TITLE_ID = R.string.osmand_oneteam_piratto_plugin_name;

	private LinearLayout pointsLayout;

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
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_common_fragment, container, false);
		this.pointsLayout = (LinearLayout) view.findViewById(R.id.items);
		view.findViewById(R.id.show_all).setVisibility(View.GONE);
		return view;
	}

	@Override
	public void onOpenDash() {
		View view = this.getView();
		List<DestinationPoint> points = PirattoManager.getInstance().getDestinationPoints();
		if (points == null || points.isEmpty()) {
			view.setVisibility(View.GONE);
			this.pointsLayout.removeAllViews();
			return;
		}

		view.setVisibility(View.VISIBLE);

		String title = view.getContext().getString(R.string.osmand_oneteam_piratto_dashboard_title, points.size());
		((TextView) view.findViewById(R.id.fav_text)).setText(title);

		this.pointsLayout.removeAllViews();
		DashboardPointsAdapter pointsAdapter = new DashboardPointsAdapter(this.getActivity(), points, this.distances, this.getDefaultLocation(), this);
		pointsAdapter.addPointsViews(this.pointsLayout);
	}

	@Override
	public void onPointDeleted(DestinationPoint destinationPoint) {
		this.onOpenDash();
	}
}
