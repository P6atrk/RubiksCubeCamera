package hu.szte.rubikscubecamera.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class ImageDecoder {

    public static String solveImage(Mat mat) {
        String cubeHalfString = "";
        return cubeHalfString;
    }

    public static Mat solveImageForTesting(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat cubeMask = createCubeMask(mat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        findContours(cubeMask, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        cvtColor(cubeMask, cubeMask, COLOR_GRAY2BGR);
        drawContours(cubeMask, contours, -1, new Scalar(0, 0, 255), 2);

        //drawNumbersOnCubeSquares(cubeMask, contours, hierarchy);

        //List<Mat> squareMasks = createSquareMasks(cubeMask, contours, hierarchy);
        //List<Mat> matSquares = createMatSquares(mat, squareMasks);
        //String colors = getMatSquareColors(matSquares);
        //System.out.println("FINALLY!!!" + colors);
        System.out.println("FINALLY!!!" + mat.cols() + "::" + mat.rows());
        drawOuterLines(mat, new Scalar(255, 255, 0), 10);
        return mat;
    }

    private static String getMatSquareColors(List<Mat> matSquares) {
        SquareInfo squareInfo = SquareInfo.createSquareInfo();
        StringBuilder colorString = new StringBuilder();

        List<List<Mat>> dstss = new ArrayList<>();
        for(int i = 0; i < matSquares.size(); i++) {
            cvtColor(matSquares.get(i), matSquares.get(i), Imgproc.COLOR_BGR2HSV);
            dstss.add(new ArrayList<>());
            for(int j = 0; j < squareInfo.lowerBounds.size(); j++) {
                Mat dst = Mat.zeros(matSquares.get(i).rows(), matSquares.get(i).cols(), CvType.CV_8U);
                Core.inRange(matSquares.get(i), squareInfo.lowerBounds.get(j), squareInfo.upperBounds.get(j), dst);
                dstss.get(i).add(dst);
            }
        }
        for(List<Mat> dsts : dstss) {
            int biggest = 0;
            for (int i = 1; i < dsts.size(); i++) {
                if(Core.countNonZero(dsts.get(i)) > Core.countNonZero(dsts.get(i - 1))) {
                    biggest = i;
                }
            }
            colorString.append(SquareInfo.SIDE_COLORS[biggest]);
        }
        System.out.println("FINAFFF!!!" + colorString.toString());
        return rearrangeColorString(colorString.toString());
    }

    private static String rearrangeColorString(String colorString) {
        // TODO eldönteni hogy 1. vagy 2. kép lesz az
        int[] urfArrangement = new int[] {15, 26, 16, 25, 17, 24, 12, 23, 13, 22, 14, 21, 9, 20, 10, 19, 8, 11, 18, 5, 7, 4, 2, 6, 1, 3, 0};
        int[] dlbArrangement = new int[] {24, 11, 25, 14, 26, 17, 21, 10, 22, 13, 23, 16, 18, 9, 19, 12, 6, 20, 15, 7, 3, 4, 8, 0, 5, 1, 2};

        if(true) {
            return appendFor(colorString, urfArrangement);
        } else {
            return appendFor(colorString, dlbArrangement);
        }
    }

    /**
     * Appends a new string with the characters of the ogString. The arrangement dictates with
     * character is going to be picked from the ogString next
     * @param ogString the Original string, these characters will be picked.
     * @param arrangement an int array, eg.: [3, 5, 0, 2, 8, 6, 7, 4, 1]
     * @return A string
     */
    private static String appendFor(String ogString, int[] arrangement) {
        StringBuilder str = new StringBuilder();
        for (int i : arrangement) {
            str.append(ogString.charAt(i));
        }
        return str.toString();
    }

    private static void drawNumbersOnCubeSquares(Mat cubeMask, List<MatOfPoint> contours, Mat hierarchy) {
        int fontScale = 1;
        Scalar fontColor = new Scalar(255, 0, 0);
        int fontThickness = 5;

        for (int i = 0; i < hierarchy.cols(); i++) {
            double[] hierarchyData = hierarchy.get(0, i);
            int nextHierarchy = (int) hierarchyData[0];
            String text = String.valueOf(nextHierarchy);
            MatOfPoint nextContour = contours.get(i);

            putText(cubeMask, text, nextContour.toArray()[0], FONT_HERSHEY_COMPLEX, fontScale, fontColor, fontThickness);
        }
    }

    private static List<Mat> createMatSquares(Mat mat, List<Mat> squareMasks) {
        List<Mat> matSquares = new ArrayList<>();
        for (Mat squareMask : squareMasks) {
            matSquares.add(applyMask(mat, squareMask));
        }
        return matSquares;
    }

    private static List<Mat> createSquareMasks(Mat cubeMask, List<MatOfPoint> contours, Mat hierarchy) {
        Scalar color = new Scalar(255, 255, 255);
        List<Mat> squareMasks = new ArrayList<>();

        for (int i = 0; i < hierarchy.cols(); i++) {
            Mat nextSquareMask = Mat.zeros(cubeMask.rows(), cubeMask.cols(), CvType.CV_8U);
            drawContours(nextSquareMask, contours, i, color, FILLED);
            squareMasks.add(nextSquareMask);
        }

        return squareMasks;
    }

    private static void drawInnerLines(Mat mat, Scalar color, int thickness) {
        // w=width, h=height, L=left, R=right, M=middle, U=up, D=down
        double w = mat.cols();
        double h = mat.rows();
        double c = 5.0 / 7 * w / 2;
        double d = c * 0.2;
        double f1 = 1 / 3.0;
        double f2 = 2 / 3.0;

        double wM = w / 2;
        double wL = wM - c;
        double wR = wM + c;
        double wDL = wM - c + d;
        double wDR = wM + c - d;

        double hM = h / 2.0;
        double hU = hM - c;
        double hD = hM + c;
        double hMD = hD + (hM - hD) / 2;
        double hMU = hU - (hU - hM) / 2.3f;

        double hC = 2 * hMU - hU;

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

    private static void drawOuterLines(Mat mat, Scalar color, int thickness) {
        // w=width, h=height, L=left, R=right, M=middle, U=up, D=down
        double w = mat.cols();
        double h = mat.cols();
        double c = 5.0 / 7.0 * w / 2.0;
        double d = c * 0.2;

        double wM = w / 2.0;
        double wL = wM - c;
        double wR = wM + c;
        double wDL = wM - c + d;
        double wDR = wM + c - d;

        double hM = h / 2.0;
        double hU = hM + c;
        double hD = hM - c;
        double hMU = hU + (hM - hU) / 2.0;
        double hMD = hD - (hD - hM) / 2.3f;

        line(mat, new Point(wDL, hMU), new Point(wM, hU), color, thickness);
        line(mat, new Point(wM, hU), new Point(wDR, hMU), color, thickness);
        line(mat, new Point(wDR, hMU), new Point(wR, hMD), color, thickness);
        line(mat, new Point(wR, hMD), new Point(wM, hD), color, thickness);
        line(mat, new Point(wM, hD), new Point(wL, hMD), color, thickness);
        line(mat, new Point(wL, hMD), new Point(wDL, hMU), color, thickness);

        line(mat, new Point(wM, hM), new Point(wM, hM), color, thickness);
    }

    private static Mat createCubeMask(Mat mat) {
        Mat mask = Mat.zeros(mat.rows(), mat.cols(), CvType.CV_8UC1);
        List<MatOfPoint> contours = new ArrayList<>();

        drawOuterLines(mask, new Scalar(255, 255, 255), 1);
        findContours(mask, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        drawContours(mask, contours, -1, new Scalar(255, 255, 255), FILLED);

        drawInnerLines(mask, new Scalar(0, 0, 0), 4);

        return mask;
    }

    /**
     * Applies a mask to a mat without changing either the mat or the mask.
     *
     * @param mat  The image which the mask will be applied to.
     * @param mask This mask is applied to the mat.
     * @return An image with only the mask portion of the image showing.
     */
    private static Mat applyMask(Mat mat, Mat mask) {
        Mat matDst = new Mat();
        Mat maskClone = mask.clone();

        cvtColor(maskClone, maskClone, COLOR_GRAY2BGR);
        Core.bitwise_and(mat, maskClone, matDst);

        return matDst;
    }

    /**
     * The ImageView's Bitmap is converted to a OpenCV Mat type.
     *
     * @param imageView CameraFragment's ImageView's Bitmap will be converted.
     * @return An OpenCV mat type
     */
    public static Mat convertImageViewToMat(ImageView imageView) {
        Mat mat = new Mat();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap8888, mat);
        return mat;
    }

    /**
     * Convert a mat to a bitmap exactly.
     *
     * @param mat OpenCV Mat to be converted
     * @return A Bitmap that is the same height and width as the mat, just a different data type.
     */
    public static Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
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
