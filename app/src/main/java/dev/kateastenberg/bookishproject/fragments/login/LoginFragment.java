package dev.kateastenberg.bookishproject.fragments.login;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import dev.kateastenberg.bookishproject.activities.LoginActivity;
import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.databinding.FragmentLoginBinding;
import dev.kateastenberg.bookishproject.intents.AuthIntent;
import dev.kateastenberg.bookishproject.viewmodels.AuthViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a LoginFragment.
A LoginFragment enables a user to enter their information to log in to their account, navigate to
RegisterFragment, or navigate to ForgotPasswordFragment.
 */
public class LoginFragment extends Fragment {

    private EditText emailInput, passwordInput;
    private TextInputLayout emailInputLayout, passwordInputLayout;
    private Button login, forgotPassword, register;
    private ProgressBar progressBar;
    private CheckBox rememberMe;
    private AuthViewModel avm;
    private String email;
    private String password;
    private PublishSubject<AuthIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;

    public LoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentLoginBinding binding = FragmentLoginBinding.inflate(inflater, container, false);

        emailInput = binding.emailInput;
        passwordInput = binding.passwordInput;
        emailInputLayout = binding.emailInputLayout;
        passwordInputLayout = binding.passwordInputLayout;
        login = binding.loginButton;
        progressBar = binding.progressBar;
        rememberMe = binding.rememberMeCheckbox;
        forgotPassword = binding.forgotPasswordButton;
        register = binding.registerButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avm = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        login.setOnClickListener(v -> {
            email = emailInput.getText().toString().trim();
            password = passwordInput.getText().toString().trim();

            emailInputLayout.setError(null);
            passwordInputLayout.setError(null);

            if (validateEmail() && validatePassword()) {
                processIntent(new AuthIntent.Login(email, password));
            }
        });
        forgotPassword.setOnClickListener(v -> {
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).navigateToForgotPassword();
            }
        });
        register.setOnClickListener(v -> {
            if (getActivity() instanceof LoginActivity) {
                ((LoginActivity) getActivity()).navigateToRegister();
            }
        });

        addErrorClearingTextWatcher(emailInput, emailInputLayout);
        addErrorClearingTextWatcher(passwordInput, passwordInputLayout);
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
    Method to validate the user's email
    Ensures it is present and matches an email address pattern
     */
    private boolean validateEmail() {

        if (email.isEmpty()) {
            emailInputLayout.setError("Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInputLayout.setError("Please enter a valid email address");
            return false;
        } else {
            emailInputLayout.setError(null); // Clear error
            return true;
        }
    }

    /*
    Method to validate a user's password
    Ensures the password is present and long enough
     */
    private boolean validatePassword() {

        if (password.isEmpty()) {
            passwordInputLayout.setError("Password cannot be empty");
            return false;
        } else if (password.length() < 6) {
            passwordInputLayout.setError("Password must be at least 6 characters");
            return false;
        } else {
            passwordInputLayout.setError(null); // Clear error
            return true;
        }
    }

    /*
    Method to clear an error from a text input layout
     */
    private void addErrorClearingTextWatcher(EditText input, TextInputLayout inputLayout) {
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length() > 0) {
                    inputLayout.setError(null);
                }
            }
        });
    }

    /*
    Method to save the user's login credentials
     */
    private void saveCredentials() {
        try {
            // create a master key to secure all data in shared prefs
            // using AES256 algorithm using GCM
            MasterKey masterKey = new MasterKey.Builder(requireContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // creates an encrypted SharedPrefs called auth_prefs
            // using the master key to secure it
            SharedPreferences sharedPrefs = EncryptedSharedPreferences.create(requireContext(),
                    "auth_prefs", masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // adds the email, password, and remember_me status to sharedPrefs
            sharedPrefs.edit()
                    .putString("email", email)
                    .putString("password", password)
                    .putBoolean("remember_me", true)
                    .apply();
        }
        catch (Exception e) {
            Log.e("LoginFragment", "Error saving credentials: " + e.getMessage());
        }
    }

    /**
     * Clears any saved login credentials from secure storage
     */
    private void clearSavedCredentials() {
        try {
            // Create or retrieve the encrypted shared preferences
            MasterKey masterKey = new MasterKey.Builder(requireContext())
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // creates an encrypted SharedPrefs called auth_prefs
            // using the master key to secure it
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    requireContext(),
                    "auth_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            // Remove all saved credentials
            sharedPreferences.edit()
                    .remove("email")
                    .remove("password")
                    .putBoolean("remember_me", false)
                    .apply();

        } catch (Exception e) {
            Log.e("Authentication", "Error clearing credentials: " + e.getMessage());
        }
    }

    private void observeViewModel() {
        avm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                login.setVisibility(View.GONE);
                rememberMe.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
            }
            else {
                login.setVisibility(View.VISIBLE);
                rememberMe.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
        avm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        avm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success.contains("Login successful")) {
                    if (rememberMe.isChecked()) {
                        saveCredentials();
                    }
                    else {
                        clearSavedCredentials();
                    }
                    if (getActivity() != null) {
                        Intent intent = new Intent(getActivity(), MainActivity.class);

                        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity());
                        startActivity(intent, options.toBundle());
                        getActivity().finish();
                    }
                }
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> avm.handleIntent(intent));
    }

    private void processIntent(AuthIntent intent) {
        intentSubject.onNext(intent);
    }

}