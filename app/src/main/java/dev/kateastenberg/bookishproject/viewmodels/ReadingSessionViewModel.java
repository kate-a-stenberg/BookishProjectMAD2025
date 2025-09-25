package dev.kateastenberg.bookishproject.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.kateastenberg.bookishproject.intents.ReadingSessionIntent;
import dev.kateastenberg.bookishproject.interfaces.UseCaseCallback;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.usecases.AbandonBookUseCase;
import dev.kateastenberg.bookishproject.usecases.FinishBookUseCase;
import dev.kateastenberg.bookishproject.usecases.LoadBooksUseCase;
import dev.kateastenberg.bookishproject.usecases.SearchBooksUseCase;
import dev.kateastenberg.bookishproject.usecases.StartBookUseCase;
import dev.kateastenberg.bookishproject.usecases.UpdateReadingUseCase;

public class ReadingSessionViewModel extends ViewModel {

    private MutableLiveData<List<UserBook>> books = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> query = new MutableLiveData<>();

    private StartBookUseCase startBookUseCase;
    private FinishBookUseCase finishBookUseCase;
    private AbandonBookUseCase abandonBookUseCase;
    private UpdateReadingUseCase updateReadingUseCase;
    private LoadBooksUseCase loadBooksUseCase;
    private SearchBooksUseCase searchBooksUseCase;

    public ReadingSessionViewModel() {
        startBookUseCase = new StartBookUseCase();
        finishBookUseCase = new FinishBookUseCase();
        abandonBookUseCase = new AbandonBookUseCase();
        updateReadingUseCase = new UpdateReadingUseCase();
        loadBooksUseCase = new LoadBooksUseCase();
        searchBooksUseCase = new SearchBooksUseCase();
    }

    public void loadBooks() {
        isLoading.setValue(true);
        loadBooksUseCase.execute(new UseCaseCallback<>() {
            @Override
            public void onSuccess(List<UserBook> userBooks) {
                isLoading.setValue(false);
                books.setValue(userBooks);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void loadBooks(String status) {
        isLoading.setValue(true);
        loadBooksUseCase.execute(status, new UseCaseCallback<List<UserBook>>() {
            @Override
            public void onSuccess(List<UserBook> userBooks) {
                isLoading.setValue(false);
                books.setValue(userBooks);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void openBook (Entry entry) {
        isLoading.setValue(true);

        startBookUseCase.execute(entry, new UseCaseCallback<UserBook>() {
            @Override
            public void onSuccess(UserBook result) {
                isLoading.setValue(false);
                loadBooks();
                successMessage.setValue(result.getBook().getTitle() + " added to your current reads!");
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });

    }

    public void updateReading(Entry entry) {
        isLoading.setValue(true);
        updateReadingUseCase.execute(entry, new UseCaseCallback<>() {
            @Override
            public void onSuccess(UserBook result) {
                isLoading.setValue(false);
                loadBooks();
                successMessage.setValue("New journal entry created!");
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void closeBook(Entry entry) {
        isLoading.setValue(true);
        finishBookUseCase.execute(entry, new UseCaseCallback<>() {
            @Override
            public void onSuccess(UserBook result) {
                isLoading.setValue(false);
                loadBooks();
                successMessage.setValue("New journal entry created!");
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void abandonBook(Entry entry) {
        isLoading.setValue(true);
        abandonBookUseCase.execute(entry, new UseCaseCallback<>() {
            @Override
            public void onSuccess(UserBook result) {
                isLoading.setValue(false);
                loadBooks();
                successMessage.setValue("New journal entry created!");
            }
            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void searchBooks(String title, String author) {
        isLoading.setValue(true);
        searchBooksUseCase.execute(title, author, new UseCaseCallback<>() {
            @Override
            public void onSuccess(List<UserBook> result) {
                isLoading.setValue(false);
                books.setValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    public void handleIntent(ReadingSessionIntent intent) {
        if (intent instanceof ReadingSessionIntent.OpenBook) {
            ReadingSessionIntent.OpenBook openIntent = (ReadingSessionIntent.OpenBook) intent;
            openBook(openIntent.entry);
        }
        else if (intent instanceof ReadingSessionIntent.UpdateReading) {
            ReadingSessionIntent.UpdateReading updateIntent = (ReadingSessionIntent.UpdateReading) intent;
            updateReading(updateIntent.entry);
        }
        else if (intent instanceof ReadingSessionIntent.CloseBook) {
            ReadingSessionIntent.CloseBook closeIntent = (ReadingSessionIntent.CloseBook) intent;
            closeBook(closeIntent.entry);
        }
        else if (intent instanceof ReadingSessionIntent.AbandonBook) {
            ReadingSessionIntent.AbandonBook abandonIntent = (ReadingSessionIntent.AbandonBook) intent;
            abandonBook(abandonIntent.entry);
        }
        else if (intent instanceof ReadingSessionIntent.SearchBooks) {
            ReadingSessionIntent.SearchBooks searchIntent = (ReadingSessionIntent.SearchBooks) intent;
            searchBooks(searchIntent.title, searchIntent.author);
        }
        else if (intent instanceof ReadingSessionIntent.LoadBooks) {
            ReadingSessionIntent.LoadBooks loadIntent = (ReadingSessionIntent.LoadBooks) intent;
            if (loadIntent.status != null) {
                loadBooks(loadIntent.status);
            }
            else {
                loadBooks();
            }
        }
    }


    // GETTERS

    public LiveData<List<UserBook>> getBooks() {
        return books;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

}
