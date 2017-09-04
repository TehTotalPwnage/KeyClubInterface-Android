/*
 * Copyright (c) 2017 Michael Nguyen
 *
 * This file is part of KeyClubInterface.
 *
 * KeyClubInterface is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * KeyClubInterface is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with KeyClubInterface.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.tehtotalpwnage.keyclubinterface.barcode;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import io.tehtotalpwnage.keyclubinterface.BarcodeScanActivity;
import io.tehtotalpwnage.keyclubinterface.camera.GraphicOverlay;

class BarcodeGraphicTracker extends Tracker<Barcode> {
    private final String TAG = BarcodeGraphicTracker.class.getSimpleName();

    private Context mContext;
    private BarcodeTrackerFactory mFactory;
    private GraphicOverlay mOverlay;
    private BarcodeGraphic mGraphic;

    BarcodeGraphicTracker(Context context, GraphicOverlay overlay, BarcodeGraphic graphic, BarcodeTrackerFactory factory) {
        mFactory = factory;
        mContext = context;
        mOverlay = overlay;
        mGraphic = graphic;
    }

    @Override
    public void onNewItem(int i, final Barcode o) {
        mGraphic.setId(i);
        Log.d(TAG, o.displayValue);
        if (mFactory.getList().contains(o.displayValue) || mFactory.isRequesting()) {
            return;
        }
        mFactory.setRequesting(true);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("Approve ID?")
                .setMessage("ID Number: " + o.displayValue)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFactory.addID(o.displayValue);
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
    public void onUpdate(Detector.Detections detections, Barcode o) {
        mOverlay.add(mGraphic);
        mGraphic.updateItem(o);
    }
}
