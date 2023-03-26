package hu.szte.rubikscubecamera.ui.cube;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CubeViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public CubeViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is cube fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}