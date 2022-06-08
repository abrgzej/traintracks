package com.itsamsung.traintracks;

import static java.lang.System.nanoTime;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.widget.Toast;

import java.util.Random;

public class DrawThread extends Thread {
    private static final String TAG = "DrawThread";
    private final Bitmap villageBitmap;
    private final Bitmap railsBitmap;
    private final SurfaceHolder surfaceHolder;

    private final Paint netPaint = new Paint();
    private final Paint bgPaint = new Paint();
    private final Paint digitPaint = new Paint();
    private final Paint TextPaint = new Paint();
    private volatile boolean running = true;
    private Field field;
    private int canvasWidth, canvasHeight;
    private Context context;
    private long clickTime = 0L;

    {
        bgPaint.setColor(Color.WHITE);
        bgPaint.setStyle(Paint.Style.FILL);
        netPaint.setColor(Color.BLACK);
        netPaint.setStyle(Paint.Style.STROKE);
        digitPaint.setColor(Color.BLACK);
        digitPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        TextPaint.setColor(Color.BLACK);
        TextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        TextPaint.setTextSize(20);
        TextPaint.setAntiAlias(true);
    }

    private static void setTextSizeForWidth(Paint paint, float desiredWidth, String text) {
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float desiredTextSize = testTextSize * desiredWidth / bounds.width();
        paint.setTextSize(desiredTextSize);
    }

    private static void setTextSize(Paint paint, float desiredSize, String text) {
        final float testTextSize = 48f;
        paint.setTextSize(testTextSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        float desiredTextSize = Math.min(testTextSize * desiredSize / bounds.width(), testTextSize * desiredSize / bounds.height());
        paint.setTextSize(desiredTextSize);
    }

    public void giveField(Field otherField) {
        field = otherField;
    }

    public void giveContext(Context otherContext) {
        context = otherContext;
    }

    public DrawThread(Context context, SurfaceHolder surfaceHolder) {
        villageBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.train_tracks_village);
        railsBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.train_tracks_rails);
        this.surfaceHolder = surfaceHolder;
    }

    public void setTowardPoint(int x, int y) {
        int dw = canvasWidth;
        int dh = canvasHeight;
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        if (fieldSizeX == fieldSizeY) {

            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            int sd = fieldSizeX - fieldSizeY;
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }

        Log.v(TAG, "state=" + field.getState());

        if (field.getState() == 0) {
            field.generatePath();
            field.updateSums();
            field.clear();
            field.setState(1);
        } else if (field.getState() == 1) {
            if (sx <= x && x < sx + fieldSizeX && sy <= y && y < sy + fieldSizeY) {
                x = x - sx;
                y = y - sy;
                int col = x / cellSize;
                int line = y / cellSize;
                if (field.getCell(col, line).equals(CellType.EMPTY)) {
                    field.putCell(col, line, CellType.RAIL);
                } else {
                    field.putCell(col, line, CellType.EMPTY);
                }
            } else {
                long clickNow = nanoTime();
                long clickDur = 500000000; // 1/2 second
                if (clickNow - clickTime < clickDur) {
                    field.clear();
                    field.updateSums();
                    field.setState(0);
                }
                clickTime = clickNow;
            }
            if (field.check()) {
                field.setState(2);
            }
        } else {
            field.setState(0);
            field.clear();
            field.updateSums();
        }
    }

    public void requestStop() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                try {
                    canvasWidth = canvas.getWidth();
                    canvasHeight = canvas.getHeight();
                    canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), bgPaint);

                    if (field.getState() == 2) {
                        drawCong(canvas);
                    } else {
                        drawNet(canvas);
                        drawVillage(canvas);
                        drawField(canvas);
                        drawDigits(canvas);
                    }

                } finally {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    private void drawCong(Canvas canvas) {
        int dw = canvas.getWidth();
        int dh = canvas.getHeight();
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        if (fieldSizeX == fieldSizeY) {

            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            int sd = fieldSizeX - fieldSizeY;
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }

        int textBox = (int) (Math.min(dw, dh) / 3);

        String congText = "You win!";
        setTextSize(TextPaint, textBox, congText);
        canvas.drawText(congText, (int) ((dw - textBox) / 2), (int) ((dh - textBox) / 2), TextPaint);

    }

    private void drawDigits(Canvas canvas) {
        int dw = canvas.getWidth();
        int dh = canvas.getHeight();
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        int textBox = cellSize * 7 / 10;
        if (fieldSizeX == fieldSizeY) {

            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            int sd = fieldSizeX - fieldSizeY;
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }for (int col = 0; col < field.getWidth(); col++) {
            Integer value = field.getSumsX()[col];
            setTextSize(digitPaint, textBox, value.toString());
            canvas.drawText(value.toString(), sx + col * cellSize + (int) ((cellSize - textBox) / 2), sy - 5, digitPaint);
        }

        for (int line = 0; line < field.getHeight(); line++) {
            Integer value = field.getSumsY()[line];
            setTextSize(digitPaint, textBox, value.toString());
            canvas.drawText(value.toString(), sx + fieldSizeX + 5, sy + cellSize * (line + 1) - (int) ((cellSize - textBox) / 2), digitPaint);
        }
    }

    private void drawField(Canvas canvas) {
        int dw = canvas.getWidth();
        int dh = canvas.getHeight();
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        if (fieldSizeX == fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }
        for (int line = 0; line < field.getHeight(); line++) {
            for (int col = 0; col < field.getWidth(); col++) {
                if (field.getCell(col, line).equals(CellType.RAIL)) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(railsBitmap, cellSize, cellSize, true),
                            sx + cellSize * col, sy + cellSize * line, bgPaint);
                }
            }
        }
    }

    private void drawVillage(Canvas canvas) {
        int dw = canvas.getWidth();
        int dh = canvas.getHeight();
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        if (fieldSizeX == fieldSizeY) {

            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            int sd = fieldSizeX - fieldSizeY;
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }
        canvas.drawBitmap(Bitmap.createScaledBitmap(villageBitmap, cellSize, cellSize, true),
                sx - cellSize - 1, sy + cellSize * field.getVillageA(), bgPaint);
        canvas.drawBitmap(Bitmap.createScaledBitmap(villageBitmap, cellSize, cellSize, true),
                sx + cellSize * field.getVillageB(), sy + fieldSizeY + 1, bgPaint);

    }

    private void drawNet(Canvas canvas) {
        int dw = canvas.getWidth();
        int dh = canvas.getHeight();
        int shift = Math.min(dw, dh) * 10 / 100;

        int cellSize = Math.min((dw - 2 * shift) / field.getWidth(), (dh - 2 * shift) / field.getHeight());

        int fieldSizeX = cellSize * field.getWidth();
        int fieldSizeY = cellSize * field.getHeight();

        int sx, sy;
        if (fieldSizeX == fieldSizeY) {

            sx = (dw - fieldSizeX) / 2;
            sy = (dh - fieldSizeY) / 2;
        } else if (fieldSizeX < fieldSizeY) {
            sx = (dw - fieldSizeX) / 2;
            sy = shift;
        } else {
            int sd = fieldSizeX - fieldSizeY;
            sx = shift;
            sy = (dh - fieldSizeY) / 2;
        }

        for (int line = 0; line <= field.getHeight(); line++) {
            canvas.drawLine(sx, sy + line * cellSize, sx + fieldSizeX, sy + line * cellSize, netPaint);
        }
        for (int col = 0; col <= field.getWidth(); col++) {
            canvas.drawLine(sx + col * cellSize, sy, sx + col * cellSize, sy + fieldSizeY, netPaint);
        }
    }
}
