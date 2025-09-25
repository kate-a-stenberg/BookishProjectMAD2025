package dev.kateastenberg.bookishproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dev.kateastenberg.bookishproject.helpers.firebase.JournalFirebaseHelper;
import dev.kateastenberg.bookishproject.intents.JournalIntent;
import dev.kateastenberg.bookishproject.models.Entry;

public class JournalViewModel extends ViewModel {

    private MutableLiveData<List<Entry>> entries = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> query = new MutableLiveData<>();
    private JournalFirebaseHelper fbHelper;

//    private StartBookUseCase startBookUseCase;
//    private FinishBookUseCase finishBookUseCase;
//    private UpdateReadingUseCase updateReadingUseCase;
//    private LoadBooksUseCase loadBooksUseCase;
//    private SearchBooksUseCase searchBooksUseCase;

    public JournalViewModel() {
        fbHelper = new JournalFirebaseHelper();
//        startBookUseCase = new StartBookUseCase();
//        finishBookUseCase = new FinishBookUseCase();
//        updateReadingUseCase = new UpdateReadingUseCase();
//        loadBooksUseCase = new LoadBooksUseCase();
//        searchBooksUseCase = new SearchBooksUseCase();
    }

    public void loadEntries() {
        isLoading.setValue(true);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("No current authenticated user");
            isLoading.setValue(false);
            return;
        }
        fbHelper.getAllEntries(userEntries -> {
            isLoading.setValue(false);
            entries.setValue(userEntries);
        });
    }

    public void searchEntries (String query) {
        isLoading.setValue(true);
        if (query == null || query.isEmpty()) {
            errorMessage.setValue("Cannot search for an empty query");
            isLoading.setValue(false);
            return;
        }

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("No current authenticated user");
            isLoading.setValue(false);
            return;
        }
        fbHelper.getEntries(userEntries -> {
            isLoading.setValue(false);
            entries.setValue(userEntries);
        }, query);
    }

    public void handleIntent (JournalIntent intent) {
        if (intent instanceof JournalIntent.LoadEntries) {
            loadEntries();
        }
        else if (intent instanceof JournalIntent.SearchEntries) {
            JournalIntent.SearchEntries searchIntent = (JournalIntent.SearchEntries) intent;
            searchEntries(searchIntent.query);
        }
    }

    // GETTERS

    public LiveData<List<Entry>> getEntries() {
        return entries;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<String> getQuery() {
        return query;
    }

}
