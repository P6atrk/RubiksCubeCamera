package hu.szte.rubikscubecamera.ui.camera;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.IOException;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.databinding.FragmentCameraBinding;

public class CameraFragment extends Fragment {
    private FragmentCameraBinding binding;
    private ImageView image1;
    private ImageView image2;
    private MainViewModel viewModel;
    private ActivityResultLauncher<Intent> takeImageActivityResultLauncher;
    private ActivityResultLauncher<Intent> browseActivityResultLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentCameraBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        image1 = binding.image1;
        image2 = binding.image2;
        ImageButton buttonCamera = binding.buttonCamera;
        ImageButton buttonBrowse = binding.buttonBrowse;
        ImageButton buttonDelete = binding.buttonDelete;

        takeImageActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                        setImage(bitmap);
                    }
                });

        browseActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri selectedImageUri = data.getData();
                        if (null != selectedImageUri) {
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        getContext().getContentResolver(),
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

        deleteImage();

        return root;
    }

    private void setImage(Bitmap bitmap) {
        if (image1.getDrawable() == null) {
            image1.setImageBitmap(bitmap);
        } else {
            image2.setImageBitmap(bitmap);
        }
    }

    private void takeImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takeImageActivityResultLauncher.launch(intent);
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