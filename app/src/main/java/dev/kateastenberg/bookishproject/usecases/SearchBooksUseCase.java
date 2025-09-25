package dev.kateastenberg.bookishproject.usecases;

import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import dev.kateastenberg.bookishproject.helpers.firebase.UserBookFirebaseHelper;
import dev.kateastenberg.bookishproject.interfaces.UseCaseCallback;
import dev.kateastenberg.bookishproject.models.UserBook;

public class SearchBooksUseCase {

    private UserBookFirebaseHelper ubfbHelper = new UserBookFirebaseHelper();

    public void execute (String title, String author, UseCaseCallback<List<UserBook>> callback) {

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            callback.onError("No current authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        ubfbHelper.searchUserBooks(userId, new UserBookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<UserBook> bookList) {
                callback.onSuccess(bookList);
            }
        }, title, author);
    }

}
