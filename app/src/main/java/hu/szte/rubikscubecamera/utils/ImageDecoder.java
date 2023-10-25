package hu.szte.rubikscubecamera.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageDecoder {

    public static String solveImage(Mat mat) {
        String cubeHalfString = "";
        return cubeHalfString;
    }

    /*
    255, 255, 255   white
    0, 70, 173      blue
    183, 18, 52     red
    255, 213, 0     yellow
    0, 155, 72      green
    255, 88, 0      orange
     */

    public static Mat solveImageForTesting(Mat mat) {
        Mat mask = Mat.zeros(mat.rows(), mat.cols(), CvType.CV_8U);
        Mat colorMask = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        ArrayList<Scalar> colorsLower = new ArrayList<>();
        colorsLower.add(new Scalar(215, 215, 215)); // white

        ArrayList<Scalar> colorsUpper = new ArrayList<>();
        colorsUpper.add(new Scalar(255, 255, 255)); // white
        //colorsUpper.add(new Scalar(0, 70, 173)); // blue
        //colorsUpper.add(new Scalar(183, 18, 52)); // red
        //colorsUpper.add(new Scalar(255, 213, 0)); // yellow
        //colorsUpper.add(new Scalar(0, 155, 72)); // green
        //colorsUpper.add(new Scalar(255, 88, 0)); // orange

        Mat openKernel = getStructuringElement(MORPH_RECT, new Size(7, 7));
        Mat closeKernel = getStructuringElement(MORPH_RECT, new Size(5, 5));

        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        //Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2HSV);

        for(int i = 0; i < colorsLower.size(); i++) {
            Core.inRange(mat, colorsLower.get(i), colorsUpper.get(i), colorMask);
            Imgproc.morphologyEx(colorMask, colorMask, MORPH_OPEN, openKernel, new Point(-1, -1), 1);
            Imgproc.morphologyEx(colorMask, colorMask, MORPH_CLOSE, closeKernel, new Point(-1, -1), 5);
            //List<Mat> colorMasks = Arrays.asList(colorMask, colorMask, colorMask);
            //Core.merge(colorMasks, colorMask);
            Core.bitwise_or(mask, colorMask, mask);
        }

        //cvtColor(mask, mask, COLOR_BGR2GRAY);
        findContours(mask, contours, hierarchy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        drawContours(mat, contours, -1, new Scalar(255, 0, 0), 5);
        drawHelperLines(mat, new Scalar(255, 0, 255), 20);

        return mat;
    }

    private static void drawHelperLines(Mat mat, Scalar color, int thickness) {
        // w=width, h=height, L=left, R=right, M=middle, U=up, D=down

        double w = mat.cols();
        double h = mat.rows();
        double c = 5.0 / 7 * w / 2;

        double wM = w / 2;
        double wL = wM - c;
        double wR = wM + c;

        double hM = h / 2;
        double hU = hM + c;
        double hD = hM - c;
        double hMU = hU + (hM - hU) / 2;
        double hMD = hD - (hD - hM) / 2;

        line(mat, new Point(wL, hMU), new Point(wM, hU), color, thickness);
        line(mat, new Point(wM, hU), new Point(wR, hMU), color, thickness);
        line(mat, new Point(wR, hMU), new Point(wR, hMD), color, thickness);
        line(mat, new Point(wR, hMU), new Point(wR, hMD), color, thickness);
        line(mat, new Point(wR, hMD), new Point(wM, hD), color, thickness);
        line(mat, new Point(wM, hD), new Point(wL, hMD), color, thickness);
        line(mat, new Point(wM, hD), new Point(wL, hMD), color, thickness);
        line(mat, new Point(wL, hMD), new Point(wL, hMU), color, thickness);
    }

    private void createMask(Mat mat) {
        Mat mask = new Mat(mat.rows(), mat.cols(), CvType.CV_8U);
    }

    /**
     * The ImageView's Bitmap is converted to a OpenCV Mat type.
     * @param imageView CameraFragment's ImageView's Bitmap will be converted.
     * @return An OpenCV mat type
     */
    public static Mat convertImageViewToMat(ImageView imageView) {
        Mat mat = new Mat();
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap8888, mat);
        return mat;
    }

    /**
     * Convert a mat to a bitmap exactly.
     * @param mat OpenCV Mat to be converted
     * @return A Bitmap that is the same height and width as the mat, just a different data type.
     */
    public static Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }
}
