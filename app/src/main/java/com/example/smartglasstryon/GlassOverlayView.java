package com.example.smartglasstryon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

public class GlassOverlayView extends View {

    private Bitmap glassesBitmap;
    private final Paint paint = new Paint();

    // Previous coordinates for smoothing
    private float prevLx = -1, prevLy = -1, prevRx = -1, prevRy = -1;

    // SMOOTHING FACTOR (0.1 = very smooth/slow, 0.9 = fast/jittery)
    private static final float ALPHA = 0.5f;

    private float scaleX = 1.0f;
    private float scaleY = 1.0f;

    public GlassOverlayView(Context context) {
        super(context);
        init();
    }

    public GlassOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GlassOverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        glassesBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.glasses1);
        paint.setAntiAlias(true);
    }

    public void setLandmarks(float lx, float ly, float rx, float ry, int imageWidth, int imageHeight) {
        // SMOOTHING LOGIC
        if (prevLx == -1) {
            prevLx = lx; prevLy = ly; prevRx = rx; prevRy = ry;
        } else {
            prevLx = (prevLx * ALPHA) + (lx * (1 - ALPHA));
            prevLy = (prevLy * ALPHA) + (ly * (1 - ALPHA));
            prevRx = (prevRx * ALPHA) + (rx * (1 - ALPHA));
            prevRy = (prevRy * ALPHA) + (ry * (1 - ALPHA));
        }

        if (imageHeight != 0 && imageWidth != 0) {
            // Standard scaling (Stretch to fill)
            scaleX = (float) getWidth() / imageHeight;
            scaleY = (float) getHeight() / imageWidth;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (prevLx == -1 || glassesBitmap == null) return;

        // 1. Map Coordinates (Rotation handled by swapping width/height in scale calc)
        float lx = getWidth() - (prevLx * scaleX);
        float ly = prevLy * scaleY;
        float rx = getWidth() - (prevRx * scaleX);
        float ry = prevRy * scaleY;

        // 2. Center Point
        float centerX = (lx + rx) / 2;
        float centerY = (ly + ry) / 2;

        // 3. Size Calculation
        float eyeDistance = (float) Math.hypot(lx - rx, ly - ry);
        float glassesWidth = eyeDistance * 4.0f;

        float scaleFactor = glassesWidth / glassesBitmap.getWidth();
        float glassesHeight = glassesBitmap.getHeight() * scaleFactor;

        // 4. VERTICAL NUDGE (THE FIX)
        // Previous value: 0.10f (Moved them UP)
        // New Value: -0.20f (Negative means move DOWN)
        // If this is too low, try -0.10f. If still too high, try -0.30f.
        float verticalNudge = eyeDistance * -0.20f;

        float drawLeft = centerX - (glassesWidth / 2);
        // The math: centerY - height - (negative number) becomes centerY - height + number
        float drawTop = centerY - (glassesHeight / 2) - verticalNudge;

        // 5. Draw
        canvas.drawBitmap(glassesBitmap, null,
                new RectF(drawLeft, drawTop, drawLeft + glassesWidth, drawTop + glassesHeight),
                paint);
    }
}