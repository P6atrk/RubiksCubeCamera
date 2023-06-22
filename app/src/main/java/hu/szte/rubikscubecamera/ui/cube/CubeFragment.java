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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.databinding.FragmentCubeBinding;
import hu.szte.rubikscubecamera.utils.KociembaImpl;

public class CubeFragment extends Fragment implements View.OnClickListener {
    private FragmentCubeBinding binding;

    private CubeViewModel cubeViewModel;

    private final int SQUARE_COUNT = 54;
    private final String SQUARE_NAME = "square";
    private final String COLOR_CHANGER_NAME = "colorChanger";
    private final String RESET_NAME = "buttonReset";

    private enum Color {
        WHITE,
        BLUE,
        RED,
        YELLOW,
        GREEN,
        ORANGE,
        EMPTY
    }

    /**
     * going from 0 to 53, contains all of the center square numbers of a cube
     */
    private final int[] CENTER_SQUARE_NUMBERS = new int[]{4, 13, 22, 31, 40, 49};

    private ImageButton[] squares;
    private ImageButton[] colorChangers;

    private Color selectedColor = Color.RED;
    private String cube;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cubeViewModel = new ViewModelProvider(this).get(CubeViewModel.class);

        binding = FragmentCubeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button buttonSolveCubeRandom = binding.buttonSolveRandomCube;
        final Button buttonReset = binding.buttonReset;
        final Button buttonRandomize = binding.buttonRandomize;

        buttonSolveCubeRandom.setOnClickListener(a -> cubeViewModel.solveRandomCube());
        buttonReset.setOnClickListener(this);
        buttonRandomize.setOnClickListener(a -> randomizeCube());

        squares = setOnClickListenerForSquares();
        colorChangers = setOnClickListenerForColorChangers();

        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void randomizeCube() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            char[] randomCube = KociembaImpl.randomCube().toCharArray();
            Color color = Color.EMPTY;
            for (int i = 0; i < randomCube.length; i++) {
                switch (randomCube[i]) {
                    case 'U': color = Color.WHITE; break;
                    case 'R': color = Color.BLUE; break;
                    case 'F': color = Color.RED; break;
                    case 'D': color = Color.YELLOW; break;
                    case 'L': color = Color.GREEN; break;
                    case 'B': color = Color.ORANGE; break;
                }
                changeColor(squares[i], color);
            }
        });
    }


    @Override
    public void onClick(View view) {
        String viewName = view.getResources().getResourceName(view.getId()).split(":id/")[1];
        if(viewName.startsWith(SQUARE_NAME)) {
            onClickSquare(view, viewName);
        } else if (viewName.startsWith(COLOR_CHANGER_NAME)) {
            onClickColorChanger(viewName);
        } else if (viewName.equals(RESET_NAME)) {
            onClickReset();
        }
    }

    private void onClickSquare(View view, String viewName) {
        int squareNumber = Integer.parseInt(viewName.split("square")[1]);
        if (!containsValue(CENTER_SQUARE_NUMBERS, squareNumber)) {
            changeColor(view, selectedColor);
        }
    }

    private void onClickColorChanger(String viewName) {
        selectedColor = Color.valueOf(viewName.split("colorChanger")[1].toUpperCase());
    }

    /**
     * sets an onClickListener to every square on the screen
     * @return returns the imagebuttons that have an onclicklistener on them
     */
    private ImageButton[] setOnClickListenerForSquares() {
        ImageButton[] squares = new ImageButton[SQUARE_COUNT];
        for (int i = 0; i < squares.length; i++) {
            String squareName = SQUARE_NAME + i;
            try {
                squares[i] = (ImageButton) binding.getClass().getDeclaredField(squareName).get(binding);
                squares[i].setOnClickListener(this);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return squares;
    }

    /**
     * sets an onclicklistener for the color changer squares. This onclicklistener is in
     * the CubeFragment.java file onclick event
     * @return colorChanger ImageButtons that have an onclicklistener
     */
    private ImageButton[] setOnClickListenerForColorChangers() {
        Color[] colors = Color.values();
        ImageButton[] colorChangers = new ImageButton[colors.length];
        for (int i = 0; i < colors.length; i++) {
            String colorChangerName = COLOR_CHANGER_NAME + stringToCapitalized(colors[i].name());;
            try {
                colorChangers[i] = (ImageButton) binding.getClass().getDeclaredField(colorChangerName).get(binding);
                colorChangers[i].setOnClickListener(this);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
        return colorChangers;
    }


    /**
     * changes to color of a square to the Color
     * @param view square
     * @param color Color
     */
    private void changeColor(View view, Color color) {
        Context c = getContext();
        int imageColor = c.getResources().getIdentifier("cube_color_" + color.name().toLowerCase(), "drawable", c.getPackageName());
        ((ImageButton) view).setImageResource(imageColor);
    }

    /**
     * resets every square to its initial value
     */
    private void onClickReset() {
        for (int i = 0; i < squares.length; i++) {
            if(!containsValue(CENTER_SQUARE_NUMBERS, i)) {
                changeColor(squares[i], Color.EMPTY);
            }
        }
    }

    /**
     * Capitalizes a word.
     * FIRST -> First, second -> Second, tHr33 -> Thr33
     * @param str word to capitalize
     * @return a capitalized word
     */
    private String stringToCapitalized(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * checks if a int array contains a specific value
     * @param array to be examined
     * @param value to search for
     * @return boolean, true if array contains value, false if not
     */
    private boolean containsValue(int[] array, int value) {
        for (int val : array) {
            if (val == value) {
                return true;
            }
        }
        return false;
    }
}