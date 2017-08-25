package io.tehtotalpwnage.keyclubinterface;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by tehtotalpwnage on 8/18/17.
 */

abstract class TrackedBarcodeGraphic extends GraphicOverlay.Graphic {
    private int mId;

    TrackedBarcodeGraphic(GraphicOverlay overlay) {
        super(overlay);
    }

    void setId(int id) {
        mId = id;
    }

    abstract void updateItem(Barcode item);
}
