package hu.szte.rubikscubecamera.utils;

import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

public class SquareInfo {
    public enum Color {
        WHITE,
        BLUE,
        RED,
        YELLOW,
        GREEN,
        ORANGE,
        EMPTY
    }

    public static final char[] SIDE_COLORS = new char[] {'U', 'R', 'F', 'D', 'L', 'B', 'E'};

    public List<Scalar> lowerBounds;
    public List<Scalar> upperBounds;

    private SquareInfo() {
        lowerBounds = new ArrayList<>();
        lowerBounds.add(new Scalar(25, 25, 25));
        lowerBounds.add(new Scalar(0, 100, 0));
        lowerBounds.add(new Scalar(0, 0, 50));
        lowerBounds.add(new Scalar(150, 50, 0));
        lowerBounds.add(new Scalar(150, 150, 0));
        lowerBounds.add(new Scalar(180, 180, 180));
        upperBounds = new ArrayList<>();
        upperBounds.add(new Scalar(80, 80, 255));
        upperBounds.add(new Scalar(50, 255, 50));
        upperBounds.add(new Scalar(80, 80, 200));
        upperBounds.add(new Scalar(255, 120, 60));
        upperBounds.add(new Scalar(255, 255, 80));
        upperBounds.add(new Scalar(255, 255, 255));
    }

    public static SquareInfo createSquareInfo() {
        return new SquareInfo();
    }

    public Scalar getColorLowerBound(Color color) {
        return lowerBounds.get(color.ordinal());
    }

    public Scalar getColorUpperBound(Color color) {
        return upperBounds.get(color.ordinal());
    }
}
