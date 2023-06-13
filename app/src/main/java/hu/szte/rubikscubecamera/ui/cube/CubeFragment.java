package hu.szte.rubikscubecamera.ui.cube;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import hu.szte.rubikscubecamera.databinding.FragmentCubeBinding;

public class CubeFragment extends Fragment {
    private FragmentCubeBinding binding;

    private CubeViewModel cubeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cubeViewModel = new ViewModelProvider(this).get(CubeViewModel.class);

        binding = FragmentCubeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textCube = binding.textCube;
        final Button buttonSolveCubeRandom = binding.buttonSolveRandomCube;

        buttonSolveCubeRandom.setOnClickListener(a -> cubeViewModel.solveRandomCube());

        cubeViewModel.getTextCube().observe(getViewLifecycleOwner(), textCube::setText);

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}