package com.itsamsung.traintracks;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Random;


public class Field {
    private static final String TAG = "Field";

    private final int height;
    private final int width;
    private final int villageA;
    private final int villageB;
    private CellType[][] field;
    private Integer[] sumsX;
    private Integer[] sumsY;
    private int state = 0;

    private Random rnd = new Random();

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void putSumsX(int index, int value) {
        sumsX[index] = value;
    }

    public void putSumsY(int index, int value) {
        sumsY[index] = value;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public int getVillageA() {
        return villageA;
    }

    public int getVillageB() {
        return villageB;
    }

    public Integer[] getSumsX() { return sumsX; }

    public Integer[] getSumsY() { return sumsY; }

    public Field(int height, int width, int villageA, int villageB) {
        this.height = height;
        this.width = width;
        this.villageA = villageA;
        this.villageB = villageB;
        field = new CellType[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = CellType.EMPTY;
            }
        }
        sumsX = new Integer[width];
        sumsY = new Integer[height];
        for (int col = 0; col < width; col++) {
            sumsX[col] = 0;
        }
        for (int line = 0; line < height; line++) {
            sumsY[line] = 0;
        }
    }

    private int getRailsCount() {
        int res = 0;
        for (int col = 0; col < height; col++) {
            for (int line = 0; line < width; line++) {
                if (getCell(line, col) == CellType.RAIL) {
                    res++;
                }
            }
        }

        return res;
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                field[x][y] = CellType.EMPTY;
            }
        }
    }

    public boolean check(){
        ArrayDeque<Pair<Integer, Integer>> flow = new ArrayDeque<Pair<Integer, Integer>>();
        ArrayList<Pair<Integer, Integer>> stepped = new ArrayList<Pair<Integer, Integer>>();
        flow.addLast(new Pair<>(0, villageA));

        boolean finished = false;
        while (true) {
            if (flow.size() == 0) break;
            Pair<Integer, Integer> current = flow.removeFirst();
            if (current.equals(new Pair<>(villageB, height - 1))) {
                finished = true;
            }
            if (getCell(current.first, current.second) == CellType.EMPTY) {
                continue;
            }
            if (stepped.contains(current)) {
                continue;
            }
            stepped.add(current);

            ArrayList<Pair<Integer, Integer>> var = new ArrayList<Pair<Integer, Integer>>();
            var.add(new Pair<Integer, Integer>(current.first - 1, current.second));
            var.add(new Pair<Integer, Integer>(current.first, current.second - 1));
            var.add(new Pair<Integer, Integer>(current.first + 1, current.second));
            var.add(new Pair<Integer, Integer>(current.first, current.second + 1));

            for (int i = 0; i < var.size(); i++) {
                if (inField(var.get(i)) && !stepped.contains(var.get(i))) {
                    flow.add(var.get(i));
                }
            }
        }
        if (finished) {
            return stepped.size() == getRailsCount() && checkSums();
        } else {
            return false;
        }
    }

    private boolean checkSums() {
        Log.v(TAG, "checkSums");
        boolean flagX = true;
        for (int col = 0; col < width; col++) {
            int res = 0;
            for (int line = 0; line < width; line++) {
                if (getCell(col, line).equals(CellType.RAIL)) {
                    res++;
                }
            }
            Log.v(TAG, "res=" + res);
            flagX = flagX && (res == sumsX[col]);
        }

        Log.v(TAG, "XY");
        boolean flagY = true;
        for (int line = 0; line < height; line++) {
            int res = 0;
            for (int col = 0; col < width; col++) {
                if (getCell(col, line).equals(CellType.RAIL)) {
                    res++;
                }
            }
            Log.v(TAG, "res=" + res);
            flagY = flagY && (res == sumsY[line]);
        }
        Log.v(TAG, "flagx" + flagX);
        Log.v(TAG, "flagy" + flagY);
        return flagX && flagY;
    }

    private boolean inField(Pair<Integer, Integer> cell) {
        return 0 <= cell.first && cell.first < width && 0 <= cell.second && cell.second < height;
    }

    public void putCell(Pair<Integer, Integer> cell, boolean isRail) {
        if (!excist(cell)) {
            throw new IndexOutOfBoundsException();
        }
        if (isRail) field[cell.first][cell.second] = CellType.RAIL;
        else field[cell.first][cell.second] = CellType.EMPTY;
    }

    private boolean excist(Pair<Integer, Integer> cell) {
        if ((0 <= cell.first && cell.first < width) && (0 <= cell.second && cell.second< height)) {
            return true;
        } else {
            Log.e(TAG, "PutCellError ---> cell=(" + cell.first + ", " + cell.second + ")");
            return false;
        }
    }

    public void putCell(int x, int y, CellType value) {
        field[x][y] = value;
    }


    public CellType getCell(int x, int y) {
        return field[x][y];
    }

    public void generatePath() {
        int maxChanges = 5;
        int minMaxChanges = 2;

        int tryChange = 0;
        int MAXTRYCHANGE = 10;
        while (true) {

            int restartCounts = 0;
            int MAXRESTARTCOUNTS = 100;
            while (true) {
                if (tryChange > MAXTRYCHANGE) {
                    tryChange = 0;
                    //Log.v(TAG, "Stage out");
                    if (maxChanges > minMaxChanges) {
                        maxChanges--;
                        //Log.v(TAG, "maxChanges=" + maxChanges);
                    }
                }
                ArrayList<Pair<Integer, Integer>> path = new ArrayList<Pair<Integer, Integer>>();
                ArrayList<Integer[]> lines = new ArrayList<Integer[]>();
                for (int i = 0; i < villageB; i++) {
                    path.add(new Pair<>(i, villageA));
                }
                for (int i = villageA; i < height; i++) {
                    path.add(new Pair<>(villageB, i));
                }
                lines.add(new Integer[]{0, villageA, villageB, villageA});
                lines.add(new Integer[]{villageB, villageA, villageB, height - 1});

                int sx = -1, sy = -1, fx = -1, fy = -1, maxLength = -1, curLength = -1, shiftStart = -1, neg_depth = -1, pos_depth = -1, dep = -1;

                int change = 0;
                while (change < maxChanges) {
                    //Log.v(TAG, "change=" + change);
                    int index = rnd.nextInt(lines.size());
                    Integer[] line = lines.get(index);
                    sx = line[0];
                    sy = line[1];
                    fx = line[2];
                    fy = line[3];

                    // reverse line
                    if ((sx == fx && sy > fy) || (sx > fx && sy == fy)) {
                        int temp = sx;
                        sx = fx;
                        fx = temp;

                        temp = sy;
                        sy = fy;
                        fy = temp;
                        //Log.v(TAG, "---------------swapped");
                    }


                    //Log.v(TAG, "sx=" + sx);
                    //Log.v(TAG, "sy=" + sy);
                    //Log.v(TAG, "fx=" + fx);
                    //Log.v(TAG, "fy=" + fy);

                    if (sx == fx) {
                        // Ver
                        //Log.v(TAG, "Vert");

                        if (Math.abs(fy - sy) < 5) {
                            restartCounts++;
                            if (restartCounts > MAXRESTARTCOUNTS) {
                                break;
                            }
                            continue;
                        }
                        // maxlength

                        maxLength = Math.abs(sy - fy + 1) - 2;
                        curLength = (int) (rnd.nextDouble() * (maxLength - 3) + 3);
                        //Log.v(TAG, "maxLength=" + maxLength);
                        //Log.v(TAG, "curLength=" + curLength);

                        // shiftposing

                        shiftStart = sy + 1 + (int) (rnd.nextDouble() * (maxLength - curLength + 1));
                        //Log.v(TAG, "shiftStart=" + shiftStart);

                        // maximin depth

                        // ===> negative check
                        int cur = sx;
                        while (true) {
                            if (cur - 1 < 0) {
                                break;
                            }
                            if (path.contains(new Pair<>(cur - 1, shiftStart))) {
                                break;
                            }
                            boolean isNotInPath = true;
                            for (int i = 1; i <= curLength; i++) {
                                if (path.contains(new Pair<>(cur - 1, shiftStart + i))) {
                                    isNotInPath = false;
                                    break;
                                }
                            }
                            if (!isNotInPath) {
                                break;
                            }
                            cur--;
                        }
                        neg_depth = cur;
                        //Log.v(TAG, "neg_depth=" + neg_depth);
                        // ===> positive check
                        cur = sx;
                        while (true) {
                            if (cur + 1 > width) {
                                break;
                            }
                            if (path.contains(new Pair<>(cur + 1, shiftStart))) {
                                break;
                            }
                            boolean isNotInPath = true;
                            for (int i = 1; i <= maxLength; i++) {
                                if (path.contains(new Pair<>(cur + 1, shiftStart + i))) {
                                    isNotInPath = false;
                                    break;
                                }
                            }
                            if (!isNotInPath) {
                                break;
                            }
                            cur++;
                        }
                        pos_depth = cur;
                        //Log.v(TAG, "pos_depth=" + pos_depth);

                        if (pos_depth == sx && neg_depth == sx) {
                            restartCounts++;
                            if (restartCounts > MAXRESTARTCOUNTS) {
                                break;
                            }
                            continue;
                        }

                        int choice = (int) (rnd.nextDouble() * 2);
                        if (choice == 0 || neg_depth == sx) {
                            // posdepth
                            //Log.v(TAG, "posdepth");
                            if (pos_depth > sx + 2) {
                                dep = (int) (rnd.nextDouble() * ((pos_depth - sx) - 2) + 2);
                                //Log.v(TAG, "dep=" + dep);

                                // clean line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    for (int pathIndex = 0; pathIndex < path.size(); pathIndex++) {
                                        if (path.get(pathIndex).first == sx && path.get(pathIndex).second == cell) {
                                            path.remove(pathIndex);
                                        }
                                    }
                                }

                                // add new paralell line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    path.add(new Pair<>(sx + dep, cell));
                                }

                                // add path shifts
                                for (int pathShift = sx; pathShift < sx + dep + 1; pathShift++) {
                                    path.add(new Pair<>(pathShift, shiftStart));
                                }
                                for (int pathShift = sx; pathShift < sx + dep + 1; pathShift++) {
                                    path.add(new Pair<>(pathShift, shiftStart + curLength));
                                }

                                // LINES

                                // remove old line
                                Integer[] rl = new Integer[]{sx, sy, fx, fy};
                                int el = 0;
                                int s = lines.size();
                                while (el < s) {
                                    if (arEquals(lines.get(el), rl)) {
                                        //Log.v(TAG, "Founded!!!");
                                        lines.remove(el);
                                        s--;
                                    }
                                    el++;
                                }

                                // add new lines
                                int px1 = sx;
                                int px4 = sx;
                                int px2 = sx + dep;
                                int px3 = sx + dep;

                                int py1 = shiftStart;
                                int py2 = shiftStart;
                                int py3 = shiftStart + curLength;
                                int py4 = shiftStart + curLength;

                                lines.add(new Integer[]{sx, sy, px1, py1});
                                lines.add(new Integer[]{px1, py1, px2, py2});
                                lines.add(new Integer[]{px2, py2, px3, py3});
                                lines.add(new Integer[]{px4, py4, px3, py3});
                                lines.add(new Integer[]{px4, py4, fx, fy});
                            } else {
                                restartCounts++;
                                if (restartCounts > MAXRESTARTCOUNTS) {
                                    break;
                                }
                                continue;
                            }
                        } else { // choice == 1 || pos_depth == 0
                            // negdepth
                            //Log.v(TAG, "negdepth");
                            if (neg_depth < sx + 2) {
                                dep = (int) (rnd.nextDouble() * ((sx - neg_depth) - 2) + 2);
                                //Log.v(TAG, "dep=" + dep);

                                // clean line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    for (int pathIndex = 0; pathIndex < path.size(); pathIndex++) {
                                        if (path.get(pathIndex).first == sx && path.get(pathIndex).second == cell) {
                                            path.remove(pathIndex);
                                        }
                                    }
                                }

                                // add new paralell line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    path.add(new Pair<>(sx - dep, cell));
                                }

                                // add path shifts
                                for (int pathShift = sx - dep; pathShift < sx; pathShift++) {
                                    path.add(new Pair<>(pathShift, shiftStart));
                                }
                                for (int pathShift = sx - dep; pathShift < sx; pathShift++) {
                                    path.add(new Pair<>(pathShift, shiftStart + curLength));
                                }

                                // LINES

                                // remove old line
                                Integer[] rl = new Integer[]{sx, sy, fx, fy};
                                int el = 0;
                                int s = lines.size();
                                while (el < s) {
                                    if (arEquals(lines.get(el), rl)) {
                                        //Log.v(TAG, "Founded!!!");
                                        lines.remove(el);
                                        s--;
                                    }
                                    el++;
                                }
                                // add new lines
                                int px1 = sx;
                                int px4 = sx;
                                int px2 = sx - dep;
                                int px3 = sx - dep;

                                int py1 = shiftStart;
                                int py2 = shiftStart;
                                int py3 = shiftStart + curLength;
                                int py4 = shiftStart + curLength;

                                lines.add(new Integer[]{sx, sy, px1, py1});
                                lines.add(new Integer[]{px1, py1, px2, py2});
                                lines.add(new Integer[]{px2, py2, px3, py3});
                                lines.add(new Integer[]{px4, py4, px3, py3});
                                lines.add(new Integer[]{px4, py4, fx, fy});
                            } else {
                                restartCounts++;
                                if (restartCounts > MAXRESTARTCOUNTS) {
                                    break;
                                }
                                continue;
                            }
                        }
                    } else {
                        // Hor
                        //Log.v(TAG, "Horizont");
                        if (Math.abs(fx - sx) < 5) {
                            restartCounts++;
                            if (restartCounts > MAXRESTARTCOUNTS) {
                                break;
                            }
                            continue;
                        }
                        // maxlength

                        maxLength = Math.abs(sx - fx + 1) - 2;
                        curLength = (int) (rnd.nextDouble() * (maxLength - 3) + 3);
                        //Log.v(TAG, "maxLength=" + maxLength);
                        //Log.v(TAG, "curLength=" + curLength);

                        // shiftposing

                        shiftStart = sx + 1 + (int) (rnd.nextDouble() * (maxLength - curLength + 1));
                        //Log.v(TAG, "shiftStart=" + shiftStart);

                        // maximin depth

                        // ===> negative check
                        int cur = sy;
                        while (true) {
                            if (cur - 1 < 0) {
                                break;
                            }
                            if (path.contains(new Pair<>(shiftStart, cur - 1))) {
                                break;
                            }
                            boolean isNotInPath = true;
                            for (int i = 1; i <= curLength; i++) {
                                if (path.contains(new Pair<>(shiftStart + i, cur - 1))) {
                                    isNotInPath = false;
                                    break;
                                }
                            }
                            if (!isNotInPath) {
                                break;
                            }
                            cur--;
                        }
                        neg_depth = cur;
                        //Log.v(TAG, "neg_depth=" + neg_depth);
                        // ===> positive check
                        cur = sy;
                        while (true) {
                            if (cur + 1 > height) {
                                break;
                            }
                            if (path.contains(new Pair<>(shiftStart, cur + 1))) {
                                break;
                            }
                            boolean isNotInPath = true;
                            for (int i = 1; i <= maxLength; i++) {
                                if (path.contains(new Pair<>(shiftStart + i, cur + 1))) {
                                    isNotInPath = false;
                                    break;
                                }
                            }
                            if (!isNotInPath) {
                                break;
                            }
                            cur++;
                        }
                        pos_depth = cur;
                        //Log.v(TAG, "pos_depth=" + pos_depth);

                        if (pos_depth == sy && neg_depth == sy) {
                            restartCounts++;
                            if (restartCounts > MAXRESTARTCOUNTS) {
                                break;
                            }
                            continue;
                        }

                        int choice = (int) (rnd.nextDouble() * 2);
                        if (choice == 0 || neg_depth == sy) {
                            // posdepth
                            //Log.v(TAG, "posdepth");
                            if (pos_depth > sy + 2) {
                                dep = (int) (rnd.nextDouble() * ((pos_depth - sy) - 2) + 2);
                                //Log.v(TAG, "dep=" + dep);

                                // clean line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    for (int pathIndex = 0; pathIndex < path.size(); pathIndex++) {
                                        if (path.get(pathIndex).first == cell && path.get(pathIndex).second == sy) {
                                            path.remove(pathIndex);
                                        }
                                    }
                                }

                                // add new paralell line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    path.add(new Pair<>(cell, sy + dep));
                                }

                                // add path shifts
                                for (int pathShift = sy; pathShift < sy + dep + 1; pathShift++) {
                                    path.add(new Pair<>(shiftStart, pathShift));
                                }
                                for (int pathShift = sy; pathShift < sy + dep + 1; pathShift++) {
                                    path.add(new Pair<>(shiftStart + curLength, pathShift));
                                }

                                // LINES

                                // remove old line
                                Integer[] rl = new Integer[]{sx, sy, fx, fy};
                                int el = 0;
                                int s = lines.size();
                                while (el < s) {
                                    if (arEquals(lines.get(el), rl)) {
                                        //Log.v(TAG, "Founded!!!");
                                        lines.remove(el);
                                        s--;
                                    }
                                    el++;
                                }

                                // add new lines
                                int py1 = sy;
                                int py4 = sy;
                                int py2 = sy + dep;
                                int py3 = sy + dep;

                                int px1 = shiftStart;
                                int px2 = shiftStart;
                                int px3 = shiftStart + curLength;
                                int px4 = shiftStart + curLength;

                                lines.add(new Integer[]{sx, sy, px1, py1});
                                lines.add(new Integer[]{px1, py1, px2, py2});
                                lines.add(new Integer[]{px2, py2, px3, py3});
                                lines.add(new Integer[]{px4, py4, px3, py3});
                                lines.add(new Integer[]{px4, py4, fx, fy});
                            } else {
                                restartCounts++;
                                if (restartCounts > MAXRESTARTCOUNTS) {
                                    break;
                                }
                                continue;
                            }
                        } else { // choice == 1 || pos_depth == 0
                            // negdepth
                            //Log.v(TAG, "negdepth");
                            if (neg_depth < sy + 2) {
                                dep = (int) (rnd.nextDouble() * ((sy - neg_depth) - 2) + 2);
                                //Log.v(TAG, "dep=" + dep);

                                // clean line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    for (int pathIndex = 0; pathIndex < path.size(); pathIndex++) {
                                        if (path.get(pathIndex).first == cell && path.get(pathIndex).second == sy) {
                                            path.remove(pathIndex);
                                        }
                                    }
                                }

                                // add new paralell line
                                for (int cell = shiftStart + 1; cell < shiftStart + curLength; cell++) {
                                    path.add(new Pair<>(cell, sy - dep));
                                }

                                // add path shifts
                                for (int pathShift = sy - dep; pathShift < sy; pathShift++) {
                                    path.add(new Pair<>(shiftStart, pathShift));
                                }
                                for (int pathShift = sy - dep; pathShift < sy; pathShift++) {
                                    path.add(new Pair<>(shiftStart + curLength, pathShift));
                                }
                                // LINES

                                // remove old line
                                Integer[] rl = new Integer[]{sx, sy, fx, fy};
                                int el = 0;
                                int s = lines.size();
                                while (el < s) {
                                    if (arEquals(lines.get(el), rl)) {
                                        //Log.v(TAG, "Founded!!!");
                                        lines.remove(el);
                                        s--;
                                    }
                                    el++;
                                }

                                // add new lines
                                int py1 = sy;
                                int py4 = sy;
                                int py2 = sy - dep;
                                int py3 = sy - dep;

                                int px1 = shiftStart;
                                int px2 = shiftStart;
                                int px3 = shiftStart + curLength;
                                int px4 = shiftStart + curLength;

                                lines.add(new Integer[]{sx, sy, px1, py1});
                                lines.add(new Integer[]{px1, py1, px2, py2});
                                lines.add(new Integer[]{px2, py2, px3, py3});
                                lines.add(new Integer[]{px4, py4, px3, py3});
                                lines.add(new Integer[]{px4, py4, fx, fy});
                            } else {
                                restartCounts++;
                                if (restartCounts > MAXRESTARTCOUNTS) {
                                    break;
                                }
                                continue;
                            }
                        }
                    }
                    change++;
                    restartCounts = 0;
                }

//                Log.i(TAG, "----------------pathCells=" + path.size());
//                Log.i(TAG, "----------------path%=" + (path.size() * 100 / (width * height)));

                if (path.size() * 100 / (width * height) < 33) {
                    restartCounts++;
                    continue;
                }

                if (restartCounts > MAXRESTARTCOUNTS) {
                    restartCounts = 0;
                    tryChange++;
                    //Log.v(TAG, "tryChange=" + tryChange);
                    continue;
                }
                fillByPath(path);
                return;
            }
        }
    }

    private boolean arEquals(Integer[] a1, Integer[] a2) {
        if (a1.length != a2.length) return false;
        boolean eq = true;
        for (int i = 0; i < a1.length; i++) {
            eq = eq && (Objects.equals(a1[i], a2[i]));
        }
        return eq;
    }

    private void fillByPath(ArrayList<Pair<Integer, Integer>> path) {
        for (Pair<Integer, Integer> cell :
                path) {
            putCell(cell, true);
        }
        updateSums();
    }

    public void updateSums() {
        for (int col = 0; col < width; col++) {
            int summa = 0;
            for (int line = 0; line < height; line++) {
                if (getCell(col, line) == CellType.RAIL) {
                    summa++;
                }
            }
            sumsX[col] = summa;
        }
        for (int line = 0; line < height; line++) {
            int summa = 0;
            for (int col = 0; col < width; col++) {
                if (getCell(col, line) == CellType.RAIL) {
                    summa++;
                }
            }
            sumsY[line] = summa;
        }
    }
}
