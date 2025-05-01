package com.example.bookishproject;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.MaterialToolbar;

public class ToolbarBuilder {

    private Activity activity;
    private Fragment currentFragment;
    private MaterialToolbar toolbar;
    private Menu menu;

    public ToolbarBuilder() {}

    public ToolbarBuilder(Activity activity) {
        this.activity = activity;
        this.toolbar = activity.findViewById(R.id.topAppBar);
        //
        ((AppCompatActivity)activity).setSupportActionBar(toolbar);
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
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

        if (fragment instanceof BookResultsFragment) {
            setBookResultsToolbar();
        }
        else if (fragment instanceof BookSearchFragment) {
            setBookSearchToolbar();
        }
        else if (fragment instanceof BooksFragment) {
            setBooksToolbar();
        }
        else if (fragment instanceof BookFragment) {
            setBookToolbar();
        }
        else if (fragment instanceof HabitsFragment) {
            setHabitsToolbar();
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
            Log.d("FragmentDebug", "ToolbarBuilder.buildToolbar: WelcomeFragment");
        }
        else {
            setBlankToolbar();
            Log.d("FragmentDebug", "ToolbarBuilder.buildToolbar: Blank");
        }
    }

    private void hideAllMenuItems() {
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
    }

    private void showBackButton(boolean show) {
        ((AppCompatActivity)activity).getSupportActionBar().setDisplayHomeAsUpEnabled(show);

        if (show) {
            toolbar.setNavigationOnClickListener(v -> {
                if (currentFragment != null) {
                    Fragment parentFragment = currentFragment.getParentFragment();
                    if (parentFragment != null && parentFragment instanceof BackPressHandler) {
                        ((BackPressHandler) parentFragment).onBackPressed();
                    }
                }
            });
        }
    }

    private void setTitleForFragment(Fragment fragment) {
        String title = "BookishProject";


        if (fragment instanceof BookFragment) {
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
    private MenuItem findMenuItem(int id) {
        return menu.findItem(id);
    }

    public void setBookToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setBookResultsToolbar() {
        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }
    }

    public void setBookSearchToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setBooksToolbar() {
        // No back button for main section
        showBackButton(false);

        // Show relevant menu items
        MenuItem searchItem = findMenuItem(R.id.search);
        MenuItem sortItem = findMenuItem(R.id.sort);
        MenuItem helpItem = findMenuItem(R.id.help);

        if (searchItem != null) searchItem.setVisible(true);
        if (sortItem != null) sortItem.setVisible(true);
        if (helpItem != null) helpItem.setVisible(true);
    }

    public void setHabitsToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setJournalEntryToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setJournalToolbar() {

        // No back button for main section
        showBackButton(false);

        // Show relevant menu items
        MenuItem searchItem = findMenuItem(R.id.search);
        MenuItem sortItem = findMenuItem(R.id.sort);
        MenuItem helpItem = findMenuItem(R.id.help);

        if (searchItem != null) searchItem.setVisible(true);
        if (sortItem != null) sortItem.setVisible(true);
        if (helpItem != null) helpItem.setVisible(true);

    }

    public void setMatchOptionsToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setMatchResultsToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setMatchSearchToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setMyBooksToolbar() {

        // No back button for main section
        showBackButton(true);

        // Show relevant menu items
        MenuItem searchItem = findMenuItem(R.id.search);
        MenuItem sortItem = findMenuItem(R.id.sort);
        MenuItem helpItem = findMenuItem(R.id.help);

        if (searchItem != null) searchItem.setVisible(true);
        if (sortItem != null) sortItem.setVisible(true);
        if (helpItem != null) helpItem.setVisible(true);

    }

    public void setOpenBooksToolbar() {

        showBackButton(true);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setRecsToolbar() {

        showBackButton(false);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setWelcomeToolbar() {

        showBackButton(false);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(true);
        }

    }

    public void setBlankToolbar() {
        showBackButton(false);

        MenuItem helpItem = findMenuItem(R.id.help);
        if (helpItem != null) {
            helpItem.setVisible(false);
        }
    }

    // Methods to handle menu item clicks
    public boolean handleMenuItemClick(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            // Back button clicked
            handleBackButton();
            return true;
        } else if (id == R.id.help) {
            // Help button clicked
            showHelpDialog();
            return true;
        } else if (id == R.id.search) {
            // Search button clicked
            handleSearch();
            return true;
        } else if (id == R.id.sort) {
            // Sort button clicked
            handleSort();
            return true;
        }

        return false;
    }

    private void handleBackButton() {

        if (currentFragment == null) {
            return;
        }

        Fragment parentFragment = currentFragment.getParentFragment();
        if (parentFragment != null && parentFragment instanceof BackPressHandler) {
            ((BackPressHandler) parentFragment).onBackPressed();
        }
        else {
            // Default behavior - let the system pop the back stack
            ((AppCompatActivity)activity).onBackPressed();
        }
    }

    private void showHelpDialog() {
        String title = "Help";
        String message = "Help information";

        if (currentFragment instanceof BookFragment) {
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

        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Got it", null)
                .show();
    }

    private void handleSearch() {
        if (currentFragment instanceof BooksFragment) {
            // TODO: Open search for books
        }
        else if (currentFragment instanceof JournalFragment) {
            // TODO: Open search for journal entries
        } else if (currentFragment instanceof MyBooksFragment) {
            // TODO: Open search for books
        }

        // TODO: Other fragment-specific search handling
    }

    private void handleSort() {
        if (currentFragment instanceof BooksFragment) {
            // Show sort options for books
            new AlertDialog.Builder(activity)
                    .setTitle("Sort Books")
                    .setItems(new String[]{"Title", "Author", "Date Added"}, (dialog, which) -> {
                        // Handle sort selection
                    })
                    .show();
        } else if (currentFragment instanceof JournalFragment) {
            // Show sort options for journal entries
        }
        // TODO: Other fragment-specific sort handling
    }

    // Add to ToolbarBuilder.java
    public void updateToolbarForActiveFragment(Fragment activeFragment) {
        Log.d("FragmentDebug", "ToolbarBuilder updateToolbarForActiveFragment");
        Log.d("FragmentDebug", "updateToolbarForActiveFragment called with: " +
                (activeFragment != null ? activeFragment.getClass().getSimpleName() +
                        "@" + Integer.toHexString(System.identityHashCode(activeFragment)) : "null"));

        // Check each host fragment explicitly
        Log.d("FragmentDebug", "homeHostFragment instanceof check: " +
                (activeFragment instanceof HomeHostFragment));
        Log.d("FragmentDebug", "booksHostFragment instanceof check: " +
                (activeFragment instanceof BooksHostFragment));
        Log.d("FragmentDebug", "recsHostFragment instanceof check: " +
                (activeFragment instanceof RecsHostFragment));
        Log.d("FragmentDebug", "journalHostFragment instanceof check: " +
                (activeFragment instanceof JournalHostFragment));

        Fragment fragmentToUse = null;

        if (activeFragment instanceof BooksHostFragment) {
            fragmentToUse = ((BooksHostFragment) activeFragment).getCurrentVisibleFragment();
        } else if (activeFragment instanceof JournalHostFragment) {
            fragmentToUse = ((JournalHostFragment) activeFragment).getCurrentVisibleFragment();
        } else if (activeFragment instanceof RecsHostFragment) {
            fragmentToUse = ((RecsHostFragment) activeFragment).getCurrentVisibleFragment();
        } else if (activeFragment instanceof HomeHostFragment) {
            fragmentToUse = ((HomeHostFragment) activeFragment).getCurrentVisibleFragment();
        }

        if (fragmentToUse != null) {
            Log.d("ToolbarBuilder", "Using child fragment: " + fragmentToUse.getClass().getSimpleName());
            buildToolbar(fragmentToUse);
        } else {
            Log.d("ToolbarBuilder", "No child fragment found, using host: " +
                    (activeFragment != null ? activeFragment.getClass().getSimpleName() : "null"));
            buildToolbar(activeFragment);
        }
    }

}
