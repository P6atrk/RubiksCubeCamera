package hu.szte.rubikscubecamera.ui.cube;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import hu.szte.rubikscubecamera.utils.KociembaImpl;

public class CubeViewModel extends ViewModel {
    private final MutableLiveData<String> textCube;

    public CubeViewModel() {
        textCube = new MutableLiveData<>();
    }

    public void solveRandomCube() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(() -> {
            String result = KociembaImpl.solveCubeRandom();
            textCube.postValue(result);
        });
    }

    public LiveData<String> getTextCube() {
        return textCube;
    }
}