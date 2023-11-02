package hu.szte.rubikscubecamera.utils;

import static org.opencv.imgproc.Imgproc.line;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class CubeLineDrawer {
    private static double f1, f2, wM, wL, wR, wDL, wDR, hU, hD, hMD, hMU, hC;

    /**
     * Draws the inner and outer lines of the Rubik's cube on the Mat.
     * This is used for Image decoding. Draws white outer and inner lines on the mat.
     * @param mat Draws on this Mat.
     */
    public static void drawCubeLines(Mat mat) {
        Scalar color = new Scalar(255, 255, 255);
        int thickness = 4;
        calculateLines(mat.cols(), mat.rows());
        // Outer lines
        line(mat, new Point(wL, hMU), new Point(wM, hU), color, thickness);
        line(mat, new Point(wM, hU), new Point(wR, hMU), color, thickness);
        line(mat, new Point(wR, hMU), new Point(wDR, hMD), color, thickness);
        line(mat, new Point(wDR, hMD), new Point(wM, hD), color, thickness);
        line(mat, new Point(wM, hD), new Point(wDL, hMD), color, thickness);
        line(mat, new Point(wDL, hMD), new Point(wL, hMU), color, thickness);
        // Inner lines
        // L
        line(mat, new Point(wL, hMU), new Point(wM, hC), color, thickness);
        // R
        line(mat, new Point(wR, hMU), new Point(wM, hC), color, thickness);
        // D
        line(mat, new Point(wM, hD), new Point(wM, hC), color, thickness);
        // R Horizontal
        line(mat, p(wR, wDR, hMU, hMD, f1), p(wM, wM, hC, hD, f1), color, thickness);
        line(mat, p(wR, wDR, hMU, hMD, f2), p(wM, wM, hC, hD, f2), color, thickness);
        // L Horizontal
        line(mat, p(wL, wDL, hMU, hMD, f1), p(wM, wM, hC, hD, f1), color, thickness);
        line(mat, p(wL, wDL, hMU, hMD, f2), p(wM, wM, hC, hD, f2), color, thickness);
        // L Vertical
        line(mat, p(wL, wM, hMU, hC, f1), p(wDL, wM, hMD, hD, f1), color, thickness);
        line(mat, p(wL, wM, hMU, hC, f2), p(wDL, wM, hMD, hD, f2), color, thickness);
        // R Vertical
        line(mat, p(wR, wM, hMU, hC, f1), p(wDR, wM, hMD, hD, f1), color, thickness);
        line(mat, p(wR, wM, hMU, hC, f2), p(wDR, wM, hMD, hD, f2), color, thickness);
        // U Horizontal
        line(mat, p(wM, wL, hU, hMU, f1), p(wM, wR, hC, hMU, f2), color, thickness);
        line(mat, p(wM, wL, hU, hMU, f2), p(wM, wR, hC, hMU, f1), color, thickness);
        // U Vertical
        line(mat, p(wM, wR, hU, hMU, f1), p(wM, wL, hC, hMU, f2), color, thickness);
        line(mat, p(wM, wR, hU, hMU, f2), p(wM, wL, hC, hMU, f1), color, thickness);
    }

    /**
     * Draws the Rubik's cube's outer lines on the canvas.
     * This function only draws in CaptureFragment and on the PreviewView.
     * @param canvas Draws lines on this canvas.
     */
    public static void drawCubeLines(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 0, 0));
        paint.setStrokeWidth(2);
        calculateLines(canvas.getWidth(), canvas.getHeight());
        // Outer lines
        canvas.drawLine((float) wL, (float) hMU, (float) wM, (float) hU, paint);
        canvas.drawLine((float) wM, (float) hU, (float) wR, (float) hMU, paint);
        canvas.drawLine((float) wR, (float) hMU, (float) wDR, (float) hMD, paint);
        canvas.drawLine((float) wDR, (float) hMD, (float) wM, (float) hD, paint);
        canvas.drawLine((float) wM, (float) hD, (float) wDL, (float) hMD, paint);
        canvas.drawLine((float) wDL, (float) hMD, (float) wL, (float) hMU, paint);
    }

    /**
     * Calculates where the lines should be drawn. Uses the width and the height
     * of the Mat/Canvas to calculate all the line ends.
     * @param w Width of the Mat/Canvas.
     * @param h Height of the Mat/Canvas.
     */
    private static void calculateLines(double w, double h) {
        // w=width, h=height, L=left, R=right, M=middle, U=up, D=down, C=center
        double c = 5.0 / 7 * w / 2;
        double d = c * 0.2;
        f1 = 1 / 3.0;
        f2 = 2 / 3.0;

        wM = w / 2;
        wL = wM - c;
        wR = wM + c;
        wDL = wM - c + d;
        wDR = wM + c - d;

        double hM = h / 2.0;
        hU = hM - c;
        hD = hM + c;
        hMD = hD + (hM - hD) / 2;
        hMU = hU - (hU - hM) / 2.3f;

        hC = 2 * hMU - hU;
    }

    /**
     * Gets a point between two points (w1, h1) (w2, h2).
     * The fraction decides where on the line the point is gonna be.
     * The fraction is calculated from the first point (w1, h1),
     * so If the fraction is 1/3 than the point will be closer to (w1, h1).
     *
     * @param w1 point 1's horizontal position
     * @param w2 point 2's horizontal position
     * @param h1 point 1's vertical position
     * @param h2 point 2's vertical position
     * @param f  fraction
     * @return an OpenCv Point
     */
    private static Point p(double w1, double w2, double h1, double h2, double f) {
        return new Point(w1 + f * (w2 - w1), h1 + f * (h2 - h1));
    }
}
