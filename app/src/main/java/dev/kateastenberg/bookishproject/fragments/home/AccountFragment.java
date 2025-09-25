package dev.kateastenberg.bookishproject.fragments.home;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import dev.kateastenberg.bookishproject.activities.LoginActivity;
import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.databinding.FragmentAccountBinding;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.intents.AuthIntent;
import dev.kateastenberg.bookishproject.models.User;
import dev.kateastenberg.bookishproject.viewmodels.AuthViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents an AccountFragment.
An AccountFragment displays the user's information and allows them to update their information.
 */
public class AccountFragment extends Fragment {

    private FragmentAccountBinding binding;
    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private Button saveButton, editButton, logoutButton, deleteButton;
    private User currentUser;
    private AuthViewModel avm;
    private PublishSubject<AuthIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false);

        usernameInput = binding.usernameInput;
        emailInput = binding.emailInput;
        passwordInput = binding.newPasswordInput;
        confirmPasswordInput = binding.confirmInput;
        usernameLayout = binding.usernameLayout;
        emailLayout = binding.emailLayout;
        passwordLayout = binding.passwordLayout;
        confirmPasswordLayout = binding.confirmPasswordLayout;
        saveButton = binding.saveButton;
        editButton = binding.editButton;
        logoutButton = binding.logoutButton;
        deleteButton = binding.deleteButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avm = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        processIntent(new AuthIntent.LoadUserData());

        saveButton.setVisibility(View.GONE);
        editButton.setVisibility(View.VISIBLE);

        processIntent(new AuthIntent.SetViewOnly());

        editButton.setOnClickListener(v -> {
            processIntent (new AuthIntent.SetEditable());
        });
        saveButton.setOnClickListener(v -> {
            saveUserData();
            processIntent (new AuthIntent.SetViewOnly());
        });
        logoutButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out?")
                .setPositiveButton("Sign Out", (dialog, which) -> processIntent (new AuthIntent.Logout()))
                .setNegativeButton("Cancel", null)
                .show());
        deleteButton.setOnClickListener(v -> new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete user")
                .setMessage("Are you sure you want to delete this user? This action cannot be undone.")
                .setPositiveButton("Delete user", (dialog, which) -> {
                    processIntent(new AuthIntent.DeleteUser(currentUser));
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .show());

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }
    }

    @Override
    public void onDestroy() {
        // Dispose to prevent memory leaks
        if (intentDisposable != null && !intentDisposable.isDisposed()) {
            intentDisposable.dispose();
        }

        if (intentSubject != null && !intentSubject.hasComplete()) {
            intentSubject.onComplete();
        }
        super.onDestroy();
    }

    /*
    Method to load user's data into the UI
     */
    private void loadUserData() {

        usernameInput.setText(currentUser.getUserName());
        emailInput.setText(currentUser.getEmail());

        if (currentUser.getUserName() == null || currentUser.getUserName().isEmpty()) {
            usernameInput.setHint("Enter your username");
        }
    }

    /*
    Method to save updated user data
     */
    private void saveUserData() {

        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (username.isEmpty()) {
            usernameInput.setError("Username cannot be empty");
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            return;
        }

        // Check if password fields are filled and match
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                binding.newPasswordInput.setError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                binding.confirmInput.setError("Passwords don't match");
                return;
            }

            // Update password
            processIntent(new AuthIntent.ChangePassword(password));
        }

        // Update user profile
        processIntent(new AuthIntent.UpdateUser(currentUser, username, email));

    }

    /*
    Method to set input fields enabled or disabled
     */
    private void setEditable(boolean value) {
        // EditText fields
        usernameInput.setFocusable(value);
        usernameInput.setFocusableInTouchMode(value);
        usernameInput.setCursorVisible(value);      // Hide/show cursor
        usernameInput.setLongClickable(value);      // Prevent long-click actions

        emailInput.setFocusable(value);
        emailInput.setFocusableInTouchMode(value);
        emailInput.setCursorVisible(value);
        emailInput.setLongClickable(value);

        passwordInput.setFocusable(value);
        passwordInput.setFocusableInTouchMode(value);
        passwordInput.setCursorVisible(value);
        passwordInput.setLongClickable(value);

        confirmPasswordInput.setFocusable(value);
        confirmPasswordInput.setFocusableInTouchMode(value);
        confirmPasswordInput.setCursorVisible(value);
        confirmPasswordInput.setLongClickable(value);

        // TextInputLayout needs to be clickable too
        usernameLayout.setClickable(value);
        emailLayout.setClickable(value);
        passwordLayout.setClickable(value);
        confirmPasswordLayout.setClickable(value);

        // When disabling, clear focus from all fields
        if (!value) {
            usernameInput.clearFocus();
            emailInput.clearFocus();
            passwordInput.clearFocus();
            confirmPasswordInput.clearFocus();
            editButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.GONE);
        }
        if (value) {
            editButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }

    private void observeViewModel() {

        avm.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                loadUserData();
            }
        });
        avm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        avm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success.contains("Account deleted successfully")) {
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
                        startActivity(intent, options.toBundle());
                        getActivity().finish();
                    }
                }
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> {
           if (intent instanceof AuthIntent.SetEditable) {
                setEditable(true);
           }
           else if (intent instanceof AuthIntent.SetViewOnly) {
                setEditable(false);
           }
           else {
               avm.handleIntent(intent);
           }
        });

    }

    private void processIntent(AuthIntent intent) {
        intentSubject.onNext(intent);
    }

}