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

    public Scalar[] lowerBounds;
    public Scalar[] upperBounds;

    private SquareInfo() {
        int colorLower = 100;
        lowerBounds = new Scalar[] {
                new Scalar(0, 0, 128),      // White
                new Scalar(0, colorLower, colorLower),   // Blue OK
                new Scalar(120, colorLower, colorLower),    // Red OK
                new Scalar(40, 40, colorLower),   // Yellow OK
                new Scalar(20, colorLower, colorLower),   // Green OK
                new Scalar(100, colorLower, colorLower)    // Orange OK
        };
        upperBounds = new Scalar[] {
                new Scalar(180, 90, 255),  // White
                new Scalar(20, 255, 255),  // Blue
                new Scalar(140, 255, 255),   // Red
                new Scalar(100, 255, 255),   // Yellow
                new Scalar(40, 255, 255),   // Green
                new Scalar(120, 255, 255)    // Orange
        };
    }

    public static SquareInfo createSquareInfo() {
        return new SquareInfo();
    }
}
