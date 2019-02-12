package com.fx.android.watercamerademo.Camera;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CaptureFocuseView extends View {

    private Paint paint;
    private static final int kk = 10;

    public CaptureFocuseView(Context context) {
        super(context);
        commonInit();
    }

    public CaptureFocuseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        commonInit();
    }

    public CaptureFocuseView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        commonInit();
    }

    private void commonInit() {
        paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(4.0f);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int w_2 = width / 2;
        int h_2 = height / 2;
        canvas.drawLine(0.0f, 0.0f, width, 0.0f, paint);
        canvas.drawLine(width, 0.0f, width, height, paint);
        canvas.drawLine(width, height, 0.0f, height, paint);
        canvas.drawLine(0.0f, height, 0.0f, 0.0f, paint);

        canvas.drawLine(w_2, 0.0f, w_2, kk, paint);
        canvas.drawLine(w_2, height, w_2, height - kk, paint);
        canvas.drawLine(0.0f, h_2, kk, h_2, paint);
        canvas.drawLine(width, h_2, width - kk, h_2, paint);
    }
}
