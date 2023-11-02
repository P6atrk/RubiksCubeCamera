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
        lowerBounds.add(new Scalar(180, 180, 180));   // White
        lowerBounds.add(new Scalar(0, 0, 50));       // Blue
        lowerBounds.add(new Scalar(0, 0, 150));      // Red
        lowerBounds.add(new Scalar(150, 150, 0));    // Yellow
        lowerBounds.add(new Scalar(0, 100, 0));      // Green
        lowerBounds.add(new Scalar(150, 50, 0));     // Orange

        upperBounds = new ArrayList<>();
        upperBounds.add(new Scalar(255, 255, 255));  // White
        upperBounds.add(new Scalar(80, 80, 200));    // Blue
        upperBounds.add(new Scalar(80, 80, 255));    // Red
        upperBounds.add(new Scalar(255, 255, 80));  // Yellow
        upperBounds.add(new Scalar(50, 255, 50));   // Green
        upperBounds.add(new Scalar(255, 120, 60));  // Orange
    }

    public static SquareInfo createSquareInfo() {
        return new SquareInfo();
    }
}
