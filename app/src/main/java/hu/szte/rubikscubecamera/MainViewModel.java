package hu.szte.rubikscubecamera;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Contains all the variables which should be preserved between fragments.
 */
public class MainViewModel extends ViewModel {
    /**
     * Stores the result of the cube solving.
     */
    private final MutableLiveData<String> result = new MutableLiveData<>("");
    /**
     * Stores the cube which is displayed on the cubeFragment.
     */
    private final MutableLiveData<String> cube = new MutableLiveData<>("EEEEUEEEEEEEEREEEEEEEEFEEEEEEEEDEEEEEEEELEEEEEEEEBEEEE");

    public LiveData<String> getResult() {
        return result;
    }

    public void setResult(String res) {
        result.postValue(res);
    }

    public LiveData<String> getCube() {
        return cube;
    }

    public void setCube(String c) {
        cube.postValue(c);
    }

}
