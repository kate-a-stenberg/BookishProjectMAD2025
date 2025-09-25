package dev.kateastenberg.bookishproject.helpers;

import android.app.ActivityOptions;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import dev.kateastenberg.bookishproject.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

import dev.kateastenberg.bookishproject.activities.LoginActivity;
import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.books.BookDetailFragment;
import dev.kateastenberg.bookishproject.fragments.books.BookResultsFragment;
import dev.kateastenberg.bookishproject.fragments.books.BookSearchFragment;
import dev.kateastenberg.bookishproject.fragments.books.BooksFragment;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.fragments.home.AboutFragment;
import dev.kateastenberg.bookishproject.fragments.home.AccountFragment;
import dev.kateastenberg.bookishproject.fragments.home.WelcomeFragment;
import dev.kateastenberg.bookishproject.fragments.journal.JournalEntryFragment;
import dev.kateastenberg.bookishproject.fragments.journal.JournalFragment;
import dev.kateastenberg.bookishproject.fragments.journal.MyBooksFragment;
import dev.kateastenberg.bookishproject.fragments.journal.OpenBooksFragment;
import dev.kateastenberg.bookishproject.fragments.recs.MatchOptionsFragment;
import dev.kateastenberg.bookishproject.fragments.recs.MatchResultsFragment;
import dev.kateastenberg.bookishproject.fragments.recs.MatchSearchFragment;
import dev.kateastenberg.bookishproject.fragments.recs.RecsFragment;
import dev.kateastenberg.bookishproject.interfaces.Searchable;

/*
This class represents a ToolbarBuilder.
A ToolbarBuilder is responsible for assembling a top app bar for a fragment.
This includes choosing which icons to display, what the help message says, whether to display the back button, and what the headline should say.
 */
public class ToolbarBuilder {

    private final AppCompatActivity activity;
    private final MaterialToolbar toolbar;
    private Fragment currentFragment;
    private Menu menu;
    private boolean isNavigating = false;
    private MenuItem searchItem, helpItem, logoutItem;
    private boolean isSearchExpanded = false;

    public ToolbarBuilder(AppCompatActivity activity) {
        this.activity = activity;
        this.toolbar = activity.findViewById(R.id.topAppBar);
        activity.setSupportActionBar(toolbar);
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
        if (menu != null) {
            searchItem = findMenuItem(R.id.search);
            helpItem = findMenuItem(R.id.help);
            logoutItem = findMenuItem(R.id.logout);

            setUpSearchView();
        }
    }

    public void buildToolbar(Fragment fragment) {

        this.currentFragment = fragment;

        if (menu == null) return;

        // Hide all menu items by default
        hideAllMenuItems();

        // Hide back button by default
        showBackButton(false);

        // Set title based on fragment
        setTitleForFragment(fragment);

        if (fragment instanceof AboutFragment) {
            setAboutToolbar();
        }
        else if (fragment instanceof AccountFragment) {
            setAccountToolbar();
        }
        else if (fragment instanceof BookResultsFragment) {
            setBookResultsToolbar();
        }
        else if (fragment instanceof BookSearchFragment) {
            setBookSearchToolbar();
        }
        else if (fragment instanceof BooksFragment) {
            setBooksToolbar();
        }
        else if (fragment instanceof BookDetailFragment) {
            setBookToolbar();
        }
        else if (fragment instanceof JournalEntryFragment) {
            setJournalEntryToolbar();
        }
        else if (fragment instanceof MatchOptionsFragment) {
            setMatchOptionsToolbar();
        }
        else if (fragment instanceof MatchResultsFragment) {
            setMatchResultsToolbar();
        }
        else if (fragment instanceof MatchSearchFragment) {
            setMatchSearchToolbar();
        }
        else if (fragment instanceof MyBooksFragment) {
            setMyBooksToolbar();
        }
        else if (fragment instanceof OpenBooksFragment) {
            setOpenBooksToolbar();
        }
        else if (fragment instanceof JournalFragment) {
            setJournalToolbar();
        }
        else if (fragment instanceof RecsFragment) {
            setRecsToolbar();
        }
        else if (fragment instanceof WelcomeFragment) {
            setWelcomeToolbar();
        }
        else {
            setBlankToolbar();
        }
    }

    private void hideAllMenuItems() {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    private void showBackButton(boolean show) {

        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(show);

            if (show) {
                toolbar.setNavigationOnClickListener(v -> handleBackButton());
            }
        }
    }

    private void setTitleForFragment(Fragment fragment) {
        String title = "BookishProject";

        if (fragment instanceof AboutFragment) {
            title = "About Bookish Project";
        }
        else if (fragment instanceof AccountFragment) {
            title = "My Account";
        }
        else if (fragment instanceof BookDetailFragment) {
            title = "Book Details";
        }
        else if (fragment instanceof BookResultsFragment) {
            title = "Book Search Results";
        }
        else if (fragment instanceof BookSearchFragment) {
            title = "Book Search";
        }
        else if (fragment instanceof BooksFragment) {
            title = "My Books";
        }
        else if (fragment instanceof JournalEntryFragment) {
            title = "Journal Entry Details";
        }
        else if (fragment instanceof JournalFragment) {
            title = "Reading Journal";
        }
        else if (fragment instanceof MatchOptionsFragment) {
            title = "Books to Match";
        }
        else if (fragment instanceof MatchResultsFragment) {
            title = "Book Matches";
        }
        else if (fragment instanceof MatchSearchFragment) {
            title = "Search Books to Match";
        }
        else if (fragment instanceof MyBooksFragment) {
            title = "My Books";
        }
        else if (fragment instanceof OpenBooksFragment) {
            title = "My Current Reads";
        }
        else if (fragment instanceof RecsFragment) {
            title = "Recommendations";
        }
        else if (fragment instanceof WelcomeFragment) {
            title = "Home";
        }

        toolbar.setTitle(title);
    }

    // Helper method to find menu items
    public MenuItem findMenuItem(int id) {
        return menu.findItem(id);
    }

    public void setAboutToolbar() {
        showBackButton(true);

        searchItem.setVisible(false);
        helpItem.setVisible(true);
        logoutItem.setVisible(true);
    }

    public void setAccountToolbar() {
        showBackButton(true);

        searchItem.setVisible(false);
        helpItem.setVisible(true);
        logoutItem.setVisible(true);
    }

    public void setBookToolbar() {

        showBackButton(true);

        searchItem.setVisible(false);
        if (searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
        }
        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setBookResultsToolbar() {
        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);
    }

    public void setBookSearchToolbar() {

        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setBooksToolbar() {
        // No back button for main section
        showBackButton(false);

        // Show relevant menu items
        helpItem.setVisible(true);
        logoutItem.setVisible(true);

        if (searchItem != null) {
            searchItem.setVisible(true);

            // Configure SearchView
            androidx.appcompat.widget.SearchView searchView =
                    (androidx.appcompat.widget.SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setSubmitButtonEnabled(true);
                searchView.setQueryHint("Search books...");
            }
        }
    }

    public void setJournalEntryToolbar() {

        showBackButton(true);

        if (searchItem != null) {
            searchItem.setVisible(false);
            // If expanded, collapse it
            if (searchItem.isActionViewExpanded()) {
                searchItem.collapseActionView();
            }
        }

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setJournalToolbar() {

        showBackButton(false);

        // Show relevant menu items
        helpItem.setVisible(true);
        logoutItem.setVisible(true);

        if (searchItem != null) {
            searchItem.setVisible(true);

            // Configure SearchView
            androidx.appcompat.widget.SearchView searchView =
                    (androidx.appcompat.widget.SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setSubmitButtonEnabled(true);
                searchView.setQueryHint("Search books...");
            }
        }

    }

    public void setMatchOptionsToolbar() {

        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setMatchResultsToolbar() {

        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setMatchSearchToolbar() {

        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setMyBooksToolbar() {

        showBackButton(true);

        // Show relevant menu items
        helpItem.setVisible(true);
        logoutItem.setVisible(true);

        if (searchItem != null) {
            searchItem.setVisible(true);

            // Add the expand/collapse listener here
            searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
                @Override
                public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                    return true; // Allow expansion
                }

                @Override
                public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                    // When search is closed, clear the search
                    if (currentFragment instanceof Searchable) {
//                        ((Searchable) currentFragment).performSearch("");
                        if (!isNavigating) {
                            ((Searchable) currentFragment).performSearch("");
                        }
                    }
                    return true; // Allow collapse
                }
            });

            // Configure SearchView
            androidx.appcompat.widget.SearchView searchView =
                    (androidx.appcompat.widget.SearchView) searchItem.getActionView();
            if (searchView != null) {
                searchView.setSubmitButtonEnabled(true);
                searchView.setQueryHint("Search books...");
            }
        }

    }

    public void setOpenBooksToolbar() {

        showBackButton(true);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setRecsToolbar() {

        showBackButton(false);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setWelcomeToolbar() {

        showBackButton(false);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);

    }

    public void setBlankToolbar() {
        showBackButton(false);

        helpItem.setVisible(true);
        logoutItem.setVisible(true);
    }

    // Methods to handle menu item clicks
    public boolean handleMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.help) {
            // Help button clicked
            showHelpDialog();
            return true;
        } else if (id == R.id.search) {
            // Search button clicked
            handleSearch();
            return true;
        } else if (id == R.id.logout) {
            new MaterialAlertDialogBuilder(activity)
                    .setTitle("Log out")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Log out", (dialog, which) -> signOut())
                    .setNegativeButton("Cancel", null)
                    .show();
            return true;
        }

        return false;
    }

    private void showHelpDialog() {
        String title = "Help";
        String message = "Help information";

        if (currentFragment instanceof AboutFragment) {
            title = "About Bookish Project";
            message = "What help do you need here?";
        }
        else if (currentFragment instanceof AccountFragment) {
            title = "My Account";
            message = "Change your name, email address, or password, log out, or delete your account.";
        }
        else if (currentFragment instanceof BookDetailFragment) {
            title = "Book Details";
            message = "Here is where you can view a book's data. Tap the edit button to update or change the data.";
        }
        else if (currentFragment instanceof BookResultsFragment) {
            title = "Book Search Results";
            message = "Viewing results of book search. Tap and hold to add a book to your collection.";
        }
        else if (currentFragment instanceof BookSearchFragment) {
            title = "Book Search";
            message = "Enter a title and/or author to search for a new book.";
        }
        else if (currentFragment instanceof BooksFragment) {
            title = "My Books";
            message = "Tap an icon to search or sort.";
        }
        else if (currentFragment instanceof JournalEntryFragment) {
            title = "Journal Entry Details";
            message = "Now viewing details of a reading activity. Tap to save or edit.";
        }
        else if (currentFragment instanceof JournalFragment) {
            title = "My Reading Journal";
            message = "Tap an icon to search or sort";
        }
        else if (currentFragment instanceof MatchOptionsFragment) {
            title = "Books to Match";
            message = "To get recommendations based on a book, tap and hold on the book.";
        }
        else if (currentFragment instanceof MatchResultsFragment) {
            title = "Book Matches";
            message = "Now viewing results of the recommendation search based on a similar book";
        }
        else if (currentFragment instanceof MatchSearchFragment) {
            title = "Search Books to Match";
            message = "Enter the title and/or author of a book you'd like to find similar books to.";
        }
        else if (currentFragment instanceof MyBooksFragment) {
            title = "My Books";
            message = "Viewing all books. Tap and hold to add a book to your current reads.";
        }
        else if (currentFragment instanceof OpenBooksFragment) {
            title = "My Current Reads";
            message = "Viewing your current reads. Tap and hold a book to log a new reading activity.";
        }
        else if (currentFragment instanceof RecsFragment) {
            title = "Recommendations";
            message = "Choose how you'd like to receive your recommendations.";
        }
        else if (currentFragment instanceof WelcomeFragment) {
            title = "Home";
            message = "Welcome!";
        }

        new MaterialAlertDialogBuilder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

    /*
    Method to handle a search operation
     */
    private void handleSearch() {
        if (currentFragment instanceof Searchable) {
            MenuItem searchItem = findMenuItem(R.id.search);

            if (searchItem != null) {
                searchItem.expandActionView();

                androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) searchItem.getActionView();

                assert searchView != null;
                searchView.setSubmitButtonEnabled(true);

                searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit (String query) {
                        if (currentFragment instanceof Searchable) {
                            ((Searchable) currentFragment).performSearch(query);
                        }
                        return true;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        if (currentFragment instanceof Searchable && newText.isEmpty()) {
                            ((Searchable)currentFragment).performSearch("");
                        }
                        return false;
                    }
                });
                // Set hint and focus
                searchView.setQueryHint("Search...");
                searchView.setIconifiedByDefault(true);
                searchView.setFocusable(true);
                searchView.requestFocusFromTouch();

            }
        }

    }

    public void setNavigating(boolean navigating) {
        this.isNavigating = navigating;
    }

    public boolean hasMenu() {
        return menu != null;
    }

    private void handleBackButton() {

        Fragment fragment = currentFragment;

        // First check if search is expanded and handle that
        MenuItem searchItem = findMenuItem(R.id.search);

        if (currentFragment instanceof MyBooksFragment) {
            if (activity instanceof MainActivity) {
                activity.getOnBackPressedDispatcher().onBackPressed();
            }
            return;
        }
        else if (searchItem != null && searchItem.isActionViewExpanded() && !(fragment instanceof MyBooksFragment)) {
            // Collapse search view first
            searchItem.collapseActionView();
            return; // Don't proceed with back navigation yet
        }

        if (currentFragment == null) {
            return;
        }

        if (activity instanceof MainActivity) {
            activity.getOnBackPressedDispatcher().onBackPressed();
        }
    }

    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity);
        activity.startActivity(intent, options.toBundle());
        activity.finish();
    }

    private void setUpSearchView() {
        if (searchItem == null) return;

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView)
                searchItem.getActionView();

        if (searchView != null) {
            searchView.setIconifiedByDefault(true);
        }

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(@NonNull MenuItem item) {
                isSearchExpanded = true;
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(@NonNull MenuItem item) {
                isSearchExpanded = false;

                if (currentFragment instanceof HostFragment) {
                    Fragment visibleChild = ((HostFragment)currentFragment)
                            .getCurrentVisibleFragment();
                    if (visibleChild instanceof Searchable) {
                        ((Searchable)visibleChild).performSearch("");
                    }
                }
                activity.invalidateOptionsMenu();

                return true;
            }
        });
    }

    public boolean isSearchExpanded() {
        return isSearchExpanded;
    }

}
