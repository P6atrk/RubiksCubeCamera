package hu.szte.rubikscubecamera.utils;

import static org.opencv.imgproc.Imgproc.arrowedLine;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class ImageDecoder {

    public static String solveImage(Mat mat) {
        String cubeHalfString = "";
        return cubeHalfString;
    }

    public static void solveImageForTesting(Mat mat) {
        //org.opencv.
        //Core. Imgproc. Imgcodecs.
        arrowedLine(mat, new Point(0, 0), new Point(14, 45), new Scalar(255, 0, 0), 10);
    }

    public static Mat convertImageViewToMat(ImageView imageView) {
        Mat mat = new Mat();
        Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap8888, mat);
        return mat;
    }

    public static Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }
}
