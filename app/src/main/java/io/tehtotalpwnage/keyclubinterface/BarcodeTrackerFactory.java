package io.tehtotalpwnage.keyclubinterface;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.ArrayMap;
import android.widget.TextView;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by tehtotalpwnage on 8/17/17.
 */

public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private boolean requesting;

    private Activity mContext;
    private GraphicOverlay mGraphicOverlay;
    private List<String> mList;

    BarcodeTrackerFactory(Activity context, GraphicOverlay graphicOverlay) {
        requesting = false;

        mContext = context;
        mGraphicOverlay = graphicOverlay;
        mList = new ArrayList<>();
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new GraphicBarcodeTracker(mContext, mGraphicOverlay, graphic, this);
    }

    public void addID(String id) {
        mList.add(id);

        ((TextView) mContext.findViewById(R.id.textView2)).setText(((Integer) mList.size()).toString());
    }

    public List<String> getList() {
        return mList;
    }

    public boolean isRequesting() {
        return requesting;
    }

    public void setRequesting(boolean requesting) {
        this.requesting = requesting;
    }
}

class BarcodeGraphic extends TrackedBarcodeGraphic {
    private static final int COLOR_CHOICES[] = {
            Color.BLUE,
            Color.CYAN,
            Color.GREEN
    };
    private static int mCurrentColorIndex = 0;

    private Paint mRectPaint;
    private Paint mTextPaint;
    private volatile Barcode mBarcode;

    BarcodeGraphic(GraphicOverlay overlay) {
        super(overlay);

        mCurrentColorIndex = (mCurrentColorIndex + 1) % COLOR_CHOICES.length;
        final int selectedColor = COLOR_CHOICES[mCurrentColorIndex];

        mRectPaint = new Paint();
        mRectPaint.setColor(selectedColor);
        mRectPaint.setStyle(Paint.Style.STROKE);
        mRectPaint.setStrokeWidth(4.0f);

        mTextPaint = new Paint();
        mTextPaint.setColor(selectedColor);
        mTextPaint.setTextSize(36.0f);
    }

    void updateItem(Barcode barcode) {
        mBarcode = barcode;
        postInvalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        Barcode barcode = mBarcode;
        if (barcode == null) {
            return;
        }

        RectF rect = new RectF(barcode.getBoundingBox());
        rect.left = translateX(rect.left);
        rect.top = translateY(rect.top);
        rect.right = translateX(rect.right);
        rect.bottom = translateY(rect.bottom);
        canvas.drawRect(rect, mRectPaint);

        canvas.drawText(barcode.rawValue, rect.left, rect.bottom, mTextPaint);
    }
}
