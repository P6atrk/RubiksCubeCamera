package hu.szte.rubikscubecamera.ui.cube;

import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Locale;

import hu.szte.rubikscubecamera.databinding.FragmentCubeBinding;

public class CubeFragment extends Fragment implements View.OnClickListener {
    private FragmentCubeBinding binding;

    private CubeViewModel cubeViewModel;

    private final int SQUARE_COUNT = 54;
    private final int COLOR_COUNT = 6;
    private final String SQUARE_NAME = "square";
    private final String COLOR_CHANGER_NAME = "colorChanger";
    private final String RESET_NAME = "buttonReset";

    private enum Color {
        YELLOW,
        GREEN,
        RED,
        BLUE,
        ORANGE,
        WHITE,
        EMPTY
    }

    private final int[] CENTER_SQUARE_NUMBERS = new int[]{5, 14, 23, 32, 41, 50};

    private Color currentColor = Color.RED;
    private ImageButton[] squares;
    private ImageButton[] colorChangers;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cubeViewModel = new ViewModelProvider(this).get(CubeViewModel.class);

        binding = FragmentCubeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button buttonSolveCubeRandom = binding.buttonSolveRandomCube;
        final Button buttonReset = binding.buttonReset;

        buttonSolveCubeRandom.setOnClickListener(a -> cubeViewModel.solveRandomCube());
        buttonReset.setOnClickListener(this);

        squares = setOnClickListenerForSquares();
        colorChangers = setOnClickListenerForColorChangers();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onClick(View view) {
        System.out.println("CLICKED ON IMAGEBUTTON: " + view.getResources().getResourceName(view.getId()));
        String viewName = view.getResources().getResourceName(view.getId()).split(":id/")[1];
        if(viewName.startsWith(SQUARE_NAME)) {
            int squareNumber = Integer.parseInt(viewName.split("square")[1]);
            if (!containsValue(CENTER_SQUARE_NUMBERS, squareNumber)) {
                changeColor(view, currentColor);
            }
        } else if (viewName.startsWith(COLOR_CHANGER_NAME)) {
            Color color = Color.valueOf(viewName.split("colorChanger")[1].toUpperCase());
            currentColor = color;
        } else if (viewName.equals(RESET_NAME)) {
            resetSquares();
        }
    }

    private ImageButton[] setOnClickListenerForSquares() {
        squares = new ImageButton[SQUARE_COUNT];

        for (int i = 0; i < squares.length; i++) {
            String squareName = SQUARE_NAME + (i + 1);
            try {
                squares[i] = (ImageButton) binding.getClass().getDeclaredField(squareName).get(binding);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        for (ImageButton square : squares) {
            square.setOnClickListener(this);
        }

        return squares;
    }

    private ImageButton[] setOnClickListenerForColorChangers() {
        colorChangers = new ImageButton[COLOR_COUNT];

        for (int i = 0; i < colorChangers.length; i++) {
            String colorName = Color.values()[i].name();
            colorName = colorName.charAt(0) + colorName.substring(1).toLowerCase();
            String colorChangerName = COLOR_CHANGER_NAME + (colorName);
            try {
                colorChangers[i] = (ImageButton) binding.getClass().getDeclaredField(colorChangerName).get(binding);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }

        for (ImageButton colorChanger : colorChangers) {
            colorChanger.setOnClickListener(this);
        }

        return squares;
    }

    private boolean containsValue(int[] array, int value) {
        for (int val : array) {
            if (val == value) {
                return true;
            }
        }
        return false;
    }

    private boolean changeColor(View view, Color color) {
        Context c = getContext();
        System.out.println(color.name().toLowerCase());
        int imageColor = c.getResources().getIdentifier("cube_color_" + color.name().toLowerCase(), "drawable", c.getPackageName());
        ((ImageButton) view).setImageResource(imageColor);
        return true;
    }
    
    private void resetSquares() {
        for (int i = 0; i < squares.length; i++) {
            if(!containsValue(CENTER_SQUARE_NUMBERS, i + 1)) {
                changeColor(squares[i], Color.EMPTY);
            }
        }
    }
}