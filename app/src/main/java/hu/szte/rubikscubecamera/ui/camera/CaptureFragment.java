package hu.szte.rubikscubecamera.ui.camera;

import static org.opencv.imgproc.Imgproc.line;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Rational;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.core.impl.ImageCaptureConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCaptureBinding;
import hu.szte.rubikscubecamera.ui.cube.CubeFragment;

public class CaptureFragment extends Fragment {

    private FragmentCaptureBinding binding;

    private PreviewView previewView;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView imageNumber;
    private Button imageCaptureButton;

    private Executor executor = Executors.newSingleThreadExecutor();

    final private int REQUEST_CODE_PERMISSIONS = 1010;


    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private boolean secondImage = false;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentCaptureBinding.inflate(getLayoutInflater());
        View root = binding.getRoot();

        previewView = binding.previewView;
        imageNumber = binding.imageNumber;
        imageCaptureButton = binding.imageCaptureButton;

        surfaceView = binding.surfaceView;
        surfaceHolder = surfaceView.getHolder();
        surfaceView.setZOrderOnTop(true);
        surfaceHolder.setFormat(PixelFormat.TRANSPARENT);

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
                if(!surfaceHolder.getSurface().isValid()) return;

                Canvas canvas = surfaceHolder.lockCanvas();

                if(canvas == null) return;
                drawOnCanvas(canvas);


                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {}

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {}
        });

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            requestMultiplePermissionsLauncher.launch(REQUIRED_PERMISSIONS);
        }


        return root;
    }

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

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();

        ImageCapture imageCapture = new ImageCapture.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, imageCapture);

        camera.getCameraControl().setLinearZoom(0f);

        imageCaptureButton.setOnClickListener(a -> {

            SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
            File file = new File(requireActivity().getCacheDir(), mDateFormat.format(new Date()) + ".jpg");
            System.out.println("ABCD imagecapture pressed");

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
                                if(!secondImage) {
                                    secondImage = true;
                                    imageNumber.setText("Image 2");
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

    private void stopCamera(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        System.out.println("ABCD unbindAll");
        requireActivity().getSupportFragmentManager().popBackStack();
    }

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(requireActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void drawOnCanvas(Canvas canvas) {
        Paint linePaint = new Paint();
        linePaint.setColor(Color.rgb(255, 0, 0));
        linePaint.setStrokeWidth(2);

        // w=width, h=height, L=left, R=right, M=middle, U=up, D=down
        float w = canvas.getWidth();
        float h = canvas.getHeight();
        float c = 5f / 7 * w / 2;
        float d = c * 0.2f;

        float wM = w / 2;
        float wL = wM - c;
        float wR = wM + c;
        float wDL = wM - c + d;
        float wDR = wM + c - d;

        float hM = h / 2;
        float hU = hM + c;
        float hD = hM - c;
        float hMU = hU + (hM - hU) / 2;
        float hMD = hD - (hD - hM) / 2.3f;

        canvas.drawLine(wDL, hMU, wM, hU, linePaint);
        canvas.drawLine(wM, hU, wDR, hMU, linePaint);
        canvas.drawLine(wDR, hMU, wR, hMD, linePaint);
        canvas.drawLine(wR, hMD, wM, hD, linePaint);
        canvas.drawLine(wM, hD, wL, hMD, linePaint);
        canvas.drawLine(wL, hMD, wDL, hMU, linePaint);
    }

    private final ActivityResultLauncher<String[]> requestMultiplePermissionsLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                for(boolean permission : permissions.values().toArray(new Boolean[0])) {
                    if(!permission) {
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
