package hu.szte.rubikscubecamera.ui.solution;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SolutionViewModel extends ViewModel {
    private final MutableLiveData<String> mText;
    private String solution = ""; // TODO: osszekotni a megoldassal valahogy

    public SolutionViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue(solution);
    }

    public LiveData<String> getText() {
        return mText;
    }
}