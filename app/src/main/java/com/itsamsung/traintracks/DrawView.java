package com.itsamsung.traintracks;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DrawView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "DrawView";
    private DrawThread drawThread;
    private final Field field;

    public DrawView(Context context, Field field) {
        super(context);
        getHolder().addCallback(this);
        this.field = field;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getContext(), getHolder());
        drawThread.giveField(field);
        drawThread.giveContext(getContext());
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawThread.requestStop();
        boolean retry = true;
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                //
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        drawThread.setTowardPoint((int)event.getX(),(int)event.getY());

        return false;
    }
}