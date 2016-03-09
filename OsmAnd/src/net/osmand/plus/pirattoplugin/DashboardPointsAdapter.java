package net.osmand.plus.pirattoplugin;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.helpers.FontCache;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;

import java.util.List;

public class DashboardPointsAdapter extends ArrayAdapter<DestinationPoint> implements View.OnClickListener {

	private LayoutInflater inflater;
	private List<DashLocationFragment.DashLocationView> distancesViewList;
	private LatLon defaultLocation;

	public DashboardPointsAdapter(Context context, List<DestinationPoint> points, List<DashLocationFragment.DashLocationView> distancesViewList, LatLon defaultLocation) {
		super(context, R.layout.dash_piratto_item, points);
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.distancesViewList = distancesViewList;
		this.defaultLocation = defaultLocation;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		if (view == null) {
			view = this.createView(position, parent);
		}
		view.setTag(R.id.tag_dashboard_piratto_point, Integer.valueOf(position));
		this.bindView(position, view);
		return view;
	}

	private View createView(int position, ViewGroup parent) {
		View view = this.inflater.inflate(R.layout.dash_piratto_item, parent, false);
		return view;
	}

	private void bindView(int position, View view) {
		DestinationPoint destinationPoint = this.getItem(position);
		if (destinationPoint == null) {
			view.setVisibility(View.GONE);
			return;
		}

		view.setVisibility(View.VISIBLE);

		LatLon point = destinationPoint.getPoint();

		String description = view.getContext().getString(R.string.osmand_oneteam_piratto_destination_point);
		((TextView) view.findViewById(R.id.name)).setText(description);
		ImageView direction = (ImageView) view.findViewById(R.id.direction_icon);
		if (this.defaultLocation != null) {
			DashLocationFragment.DashLocationView dv = new DashLocationFragment.DashLocationView(direction, (TextView) view.findViewById(R.id.distance), point);
			dv.paint = false;
			dv.arrowResId = R.drawable.ic_action_start_navigation;
			this.distancesViewList.add(dv);
		}

		Typeface typeface = FontCache.getRobotoMedium(view.getContext());
		Button remove = (Button) view.findViewById(R.id.remove_tag);
		remove.setOnClickListener(this);
		remove.setTypeface(typeface);

		view.findViewById(R.id.destination_point_header).setOnClickListener(this);
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		switch (id) {
			case R.id.remove_tag:
				this.onClickRemoveTag(view);
				return;
			case R.id.destination_point_header:
				this.onClickShowPoint(view);
				return;
		}
	}

	private void onClickRemoveTag(View view) {
		Integer position = (Integer) view.getTag(R.id.tag_dashboard_piratto_point);
		if (this.getContext() instanceof FragmentActivity) {
			FragmentActivity activity = (FragmentActivity) this.getContext();
			FragmentManager manager = activity.getSupportFragmentManager();
			PirattoDeleteDialog.newInstance(this.getItem(position)).show(manager, PirattoDeleteDialog.TAG);
		}
	}

	private void onClickShowPoint(View view) {
		Integer position = (Integer) view.getTag(R.id.tag_dashboard_piratto_point);
		DestinationPoint destinationPoint = this.getItem(position);
		LatLon point = destinationPoint.getPoint();

		OsmandSettings settings = ((OsmandApplication) this.getContext().getApplicationContext()).getSettings();

		settings.setMapLocationToShow(point.getLatitude(), point.getLongitude(),
				15, new PointDescription(PointDescription.POINT_TYPE_PIRATTO_MARKER, destinationPoint.getAddress()), false,
				point); //$NON-NLS-1$
		MapActivity.launchMapActivityMoveToTop(view.getContext());
	}
}
