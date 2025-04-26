package com.example.bookishproject;

import android.app.Activity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

public class ToolbarBuilder {

    private ImageButton backButton, helpButton, searchButton, addButton;
    private TextView searchInput;
    private Activity activity;
    private Navigator navigator;
    private AppToolbarProvider provider;

    public ToolbarBuilder() {}

    public ToolbarBuilder(Activity activity) {
        this.activity = activity;
    }

    // GETTERS AND SETTERS

    public ToolbarProvider getProvider() {
        return this.provider;
    }
    public Activity getActivity() {
        return this.activity;
    }
    public void setProvider(AppToolbarProvider provider) {
        this.provider = provider;

        backButton = provider.getBackButton();
        helpButton = provider.getHelpButton();
        searchButton = provider.getSearchButton();
        addButton = provider.getAddButton();
        searchInput = provider.getSearchInput();

        navigator = provider.getNavigator();
    }
    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public void buildToolbar(Fragment fragment) {
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
        else {
            setBlankToolbar();
        }
    }

    public void setBookToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToBooksFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Viewing a Book")
                .setMessage("Here is where you can view a book's data. Tap the edit button to update or change the data.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setBookResultsToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToBookSearchFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Results of a book search")
                .setMessage("Here are the results of your search. Tap briefly for info, or tap and hold to add to your collection.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setBookSearchToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToBooksFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Search for a new book")
                .setMessage("Enter the title and/or author of the book you'd like to search for.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setBooksToolbar() {

        backButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Viewing all books")
                .setMessage("Enter search information in the box below, tap the search button to search, " +
                        "or tap the plus to add a new book to your collection.")
                .setPositiveButton("Got it", null)
                .show());

        searchButton.setOnClickListener(v -> {
            // TODO: implement filter/sort functionality
        });

        addButton.setOnClickListener(v -> navigator.navigateToBookSearchFragment());

    }

    public void setHabitsToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToRecsFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Recommendations based on reading habits")
                .setMessage("Tap to get recommendations based on the books you've already read and liked.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setJournalEntryToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToJournalFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Viewing journal entry")
                .setMessage("Tap the edit icon to edit this journal entry.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setJournalToolbar() {

        backButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Reading journal")
                .setMessage("Tap and hold to view a journal entry details, filter by title/author, or add a new entry.")
                .setPositiveButton("Got it", null)
                .show());

        searchButton.setOnClickListener(v -> {
            // TODO: implement search/filter
        });

        addButton.setOnClickListener(v -> navigator.navigateToOpenBooksFragment());

    }

    public void setMatchOptionsToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToRecsFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Choosing a book to match")
                .setMessage("Tap and hold the book you'd like to get similar recommendations to.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setMatchResultsToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToMatchSearchFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Viewing similar books")
                .setMessage("Tap and hold to add a book to your collection")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setMatchSearchToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToRecsFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Find similar books")
                .setMessage("Enter the title and/or author of a book you'd like to find a book similar to")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setMyBooksToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.GONE);

        backButton.setOnClickListener(v -> navigator.navigateToOpenBooksFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Viewing my books")
                .setMessage("Enter search information in the box below, tap the search button to search, " +
                        "or tap and hold a book to add to current reads.")
                .setPositiveButton("Got it", null)
                .show());

        searchButton.setOnClickListener(v -> {
            // TODO: implement filter/sort functionality
        });

        addButton.setOnClickListener(v -> navigator.navigateToBookSearchFragment());

    }

    public void setOpenBooksToolbar() {

        backButton.setVisibility(View.VISIBLE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.VISIBLE);
        searchButton.setVisibility(View.VISIBLE);
        addButton.setVisibility(View.VISIBLE);

        backButton.setOnClickListener(v -> navigator.navigateToJournalFragment());

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Current reads")
                .setMessage("Tap and hold to create a new journal entry, search for books, or add a new current read.")
                .setPositiveButton("Got it", null)
                .show());

        searchButton.setOnClickListener(v -> {
            // TODO: implement search/filter method
        });

        addButton.setOnClickListener(v -> navigator.navigateToMyBooksFragment());

    }

    public void setRecsToolbar() {

        backButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.VISIBLE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);

        helpButton.setOnClickListener(v -> new AlertDialog.Builder(activity).setTitle("Recommendations")
                .setMessage("Choose a recommendation strategy.")
                .setPositiveButton("Got it", null)
                .show());

    }

    public void setBlankToolbar() {
        backButton.setVisibility(View.GONE);
        helpButton.setVisibility(View.GONE);
        searchInput.setVisibility(View.GONE);
        searchButton.setVisibility(View.GONE);
        addButton.setVisibility(View.GONE);
    }

}
