package dev.kateastenberg.bookishproject.fragments.login;

import android.app.ActivityOptions;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

import dev.kateastenberg.bookishproject.activities.LoginActivity;
import dev.kateastenberg.bookishproject.databinding.FragmentResetInstructionsBinding;
import dev.kateastenberg.bookishproject.intents.AuthIntent;
import dev.kateastenberg.bookishproject.viewmodels.AuthViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a PasswordResetFragment.
A PasswordResetFragment requests a password reset email from Firebase
and displays instructions to user on how to reset their password.
 */
public class PasswordResetFragment extends Fragment {

    private String userEmail;
    private TextView email;
    private LinearLayout progressLayout, instructionsLayout;
    private Button checkEmail, chooseEmail, backToLogin;
    private AuthViewModel avm;
    private PublishSubject<AuthIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentResetInstructionsBinding binding = FragmentResetInstructionsBinding.inflate(inflater, container, false);

        if (getArguments() != null) {
            userEmail = getArguments().getString("email");
        }

        email = binding.userEmail;
        progressLayout = binding.progressLayout;
        instructionsLayout = binding.instructionsLayout;
        checkEmail = binding.emailButton;
        chooseEmail = binding.chooseEmailAppButton;
        backToLogin = binding.backButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        avm = new ViewModelProvider(this).get(AuthViewModel.class);

        observeViewModel();

        processIntent(new AuthIntent.ResetPassword(userEmail));

        checkEmail.setOnClickListener(v -> openEmailApp());
        chooseEmail.setOnClickListener(v -> showEmailAppsList());
        backToLogin.setOnClickListener(v -> returnToLogin());

        progressLayout.setVisibility(View.VISIBLE);
        instructionsLayout.setVisibility(View.GONE);

        email.setText(userEmail);

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
    Method to open the user's email app
     */
    private void openEmailApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
            showEmailAppsList();
        }
    }

    /*
    Method to choose the user's preferred email app
     */
    private void showEmailAppsList() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_EMAIL);
        Intent chooser = Intent.createChooser(intent, "Open email with:");
        try {
            startActivity(chooser);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No email apps found", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    Method to go back to LoginFragment
     */
    private void returnToLogin() {
        if (getActivity() instanceof LoginActivity) {
            ((LoginActivity) getActivity()).navigateToLogin();
        }
    }

    private void observeViewModel() {
        avm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                progressLayout.setVisibility(View.VISIBLE);
                instructionsLayout.setVisibility(View.GONE);
            }
            else {
                progressLayout.setVisibility(View.GONE);
                instructionsLayout.setVisibility(View.VISIBLE);
            }
        });
        avm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                if (error.contains("Error") || error.contains("error")) {
                    getParentFragmentManager().popBackStack();
                }
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        avm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> avm.handleIntent(intent));
    }

    private void processIntent(AuthIntent intent) {
        intentSubject.onNext(intent);
    }


}