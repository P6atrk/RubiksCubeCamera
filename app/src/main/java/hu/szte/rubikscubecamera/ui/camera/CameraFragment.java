package hu.szte.rubikscubecamera.ui.camera;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCameraBinding;
import hu.szte.rubikscubecamera.ui.cube.CubeFragment;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

public class CameraFragment extends Fragment {
    private FragmentCameraBinding binding;
    private ImageView image1;
    private ImageView image2;
    private ActivityResultLauncher<Intent> browseActivityResultLauncher;
    private ConstraintLayout cameraFragmentContainer;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        OpenCVLoader.initDebug();

        MainViewModel viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        cameraFragmentContainer = binding.cameraFragmentContainer;
        image1 = binding.image1;
        image2 = binding.image2;
        ImageButton buttonCamera = binding.buttonCamera;
        ImageButton buttonBrowse = binding.buttonBrowse;
        ImageButton buttonDelete = binding.buttonDelete;
        Button buttonGenerate = binding.buttonGenerate;

        /*takeImageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        setImage(bitmap);
                    }
                });*/

        browseActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        requireActivity().getContentResolver(),
                                        selectedImageUri);
                                setImage(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                });


        buttonCamera.setOnClickListener(image -> takeImage());
        buttonBrowse.setOnClickListener(image -> browseImage());
        buttonDelete.setOnClickListener(image -> deleteImage());
        buttonGenerate.setOnClickListener(button -> imageDecoder(image1, image2));

        deleteImage();

        List<Bitmap> imageBitmaps = getLastTwoBitmaps();
        if(!imageBitmaps.isEmpty()) {
            setImage(imageBitmaps.get(0));
            setImage(imageBitmaps.get(1));
        }
        return root;
    }

    @Override
    public void onResume() {
        System.out.println("ABCD RESUMED!");
        super.onResume();
    }

    private List<Bitmap> getLastTwoBitmaps() {
        Bitmap bitmap1;
        Bitmap bitmap2;
        File cacheDir = requireActivity().getCacheDir();
        File[] files = cacheDir.listFiles();
        File largest = null;
        File secondLargest = null;
        if(cacheDir.exists() && files != null && files.length > 2) {
            largest = files[0];
            secondLargest = files[1];
            if (files[0].lastModified() < files[1].lastModified()) {
                largest = files[0];
                secondLargest = files[1];
            }
            for(int i = 2; i < files.length; i++) {
                if (files[i].lastModified() > largest.lastModified()) {
                    secondLargest = largest;
                    largest = files[i];
                } else if (files[i].lastModified() > secondLargest.lastModified()
                        && files[i].lastModified() != largest.lastModified()) {
                    secondLargest = files[i];
                }
            }

            for(File file : files) {
                System.out.println("1234: " + file.getName() + "::" + file.lastModified());
            }

            System.out.println("1234 LARGEST: " + largest.getName() + "::" + largest.lastModified());
            System.out.println("1234 SECOND_LARGEST: " + secondLargest.getName() + "::" + secondLargest.lastModified());
        }
        if(largest == null || secondLargest == null) {
            return new ArrayList<>();
        }
        bitmap1 = BitmapFactory.decodeFile(largest.getAbsolutePath());
        bitmap2 = BitmapFactory.decodeFile(secondLargest.getAbsolutePath());
        List<Bitmap> list = new ArrayList<>();
        list.add(bitmap1);
        list.add(bitmap2);
        return list;
    }

    private void imageDecoder(ImageView imageView1, ImageView imageView2) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String cubeString = "EEEEUEEEEEEEEREEEEEEEEFEEEEEEEEDEEEEEEEELEEEEEEEEBEEEE";
            Mat mat1 = convertImageViewToMat(imageView1);
            Mat mat2 = convertImageViewToMat(imageView2);

            // Do stuff with mat

            cubeString = "EUUEUEEEEEEEEREEEEEEEEFEEEEEEEEDEEEEEEEELEEEEEEEEBEEEE";

            CubeFragment cubeFragment = new CubeFragment();

            Bundle args = new Bundle();
            args.putString("cubeString", cubeString);
            cubeFragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, cubeFragment)
                    .commit();

            // Draw mat with imageView
            //imageView1.setImageBitmap(convertMatToBitmap(mat1));
            //imageView2.setImageBitmap(convertMatToBitmap(mat2));
        });
    }

    private Mat convertImageViewToMat(ImageView imageView) {
        Mat mat = new Mat();
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        Bitmap bitmap8888 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap8888, mat);
        return mat;
    }

    private Bitmap convertMatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888); // this creates a MUTABLE bitmap
        Utils.matToBitmap(mat, bitmap);
        return bitmap;
    }

    private void setImage(Bitmap bitmap) {
        if (image1.getDrawable() == null) {
            image1.setImageBitmap(bitmap);
        } else {
            image2.setImageBitmap(bitmap);
        }
        System.out.println("ABCD image bitmap set" + bitmap.toString());
    }

    private void takeImage() {
        CaptureFragment captureFragment = new CaptureFragment();
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();

        Fragment oldFragment = fragmentManager.findFragmentById(cameraFragmentContainer.getId());
        if (oldFragment != null) {
            fragmentManager.beginTransaction().remove(oldFragment).commit();
        }

        fragmentManager.beginTransaction()
                .replace(cameraFragmentContainer.getId(), captureFragment)
                .addToBackStack(null)
                .commit();
    }

    private void browseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        browseActivityResultLauncher.launch(intent);

    }

    private void deleteImage() {
        image1.setImageResource(0);
        image2.setImageResource(0);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}