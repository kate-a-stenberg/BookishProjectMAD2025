package dev.kateastenberg.bookishproject.fragments.login;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.databinding.FragmentRegisterBinding;
import dev.kateastenberg.bookishproject.intents.AuthIntent;
import dev.kateastenberg.bookishproject.viewmodels.AuthViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a RegisterFragment.
A RegisterFragment allows a user to create a new user account.
 */
public class RegisterFragment extends Fragment {

    private EditText emailInput, nameInput, passwordInput, confirmPasswordInput;
    private TextInputLayout usernameInputLayout, emailInputLayout, passwordInputLayout, confirmPasswordInputLayout;
    private LinearLayout progressLayout, registerLayout;
    private Button register;
    private AuthViewModel avm;
    private PublishSubject<AuthIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;

    public RegisterFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        dev.kateastenberg.bookishproject.databinding.FragmentRegisterBinding binding = FragmentRegisterBinding.inflate(inflater, container, false);

        emailInput = binding.emailInput;
        passwordInput = binding.passwordInput;
        confirmPasswordInput = binding.confirmPasswordInput;
        nameInput = binding.usernameInput;
        usernameInputLayout = binding.nameInputLayout;
        emailInputLayout = binding.emailInputLayout;
        passwordInputLayout = binding.passwordInputLayout;
        confirmPasswordInputLayout = binding.confirmPasswordInputLayout;
        progressLayout = binding.progressLayout;
        registerLayout = binding.registerLayout;
        register = binding.registerButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avm = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        progressLayout.setVisibility(View.GONE);
        registerLayout.setVisibility(View.VISIBLE);

        register.setOnClickListener(v -> {

            String email = emailInput.getText().toString().trim();
            String username = nameInput.getText().toString().trim();
            String password = passwordInput.getText().toString();

            if (validateEmail() && validateUsername() && validatePassword() && validateConfirmPassword()) {
                processIntent(new AuthIntent.Register(email, username, password));
            }
            else {
                Toast.makeText(getContext(), "Please enter valid information", Toast.LENGTH_SHORT).show();
            }
        });

        addErrorClearingTextWatcher(nameInput, usernameInputLayout);
        addErrorClearingTextWatcher(emailInput, emailInputLayout);
        addErrorClearingTextWatcher(passwordInput, passwordInputLayout);
        addErrorClearingTextWatcher(confirmPasswordInput, confirmPasswordInputLayout);

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
    Ensures the email is present and follows an email pattern
     */
    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim();

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
    Method to validate a username
    Ensures the username is present
     */
    private boolean validateUsername() {
        String name = nameInput.getText().toString().trim();

        if (name.isEmpty()) {
            usernameInputLayout.setError("Name cannot be empty");
            return false;
        } else {
            usernameInputLayout.setError(null); // Clear error
            return true;
        }
    }

    /*
    Method to validate a user's password
    Ensures the user's password is present and long enough
     */
    private boolean validatePassword() {
        String password = passwordInput.getText().toString();

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
    Method to validate the user's confirmed password
    Ensures the confirmed password matches the password
     */
    private boolean validateConfirmPassword() {
        String confirmPassword = confirmPasswordInput.getText().toString();

        if (!confirmPassword.equals(passwordInput.getText().toString())) {
            confirmPasswordInputLayout.setError("Passwords must match");
            return false;
        } else {
            confirmPasswordInputLayout.setError(null); // Clear error
            return true;
        }
    }

    /*
    Method to remove error messages from a text input layout
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

    private void observeViewModel() {
        avm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                register.setEnabled(true);
                progressLayout.setVisibility(View.GONE);
                registerLayout.setVisibility(View.VISIBLE);
            }
            else {
                register.setEnabled(false);
                registerLayout.setVisibility(View.GONE);
                progressLayout.setVisibility(View.VISIBLE);
            }
        });
        avm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        avm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                if (success.contains("Registration successful")) {
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