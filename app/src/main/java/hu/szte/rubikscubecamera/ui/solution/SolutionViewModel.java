package hu.szte.rubikscubecamera.ui.solution;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<String> mText;

    public SolutionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is solution fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}