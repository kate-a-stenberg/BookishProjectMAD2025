package dev.kateastenberg.bookishproject.fragments.journal;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.books.BookDetailFragment;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;

import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.models.UserBook;

/*
This class represents a JournalHostFragment.
A JournalHostFragment is a host fragment for all fragments in the journal section.
It is responsible for navigation and back navigation in this section, as well as passing arguments between fragments.
 */
public class JournalHostFragment extends HostFragment {

    private String lastSearchQuery = "";

    public JournalHostFragment() {
        // Required empty public constructor
    }

    @Override
    protected Fragment createInitialFragment() {
        return new JournalFragment();
    }

    @Override
    protected String getInitialFragmentTag() {
        return "entry_list";
    }

    /*
    Method to navigate to a journal entry with a specific existing entry
    Used by JournalFragment
     */
    public void navigateToJournalEntry(Entry entry, ImageView cover, String transitionName, boolean editMode) {

        // remember the current fragment
        Fragment currentFragment = getCurrentVisibleFragment();
        JournalEntryFragment fragment = new JournalEntryFragment();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getToolbarBuilder().setNavigating(true);
        }

        if (currentFragment instanceof JournalFragment) {
            JournalFragment journalFragment = (JournalFragment) currentFragment;
            String query = journalFragment.getCurrentSearchQuery();

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());

            this.lastSearchQuery = query;
            journalFragment.saveScrollPosition();
        }

        cover.setTransitionName(transitionName);
        fragment.setSharedElementEnterTransition(createContainerTransform());

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        // we're passing an entry
        args.putParcelable("entry", entry);
        // tell the JournalEntryFragment to open in view-only mode
        args.putBoolean("edit_mode", editMode);
        args.putString("transition_name", transitionName);  // Make sure this is included
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction().setReorderingAllowed(true).addSharedElement(cover, transitionName)
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getToolbarBuilder().setNavigating(false);
        }

    }

    /*
    Method to navigate to a new OpenBooksFragment
    Used by JournalFragment
     */
    public void navigateToOpenBooks() {

        OpenBooksFragment fragment = new OpenBooksFragment();

        Fragment currentFragment = getCurrentVisibleFragment();

        if (currentFragment instanceof JournalFragment) {
            // Let the BooksFragment save any important state
            ((JournalFragment) currentFragment).saveScrollPosition();

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());

        }

        fragment.setEnterTransition(createFadeIn());
        fragment.setReturnTransition(createFadeOut());

        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to MyBooksFragment
    Used by OpenBooksFragment
     */
    public void navigateToMyBooks() {
        // remember the current fragment
        Fragment currentFragment = getCurrentVisibleFragment();

        // make the new fragment
        MyBooksFragment fragment = new MyBooksFragment();

        if (currentFragment instanceof OpenBooksFragment) {
            // Let the OpenBooksFragment save any important state
            ((OpenBooksFragment) currentFragment).saveScrollPosition();

            currentFragment.setExitTransition(createSlideOutLeft());
            currentFragment.setReenterTransition(createSlideInLeft());

        }

        fragment.setEnterTransition(createSlideInRight());
        fragment.setReturnTransition(createSlideOutRight());

        getChildFragmentManager().beginTransaction().setReorderingAllowed(true)
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a BookFragment
    Used by JournalEntryFragment
     */
    public void navigateToBook(UserBook userBook, ImageView cover, String transitionName) {
        // make new fragment
        BookDetailFragment fragment = new BookDetailFragment(userBook);
        Fragment currentFragment = getCurrentVisibleFragment();

        if (currentFragment instanceof JournalEntryFragment) {
            JournalEntryFragment journalEntryFragment = (JournalEntryFragment) currentFragment;

            journalEntryFragment.setExitTransition(createFadeOut());
            journalEntryFragment.setReenterTransition(createFadeIn());

        }

        // pass a Book argument to the fragment
        Bundle args = new Bundle();
        args.putParcelable("book", userBook);
        fragment.setArguments(args);

        cover.setTransitionName(transitionName);
        fragment.setSharedElementEnterTransition(createContainerTransform());

        getChildFragmentManager().beginTransaction().setReorderingAllowed(true).addSharedElement(cover, transitionName)
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onBackPressed() {

        Log.d("BackNavigation", "JournalHostFragment.onBackPressed() called, backstack entries: " +
                getChildFragmentManager().getBackStackEntryCount());

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStack();

            // Wait for the fragment transaction to complete
            getChildFragmentManager().executePendingTransactions();

            // Get the now-visible fragment after back navigation
            Fragment currentFragment = getCurrentVisibleFragment();

            // If it's the BooksFragment, restore its search query
            if (currentFragment instanceof JournalFragment && !lastSearchQuery.isEmpty()) {

                // Set the query in the fragment
                ((JournalFragment) currentFragment).setSearchQuery(lastSearchQuery);

                // Update the toolbar with a delay to ensure everything is set up
                if (getActivity() instanceof MainActivity) {
                    new android.os.Handler().postDelayed(() -> {
                        if (getActivity() != null) {
                            MainActivity activity = (MainActivity) getActivity();
                            activity.setToolbar(this);

                            // Expand the search view with the saved query
                            if (activity.getToolbarBuilder().hasMenu()) {
                                MenuItem searchItem = activity.getToolbarBuilder().findMenuItem(R.id.search);
                                if (searchItem != null) {
                                    searchItem.expandActionView();
                                    androidx.appcompat.widget.SearchView searchView =
                                            (androidx.appcompat.widget.SearchView) searchItem.getActionView();

                                    // Set up the close button listener before setting the query
                                    assert searchView != null;
                                    searchView.setOnCloseListener(() -> {
                                        // Clear the search when the X is pressed
                                        ((JournalFragment) currentFragment).clearSearch();
                                        return false; // Return false to allow the default closing behavior
                                    });

                                    // Add listener for the X button (clear button)
                                    ImageView closeButton = searchView.findViewById(
                                            androidx.appcompat.R.id.search_close_btn);
                                    if (closeButton != null) {
                                        closeButton.setOnClickListener(v -> {
                                            // Clear the text in the search view
                                            searchView.setQuery("", false);
                                            // Clear the results
                                            ((JournalFragment) currentFragment).clearSearch();
                                        });
                                    }

                                    searchView.setQuery(lastSearchQuery, false);
                                    searchView.setIconified(false);
                                    searchView.requestFocus();

                                    // Clear focus after a short delay
                                    new android.os.Handler().postDelayed(searchView::clearFocus, 100);
                                }
                            }
                        }
                    }, 200);
                }
            }

            return true;
        }
        return false;
    }

}