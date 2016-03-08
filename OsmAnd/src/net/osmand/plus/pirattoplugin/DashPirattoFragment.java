package net.osmand.plus.pirattoplugin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.osmand.plus.R;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;

import java.util.List;

public class DashPirattoFragment extends DashLocationFragment {

	private static final String TAG = "DASH_PIRATTO_FRAGMENT";
	private static final int TITLE_ID = R.string.osmand_oneteam_piratto_plugin_name;

	private ListView pointsListView;

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
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_piratto_fragment, container, false);
		this.pointsListView = (ListView) view.findViewById(R.id.lst_piratto_points);
		return view;
	}

	@Override
	public void onOpenDash() {
		List<DestinationPoint> points = PirattoManager.getInstance().getDestinationPoints();
		if (points == null || points.isEmpty()) {
			this.getView().setVisibility(View.GONE);
			this.pointsListView.setAdapter(null);
			return;
		}

		this.getView().setVisibility(View.VISIBLE);

		DashboardPointsAdapter pointsAdapter = new DashboardPointsAdapter(this.getActivity(), points, this.distances, this.getDefaultLocation());
		this.pointsListView.setAdapter(pointsAdapter);
	}
}
