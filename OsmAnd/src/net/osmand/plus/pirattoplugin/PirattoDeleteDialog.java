package net.osmand.plus.pirattoplugin;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.osmand.plus.R;
import net.osmand.plus.activities.MapActivity;
import net.osmand.plus.pirattoplugin.core.DestinationPoint;
import net.osmand.plus.pirattoplugin.core.PirattoManager;
import net.osmand.plus.views.OsmandMapLayer;

import java.util.List;

public class PirattoDeleteDialog extends DialogFragment {

	public static final String TAG = "PirattoDeleteDialog";
	public static final String ARG_DESTINATION_POINT = "ARG_DESTINATION_POINT";

	private DestinationPoint destinationPoint;

	public static PirattoDeleteDialog newInstance(DestinationPoint destinationPoint) {
		PirattoDeleteDialog fragment = new PirattoDeleteDialog();
		Bundle args = new Bundle();
		args.putSerializable(ARG_DESTINATION_POINT, destinationPoint);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		this.destinationPoint = (DestinationPoint) this.getArguments().getSerializable(ARG_DESTINATION_POINT);

		Activity activity = this.getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setTitle(activity.getString(R.string.osmand_oneteam_piratto_delete));
		builder.setMessage(activity.getString(R.string.osmand_oneteam_piratto_delete_confirm));
		builder.setCancelable(true);
		builder.setPositiveButton(R.string.shared_string_yes, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				PirattoDeleteDialog.this.deleteDestinationPoint();
			}
		});
		builder.setNegativeButton(R.string.shared_string_cancel, null);

		return builder.create();
	}

	private void deleteDestinationPoint() {
		this.cancelDestinationPoint();
		Activity activity = this.getActivity();
		if (activity instanceof MapActivity) {
			((MapActivity) activity).getContextMenu().close();
		}
	}

	private void cancelDestinationPoint() {
		PirattoManager pirattoManager = PirattoManager.getInstance();
		pirattoManager.removeDestinationPoint(this.destinationPoint);

		Activity activity = this.getActivity();
		if (!(activity instanceof MapActivity)) {
			return;
		}

		List<OsmandMapLayer> layers = ((MapActivity) activity).getMapView().getLayers();
		for (OsmandMapLayer layer : layers) {
			if (layer instanceof PirattoPositionLayer) {
				((PirattoPositionLayer) layer).refresh();
			}
		}
	}
}
