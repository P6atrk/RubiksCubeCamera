package hu.szte.rubikscubecamera.ui.solution;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.szte.rubikscubecamera.databinding.FragmentSolutionBinding;

public class SolutionFragment extends Fragment {

    private FragmentSolutionBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SolutionViewModel solutionViewModel =
                new ViewModelProvider(this).get(SolutionViewModel.class);

        binding = FragmentSolutionBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textSolution;
        solutionViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}