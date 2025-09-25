package dev.kateastenberg.bookishproject.usecases;

import java.util.List;

import dev.kateastenberg.bookishproject.helpers.firebase.JournalFirebaseHelper;
import dev.kateastenberg.bookishproject.helpers.firebase.UserBookFirebaseHelper;
import dev.kateastenberg.bookishproject.interfaces.UseCaseCallback;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.models.UserBook;

public class AbandonBookUseCase {

    private JournalFirebaseHelper jfbHelper = new JournalFirebaseHelper();
    private UserBookFirebaseHelper ubfbHelper = new UserBookFirebaseHelper();

    public void execute (Entry entry, UseCaseCallback<UserBook> callback) {

        UserBook userBook = entry.getUserBook();

        // set UserBook status
        userBook.setStatus("Abandoned");

        jfbHelper.addEntry(entry, new JournalFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Entry> entries) {
                ubfbHelper.updateUserBook(userBook, new UserBookFirebaseHelper.FirebaseCallback() {
                    @Override
                    public void onCallback(List<UserBook> books) {
                        callback.onSuccess(userBook);
                    }
                });
            }
        });

    }

}
