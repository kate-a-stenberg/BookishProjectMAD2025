package dev.kateastenberg.bookishproject.helpers.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.models.Book;

/*
A class to handle interactions with the firebase database.
Uses a DatabaseReference object
 */
public class BookFirebaseHelper {

    private final DatabaseReference dbRef;

    /*
    No-argument constructor
     */
    public BookFirebaseHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("books");
    }

    /*
    Add a new Book to the database
     */
    public void addBook(Book book) {
        // create a bookId
        // if a book has an ApiId, push it to the database, get the key, and call it the bookId
        String bookId = book.getId() != null ? book.getId() : dbRef.push().getKey();
        // the child of this bookId in the database will be the Book
        assert bookId != null;
        dbRef.child(bookId).setValue(book);
    }

    /*
    Method to get all the books from the database using a callback argument
     */
    public void getAllBooks(final FirebaseCallback callback) {
        // Add a new ValueEventListener
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                // create a new ArrayList of Books
                List<Book> bookList = new ArrayList<>();

                // go through all data points in the database
                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    // create a Book out of that data
                    Book book = snapshot.getValue(Book.class);
                    // if the book isn't null, add it to the book list
                    if (book != null) {
                        bookList.add(book);
                    }
                }

                // give the book list to the app
                callback.onCallback(bookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    /*
    Method to delete a book from the database
     */
    public void deleteBook(String bookId) {
        if (!isAdminUser()) {
            System.out.println("You do not have permission to do this");
            return;
        }
        if (bookId != null) {
            dbRef.child(bookId).removeValue();
        }
    }

    /*
    Method to update a Book's attributes in the database
     */
    public void updateBook(Book book, final FirebaseCallback callback) {
        if (book != null && book.getId() != null) {
            dbRef.child(book.getId()).setValue(book)
                    .addOnSuccessListener(aVoid -> {
                        // Load all books again to ensure data consistency
                        getAllBooks(callback);
                    })
                    .addOnFailureListener(e -> {
                        // but still load all books again
                        getAllBooks(callback);
                    });
        } else {
            // if the book ApiId is null
            getAllBooks(callback); // Still call the callback to avoid blocking UI
        }
    }

    /*
    Method to get books matching a certain query
     */
    public void getBooks(final FirebaseCallback callback, String searchTitle, String searchAuthor) {

        // Add a new ValueEventListener
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                // create a new ArrayList of Books
                List<Book> bookList = new ArrayList<>();

                // go through all data points in the database
                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    // create a Book out of that data
                    Book book = snapshot.getValue(Book.class);
                    // if the book isn't null, add it to the book list
                    if (book != null) {
                        String title = book.getTitle();
                        String author = book.getAuthor();
                        boolean titleMatch = title != null && title.toLowerCase().contains(searchTitle.toLowerCase());
                        boolean authorMatch = author != null && author.toLowerCase().contains(searchAuthor.toLowerCase());

                        if (titleMatch || authorMatch) {
                            bookList.add(book);
                        }
                    }
                }

                // give the book list to the app
                callback.onCallback(bookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    /*
    Method to check if a Book is in the universal collection
     */
    public void isBookInCollection(String apiId, ResultCallback<Boolean> callback) {
        if (apiId == null) {
            callback.onResult(false);
            return;
        }

        dbRef.child(apiId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                callback.onResult(dataSnapshot.exists());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("BookHelper", "Error checking book: " + error.getMessage());
                callback.onResult(false);
            }
        });
    }

    /*
    interface for a FirebaseCallback type-class
     */
    public interface FirebaseCallback {
        void onCallback(List<Book> bookList);
    }

    // Callback interface
    public interface ResultCallback<T> {
        void onResult(T result);
    }

    /*
    Method to determine if the current user is an admin user or not
    Note: use positive form for clarity despite usually being used in negative
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAdminUser() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            return false;
        }
        return "kate.a.stenberg@gmail.com".equals(currentUser.getEmail());
    }

}
