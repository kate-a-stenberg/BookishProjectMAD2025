package com.example.bookishproject;

import android.graphics.Rect;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.SearchView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.bookishproject.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/*
This class represents the app's MainActivity.
It has a view binding, active fragment, HostFragments for each navigation section, a ToolbarBuilder, and a BottomNavigationView.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HostFragment activeFragment;
    private HostFragment booksHostFragment, homeHostFragment, recsHostFragment, journalHostFragment;
    private ToolbarBuilder toolbarBuilder;
    private BottomNavigationView navBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbarBuilder = new ToolbarBuilder(this);

        if (savedInstanceState != null) {
            // Restore the active fragment reference
            String activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT_TAG");
            if (activeFragmentTag != null) {
                // Find existing fragments from the fragment manager
                homeHostFragment = (HostFragment) getSupportFragmentManager().findFragmentByTag("HOME");
                booksHostFragment = (HostFragment) getSupportFragmentManager().findFragmentByTag("BOOKS");
                recsHostFragment = (HostFragment) getSupportFragmentManager().findFragmentByTag("RECS");
                journalHostFragment = (HostFragment) getSupportFragmentManager().findFragmentByTag("JOURNAL");

                // Set active fragment based on saved tag
                if (activeFragmentTag.equals("HOME")) {
                    activeFragment = homeHostFragment;
                } else if (activeFragmentTag.equals("BOOKS")) {
                    activeFragment = booksHostFragment;
                } else if (activeFragmentTag.equals("RECS")) {
                    activeFragment = recsHostFragment;
                } else if (activeFragmentTag.equals("JOURNAL")) {
                    activeFragment = journalHostFragment;
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
                    .add(R.id.fragment_container, booksHostFragment, "BOOKS").hide(booksHostFragment)
                    .add(R.id.fragment_container, recsHostFragment, "RECS").hide(recsHostFragment)
                    .add(R.id.fragment_container, journalHostFragment, "JOURNAL").hide(journalHostFragment)
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
        navBar = binding.bottomNavBar;
        // set operations for nav bar selection
        navBar.setOnItemSelectedListener(item -> {
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

        // make sure the keyboard isn't interrupting the view
        setupKeyboardVisibilityListener();

        // decide how to handle back navigation
        handleBackNavigation();

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    Method to ensure the keyboard doesn't get in the way of the view
     */
    private void setupKeyboardVisibilityListener() {
        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean keyboardVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootView.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootView.getRootView().getHeight();
                int visibleHeight = r.height();

                boolean isKeyboardNowVisible = visibleHeight < screenHeight * 0.85;

                if (isKeyboardNowVisible != keyboardVisible) {
                    keyboardVisible = isKeyboardNowVisible;

                    // Make sure toolbar color is restored if keyboard is hidden
//                    if (!keyboardVisible) {
//                        binding.materialToolbar.setBackgroundColor(currentTextColor);
//                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar_menu, menu);
        toolbarBuilder.setMenu(menu);

        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        // If a fragment is already showing, update the toolbar
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (currentFragment != null) {
            toolbarBuilder.buildToolbar(currentFragment);
        }

        // Configure the search view
//        searchView.setQueryHint("Search books...");
//
//        // Set up search listener
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) {
//                // Let the current fragment handle the search
//                Fragment currentFragment = getSupportFragmentManager()
//                        .findFragmentById(R.id.fragment_container);
//
//                if (currentFragment instanceof Searchable) {
//                    ((Searchable) currentFragment).performSearch(query);
//                    searchView.clearFocus(); // Hide keyboard
//                    return true;
//                }
//                return false;
//            }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                // Handle search text changes (for real-time filtering)
//                return false;
//            }
//        });

        return true;
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
    protected void setToolbar(HostFragment activeFragment) {
        // Get the currently visible fragment
        Fragment visibleFragment = null;

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

    private void performSearch(String query) {
        // Your search implementation here
        // This could filter your RecyclerView, call a search API, etc.
        // TODO
    }

    public ToolbarBuilder getToolbarBuilder() {
        return this.toolbarBuilder;
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

    /*
    Method to handle back navigation
     */
    public void handleBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // initially it has not been handled
                boolean handled = false;
                if (activeFragment != null) {
                    // see if the activeFragment can/will handle it
                    handled = (activeFragment).onBackPressed();

                    // if it has been handled
                    if (handled) {

                        // Wait a brief moment for transition to complete
                        activeFragment.getChildFragmentManager().executePendingTransactions();

                        // update toolbar after the fragment change
                        setToolbar(activeFragment);
                    }
                }

                // if it hasn't been handled
                if (!handled) {
                    // disable current callback
                    setEnabled(false);
                    // handle back behavior in the default way
                    // exit to home screen
                    getOnBackPressedDispatcher().onBackPressed();
                    // re-enable the current callback
                    setEnabled(true);
                }
            }
        });
    }

}