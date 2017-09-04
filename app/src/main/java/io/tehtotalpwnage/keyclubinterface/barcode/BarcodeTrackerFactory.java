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

import android.app.Activity;
import android.widget.TextView;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import io.tehtotalpwnage.keyclubinterface.camera.GraphicOverlay;
import io.tehtotalpwnage.keyclubinterface.R;

public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private boolean requesting;

    private Activity mContext;
    private GraphicOverlay mGraphicOverlay;
    private List<String> mList;

    public BarcodeTrackerFactory(Activity context, GraphicOverlay graphicOverlay) {
        requesting = false;

        mContext = context;
        mGraphicOverlay = graphicOverlay;
        mList = new ArrayList<>();
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new BarcodeGraphicTracker(mContext, mGraphicOverlay, graphic, this);
    }

    void addID(String id) {
        mList.add(id);

        ((TextView) mContext.findViewById(R.id.textView2)).setText(NumberFormat.getInstance().format(mList.size()));
    }

    public List<String> getList() {
        return mList;
    }

    boolean isRequesting() {
        return requesting;
    }

    void setRequesting(boolean requesting) {
        this.requesting = requesting;
    }
}
