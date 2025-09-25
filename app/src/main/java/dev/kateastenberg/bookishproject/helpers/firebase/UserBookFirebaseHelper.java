package dev.kateastenberg.bookishproject.helpers.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.models.UserBook;

/*
This class represents a UserBookFirebaseHelper.
A UserBookFirebaseHelper interacts with the "user_books" branch of the Firebase database.
 */
public class UserBookFirebaseHelper {

    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    public UserBookFirebaseHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("user_books");
        auth = FirebaseAuth.getInstance();
    }

    /*
    Method to search UserBooks by a specified query
     */
    public void searchUserBooks(String userId, final FirebaseCallback callback, String searchTitle, String searchAuthor) {
        dbRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                List<UserBook> userBookList = new ArrayList<>();

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    UserBook userBook = snapshot.getValue(UserBook.class);
                    if (userBook != null) {
                        String title = userBook.getBook().getTitle();
                        String author = userBook.getBook().getAuthor();

                        boolean titleMatch;
                        boolean authorMatch;
                        if (!searchTitle.isEmpty() && !searchAuthor.isEmpty()) {
                            titleMatch = title != null && title.toLowerCase().contains(searchTitle.toLowerCase());
                            authorMatch = author != null && author.toLowerCase().contains(searchAuthor.toLowerCase());
                        }
                        else {
                            titleMatch = title != null && title.toLowerCase().contains(searchTitle.toLowerCase());
                            authorMatch = author != null && author.toLowerCase().contains(searchTitle.toLowerCase());
                        }

                        if (titleMatch || authorMatch) {
                            userBookList.add(userBook);
                        }
                    }
                }

                callback.onCallback(userBookList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Database error: " + error.getMessage());
            }
        });
    }

    /*
    Method to add a UserBook
     */
    public void addUserBook(UserBook userBook) {

        if (userBook == null) {
            System.out.println("Error: UserBook object is null");
            return;
        }

        String userId = userBook.getUserId();
        if (userId == null) {
            // Try to get current authenticated user
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
                userBook.setUserId(userId);
            } else {
                System.out.println("Error: No user ID provided and no authenticated user");
                return;
            }
        }

        String userBookID = userBook.getUserBookId();
        if (userBookID == null) {
            userBookID = dbRef.child(userId).push().getKey();
            userBook.setUserBookId(userBookID);
        }

        assert userBookID != null;
        dbRef.child(userId).child(userBookID).setValue(userBook);
    }

    /*
    Method to get all UserBooks for a specified user
     */
    public void getBooksForUser(String userId, final FirebaseCallback callback) {

        if (userId == null) {
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            } else {
                System.out.println("Error: No user ID provided and no authenticated user");
                return;
            }
        }

        final String finalUserID = userId;

        dbRef.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                List<UserBook> bookList = new ArrayList<>();

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    UserBook userBook = snapshot.getValue(UserBook.class);
                    if (userBook != null) {
                        userBook.setUserId(finalUserID);
                        if (userBook.getUserBookId() == null) {
                            userBook.setUserBookId(snapshot.getKey());
                        }
                        bookList.add(userBook);
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

    /*
    Method to update a UserBook
    What this does is actually takes a new UserBook object with the same userBookId as an existing
    UserBook and replaces is. This assumes the UserBook being passed as an argument has all the
    correct new information and should replace the UserBook with the outdated information that
    exists in the same spot.
     */
    public void updateUserBook(UserBook userBook, final FirebaseCallback callback) {
        if (userBook != null && userBook.getUserBookId() != null) {
            String userID = userBook.getUserId();

            if (userID == null) {
                if (auth.getCurrentUser() != null) {
                    userID = auth.getCurrentUser().getUid();
                    userBook.setUserId(userID);
                }
                else {
                    System.out.println("Error: No user ID provided and no authenticated user");
                    return;
                }
            }

            String userBookID = userBook.getUserBookId();
            if (userBookID == null) {
                System.out.println("Error: userBookID is required");
                return;
            }

            final String finalUserID = userID;

            dbRef.child(userID).child(userBookID).setValue(userBook).addOnSuccessListener(aVoid -> getBooksForUser(finalUserID, callback)).addOnFailureListener(e -> {
                System.out.println("Error updating book: " + e.getMessage());
                getBooksForUser(finalUserID, callback);
            });
        }
        else {
            System.out.println("Error: UserBook object is null");
        }
    }

    /*
    Method to remove a book from the user's collection
     */
    public void removeUserBook(String userID, String userBookId) {

        if (userID == null) {
            if (auth.getCurrentUser() != null) {
                userID = auth.getCurrentUser().getUid();
            } else {
                System.out.println("Error: No user ID provided and no authenticated user");
                return;
            }
        }

        if (userBookId == null) {
            System.out.println("Error: Book ID is required");
            return;
        }

        dbRef.child(userID).child(userBookId).removeValue();

    }

    /*
    Method to get all UserBooks for a specified user with a specified status
     */
    public void getBooksByStatus(String userId, String status, final FirebaseCallback callback) {

        if (userId == null) {
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            } else {
                System.out.println("Error: No user ID provided and no authenticated user");
                return;
            }
        }

        final String finalUserID = userId; // For use in the lambda

        dbRef.child(userId).orderByChild("status").equalTo(status).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                List<UserBook> bookList = new ArrayList<>();

                for (DataSnapshot snapshot : datasnapshot.getChildren()) {
                    UserBook userBook = snapshot.getValue(UserBook.class);
                    if (userBook != null) {
                        userBook.setUserId(finalUserID);
                        userBook.setUserBookId(snapshot.getKey());
                        bookList.add(userBook);
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

    /*
    Method to check whether a Book is in a User's UserBook collection
     */
    public void isBookInUserCollection(String userId, String bookId, ResultCallback<Boolean> callback) {

        if (userId == null) {
            if (auth.getCurrentUser() != null) {
                userId = auth.getCurrentUser().getUid();
            } else {
                Log.e("UserBookHelper", "Error: No user ID provided and no authenticated user");
                callback.onResult(false); // Call the callback with false
                return;
            }
        }

        dbRef.child(userId).orderByChild("bookId").equalTo(bookId).limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                callback.onResult(datasnapshot.exists() && datasnapshot.getChildrenCount() > 0);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("UserBookHelper", "Error checking book: " + databaseError.getMessage());
                callback.onResult(false);
            }
        });
    }

    /*
    interface for a FirebaseCallback type-class
    */
    public interface FirebaseCallback {
        void onCallback(List<UserBook> bookList);
    }

    // Callback interface
    public interface ResultCallback<T> {
        void onResult(T result);
    }

}
