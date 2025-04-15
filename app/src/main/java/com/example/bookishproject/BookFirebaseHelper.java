package com.example.bookishproject;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;

public class BookFirebaseHelper {

    private static final String TAG = "BooksFirebaseHelper"; // For logging

    private DatabaseReference dbRef;

    public BookFirebaseHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("books");
    }

    public void addBook(Book book) {
        String bookId = book.getApiId() != null ? book.getApiId() : dbRef.push().getKey();
        dbRef.child(bookId).setValue(book);
    }

    public void getAllBooks(final FirebaseCallback callback) {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                List<Book> bookList = new ArrayList<>();

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null) {
                        bookList.add(book);
                    }
                }

                callback.onCallback(bookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                System.out.println("Database error: " + databaseError.getMessage());
            }
        });
    }

    public void deleteBook(String bookId) {
        if (bookId != null) {
            dbRef.child(bookId).removeValue();
        }
    }

    public void updateBook(Book book, final FirebaseCallback callback) {
        if (book != null && book.getApiId() != null) {
            dbRef.child(book.getApiId()).setValue(book)
                    .addOnSuccessListener(aVoid -> {
                        // Success - fetch all books again to ensure data consistency
                        getAllBooks(callback);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("BookFirebaseHelper", "Error updating book", e);
                        // You might want to handle the error case differently
                        getAllBooks(callback);
                    });
        } else {
            // Handle the case where book or apiId is null
            Log.e("BookFirebaseHelper", "Book or apiId is null");
            getAllBooks(callback); // Still call the callback to avoid blocking UI
        }
    }

    public void getBooksWithCategory(String category, final FirebaseCallback callback) {
        Log.d(TAG, "Filtering books by category: " + category);

        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<Book> filteredBooks = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Book book = snapshot.getValue(Book.class);
                    if (book != null && book.getCategories() != null) {
                        // Check if any category contains our search term (case-insensitive)
                        for (String bookCategory : book.getCategories()) {
                            if (bookCategory.toLowerCase().contains(category.toLowerCase())) {
                                filteredBooks.add(book);
                                break; // Found a match, no need to check other categories
                            }
                        }
                    }
                }

                callback.onCallback(filteredBooks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
            }
        });
    }

    public interface FirebaseCallback {
        void onCallback(List<Book> bookList);
    }

}
