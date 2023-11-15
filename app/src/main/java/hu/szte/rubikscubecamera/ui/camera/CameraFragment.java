package hu.szte.rubikscubecamera.ui.camera;

import static hu.szte.rubikscubecamera.utils.ImageDecoder.convertImageViewToMat;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.convertMatToBitmap;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.solveImage;
import static hu.szte.rubikscubecamera.utils.ImageDecoder.solveImageForDebugging;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {

    private FragmentCameraBinding binding;
    private ImageView image1;
    private ImageView image2;
    private ActivityResultLauncher<Intent> browseActivityResultLauncher;
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

    private List<Bitmap> getLastTwoBitmaps() {
        Bitmap bitmap1;
        Bitmap bitmap2;
        File cacheDir = requireActivity().getCacheDir();
        File[] files = cacheDir.listFiles();
        File largest = null;
        File secondLargest = null;
        if (cacheDir.exists() && files != null && files.length > 2) {
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

    private void setImages() {
        List<Bitmap> imageBitmaps = getLastTwoBitmaps();

        deleteImages();

        if (imageBitmaps.size() == 2) {
            setImage(imageBitmaps.get(0));
            setImage(imageBitmaps.get(1));
        }
    }

    private void setImage(Bitmap bitmap) {
        if (image1.getDrawable() == null) {
            image1.setImageBitmap(bitmap);
        } else {
            image2.setImageBitmap(bitmap);
        }
    }

    private void takeImage() {
        Navigation.findNavController(root)
                .navigate(R.id.action_navigation_camera_to_navigation_capture);
    }

    private void deleteImages() {
        image1.setImageResource(0);
        image2.setImageResource(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        setImages();
        super.onResume();
    }
}