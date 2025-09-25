package dev.kateastenberg.bookishproject.fragments.login;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.activities.LoginActivity;
import dev.kateastenberg.bookishproject.databinding.FragmentForgotPasswordBinding;

/*
This class represents a ForgotPasswordFragment.
A ForgotPasswordFragment enables a user to recover a lost password by entering their account email.
 */
public class ForgotPasswordFragment extends Fragment {

    private EditText emailInput;
    private Button resetButton;

    public ForgotPasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentForgotPasswordBinding binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);

        emailInput = binding.emailInput;
        resetButton = binding.resetButton;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        resetButton.setOnClickListener(v -> resetPassword());
    }

    /*
    Method to get account info from user to send to ResetFragment
     */
    private void resetPassword() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email address.", Toast.LENGTH_SHORT).show();
            return;
        }

        resetButton.setEnabled(false);

        if (getActivity() instanceof LoginActivity) {
            ((LoginActivity) getActivity()).navigateToResetInstructions(email);
        }
    }
}