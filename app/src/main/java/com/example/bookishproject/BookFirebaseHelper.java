package com.example.bookishproject;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

/*
A class to handle interactions with the firebase database.
Uses a DatabaseReference object
 */
public class BookFirebaseHelper {

    private DatabaseReference dbRef;

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
        String bookId = book.getApiId() != null ? book.getApiId() : dbRef.push().getKey();
        // the child of this bookId in the database will be the Book
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
        if (bookId != null) {
            dbRef.child(bookId).removeValue();
        }
    }

    /*
    Method to update a Book's attributes in the database
     */
    public void updateBook(Book book, final FirebaseCallback callback) {
        if (book != null && book.getApiId() != null) {
            dbRef.child(book.getApiId()).setValue(book)
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
    interface for a FirebaseCallback type-class
     */
    public interface FirebaseCallback {
        void onCallback(List<Book> bookList);
    }

}
