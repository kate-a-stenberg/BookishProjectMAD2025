package dev.kateastenberg.bookishproject.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dev.kateastenberg.bookishproject.helpers.firebase.UserBookFirebaseHelper;
import dev.kateastenberg.bookishproject.interfaces.UseCaseCallback;
import dev.kateastenberg.bookishproject.models.UserBook;

public class LoadBooksUseCase {

    private UserBookFirebaseHelper ubfbHelper = new UserBookFirebaseHelper();

    public void execute(UseCaseCallback<List<UserBook>> callback) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onError("No authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ubfbHelper.getBooksForUser(userId, new UserBookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<UserBook> bookList) {
                callback.onSuccess(bookList);
            }
        });
    }

    public void execute(String status, UseCaseCallback<List<UserBook>> callback) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onError("No authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ubfbHelper.getBooksByStatus(userId, status, new UserBookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<UserBook> bookList) {
                callback.onSuccess(bookList);
            }
        });
    }

}
