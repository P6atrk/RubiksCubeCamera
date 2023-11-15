package hu.szte.rubikscubecamera.ui.camera;

import static hu.szte.rubikscubecamera.utils.ImageDecoder.convertImageViewToMat;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.convertMatToBitmap;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.solveImage;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.solveImageForDebugging;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCameraBinding;

/**
 * Displays 2 images.
 * 1 button is for taking the 2 pictures and the other button is for generating the cube.
 */
public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private ImageView image1;
    private ImageView image2;
    private ConstraintLayout cameraFragmentContainer;
    private View root;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        OpenCVLoader.initDebug();

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        cameraFragmentContainer = binding.cameraFragmentContainer;
        image1 = binding.image1;
        image2 = binding.image2;
        ImageButton buttonCamera = binding.buttonCamera;
        Button buttonGenerate = binding.buttonGenerate;

        buttonCamera.setOnClickListener(image -> takeImage());
        if (false) {
            buttonGenerate.setOnClickListener(button -> imageDecoderForDebugging(image1, image2));
        } else {
            buttonGenerate.setOnClickListener(button -> imageDecoder(image1, image2));
        }

        deleteImages();

        return root;
    }

    /**
     * Decodes the cube from the image and gives the result to
     * the CubeFragment (which displays it).
     * The function uses 2 imageViews, 1 image contains 3 sides of the cube.
     *
     * @param imageView1 The UFR side of the cube.
     * @param imageView2 The DLB side of the cube.
     */
    private void imageDecoder(ImageView imageView1, ImageView imageView2) {
        if (imageView1.getDrawable() == null || imageView1.getDrawable() == null) {
            Toast.makeText(
                    requireActivity(),
                    "There are no images to decode.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String cubeString;
            cubeString = solveImage(convertImageViewToMat(imageView2), true)
                    + solveImage(convertImageViewToMat(imageView1), false);

            Bundle bundle = new Bundle();
            bundle.putString("cubeString", cubeString);
            cameraFragmentContainer.post(() ->
                    Navigation.findNavController(root)
                            .navigate(
                                    R.id.action_navigation_camera_to_navigation_cube,
                                    bundle,
                                    new NavOptions.Builder()
                                            .setPopUpTo(R.id.navigation_camera, true)
                                            .build()
                            )
            );
        });
    }

    /**
     * Was used for debugging the image decoding.
     * Helps display the results of certain image manipulation stuff on
     * the ImageViews.
     *
     * @param imageView1 The UFR side of the cube.
     * @param imageView2 The DLB side of the cube.
     */
    private void imageDecoderForDebugging(ImageView imageView1, ImageView imageView2) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            Mat mat1 = solveImageForDebugging(convertImageViewToMat(imageView1), false);
            Mat mat2 = solveImageForDebugging(convertImageViewToMat(imageView2), true);

            cameraFragmentContainer.post(() -> {
                imageView1.setImageBitmap(convertMatToBitmap(mat1));
                imageView2.setImageBitmap(convertMatToBitmap(mat2));
            });
        });
    }

    /**
     * Returns the last 2 bitmaps from the cache of the app.
     * These 2 bitmaps are the last images the user has taken.
     * These will be displayed on the cameraFragment in correct order.
     *
     * @return A list of 2 bitmaps.
     */
    private List<Bitmap> getLastTwoBitmaps() {
        Bitmap bitmap1;
        Bitmap bitmap2;
        File cacheDir = requireActivity().getCacheDir();
        File[] files = cacheDir.listFiles();
        File largest = null;
        File secondLargest = null;
        if (cacheDir.exists() && files != null && files.length >= 2) {
            largest = files[0];
            secondLargest = files[1];
            if (files[0].lastModified() < files[1].lastModified()) {
                largest = files[0];
                secondLargest = files[1];
            }
            for (int i = 2; i < files.length; i++) {
                if (files[i].lastModified() > largest.lastModified()) {
                    secondLargest = largest;
                    largest = files[i];
                } else if (files[i].lastModified() > secondLargest.lastModified()
                        && files[i].lastModified() != largest.lastModified()) {
                    secondLargest = files[i];
                }
            }
        }
        if (largest == null || secondLargest == null) {
            return new ArrayList<>();
        }
        bitmap1 = BitmapFactory.decodeFile(largest.getAbsolutePath());
        bitmap2 = BitmapFactory.decodeFile(secondLargest.getAbsolutePath());
        List<Bitmap> list = new ArrayList<>();
        list.add(bitmap1);
        list.add(bitmap2);
        return list;
    }

    /**
     * Sets the images on the cameraFragment.
     * If there is less than 2, it won't display any.
     */
    private void setImages() {
        List<Bitmap> imageBitmaps = getLastTwoBitmaps();

        deleteImages();

        if (imageBitmaps.size() == 2) {
            setImage(imageBitmaps.get(0));
            setImage(imageBitmaps.get(1));
        }
    }

    /**
     * Sets a single image on the cameraFragment.
     *
     * @param bitmap Bitmap of the image to set.
     */
    private void setImage(Bitmap bitmap) {
        if (image1.getDrawable() == null) {
            image1.setImageBitmap(bitmap);
        } else {
            image2.setImageBitmap(bitmap);
        }
    }

    /**
     * Navigates to the captureFragment, so the images can be taken.
     */
    private void takeImage() {
        Navigation.findNavController(root)
                .navigate(R.id.action_navigation_camera_to_navigation_capture);
    }

    /**
     * Deletes both images on the cameraFragment.
     * This won't delete them from the cache.
     */
    private void deleteImages() {
        image1.setImageResource(0);
        image2.setImageResource(0);
    }

    /**
     * If the fragment is resumed the images will be set automatically.
     */
    @Override
    public void onResume() {
        setImages();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}