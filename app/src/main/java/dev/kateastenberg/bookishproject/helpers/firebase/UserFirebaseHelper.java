package dev.kateastenberg.bookishproject.helpers.firebase;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import dev.kateastenberg.bookishproject.models.User;

/*
This class represents a UserFirebaseHelper.
A UserFirebaseHelper interacts with the "users" path of the Firebase database.
 */
public class UserFirebaseHelper {

    private final DatabaseReference dbRef;
    private final FirebaseAuth auth;

    public UserFirebaseHelper() {
        dbRef = FirebaseDatabase.getInstance().getReference("users");
        auth = FirebaseAuth.getInstance();
    }

    public void createUser(User user, final UserCallback callback) {
        if (user == null) {
            callback.onError("User object is null");
            return;
        }

        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("No authenticated user found");
            return;
        }

        String userId = firebaseUser.getUid();
        user.setUserId(userId);

        dbRef.child(user.getUserId()).setValue(user)
                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                .addOnFailureListener(e -> callback.onError(e.getMessage()));

    }

    public void createUserFromAuth(final UserCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser == null) {
            callback.onError("No authenticated user");
            return;
        }

        dbRef.child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User existingUser = snapshot.getValue(User.class);
                    callback.onSuccess(existingUser);
                }
                else {
                    String username = firebaseUser.getDisplayName();
                    if (username == null || username.isEmpty()) {
                        username = "User" + firebaseUser.getUid().substring(0, 5);
                    }
                    User newUser = new User(
                            firebaseUser.getUid(),
                            username,
                            firebaseUser.getEmail()
                    );
                    createUser(newUser, callback);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });

    }

    public void getCurrentUser(final UserCallback callback) {
        FirebaseUser firebaseUser = auth.getCurrentUser();
        if (firebaseUser != null) {
            getUserById(firebaseUser.getUid(), callback);
        }
    }

    public void getUserById(String id, final UserCallback callback) {
        if (id != null) {
            dbRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        User user = snapshot.getValue(User.class);
                        callback.onSuccess(user);
                    }
                    else {
                        callback.onError("User not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
        else {
            callback.onError("User ID is required");
        }
    }

    public void updateUser(User user, final UserCallback callback) {
        if (user != null && user.getUserId() != null) {
            dbRef.child(user.getUserId()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        dbRef.child(user.getUserId()).setValue(user)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(user))
                                .addOnFailureListener(e -> callback.onError(e.getMessage()));
                    }
                    else {
                        callback.onError("User not found");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    callback.onError(error.getMessage());
                }
            });
        }
    }

    public void deleteUser(String userId, final VoidCallback callback) {
        if (userId != null) {
            dbRef.child(userId).removeValue()
                    .addOnSuccessListener(aVoid -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onError(e.getMessage()));
        }
    }

    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    public interface VoidCallback {
        void onSuccess();
        void onError(String error);
    }

}
