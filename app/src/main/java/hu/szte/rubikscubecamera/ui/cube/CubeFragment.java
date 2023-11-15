package hu.szte.rubikscubecamera.ui.cube;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.MainViewModel;
import hu.szte.rubikscubecamera.R;
import hu.szte.rubikscubecamera.databinding.FragmentCubeBinding;
import hu.szte.rubikscubecamera.utils.KociembaImpl;
import hu.szte.rubikscubecamera.utils.SquareInfo;

public class CubeFragment extends Fragment implements View.OnClickListener {
    private FragmentCubeBinding binding;
    private MainViewModel viewModel;
    private final String SQUARE_NAME = "square";
    private final String COLOR_CHANGER_NAME = "colorChanger";
    private String cubeOld = "EEEEUEEEEEEEEREEEEEEEEFEEEEEEEEDEEEEEEEELEEEEEEEEBEEEE";
    private ConstraintLayout constraintLayout;
    private View root;

    private ImageButton[] squares;

    private SquareInfo.Color selectedColor = SquareInfo.Color.RED;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        binding = FragmentCubeBinding.inflate(inflater, container, false);
        root = binding.getRoot();

        final Button buttonSolveCube = binding.buttonSolveCube;
        final Button buttonReset = binding.buttonReset;
        final Button buttonRandomize = binding.buttonRandomize;
        constraintLayout = binding.cubeFragmentContrainstLayout;

        viewModel.getCube().observe(getViewLifecycleOwner(), this::changeAllSquareColor);

        buttonSolveCube.setOnClickListener(a -> solveCube(viewModel.getCube().getValue()));
        buttonReset.setOnClickListener(this);
        buttonRandomize.setOnClickListener(a -> randomizeCube());

        squares = setOnClickListenerForSquares();
        setOnClickListenerForColorChangers();

        // only "cubeString" is stored in arguments
        if (getArguments() != null) {
            String cubeString = getArguments().getString("cubeString");
            setCubeByImage(cubeString);
        }

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void randomizeCube() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> viewModel.setCube(KociembaImpl.randomCube()));
    }

    private void setCubeByImage(String cubeString) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> viewModel.setCube(cubeString));
    }

    public void solveCube(String cube) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String result = KociembaImpl.solveCube(cube);
            if (KociembaImpl.verifyCube(cube)) {
                constraintLayout.post(() -> {
                    viewModel.setResult(result);
                    Navigation.findNavController(root)
                            .navigate(
                                    R.id.action_navigation_cube_to_navigation_solution,
                                    new Bundle(),
                                    new NavOptions.Builder().setPopUpTo(
                                            R.id.navigation_cube,
                                            true
                                    ).build()
                            );
                });
            } else {
                new Handler(Looper.getMainLooper())
                        .post(() -> Toast.makeText(
                                requireActivity(),
                                "Some squares on the cube are wrong. Check for mistakes.",
                                Toast.LENGTH_LONG).show()
                        );
            }
        });

    }

    @Override
    public void onClick(View view) {
        String viewName = view.getResources().getResourceName(view.getId()).split(":id/")[1];
        String RESET_NAME = "buttonReset";
        if (viewName.startsWith(SQUARE_NAME)) {
            onClickSquare(viewName);
        } else if (viewName.startsWith(COLOR_CHANGER_NAME)) {
            onClickColorChanger(viewName);
        } else if (viewName.equals(RESET_NAME)) {
            onClickReset();
        }
    }

    private void onClickSquare(String viewName) {
        int squareNumber = Integer.parseInt(viewName.split("square")[1]);
        changeCubeStringAtIndexWithChar(squareNumber, SquareInfo.SIDE_COLORS[selectedColor.ordinal()]);
    }

    private void onClickColorChanger(String viewName) {
        selectedColor = SquareInfo.Color.valueOf(viewName.split("colorChanger")[1].toUpperCase());
    }

    private void changeAllSquareColor(String cubeNew) {
        SquareInfo.Color color = SquareInfo.Color.EMPTY;
        for (int i = 0; i < cubeNew.length(); i++) {
            if (cubeOld.charAt(i) == cubeNew.charAt(i)) continue;
            switch (cubeNew.charAt(i)) {
                case 'U':
                    color = SquareInfo.Color.WHITE;
                    break;
                case 'R':
                    color = SquareInfo.Color.BLUE;
                    break;
                case 'F':
                    color = SquareInfo.Color.RED;
                    break;
                case 'D':
                    color = SquareInfo.Color.YELLOW;
                    break;
                case 'L':
                    color = SquareInfo.Color.GREEN;
                    break;
                case 'B':
                    color = SquareInfo.Color.ORANGE;
                    break;
                case 'E':
                    color = SquareInfo.Color.EMPTY;
                    break;
            }
            changeColor(squares[i], color);
        }
        cubeOld = viewModel.getCube().getValue();
    }

    /**
     * sets an onClickListener to every square on the screen
     *
     * @return returns the imagebuttons that have an onclicklistener on them
     */
    private ImageButton[] setOnClickListenerForSquares() {
        int SQUARE_COUNT = 54;
        ImageButton[] squares = new ImageButton[SQUARE_COUNT];
        for (int i = 0; i < squares.length; i++) {
            String squareName = SQUARE_NAME + i;
            try {
                squares[i] = (ImageButton) binding.getClass().getDeclaredField(squareName).get(binding);
                assert squares[i] != null;
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
     */
    private void setOnClickListenerForColorChangers() {
        SquareInfo.Color[] colors = SquareInfo.Color.values();
        for (SquareInfo.Color color : colors) {
            String colorChangerName = COLOR_CHANGER_NAME + stringToCapitalized(color.name());
            try {
                ImageButton currentButton = (ImageButton) (binding.getClass().getDeclaredField(colorChangerName).get(binding));
                assert currentButton != null;
                currentButton.setOnClickListener(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * changes the one of the cube's chars at the index with the provided char
     *
     * @param i  this is where the character will be changed
     * @param ch the is the character it will be changed with
     */
    private void changeCubeStringAtIndexWithChar(int i, char ch) {
        String cubeVal = viewModel.getCube().getValue();
        viewModel.setCube(cubeVal.substring(0, i) + ch + cubeVal.substring(i + 1));
    }

    /**
     * changes to color of a square to the Color
     *
     * @param view  square object
     * @param color Color
     */
    private void changeColor(View view, SquareInfo.Color color) {
        Context c = getContext();
        int imageColor = c.getResources().getIdentifier("cube_color_" + color.name().toLowerCase(), "drawable", c.getPackageName());
        ((ImageButton) view).setImageResource(imageColor);
    }

    /**
     * resets every square to its initial value
     */
    private void onClickReset() {
        for (int i = 0; i < squares.length; i++) {
            String CUBE_START_POSITION = "EEEEUEEEEEEEEREEEEEEEEFEEEEEEEEDEEEEEEEELEEEEEEEEBEEEE";
            viewModel.setCube(CUBE_START_POSITION);
        }
    }

    /**
     * Capitalizes a word.
     * FIRST -> First, second -> Second, tHr33 -> Thr33
     *
     * @param str word to capitalize
     * @return a capitalized word
     */
    private String stringToCapitalized(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}