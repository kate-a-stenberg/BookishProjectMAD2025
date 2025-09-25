package dev.kateastenberg.bookishproject.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.transition.Fade;
import android.transition.Slide;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.databinding.ActivityLoginBinding;
import dev.kateastenberg.bookishproject.fragments.login.ForgotPasswordFragment;
import dev.kateastenberg.bookishproject.fragments.login.LoginFragment;
import dev.kateastenberg.bookishproject.fragments.login.PasswordResetFragment;
import dev.kateastenberg.bookishproject.fragments.login.RegisterFragment;

/*
This class represents a Login Activity.
A Login Activity enables a user to create a user account, log in to the account,
and retrieve a lost password.
 */
public class LoginActivity extends AppCompatActivity {

    private Fragment currentFragment;
    protected static final int TRANSITION_DURATION_SHORT = 300;
    protected static final int TRANSITION_DURATION_MEDIUM = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        getWindow().setEnterTransition(createFadeInLong());
        getWindow().setExitTransition(createFadeOutLong());
        setContentView(binding.getRoot());

        currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.loginFragmentContainerView);
        MaterialToolbar toolbar = binding.introAppBar;
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> processBackPress());

        setUpBackPressHandling();

    }

    @Override
    public void onStart() {
        super.onStart();

        // if there is a current Firebase user, go right to MainActivity
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);

            // start MainActivity with a transition
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
            startActivity(intent, options.toBundle());
            this.finish();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    /*
    Method to set up back navigation
     */
    public void setUpBackPressHandling() {
        getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                processBackPress();
            }
        });
    }

    /*
    Method for back navigation
     */
    public void processBackPress() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            getSupportFragmentManager().executePendingTransactions();
            currentFragment = getCurrentFragment();
        }
        else {
            finish();
        }
    }

    /*
    Method to navigate to LoginFragment
    Used by PasswordResetFragment
     */
    public void navigateToLogin() {
        LoginFragment loginFragment = new LoginFragment();
        currentFragment = getCurrentFragment();

        if (currentFragment instanceof PasswordResetFragment) {
            loginFragment.setEnterTransition(createFadeIn());
            loginFragment.setReturnTransition(createFadeOut());

            currentFragment.setExitTransition(createSlideOutBottom());
            currentFragment.setReenterTransition(createSlideInBottom());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.loginFragmentContainerView, loginFragment)
                .addToBackStack(null)
                .commit();
        currentFragment = loginFragment;
    }

    /*
    Method to navigate to RegisterFragment
    Used by LoginFragment
     */
    public void navigateToRegister() {
        RegisterFragment registerFragment = new RegisterFragment();
        currentFragment = getCurrentFragment();

        if (currentFragment instanceof LoginFragment) {
            registerFragment.setEnterTransition(createSlideInBottom());
            registerFragment.setReturnTransition(createSlideOutBottom());

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.loginFragmentContainerView, registerFragment)
                .addToBackStack(null)
                .commit();

        currentFragment = registerFragment;
    }

    /*
    Method to navigate to ForgotPasswordFragment
    Used by LoginFragment
     */
    public void navigateToForgotPassword() {
        ForgotPasswordFragment forgotPasswordFragment = new ForgotPasswordFragment();
        currentFragment = getCurrentFragment();

        if (currentFragment instanceof LoginFragment) {
            forgotPasswordFragment.setEnterTransition(createSlideInBottom());
            forgotPasswordFragment.setReturnTransition(createSlideOutBottom());

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.loginFragmentContainerView, forgotPasswordFragment)
                .addToBackStack(null).commit();

        currentFragment = forgotPasswordFragment;
    }

    /*
    Method to navigate to PasswordResetFragment
    Used by ForgotPasswordFragment
     */
    public void navigateToResetInstructions(String email) {
        PasswordResetFragment passwordResetFragment = new PasswordResetFragment();
        currentFragment = getCurrentFragment();

        Bundle args = new Bundle();
        args.putString("email", email);
        passwordResetFragment.setArguments(args);

        if (currentFragment instanceof ForgotPasswordFragment) {
            currentFragment.setExitTransition(createSlideOutRight());
            currentFragment.setReenterTransition(createSlideInRight());

            passwordResetFragment.setEnterTransition(createSlideInLeft());
            passwordResetFragment.setReturnTransition(createSlideOutLeft());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.loginFragmentContainerView, passwordResetFragment)
                .addToBackStack(null).commit();

        currentFragment = passwordResetFragment;
    }

    /*
    Method to make the soft keyboard automatically collapse then the user
    taps the screen outside the keyboard
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        // if there is a tap event
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // find out where the tap was
            View v = getCurrentFocus();
            // if the tap was in an EditText
            if (v instanceof EditText) {
                // make a rectangle
                Rect outRect = new Rect();
                // the rectangle will be the visible boundaries of the EditText
                v.getGlobalVisibleRect(outRect);
                // if the touch coordinates are not inside the rectangle
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    // get rid of the blinking cursor
                    v.clearFocus();
                    // get reference to the IMM, which manages soft keyboard
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    // ask the IMM to hide the soft keyboard in whatever window the EditText is in
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    /*
    Method to get the current visible fragment
     */
    private Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentById(R.id.loginFragmentContainerView);
    }

    // THIS SECTION IS ALL CREATING TRANSITIONS

    protected Fade createFadeIn() {
        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(TRANSITION_DURATION_SHORT);
        return fadeIn;
    }

    protected Fade createFadeOut() {
        Fade fadeOut = new Fade(Fade.OUT);
        fadeOut.setDuration(TRANSITION_DURATION_SHORT);
        return fadeOut;
    }

    protected Slide createSlideInRight() {
        Slide slideIn = new Slide(Gravity.END);
        slideIn.setDuration(TRANSITION_DURATION_MEDIUM);
        return slideIn;
    }

    protected Slide createSlideOutLeft() {
        Slide slideOut = new Slide(Gravity.START);
        slideOut.setDuration(TRANSITION_DURATION_SHORT);
        return slideOut;
    }

    protected Slide createSlideInLeft() {
        Slide slideIn = new Slide(Gravity.START);
        slideIn.setDuration(TRANSITION_DURATION_MEDIUM);
        return slideIn;
    }

    protected Slide createSlideOutRight() {
        Slide slideOut = new Slide(Gravity.END);
        slideOut.setDuration(TRANSITION_DURATION_SHORT);
        return slideOut;
    }

    protected Slide createSlideInBottom() {
        Slide slideUp = new Slide(Gravity.BOTTOM);
        slideUp.setDuration(TRANSITION_DURATION_SHORT);
        return slideUp;
    }

    protected Slide createSlideOutBottom() {
        Slide slideDown = new Slide(Gravity.BOTTOM);
        slideDown.setDuration(TRANSITION_DURATION_SHORT);
        return slideDown;
    }

    protected Fade createFadeInLong() {
        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(1000);
        return fadeIn;
    }

    protected Fade createFadeOutLong() {
        Fade fadeIn = new Fade(Fade.OUT);
        fadeIn.setDuration(1000);
        return fadeIn;
    }

}