package net.osmand.plus.pirattoplugin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.osmand.plus.R;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.dashboard.DashboardOnMap;
import net.osmand.plus.dashboard.tools.DashFragmentData;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class DashPirattoFragment extends DashLocationFragment implements Observer, PirattoDeleteDialog.PirattoDeleteCallback {

	private static final String TAG = "DASH_PIRATTO_FRAGMENT"; //$NON-NLS-1$
	private static final int TITLE_ID = R.string.osmand_oneteam_piratto_plugin_name;

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

	private LinearLayout pointsLayout;
	private boolean isActive;

	@Override
	public View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = getActivity().getLayoutInflater().inflate(R.layout.dash_common_fragment, container, false);
		this.pointsLayout = (LinearLayout) view.findViewById(R.id.items);
		return view;
	}

	@Override
	public void onOpenDash() {
		super.onOpenDash();
		this.isActive = true;

		List<DestinationPoint> points = PirattoManager.getInstance().getDestinationPoints();
		View view = this.getView();

		String title = view.getContext().getString(R.string.osmand_oneteam_piratto_dashboard_title, points.size());
		((TextView) view.findViewById(R.id.fav_text)).setText(title);

		String update = view.getContext().getString(R.string.osmand_oneteam_piratto_dashboard_update);
		Button manualUpdate = (Button) view.findViewById(R.id.show_all);
		manualUpdate.setText(update);
		manualUpdate.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				PirattoManager.getInstance().refresh();
				Toast.makeText(v.getContext(), v.getContext().getString(R.string.osmand_oneteam_piratto_update_points), Toast.LENGTH_SHORT).show();
			}
		});

		this.pointsLayout.removeAllViews();
		DashboardPointsAdapter pointsAdapter = new DashboardPointsAdapter(this.getActivity(), points, this.distances, this.getDefaultLocation(), this);
		pointsAdapter.addPointsViews(this.pointsLayout);
	}

	@Override
	public void onCloseDash() {
		super.onCloseDash();
		this.isActive = false;
	}

	@Override
	public void onPointDeleted(DestinationPoint destinationPoint) {
		this.onOpenDash();
	}

	@Override
	public void update(Observable observable, Object data) {

		if (data == null || !(data instanceof ArrayList)) {
			return;
		}

		final ArrayList<DestinationPoint> points = (ArrayList) data;
		this.getActivity().runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (points.isEmpty() && DashPirattoFragment.this.isActive) {
					Toast.makeText(DashPirattoFragment.this.getActivity(), DashPirattoFragment.this.getString(R.string.osmand_oneteam_piratto_update_no_points), Toast.LENGTH_SHORT).show();
				}
				DashPirattoFragment.this.onOpenDash();
			}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		PirattoManager.getInstance().addObserver(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		PirattoManager.getInstance().deleteObserver(this);
	}
}
