package com.itsamsung.traintracks;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MainActivity";
    private final int height = 15;
    private final int width = 15;
    private final Field field = new Field(height, width, 3, 3);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(new DrawView(this, field));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        for (int line = 0; line < field.getHeight(); line++) {
            for (int col = 0; col < field.getWidth(); col++) {
                outState.putInt(String.format("Field%s%s", col, line), field.getCell(col, line).toInt());
            }
        }
        for (int col = 0; col < field.getWidth(); col++) {
            outState.putInt(String.format("SumsX%s", col), field.getSumsX()[col]);
        }
        for (int line = 0; line < field.getHeight(); line++) {
            outState.putInt(String.format("SumsY%s", line), field.getSumsY()[line]);
        }
        Log.v(LOG_TAG, "SaveState=" + field.getState());
        outState.putInt("state", field.getState());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        for (int line = 0; line < field.getHeight(); line++) {
            for (int col = 0; col < field.getWidth(); col++) {
                int val = savedInstanceState.getInt(String.format("Field%s%s", col, line));
                CellType ct;
                if (val == 0) {
                    ct = CellType.EMPTY;
                } else {
                    ct = CellType.RAIL;
                }
                field.putCell(col, line, ct);
            }
        }
        for (int col = 0; col < field.getWidth(); col++) {
            field.putSumsX(col, savedInstanceState.getInt(String.format("SumsX%s", col)));
        }
        for (int line = 0; line < field.getHeight(); line++) {
            field.putSumsY(line, savedInstanceState.getInt(String.format("SumsY%s", line)));
        }
        field.setState(savedInstanceState.getInt("state"));
        Log.v(LOG_TAG, "state=" + field.getState());
    }
}