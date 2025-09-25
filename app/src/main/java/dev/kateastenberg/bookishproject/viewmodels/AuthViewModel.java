package dev.kateastenberg.bookishproject.viewmodels;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import dev.kateastenberg.bookishproject.helpers.firebase.UserFirebaseHelper;
import dev.kateastenberg.bookishproject.intents.AuthIntent;
import dev.kateastenberg.bookishproject.models.User;

public class AuthViewModel extends ViewModel {

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    private UserFirebaseHelper ufbHelper = new UserFirebaseHelper();
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private void login(String email, String password) {
        isLoading.setValue(true);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
           if (task.isSuccessful()) {
                ufbHelper.createUserFromAuth(new UserFirebaseHelper.UserCallback() {
                    @Override
                    public void onSuccess(User user) {
                        isLoading.setValue(false);
                        successMessage.setValue("Login successful");
                    }

                    @Override
                    public void onError(String error) {
                        isLoading.setValue(false);
                        errorMessage.setValue("Error accessing profile: " + error);
                    }
                });
           }
           else {
               isLoading.setValue(false);
               errorMessage.setValue(getString(task));
           }
        });
    }

    /*
    Method to get an error message from a failed task
     */
    @Nullable
    private static String getString(Task<AuthResult> task) {
        Exception exception = task.getException();
        String errorMessage = "Authentication failed";

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email address";
        }
        else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid email or password";
        }
        else if (exception instanceof FirebaseNetworkException) {
            errorMessage = "Network error. Please check your connection";
        }
        else if (exception != null) {
            errorMessage = exception.getMessage();
        }
        return errorMessage;
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
    }

    private void deleteUser (User user) {

        isLoading.setValue(true);

        ufbHelper.deleteUser(user.getUserId(), new UserFirebaseHelper.VoidCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser firebaseUser = auth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.delete().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            successMessage.setValue("Account deleted successfully");
                        }
                        else {
                            if (task.getException() != null) {
                                errorMessage.setValue("Error deleting authentication: " + task.getException().getMessage());
                            }
                        }
                    });
                }
                else {
                    errorMessage.setValue("No authenticated user");
                }
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("Error deleting user: " + error);
                isLoading.setValue(false);
            }
        });

    }

    public void loadUserData() {

        isLoading.setValue(true);

        ufbHelper.getCurrentUser(new UserFirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                currentUser.setValue(user);
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue("unable to retrieve user information");
                isLoading.setValue(false);
            }
        });
    }

    private void updateUser(User user, String name, String email) {
        isLoading.setValue(true);

        user.setEmail(email);
        user.setUserName(name);

        ufbHelper.updateUser(user, new UserFirebaseHelper.UserCallback() {
            @Override
            public void onSuccess(User user) {
                successMessage.setValue("User data successfully updated!");
                isLoading.setValue(false);
            }

            @Override
            public void onError(String error) {
                errorMessage.setValue(error);
                isLoading.setValue(false);
            }
        });
    }

    private void changePassword(String password) {
        isLoading.setValue(true);

        FirebaseUser fbUser = auth.getCurrentUser();
        if (fbUser != null) {
            fbUser.updatePassword(password).addOnCompleteListener(task -> {
                isLoading.setValue(false);
                if (task.isSuccessful()) {
                    successMessage.setValue("Password updated successfully");
                }
                else {
                    if (task.getException() != null) {
                        errorMessage.setValue("Error updating password: " + task.getException());
                    }
                    else {
                        errorMessage.setValue("Failed to update password");
                    }
                }
            });
        }

        else {
            isLoading.setValue(false);
            errorMessage.setValue("No authenticated user");
        }
    }

    private void register (String email, String username, String password) {

        isLoading.setValue(true);

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser fbUser = auth.getCurrentUser();
                if (fbUser != null) {
                    fbUser.updateProfile(new UserProfileChangeRequest.Builder().setDisplayName(username).build()).addOnCompleteListener(profileTask -> {
                        ufbHelper.createUserFromAuth(new UserFirebaseHelper.UserCallback() {
                            @Override
                            public void onSuccess(User user) {
                                successMessage.setValue("Registration successful");
                                isLoading.setValue(false);
                            }

                            @Override
                            public void onError(String error) {
                                errorMessage.setValue("Registration failed: " + error);
                                isLoading.setValue(false);
                            }
                        });
                    });
                }
            }
            else {
                errorMessage.setValue("Registration failed");
                isLoading.setValue(false);
            }
        });
    }

    public void resetPassword(String email) {
        isLoading.setValue(true);
        auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                isLoading.setValue(false);
                successMessage.setValue("Password reset email sent");
            }
            else {
                if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                    isLoading.setValue(false);
                    successMessage.setValue("Password reset email sent");
                }
                else if (task.getException() instanceof FirebaseNetworkException) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Network error. Please check your connection");
                }
                else if (task.getException() != null) {
                    isLoading.setValue(false);
                    errorMessage.setValue("Failed to send reset email. Error: " + task.getException());
                }
            }
        });
    }

    public void handleIntent(AuthIntent intent) {
        if (intent instanceof AuthIntent.Login) {
            AuthIntent.Login loginIntent = (AuthIntent.Login) intent;
            login(loginIntent.email, loginIntent.password);
        }
        else if (intent instanceof AuthIntent.Logout) {
            logout();
        }
        else if (intent instanceof AuthIntent.DeleteUser) {
            AuthIntent.DeleteUser deleteIntent = (AuthIntent.DeleteUser) intent;
            deleteUser(deleteIntent.user);
        }
        else if (intent instanceof AuthIntent.ResetPassword) {
            AuthIntent.ResetPassword resetIntent = (AuthIntent.ResetPassword) intent;
            resetPassword(resetIntent.email);
        }
        else if (intent instanceof AuthIntent.Register) {
            AuthIntent.Register registerIntent = (AuthIntent.Register) intent;
            register(registerIntent.email, registerIntent.username, registerIntent.password);
        }
        else if (intent instanceof AuthIntent.UpdateUser) {
            AuthIntent.UpdateUser updateIntent = (AuthIntent.UpdateUser) intent;
            updateUser(updateIntent.user, updateIntent.name, updateIntent.email);
        }
        else if (intent instanceof AuthIntent.ChangePassword) {
            AuthIntent.ChangePassword changeIntent = (AuthIntent.ChangePassword) intent;
            changePassword(changeIntent.password);
        }
        else if (intent instanceof AuthIntent.LoadUserData) {
            loadUserData();
        }

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
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }
    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

}
