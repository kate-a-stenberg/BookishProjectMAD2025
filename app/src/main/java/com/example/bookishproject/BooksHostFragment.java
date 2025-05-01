package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/*
A BooksHostFragment is a host fragment for the books section.
It is responsible for managing all fragments in the books section, including navigation, back navigation, and argument passing.
 */
public class BooksHostFragment extends HostFragment {

    public BooksHostFragment() {
        // Required empty public constructor
    }

    @Override
    protected Fragment createInitialFragment() {
        return new BooksFragment();
    }

    @Override
    protected String getInitialFragmentTag() {
        return "books_list";
    }

    /*
    Method to navigate to a BookFragment given an argument (a book to populate the fragment with)
     */
    public void navigateToBook(Book book) {
        // keep track of the current fragment (before navigating away)
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        // if the current fragment is a BooksFragment
        if (currentFragment instanceof BooksFragment) {
            // Let the BooksFragment save any important state
            ((BooksFragment) currentFragment).saveScrollPosition();
        }

        // package up a Book to pass to the new BookFragment
        BookDetailFragment fragment = new BookDetailFragment(book);
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        fragment.setArguments(args);

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a BookResultsFragment given an argument (a query to dictate returned results)
     */
    public void navigateToBookResults(String query) {
        // make a new BookResultsFragment
        BookResultsFragment fragment = BookResultsFragment.newInstance(query);

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a BookSearchFragment
     */
    public void navigateToBookSearch() {
        // make a new BookSearchFragment
        BookSearchFragment fragment = new BookSearchFragment();

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to BooksFragment
    This is not currently used because this is the default fragment for this section and no need to navigate to it
     */
    public void navigateToBooks() {
        // make a new BooksFragment
        BooksFragment fragment = new BooksFragment();

        // swap out the current fragment with the new fragment and keep the current fragment on the back stack
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

}