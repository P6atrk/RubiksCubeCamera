package hu.szte.rubikscubecamera.ui.camera;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCaptureBinding;
import hu.szte.rubikscubecamera.utils.CubeLineDrawer;

/**
 * The user can take 2 pictures here. 1 for each half of the cube.
 * Displays the outline of a cube. The user has to position their
 * cube into the outline.
 * There is some text which guides the user.
 * There is also a button with which the pictures can be taken.
 */
public class CaptureFragment extends Fragment {

    private FragmentCaptureBinding binding;
    private PreviewView previewView;
    private TextView imageText;
    private Button imageCaptureButton;
    private View root;

    private boolean secondImage = false;

    private final Executor executor = Executors.newSingleThreadExecutor();
    private final int REQUEST_CODE_PERMISSIONS = 1010;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCaptureBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        if (((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(false);
        }

        previewView = binding.previewView;
        imageText = binding.imageNumber;
        imageCaptureButton = binding.imageCaptureButton;

        SurfaceView surfaceView = binding.surfaceView;
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceView.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if (!surfaceHolder.getSurface().isValid()) return;

                Canvas canvas = surfaceHolder.lockCanvas();

                if (canvas == null) return;
                CubeLineDrawer.drawInnerLines(canvas);
                CubeLineDrawer.drawOuterLines(canvas);

                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
            }
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            requestMultiplePermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }

        return root;
    }

    /**
     * If all permissions are granted, the camera will start with this function.
     */
    private void startCamera() {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity());

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(requireActivity()));
    }

    /**
     * Creates the camera with all of its functions.
     *
     * @param cameraProvider All the functions attach to this cameraProvider.
     */
    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder()
                .setTargetResolution(new Size(720, 960))
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(720, 960))
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

        camera.getCameraControl().setLinearZoom(0f);

        imageCaptureButton.setOnClickListener(a -> {

            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(requireActivity().getCacheDir(), mDateFormat.format(new Date()) + ".jpg");

            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
            imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                @Override
                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                    new Handler(
                            Looper.getMainLooper()).post(() -> {
                        Toast.makeText(
                                requireActivity(),
                                "Image Saved successfully",
                                Toast.LENGTH_SHORT).show();
                        if (!secondImage) {
                            secondImage = true;
                            imageText.setText(R.string.image_text_2);
                        } else {
                            stopCamera(cameraProvider);
                        }
                    });
                }

                @Override
                public void onError(@NonNull ImageCaptureException error) {
                    error.printStackTrace();
                }
            });
        });
    }

    /**
     * Stops the camera and goes back to the previous fragment.
     *
     * @param cameraProvider This is the camera that needs to be stopped.
     */
    private void stopCamera(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Navigation.findNavController(root).popBackStack();
    }

    /**
     * Checks for the all permissions.
     *
     * @return returns true if all the permissions were granted.
     */
    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Requests the permissions if they weren't granted already, if they are granted now,
     * the camera will start up.
     */
    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                for (boolean permission : permissions.values().toArray(new Boolean[0])) {
                    if (!permission) {
                        Toast.makeText(requireActivity(), "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                        requireActivity().finish();
                    }
                }
                startCamera();
            }
    );

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
