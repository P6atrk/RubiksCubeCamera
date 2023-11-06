package hu.szte.rubikscubecamera.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

import java.util.ArrayList;
import java.util.List;

public class ImageDecoder {

    public static String solveImage(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat cubeMask = createCubeMask(mat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        findContours(cubeMask, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        cvtColor(cubeMask, cubeMask, COLOR_GRAY2BGR);
        drawContours(cubeMask, contours, -1, new Scalar(0, 0, 255), 2);

        List<Mat> squareMasks = createSquareMasks(cubeMask, contours, hierarchy);
        List<Mat> matSquares = createMatSquares(mat, squareMasks);

        return getMatSquareColors(matSquares);
    }

    public static String solveImageForTesting(Mat mat) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat cubeMask = createCubeMask(mat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        findContours(cubeMask, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        cvtColor(cubeMask, cubeMask, COLOR_GRAY2BGR);
        drawContours(cubeMask, contours, -1, new Scalar(0, 0, 255), 2);

        //drawNumbersOnCubeSquares(cubeMask, contours, hierarchy);

        List<Mat> squareMasks = createSquareMasks(cubeMask, contours, hierarchy);
        List<Mat> matSquares = createMatSquares(mat, squareMasks);

        return getMatSquareColors(matSquares);
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
        return rearrangeColorString(colorString.toString());
    }

    private static String rearrangeColorString(String colorString) {
        // U1 U2 ... U9 R1 ... R9 F1 ... F9 D1 ... D9 L1 ... L9 B1 ... B9
        // TODO: eldönteni hogy 1. vagy 2. kép lesz az
        int[] urfArrangement = new int[] {
                26, 24, 22, 25, 21, 17, 23, 18, 14,
                10, 15, 19, 4, 8, 13, 0, 2, 7,
                20, 16, 11, 12, 9, 5, 6, 3, 1};
        int[] dlbArrangement = new int[] {
                26, 24, 22, 25, 21, 17, 23, 18, 14,
                1, 3, 6, 5, 9, 12, 11, 16, 20,
                7, 2, 0, 13, 8, 4, 19, 15, 10};

        if(colorString.charAt(9) == 'F') {
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

    /**
     * Creates a mask where the only white parts are the squares on the cube.
     * @param mat OpenCv mat, mask will be the same height and width as this mat.
     * @return A 1 channel mask with the same dims as mat.
     */
    private static Mat createCubeMask(Mat mat) {
        Mat mask = Mat.zeros(mat.rows(), mat.cols(), CvType.CV_8UC1);
        List<MatOfPoint> contours = new ArrayList<>();

        CubeLineDrawer.drawOuterLines(mask, new Scalar(255, 255, 255), 1);
        findContours(mask, contours, new Mat(), RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        drawContours(mask, contours, -1, new Scalar(255, 255, 255), FILLED);
        CubeLineDrawer.drawInnerLines(mask, new Scalar(0, 0, 0), 4);

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
}
