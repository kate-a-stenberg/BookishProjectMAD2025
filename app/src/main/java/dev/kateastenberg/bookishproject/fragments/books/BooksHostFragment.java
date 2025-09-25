package dev.kateastenberg.bookishproject.fragments.books;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;


import java.util.List;

import dev.kateastenberg.bookishproject.R;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;

/*
A BooksHostFragment is a host fragment for the books section.
It is responsible for managing all fragments in the books section, including navigation, back navigation, and argument passing.
 */
public class BooksHostFragment extends HostFragment {

    private String lastSearchQuery = "";

    public BooksHostFragment() {
        // Required empty public constructor
    }

    /*
    Method to navigate to a BookDetailFragment given an argument (a book to populate the fragment with)
    Used by BooksFragment
     */
    public void navigateToBook(UserBook userBook, ImageView cover, String transitionName) {
        // keep track of the current fragment (before navigating away)
        Fragment currentFragment = getCurrentVisibleFragment();
        BookDetailFragment fragment = new BookDetailFragment(userBook);

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getToolbarBuilder().setNavigating(true);
        }

        // if the current fragment is a BooksFragment
        if (currentFragment instanceof BooksFragment) {
            BooksFragment booksFragment = (BooksFragment) currentFragment;

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());

            // Let the BooksFragment save any important state
            this.lastSearchQuery = booksFragment.getCurrentSearchQuery();
            booksFragment.saveScrollPosition();
        }

        // package up a Book to pass to the new BookFragment
        Bundle args = new Bundle();
        args.putParcelable("user_book", userBook);
        args.putString("transition_name", transitionName);
        fragment.setArguments(args);

        cover.setTransitionName(transitionName);
        fragment.setSharedElementEnterTransition(createContainerTransform());

        // Hide current fragment and add the new one instead of replacing
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
    Method to navigate to a BookResultsFragment given an argument (a query to dictate returned results)
    Used by BookSearchFragment
     */
    public void navigateToBookResults(String title, String author, Boolean apiSearch) {

        Fragment currentFragment = getCurrentVisibleFragment();

        // make a new BookResultsFragment
        BookResultsFragment fragment = BookResultsFragment.newInstance(title, author, apiSearch);

        // Set up transitions for the results fragment
        fragment.setEnterTransition(createSlideInRight());
        // Set a return transition for when we go back from results
        fragment.setReturnTransition(createSlideOutRight());

        // If coming from search fragment, set its exit transition
        if (currentFragment instanceof BookSearchFragment) {
            currentFragment.setExitTransition(createSlideOutLeft());
            // Set reenter transition for when we return to search
            currentFragment.setReenterTransition(createSlideInLeft());
        }

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a BookSearchFragment
    Used by BooksFragment
     */
    public void navigateToBookSearch() {
        // make a new BookSearchFragment
        BookSearchFragment fragment = new BookSearchFragment();

        Fragment currentFragment = getCurrentVisibleFragment();

        if (currentFragment instanceof BooksFragment) {
            BooksFragment booksFragment = (BooksFragment) currentFragment;

            currentFragment.setExitTransition(createFadeOut());
            currentFragment.setReenterTransition(createFadeIn());

            // Let the BooksFragment save any important state
            this.lastSearchQuery = booksFragment.getCurrentSearchQuery();
            booksFragment.saveScrollPosition();
        }

        fragment.setEnterTransition(createFadeIn());
        fragment.setReturnTransition(createFadeOut());

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .hide(currentFragment)
                .add(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onBackPressed() {

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {

            Fragment currentFragment = getCurrentVisibleFragment();

            if (currentFragment instanceof BookDetailFragment) {

                Fragment booksFragment = null;
                List<Fragment> fragments = getChildFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof BooksFragment) {
                        booksFragment = fragment;
                        break;
                    }
                }

                if (booksFragment != null) {
                    booksFragment.setReenterTransition(createFadeIn());
                }

                BookDetailFragment detailFragment = (BookDetailFragment) currentFragment;
                String transitionName = null;

                if (detailFragment.getArguments() != null) {
                    transitionName = detailFragment.getArguments().getString("transition_name");
                }

                View sharedElement = detailFragment.getCover();
                if (sharedElement != null && transitionName != null) {
                    sharedElement.setTransitionName(transitionName);

                    // Pop back stack with the shared element transition
                    getChildFragmentManager().popBackStack();
                    return true;
                }

                getCurrentVisibleFragment().setSharedElementReturnTransition(createContainerTransform());
            }

            getChildFragmentManager().popBackStack();
            getChildFragmentManager().executePendingTransactions();

            // Get the now-visible fragment after back navigation
            currentFragment = getCurrentVisibleFragment();

            // If it's the BooksFragment, restore its search query
            if (currentFragment instanceof BooksFragment && !lastSearchQuery.isEmpty()) {

                BooksFragment booksFragment = (BooksFragment) currentFragment;

                // Set the query in the fragment
                booksFragment.setSearchQuery(lastSearchQuery);

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
                                        booksFragment.clearSearch();
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
                                            booksFragment.clearSearch();
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

    @Override
    protected Fragment createInitialFragment() {
        return new BooksFragment();
    }

    @Override
    protected String getInitialFragmentTag() {
        return "books_list";
    }

}