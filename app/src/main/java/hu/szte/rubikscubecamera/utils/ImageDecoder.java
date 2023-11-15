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
import org.opencv.imgproc.Imgproc;

import static org.opencv.imgproc.Imgproc.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Decodes the cube from the image.
 * Also has some function which help in debugging.
 */
public class ImageDecoder {

    /**
     * Solves the cube and gives back a string representation.
     *
     * @param mat   The mat where the cube is.
     * @param isURF Tells which image is the current one.
     * @return String representation of the cube on the images.
     */
    public static String solveImage(Mat mat, boolean isURF) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat cubeMask = createCubeMask(mat);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();

        findContours(cubeMask, contours, hierarchy, RETR_LIST, CHAIN_APPROX_SIMPLE);
        cvtColor(cubeMask, cubeMask, COLOR_GRAY2BGR);
        drawContours(cubeMask, contours, -1, new Scalar(0, 0, 255), 2);

        contours = rearrangeContours(contours);

        List<Mat> squareMasks = createSquareMasks(cubeMask, contours);
        List<Mat> matSquares = createMatSquares(mat, squareMasks);

        return rearrangeColorString(getMatSquareColors(matSquares), isURF);
    }

    /**
     * Used for debugging the solveImage function.
     * Currently returns a mask which shows every blue color on the image.
     *
     * @param mat   The mat where the cube is.
     * @param isURF Tells which image is the current one.
     * @return A mask which shows every blue color on the image.
     */
    public static Mat solveImageForDebugging(Mat mat, boolean isURF) {
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGRA2BGR);
        Mat cubeMask = createCubeMask(mat);

        List<MatOfPoint> contours = new ArrayList<>();

        findContours(cubeMask, contours, new Mat(), RETR_LIST, CHAIN_APPROX_SIMPLE);
        cvtColor(cubeMask, cubeMask, COLOR_GRAY2BGR);

        contours = rearrangeContours(contours);
        drawOnCubeSquares(cubeMask, contours);

        List<Mat> squareMasks = createSquareMasks(cubeMask, contours);
        List<Mat> matSquares = createMatSquares(mat, squareMasks);

        return getMatSquareColors(matSquares, SquareInfo.Color.BLUE.ordinal());
    }

    /**
     * Used for debugging. Draws the color texts on the image above the correct squares.
     *
     * @param mat          Mat to draw on.
     * @param contours     The contours of the squares in the correct order.
     * @param colorsString The colors of the squares.
     */
    private static void drawOnCubeSquares(Mat mat, List<MatOfPoint> contours, String colorsString) {
        int fontScale = 1;
        Scalar fontColor = new Scalar(255, 0, 0);
        int fontThickness = 5;

        for (int i = 0; i < contours.size(); i++) {
            String text = String.valueOf(colorsString.charAt(i));
            MatOfPoint nextContour = contours.get(i);

            putText(mat, text, nextContour.toArray()[0], FONT_HERSHEY_COMPLEX, fontScale, fontColor, fontThickness);
        }
    }

    /**
     * Used for debugging. Draws the number on the cube squares.
     * This number represents their place in the contours list.
     *
     * @param mat      Mat to draw on.
     * @param contours Contours of the squares.
     */
    private static void drawOnCubeSquares(Mat mat, List<MatOfPoint> contours) {
        int fontScale = 1;
        Scalar fontColor = new Scalar(255, 0, 0);
        int fontThickness = 5;

        for (int i = 0; i < contours.size(); i++) {
            String text = String.valueOf(i);
            MatOfPoint nextContour = contours.get(i);

            putText(mat, text, nextContour.toArray()[0], FONT_HERSHEY_COMPLEX, fontScale, fontColor, fontThickness);
        }
    }

    /**
     * Rearranges the contours based on their corrected position.
     * Up->Down and Left->Right.
     *
     * @param contours List of cubeMask square contours.
     * @return A list of contours in the correct order.
     */
    private static List<MatOfPoint> rearrangeContours(List<MatOfPoint> contours) {
        LinkedHashMap<Integer, Point> points = new LinkedHashMap<>();
        LinkedHashMap<Integer, Point> pointsSorted = new LinkedHashMap<>();
        List<MatOfPoint> correctContours = new ArrayList<>();

        for (int i = 0; i < contours.size(); i++) {
            points.put(i, contours.get(i).toArray()[0]);
        }
        for (int i = 0; i < points.size() - 1; i++) {
            for (int j = i + 1; j < points.size(); j++) {
                if (Math.abs(points.get(i).y - points.get(j).y) < 6) {
                    points.get(j).y = points.get(i).y;
                }
            }
        }
        for (int j = 0; j < points.size(); j++) {
            int smallest = 0;
            for (int i = 0; i < points.size(); i++) {
                if (points.get(i) == null) {
                    continue;
                }
                if (points.get(i).y < points.get(smallest).y) {
                    smallest = i;
                } else if (points.get(i).y == points.get(smallest).y) {
                    if (points.get(i).x < points.get(smallest).x) {
                        smallest = i;
                    }
                }
            }
            pointsSorted.put(smallest, points.get(smallest));
            points.replace(smallest, null);
        }
        List<Integer> correctPlacements = new ArrayList<>(pointsSorted.keySet());
        for (Integer i : correctPlacements) {
            correctContours.add(contours.get(i));
        }
        return correctContours;
    }

    /**
     * As an input gets a list of cube squares cutouts.
     * Decides which color is the cube square and stores it in a string.
     * Outputs the colors of the squares as a string.
     *
     * @param matSquares List of cube square cutouts.
     * @return A list of chars in a string. Each char represent a square of the cube.
     */
    private static String getMatSquareColors(List<Mat> matSquares) {
        SquareInfo squareInfo = SquareInfo.createSquareInfo();
        StringBuilder colorString = new StringBuilder();

        List<List<Mat>> matSquareColorCounters = new ArrayList<>();
        for (int i = 0; i < matSquares.size(); i++) {
            cvtColor(matSquares.get(i), matSquares.get(i), Imgproc.COLOR_BGR2HSV);
            matSquareColorCounters.add(new ArrayList<>());
            for (int j = 0; j < squareInfo.lowerBounds.length; j++) {
                Mat colorCounter = Mat.zeros(matSquares.get(i).rows(), matSquares.get(i).cols(), CvType.CV_8U);
                Core.inRange(matSquares.get(i), squareInfo.lowerBounds[j], squareInfo.upperBounds[j], colorCounter);
                matSquareColorCounters.get(i).add(colorCounter);
            }
        }
        for (List<Mat> colorCounters : matSquareColorCounters) {
            int biggest = 0;
            for (int i = 1; i < colorCounters.size(); i++) {
                int biggestCount = Core.countNonZero(colorCounters.get(biggest));
                if (Core.countNonZero(colorCounters.get(i)) > biggestCount) {
                    biggest = i;
                }
            }
            colorString.append(SquareInfo.SIDE_COLORS[biggest]);
        }
        return colorString.toString();
    }

    private static Mat getMatSquareColors(List<Mat> matSquares, int color) {
        SquareInfo squareInfo = SquareInfo.createSquareInfo();

        Mat matCube = Mat.zeros(matSquares.get(0).rows(), matSquares.get(0).cols(), CvType.CV_8UC3);
        for (Mat matSquare : matSquares) {
            Core.bitwise_or(matCube, matSquare, matCube);
        }

        List<Mat> matCubeColorMasks = new ArrayList<>();
        cvtColor(matCube, matCube, Imgproc.COLOR_BGR2HSV);
        for (int j = 0; j < squareInfo.lowerBounds.length; j++) {
            Mat cubeColorMask = Mat.zeros(matCube.rows(), matCube.cols(), CvType.CV_8U);
            Core.inRange(matCube, squareInfo.lowerBounds[j], squareInfo.upperBounds[j], cubeColorMask);
            matCubeColorMasks.add(cubeColorMask);
        }

        return matCubeColorMasks.get(SquareInfo.Color.values()[color].ordinal());
    }

    /**
     * Return a string made out of the chars from the input string.
     * The whole string is rearranges based on the int[] vars.
     * This input string is the half of the cube string representation.
     *
     * @param colorString String to be rearranged.
     * @param isURF       Decides if the string represents the urf or the dlb side of the cube.
     * @return a rearranged string made out of the input string.
     */
    private static String rearrangeColorString(String colorString, boolean isURF) {
        // U1 U2 ... U9 R1 ... R9 F1 ... F9 D1 ... D9 L1 ... L9 B1 ... B9
        int[] urfArrangement = new int[]{
                0, 2, 4, 1, 5, 9, 3, 8, 11,
                16, 12, 7, 22, 18, 14, 26, 24, 20,
                6, 10, 15, 13, 17, 21, 19, 23, 25};
        int[] dlbArrangement = new int[]{
                3, 1, 0, 8, 5, 2, 11, 9, 4,
                25, 23, 19, 21, 17, 13, 15, 10, 6,
                20, 24, 26, 14, 18, 22, 7, 12, 16};

        if (isURF) {
            return appendFor(colorString, urfArrangement);
        } else {
            return appendFor(colorString, dlbArrangement);
        }
    }

    /**
     * Appends a new string with the characters of the ogString. The arrangement dictates with
     * character is going to be picked from the ogString next
     *
     * @param ogString    the Original string, these characters will be picked.
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

    /**
     * Returns a list of Mat made out of the cube squares.
     * These mats are the actual image cutouts of the cube.
     * There are 27 mats in the list.
     *
     * @param mat         The original image of the cube.
     * @param squareMasks A list of cube square masks. One for each square.
     * @return A Mat List of square cutouts of the original cube image. 27 in length.
     */
    private static List<Mat> createMatSquares(Mat mat, List<Mat> squareMasks) {
        List<Mat> matSquares = new ArrayList<>();
        for (Mat squareMask : squareMasks) {
            matSquares.add(applyMask(mat, squareMask));
        }
        return matSquares;
    }

    /**
     * Creates a list of masks from the cubeMask. A single mask is
     * a single cube square mask. There are 27 of them, one for each square on the image.
     *
     * @param cubeMask The Mat mask for the whole cube.
     * @param contours Contours of the cubeMask.
     * @return A list of Mat masks. 27 in length. One for each cube square.
     */
    private static List<Mat> createSquareMasks(Mat cubeMask, List<MatOfPoint> contours) {
        Scalar color = new Scalar(255, 255, 255);
        List<Mat> squareMasks = new ArrayList<>();

        for (int i = 0; i < contours.size(); i++) {
            Mat nextSquareMask = Mat.zeros(cubeMask.rows(), cubeMask.cols(), CvType.CV_8U);
            drawContours(nextSquareMask, contours, i, color, FILLED);
            squareMasks.add(nextSquareMask);
        }

        return squareMasks;
    }

    /**
     * Creates a mask where the only white parts are the squares of the cube.
     *
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
     * Applies a mask to a mat and return it without changing either the mat or the mask.
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
     * Convert a mat to a bitmap.
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
