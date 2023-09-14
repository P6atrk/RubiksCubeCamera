package hu.szte.rubikscubecamera;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MainViewModel extends ViewModel {
    private final MutableLiveData<String> result = new MutableLiveData<>("");
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
