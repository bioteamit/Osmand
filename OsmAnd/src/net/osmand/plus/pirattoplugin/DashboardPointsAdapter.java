package net.osmand.plus.pirattoplugin;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.osmand.data.LatLon;
import net.osmand.data.PointDescription;
import net.osmand.plus.OsmandApplication;
import net.osmand.plus.OsmandSettings;
import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.dashboard.DashLocationFragment;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;

import java.util.List;

public class DashboardPointsAdapter {

	private Context context;
	private LayoutInflater inflater;
	private List<DestinationPoint> points;
	private List<DashLocationFragment.DashLocationView> distancesViewList;
	private LatLon defaultLocation;
	private PirattoDeleteDialog.PirattoDeleteCallback pirattoDeleteCallback;

	public DashboardPointsAdapter(Context context, List<DestinationPoint> points, List<DashLocationFragment.DashLocationView> distancesViewList, LatLon defaultLocation, PirattoDeleteDialog.PirattoDeleteCallback deleteCallback) {
		this.context = context;
		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.points = points;
		this.distancesViewList = distancesViewList;
		this.defaultLocation = defaultLocation;
		this.pirattoDeleteCallback = deleteCallback;
	}

	public void addPointsViews(ViewGroup viewGroup) {
		if (viewGroup == null) {
			return;
		}

		for(int i = 0; i < this.points.size(); i++) {
			View view = this.createView(i, viewGroup);
			this.bindView(i, view);
			viewGroup.addView(view);
		}
		return;
	}

	private View createView(int position, ViewGroup parent) {
		View view = this.inflater.inflate(R.layout.dash_piratto_item, parent, false);
		return view;
	}

	private void bindView(final int position, View view) {
		DestinationPoint destinationPoint = this.points.get(position);
		if (destinationPoint == null) {
			view.setVisibility(View.GONE);
			return;
		}

		view.setVisibility(View.VISIBLE);

		LatLon point = destinationPoint.getPoint();

		PointDescription pointDescription = new PointDescription(PointDescription.POINT_TYPE_PIRATTO_MARKER, destinationPoint.getAddress());
		pointDescription.setLat(point.getLatitude());
		pointDescription.setLon(point.getLongitude());

		ImageView iconView = ((ImageView) view.findViewById(R.id.icon));
		iconView.setImageDrawable(this.getMyApplication().getIconsCache().getThemedIcon(R.drawable.ic_type_piratto));

		String name = pointDescription.getName();
		TextView nameView = ((TextView) view.findViewById(R.id.address));
		nameView.setText(name);

		ImageView directionView = (ImageView) view.findViewById(R.id.direction);
		TextView distanceView = (TextView) view.findViewById(R.id.distance);
		directionView.setImageDrawable(this.getMyApplication().getIconsCache().getIcon(R.drawable.ic_destination_arrow_white, R.color.color_distance));
		if (this.defaultLocation != null) {
			DashLocationFragment.DashLocationView dv = new DashLocationFragment.DashLocationView(directionView, distanceView, point);
			dv.paint = false;
			dv.arrowResId = R.drawable.ic_action_start_navigation;
			this.distancesViewList.add(dv);
		}

		ImageButton removeView = (ImageButton) view.findViewById(R.id.remove);
		removeView.setImageDrawable(this.getMyApplication().getIconsCache().
				getThemedIcon(R.drawable.ic_action_remove_dark));

		removeView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DashboardPointsAdapter.this.onClickRemove(position);
			}
		});

		ImageButton navigateView = (ImageButton) view.findViewById(R.id.navigate_to);
		navigateView.setImageDrawable(this.getMyApplication().getIconsCache().getThemedIcon(R.drawable.ic_action_gdirections_dark));
		navigateView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DashboardPointsAdapter.this.onClickNavigate(position);
			}
		});

		View itemView = view.findViewById(R.id.destination_point_item);
		itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DashboardPointsAdapter.this.onClickShowPoint(position);
			}
		});
	}

	private void onClickRemove(int position) {
		DestinationPoint destinationPoint = this.points.get(position);
		if (!(this.context instanceof FragmentActivity)
				|| destinationPoint == null) {
			return;
		}

		FragmentActivity activity = (FragmentActivity) this.context;
		FragmentManager manager = activity.getSupportFragmentManager();
		PirattoDeleteDialog.newInstance(destinationPoint, this.pirattoDeleteCallback).show(manager, PirattoDeleteDialog.TAG);
	}

	private void onClickShowPoint(int position) {
		DestinationPoint destinationPoint = this.points.get(position);
		if (destinationPoint == null) {
			return;
		}

		LatLon point = destinationPoint.getPoint();

		OsmandSettings settings = ((OsmandApplication) this.context.getApplicationContext()).getSettings();

		settings.setMapLocationToShow(point.getLatitude(), point.getLongitude(),
				15, new PointDescription(PointDescription.POINT_TYPE_PIRATTO_MARKER, destinationPoint.getAddress()), false,
				point); //$NON-NLS-1$
		MapActivity.launchMapActivityMoveToTop(this.context);
	}

	private void onClickNavigate(int position) {
		DestinationPoint destinationPoint = this.points.get(position);
		if (!(this.context instanceof FragmentActivity)
				|| destinationPoint == null) {
			return;
		}

		PirattoManager.getInstance().navigateTo(this.context, destinationPoint);
	}

	private OsmandApplication getMyApplication() {
		return ((OsmandApplication) this.context.getApplicationContext());
	}
}
