package hu.szte.rubikscubecamera.utils;

import static org.opencv.imgproc.Imgproc.line;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class CubeLineDrawer {
    private static final double MD_LENGTH_FRACTION = 3.0;
    private static final double F1 = 1.0 / 3.0, F2 = 2.0 / 3.0;

    private static final Paint paint = new Paint();

    private static Scalar color;
    private static int thickness = 40;

    private static Point M, D , RU, LU, U, LD, RD;

    /**
     * Draws the outer lines of the cube. Works with Canvas.
     * @param canvas canvas to draw on.
     */
    public static void drawOuterLines(Canvas canvas) {
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStrokeWidth(4);

        calculateLines(canvas.getWidth(), canvas.getHeight());

        drawLine(canvas, LU, U);
        drawLine(canvas, LU, LD);
        drawLine(canvas, RU, U);
        drawLine(canvas, RU, RD);
        drawLine(canvas, D, LD);
        drawLine(canvas, D, RD);
    }

    /**
     * Draws the outer lines of the cube. Works with Mat.
     * @param mat Mat to draw on.
     */
    public static void drawOuterLines(Mat mat, Scalar color, int thickness) {
        CubeLineDrawer.color = color;
        CubeLineDrawer.thickness = thickness;

        calculateLines(mat.cols(), mat.rows());

        drawLine(mat, M, LU);
        drawLine(mat, M, RU);
        drawLine(mat, M, D);
        drawLine(mat, LU, U);
        drawLine(mat, LU, LD);
        drawLine(mat, RU, U);
        drawLine(mat, RU, RD);
        drawLine(mat, D, LD);
        drawLine(mat, D, RD);
    }

    /**
     * Draws the inner lines of the cube. Works with Canvas.
     * @param canvas Canvas to draw on.
     */
    public static void drawInnerLines(Canvas canvas) {
        paint.setColor(Color.rgb(255, 255, 255));
        paint.setStrokeWidth(4);

        calculateLines(canvas.getWidth(), canvas.getHeight());

        drawLine(canvas, M, LU);
        drawLine(canvas, M, RU);
        drawLine(canvas, M, D);
        draw2ParallelLines(canvas, LU, LD, M, D);
        draw2ParallelLines(canvas, LU, M, LD, D);
        draw2ParallelLines(canvas, M, D, RU, RD);
        draw2ParallelLines(canvas, M, RU, D, RD);
        draw2ParallelLines(canvas, LU, U, M, RU);
        draw2ParallelLines(canvas, LU, M, U, RU);
    }

    /**
     * Draws the inner lines of the cube. Works with Mat.
     * @param mat Mat to draw on.
     */
    public static void drawInnerLines(Mat mat, Scalar color, int thickness) {
        CubeLineDrawer.color = color;
        CubeLineDrawer.thickness = thickness;

        calculateLines(mat.cols(), mat.rows());

        drawLine(mat, M, LU);
        drawLine(mat, M, RU);
        drawLine(mat, M, D);
        draw2ParallelLines(mat, LU, LD, M, D);
        draw2ParallelLines(mat, LU, M, LD, D);
        draw2ParallelLines(mat, M, D, RU, RD);
        draw2ParallelLines(mat, M, RU, D, RD);
        draw2ParallelLines(mat, LU, U, M, RU);
        draw2ParallelLines(mat, LU, M, U, RU);
    }


    // TODO: Kommentelni es egyszeruseteni
    /**
     *
     * @param canvas
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     */
    private static void draw2ParallelLines(Canvas canvas, Point p1, Point p2, Point p3, Point p4) {
        drawLine(canvas, f(p1, p2, F1), f(p3, p4, F1));
        drawLine(canvas, f(p1, p2, F2), f(p3, p4, F2));
    }

    // TODO: Kommentelni es egyszeruseteni
    /**
     *
     * @param mat
     * @param p1
     * @param p2
     * @param p3
     * @param p4
     */
    private static void draw2ParallelLines(Mat mat, Point p1, Point p2, Point p3, Point p4) {
        drawLine(mat, f(p1, p2, F1), f(p3, p4, F1));
        drawLine(mat, f(p1, p2, F2), f(p3, p4, F2));
    }

    /**
     * Draws a single line on the Canvas.
     * @param canvas Canvas to draw on.
     * @param p1 Point1, Starting point of the line.
     * @param p2 Point2, Ending point of the line.
     */
    private static void drawLine(Canvas canvas, Point p1, Point p2) {
        canvas.drawLine((float) p1.x, (float) p1.y, (float) p2.x, (float) p2.y, paint);
    }

    /**
     * Draws a single line on the Mat.
     * @param mat Mat to draw on.
     * @param p1 Point1, Starting point of the line.
     * @param p2 Point2, Ending point of the line.
     */
    private static void drawLine(Mat mat, Point p1, Point p2) {
        line(mat, p1, p2, color, thickness);
    }

    /**
     * Calculates where the lines should be drawn. Uses the width and the height
     * of the Mat/Canvas to calculate all the line ends.
     *
     * @param w Width of the Mat/Canvas.
     * @param h Height of the Mat/Canvas.
     */
    private static void calculateLines(double w, double h) {
        double half = 1.0 / 2.0;

        M = new Point(w / 2, h / 2);
        D = new Point(M.x, M.y + w / MD_LENGTH_FRACTION);
        RU = rotatePointAroundPoint(M, D, -120);
        LU = rotatePointAroundPoint(M, D, 120);

        Point OU = reflectPointAboutLine(M, LU, RU);
        Point OLD = reflectPointAboutLine(M, LU, D);
        Point ORD = reflectPointAboutLine(M, RU, D);

        U = f(OU, f(M, OU, half), half);
        LD = f(OLD, f(M, OLD, half), half);
        RD = f(ORD, f(M, ORD, half), half);
    }

    /**
     * Gets a point between two points (x1, y1) (x2, y2).
     * The fraction decides where on the line the point is gonna be.
     * The fraction is calculated from the first point (x1, y1),
     * so If the fraction is 1/3 than the point will be closer to (x1, y1).
     *
     * @param point1 OpenCv Point point1
     * @param point2 OpenCv Point point2
     * @param f  fraction
     * @return an OpenCv Point
     */
    private static Point f(Point point1, Point point2, double f) {
        return new Point(point1.x + f * (point2.x - point1.x), point1.y + f * (point2.y - point1.y));
    }

    // TODO: Kommentelni es egyszeruseteni
    /**
     *
     * @param pivot
     * @param point
     * @param angleDegrees
     * @return
     */
    private static Point rotatePointAroundPoint(Point pivot, Point point, double angleDegrees) {
        // Convert the angle from degrees to radians
        double angleRadians = angleDegrees * (Math.PI / 180);
        double cosTheta = Math.cos(angleRadians);
        double sinTheta = Math.sin(angleRadians);

        return new Point(
                (int) (cosTheta * (point.x - pivot.x) - sinTheta * (point.y - pivot.y) + pivot.x),
                (int) (sinTheta * (point.x - pivot.x) + cosTheta * (point.y - pivot.y) + pivot.y));
    }

    // TODO: Kommentelni es egyszerusiteni
    /**
     *
     * @param point
     * @param linePoint1
     * @param linePoint2
     * @return
     */
    public static Point reflectPointAboutLine(Point point, Point linePoint1, Point linePoint2) {
        double dx = linePoint2.x - linePoint1.x;
        double dy = linePoint2.y - linePoint1.y;
        double a = (dx * dx - dy * dy) / (dx * dx + dy * dy);
        double b = 2 * dx * dy / (dx * dx + dy * dy);
        double x = Math.round(a * (point.x - linePoint1.x) + b * (point.y - linePoint1.y) + linePoint1.x);
        double y = Math.round(b * (point.x - linePoint1.x) - a * (point.y - linePoint1.y) + linePoint1.y);
        return new Point(x, y);
    }
}
