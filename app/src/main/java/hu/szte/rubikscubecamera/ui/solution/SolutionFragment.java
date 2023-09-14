package hu.szte.rubikscubecamera.ui.solution;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.databinding.FragmentSolutionBinding;

public class SolutionFragment extends Fragment {

    private FragmentSolutionBinding binding;

    private MainViewModel viewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentSolutionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSolution;
        viewModel.getResult().observe(getViewLifecycleOwner(), res -> {
            System.out.println("SOLUTION: " + res);
            textView.setText(res);
        });
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}