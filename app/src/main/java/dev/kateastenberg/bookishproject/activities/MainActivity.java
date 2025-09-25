package dev.kateastenberg.bookishproject.activities;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import android.transition.Fade;

import dev.kateastenberg.bookishproject.fragments.books.BooksHostFragment;
import dev.kateastenberg.bookishproject.fragments.home.HomeHostFragment;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.fragments.journal.JournalHostFragment;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.fragments.recs.RecsHostFragment;
import dev.kateastenberg.bookishproject.helpers.firebase.UserFirebaseHelper;
import dev.kateastenberg.bookishproject.helpers.ToolbarBuilder;
import dev.kateastenberg.bookishproject.databinding.ActivityMainBinding;
import dev.kateastenberg.bookishproject.models.User;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/*
This class represents the app's MainActivity.
It has a view binding, active fragment, HostFragments for each navigation section,
a ToolbarBuilder, a BottomNavigationView, a FirebaseAuth, and an AuthStateListener.
 */
public class MainActivity extends AppCompatActivity {

    private HostFragment activeFragment;
    private HostFragment booksHostFragment, homeHostFragment, recsHostFragment, journalHostFragment;
    private ToolbarBuilder toolbarBuilder;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen.installSplashScreen(this);

        checkUserLoginStatus();

        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        getWindow().setEnterTransition(createFadeIn());
        getWindow().setExitTransition(createFadeOut());
        setContentView(binding.getRoot());

        // set up out FirebaseAuth. This will monitor user account status
        auth = FirebaseAuth.getInstance();
        authListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                Toast.makeText(MainActivity.this,
                        "Session expired. Please sign in again", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
            else {
                loadUserData();
            }
        };

        toolbarBuilder = new ToolbarBuilder(this);

        // if there is a saved state
        if (savedInstanceState != null) {
            // Restore the active fragment reference
            String activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT_TAG");
            if (activeFragmentTag != null) {
                // Find existing fragments from the fragment manager
                homeHostFragment = (HostFragment) getSupportFragmentManager()
                        .findFragmentByTag("HOME");
                booksHostFragment = (HostFragment) getSupportFragmentManager()
                        .findFragmentByTag("BOOKS");
                recsHostFragment = (HostFragment) getSupportFragmentManager()
                        .findFragmentByTag("RECS");
                journalHostFragment = (HostFragment) getSupportFragmentManager()
                        .findFragmentByTag("JOURNAL");

                // Set active fragment based on saved tag
                switch (activeFragmentTag) {
                    case "HOME":
                        activeFragment = homeHostFragment;
                        break;
                    case "BOOKS":
                        activeFragment = booksHostFragment;
                        break;
                    case "RECS":
                        activeFragment = recsHostFragment;
                        break;
                    case "JOURNAL":
                        activeFragment = journalHostFragment;
                        break;
                }
            }
        }

        else {
            // if we have no saved state, make four new host fragments
            booksHostFragment = new BooksHostFragment();
            recsHostFragment = new RecsHostFragment();
            journalHostFragment = new JournalHostFragment();
            homeHostFragment = new HomeHostFragment();

            // Add all fragments but show only the home fragment
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, booksHostFragment, "BOOKS")
                    .hide(booksHostFragment)
                    .add(R.id.fragment_container, recsHostFragment, "RECS")
                    .hide(recsHostFragment)
                    .add(R.id.fragment_container, journalHostFragment, "JOURNAL")
                    .hide(journalHostFragment)
                    .add(R.id.fragment_container, homeHostFragment, "HOME")
                    .commit();

            // set homeHostFragment as the active fragment
            activeFragment = homeHostFragment;

            // force all fragment transactions to complete before proceeding
            getSupportFragmentManager().executePendingTransactions();

            // build toolbar based on the active fragment
            if (activeFragment != null) {
                toolbarBuilder.buildToolbar(activeFragment.getCurrentVisibleFragment());
            }

        }

        // establish the nav bar
        BottomNavigationView navBar = binding.bottomNavBar;
        // set operations for nav bar selection
        navBar.setOnItemSelectedListener(item -> {
            HostFragment fragment = getHostFragment(item);

            // hide the current active fragment and move to the selected fragment
            if (fragment != null && fragment != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(fragment)
                        .commit();
                // set the new fragment as the active fragment
                activeFragment = fragment;

                // update the toolbar
                setToolbar(activeFragment);

                return true;
            }

            return false;

        });

        // decide how to handle back navigation
        handleBackNavigation();

    }

    /*
    Method to get the HostFragment of the selected navigation section
     */
    @Nullable
    private HostFragment getHostFragment(MenuItem item) {
        HostFragment fragment = null;

        // get the ID of the selected item
        // use it to determine which section to navigate to
        int itemId = item.getItemId();
        if (itemId == R.id.navHome) {
            fragment = homeHostFragment;
        } else if (itemId == R.id.navBooks) {
            fragment = booksHostFragment;
        } else if (itemId == R.id.navRecs) {
            fragment = recsHostFragment;
        } else if (itemId == R.id.navJournal) {
            fragment = journalHostFragment;
        }
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // whenever we resume, make sure the session is still valid
        checkSessionTimeout();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }

    /*
    Method to save the current state
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save which fragment is active
        if (activeFragment != null) {
            // Get the tag based on which host fragment is active
            String tag = null;
            if (activeFragment == homeHostFragment) tag = "HOME";
            else if (activeFragment == booksHostFragment) tag = "BOOKS";
            else if (activeFragment == recsHostFragment) tag = "RECS";
            else if (activeFragment == journalHostFragment) tag = "JOURNAL";

            if (tag != null) {
                outState.putString("ACTIVE_FRAGMENT_TAG", tag);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // inflate menu
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        // use ToolbarBuilder to create the many
        toolbarBuilder.setMenu(menu);

        // If a fragment is already showing, update the toolbar
        if (activeFragment != null) {
            setToolbar(activeFragment);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Let ToolbarBuilder refresh menu items if search is not expanded
        if (!toolbarBuilder.isSearchExpanded()) {
            if (activeFragment != null) {
                Fragment visibleFragment = activeFragment.getCurrentVisibleFragment();
                if (visibleFragment != null) {
                    toolbarBuilder.buildToolbar(visibleFragment);
                }
                else {
                    toolbarBuilder.buildToolbar(activeFragment);
                }
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Let toolbar builder handle the item click
        if (toolbarBuilder.handleMenuItemClick(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    Method to build a toolbar
     */
    public void setToolbar(HostFragment activeFragment) {

        Fragment visibleFragment = null;

        // Get the currently visible fragment
        if (activeFragment != null) {
            visibleFragment = activeFragment.getCurrentVisibleFragment();
        }

        // Update the toolbar with the visible fragment
        if (visibleFragment != null) {
            toolbarBuilder.buildToolbar(visibleFragment);
        } else {
            toolbarBuilder.buildToolbar(activeFragment);
        }
    }

    /*
    Method to get the activity's toolbarBuilder
     */
    public ToolbarBuilder getToolbarBuilder() {
        return this.toolbarBuilder;
    }

    /*
    Method to handle back navigation
     */
    public void handleBackNavigation() {

        getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                processBackPress();
            }
        });
    }

    /*
    Method to process a back press
     */
    public void processBackPress() {

        boolean handled = false;

        if (activeFragment != null) {

            // have the active fragment perform its own back handling if it can
            handled = activeFragment.onBackPressed();

            if (handled) {
                // Wait for transition to complete
                activeFragment.getChildFragmentManager().executePendingTransactions();

                // Update toolbar after the fragment change
                setToolbar(activeFragment);
            }
        }

        if (!handled) {
            // If not handled by the fragment, perform default back action
            // Just finish the activity
            finish();
        }
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
    Method to load user data
     */
    private void loadUserData() {
        // get a new firebase helper
        UserFirebaseHelper fbHelper = new UserFirebaseHelper();
        // get the current user
        fbHelper.getCurrentUser(new UserFirebaseHelper.UserCallback() {
            @Override
            // if successful
            public void onSuccess(User user) {
                // update UI based on the user
                updateUI();
            }

            @Override
            // if not successful
            public void onError(String error) {
                // show error
                Toast.makeText(MainActivity.this, "Error loading profile: " + error,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*
    Method to updated the UI based on user
     */
    private void updateUI() {
        // not currently necessary - only UI elements based on user is AccountFragment,
        // which updates itself. Maybe become useful in future
    }

    /*
    Method to navigate to Login Activity
    Used here
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        
        // go to LoginActivity with transitions
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this);
        startActivity(intent, options.toBundle());
        finish();
    }

    /*
    Method to check if session is still valid
     */
    private void checkSessionTimeout() {
        // get the current firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // try to get a JWT that verifies the user's id
            user.getIdToken(true)
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            // Token refresh failed, session might be expired
                            FirebaseAuth.getInstance().signOut();
                            navigateToLogin();
                        }
                    });
        }
    }

    private void checkUserLoginStatus() {
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }

    // TRANSITIONS

    private Fade createFadeIn() {
        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.setDuration(1000);
        return fadeIn;
    }

    private Fade createFadeOut() {
        Fade fadeIn = new Fade(Fade.OUT);
        fadeIn.setDuration(1000);
        return fadeIn;
    }

}