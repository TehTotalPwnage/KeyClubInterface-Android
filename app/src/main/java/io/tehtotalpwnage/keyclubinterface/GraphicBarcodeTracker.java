package io.tehtotalpwnage.keyclubinterface;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by tehtotalpwnage on 8/18/17.
 */

public class GraphicBarcodeTracker extends Tracker {
    private final String TAG = GraphicBarcodeTracker.class.getSimpleName();

    private Context mContext;
    private BarcodeTrackerFactory mFactory;
    private GraphicOverlay mOverlay;
    private TrackedBarcodeGraphic mGraphic;

    GraphicBarcodeTracker(Context context, GraphicOverlay overlay, TrackedBarcodeGraphic graphic, BarcodeTrackerFactory factory) {
        mFactory = factory;
        mContext = context;
        mOverlay = overlay;
        mGraphic = graphic;
    }

    @Override
    public void onNewItem(int i, final Object o) {
        mGraphic.setId(i);
        Log.d(TAG, ((Barcode) o).displayValue);
        if (mFactory.getList().contains(((Barcode) o).displayValue) || mFactory.isRequesting()) {
            return;
        }
        mFactory.setRequesting(true);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("Approve ID?")
                .setMessage("ID Number: " + ((Barcode) o).displayValue)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFactory.addID(((Barcode) o).displayValue);
                        mFactory.setRequesting(false);
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mFactory.setRequesting(false);
                    }
        });
        ((BarcodeScanActivity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.show();
            }
        });
    }


    @Override
    public void onMissing(Detector.Detections detections) {
        mOverlay.remove(mGraphic);
    }

    @Override
    public void onDone() {
        mOverlay.remove(mGraphic);
    }

    @Override
    public void onUpdate(Detector.Detections detections, Object o) {
        mOverlay.add(mGraphic);
        mGraphic.updateItem((Barcode) o);
    }
}
