package hu.szte.rubikscubecamera.ui.cube;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.szte.rubikscubecamera.databinding.FragmentCubeBinding;

public class CubeFragment extends Fragment {

    private FragmentCubeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        CubeViewModel cubeViewModel =
                new ViewModelProvider(this).get(CubeViewModel.class);

        binding = FragmentCubeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textCube;
        cubeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}