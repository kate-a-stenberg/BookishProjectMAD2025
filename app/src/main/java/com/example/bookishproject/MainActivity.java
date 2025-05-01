package com.example.bookishproject;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
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
It has view binding, a tab layout, a view pager, a view pager adapter
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private Fragment activeFragment;
    private BooksHostFragment booksHostFragment;
    private HomeHostFragment homeHostFragment;
    private RecsHostFragment recsHostFragment;
    private JournalHostFragment journalHostFragment;
    private ToolbarBuilder toolbarBuilder;
    private BottomNavigationView navBar;
    private int currentSection = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("STARTUP CRASH", "MainActivity.onCreate() called");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        toolbarBuilder = new ToolbarBuilder(this);
        Log.d("STARTUP CRASH", "MainActivity.onCreate(): set toolbarBuilder");

        if (savedInstanceState != null) {
            // Restore the active fragment reference
            String activeFragmentTag = savedInstanceState.getString("ACTIVE_FRAGMENT_TAG");
            if (activeFragmentTag != null) {
                // Find existing fragments from the fragment manager
                homeHostFragment = (HomeHostFragment) getSupportFragmentManager().findFragmentByTag("HOME");
                booksHostFragment = (BooksHostFragment) getSupportFragmentManager().findFragmentByTag("BOOKS");
                recsHostFragment = (RecsHostFragment) getSupportFragmentManager().findFragmentByTag("RECS");
                journalHostFragment = (JournalHostFragment) getSupportFragmentManager().findFragmentByTag("JOURNAL");

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
            Log.d("STARTUP CRASH", "MainActivity.onCreate(): creating new host fragments");
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
            Log.d("STARTUP CRASH", "MainActivity.onCreate(): added all fragments but showed only home fragment");


            activeFragment = homeHostFragment;
            Log.d("FragmentDebug", "Active fragment set to: " +
                    activeFragment.getClass().getSimpleName() +
                    "@" + Integer.toHexString(System.identityHashCode(activeFragment)));

            Log.d("FragmentDebug", "Is active fragment instanceof HomeHostFragment? " +
                    (activeFragment instanceof HomeHostFragment));
            Log.d("FragmentDebug", "Is active fragment instanceof JournalHostFragment? " +
                    (activeFragment instanceof JournalHostFragment));
            getSupportFragmentManager().executePendingTransactions();

            new android.os.Handler().postDelayed(() -> {
                if (activeFragment != null && activeFragment instanceof HostFragment) {
                    toolbarBuilder.buildToolbar(((HostFragment) activeFragment).getCurrentVisibleFragment());
                }
            }, 300);

        }

        navBar = binding.bottomNavBar;
        navBar.setOnItemSelectedListener(item -> {
            Log.d("STARTUP CRASH", "MainActivity setting navBar onItemSelectedListener");
            Fragment fragment = null;

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

            if (fragment != null && fragment != activeFragment) {
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(fragment)
                        .commit();
                activeFragment = fragment;

                toolbarBuilder.updateToolbarForActiveFragment(activeFragment);

                return true;
            }

            return false;

        });

        setupKeyboardVisibilityListener(); // Add this line
        Log.d("STARTUP CRASH", "MainActivity setUpKeyboardVisibilityListener");

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("FragmentDebug", "MainActivity handleOnBackPressed");
                boolean handled = false;
                Log.d("FragmentDebug", "MainActivity handleOnBackPressed: not yet handled");
                if (activeFragment instanceof BackPressHandler) {
                    Log.d("FragmentDebug", "MainActivity handleOnBackPressed: active fragment is instanceof BackPressHandler");
                    handled = ((BackPressHandler) activeFragment).onBackPressed();

                    if (handled) {
                        Log.d("FragmentDebug", "MainActivity handleOnBackPressed: if handled yes");

                        // Wait a brief moment for transition to complete
                        activeFragment.getChildFragmentManager().executePendingTransactions();
                        Log.d("FragmentDebug", "MainActivity handleOnBackPressed: child fragment manager.executePendingTransactions");

                        // Update UI on main thread after a short delay
                        // Use a handler to post a delayed action
                        new android.os.Handler().postDelayed(() -> {
                            updateToolbarAfterFragmentChange();
                            Log.d("FragmentDebug", "MainActivity handleOnBackPressed: updating toolbar");
                        }, 100);
                    }
                }

                if (!handled) {
                    Log.d("FragmentDebug", "MainActivity handleOnBackPressed: not handled");
                    setEnabled(false);
                    Log.d("FragmentDebug", "MainActivity handleOnBackPressed: not enabled");
                    getOnBackPressedDispatcher().onBackPressed();
                    Log.d("FragmentDebug", "MainActivity handleOnBackPressed: getOnBackPressedDispatcher().onBackPressed()");
                    setEnabled(true);
                    Log.d("FragmentDebug", "MainActivity handleOnBackPressed: enabled yes");
                }
            }
        });

    }

    @Override
    public void onAttachedToWindow() {
        Log.d("STARTUP CRASH", "MainActivity.onAttachedToWindow()");
        super.onAttachedToWindow();
        Log.d("STARTUP CRASH", "MainActivity called super.onAttachedToWindow()");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("STARTUP CRASH", "MainActivity.onResume()");
    }

    public int getCurrentSection() {
        return currentSection;
    }

    public void setToolbar(Fragment fragment) {
        toolbarBuilder.buildToolbar(fragment);
        Log.d("STARTUP CRASH", "MainActivity.setToolbar(): " + fragment.getClass().getSimpleName());
    }

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
        Log.d("STARTUP CRASH", "MainActivity.onCreateOptionsMenu()");
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
        Log.d("STARTUP CRASH", "MainActivity.onOptionsItemSelected()");
        // Let toolbar builder handle the item click
        if (toolbarBuilder.handleMenuItemClick(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void showFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();

        // Update toolbar for this fragment
        toolbarBuilder.buildToolbar(fragment);
    }

    protected void updateToolbarAfterFragmentChange() {
        Log.d("FragmentDebug", "MainActivity updateToolbarAfterFragmentChange()");
        // Force any pending fragment transactions to complete
        getSupportFragmentManager().executePendingTransactions();

        // Get the currently visible fragment
        Fragment visibleFragment = null;
        if (activeFragment instanceof BackPressHandler) {
            if (activeFragment instanceof BooksHostFragment) {
                visibleFragment = ((BooksHostFragment) activeFragment).getCurrentVisibleFragment();
            }
            else if (activeFragment instanceof JournalHostFragment) {
                visibleFragment = ((JournalHostFragment) activeFragment).getCurrentVisibleFragment();
            }
            else if (activeFragment instanceof RecsHostFragment) {
                visibleFragment = ((RecsHostFragment) activeFragment).getCurrentVisibleFragment();
            }
            else if (activeFragment instanceof HomeHostFragment) {
                visibleFragment = ((HomeHostFragment) activeFragment).getCurrentVisibleFragment();
            }
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save which fragment is active
        if (activeFragment instanceof HomeHostFragment) {
            outState.putString("ACTIVE_FRAGMENT_TAG", "HOME");
        } else if (activeFragment instanceof BooksHostFragment) {
            outState.putString("ACTIVE_FRAGMENT_TAG", "BOOKS");
        } else if (activeFragment instanceof RecsHostFragment) {
            outState.putString("ACTIVE_FRAGMENT_TAG", "RECS");
        } else if (activeFragment instanceof JournalHostFragment) {
            outState.putString("ACTIVE_FRAGMENT_TAG", "JOURNAL");
        }
    }

}