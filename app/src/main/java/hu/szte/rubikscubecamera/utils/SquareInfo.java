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
        lowerBounds = new Scalar[] {
                new Scalar(0, 0, 100),      // White
                new Scalar(0, 70, 60),   // Blue OK
                new Scalar(122, 40, 70),    // Red OK
                new Scalar(70, 30, 70),   // Yellow OK
                new Scalar(20, 40, 55),   // Green OK
                new Scalar(100, 100, 100)    // Orange OK
        };
        upperBounds = new Scalar[] {
                new Scalar(180, 70, 255),  // White
                new Scalar(19, 255, 255),  // Blue
                new Scalar(140, 255, 255),   // Red
                new Scalar(100, 255, 255),   // Yellow
                new Scalar(70, 255, 255),   // Green
                new Scalar(121, 255, 255)    // Orange
        };
    }
    /*
    fehér->kék
    */
    public static SquareInfo createSquareInfo() {
        return new SquareInfo();
    }
}
