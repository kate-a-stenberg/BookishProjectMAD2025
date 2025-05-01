package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

/*
This class represents a JournalHostFragment.
A JournalHostFragment is a host fragment for all fragments in the journal section.
It is responsible for navigation and back navigation in this section, as well as passing arguments between fragments.
 */
public class JournalHostFragment extends HostFragment {

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
     */
    public void navigateToJournalEntry(Entry entry) {
        // remember the current fragment
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        // initialize the new fragment
        JournalEntryFragment fragment = new JournalEntryFragment(entry);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        // we're passing an entry
        args.putParcelable("entry", entry);
        // tell the JournalEntryFragment to open in view-only mode
        args.putBoolean("edit_mode", false);
        fragment.setArguments(args);

        if (currentFragment instanceof JournalFragment) {
            // Let the JournalFragment save any important state
            ((JournalFragment) currentFragment).saveScrollPosition();
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a JournalEntryFragment using a Book
     */
    public void navigateToJournalEntry(Book book) {
        // remember current fragment
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        // make new fragment
        JournalEntryFragment fragment = new JournalEntryFragment(book);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        // we're passing a book to the new fragment
        args.putParcelable("book", book);
        // tell the new fragment to go to edit mode
        args.putBoolean("edit_mode", true);
        fragment.setArguments(args);

        if (currentFragment instanceof OpenBooksFragment) {
            // Let the OpenBooksFragment save any important state
            ((OpenBooksFragment) currentFragment).saveScrollPosition();
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a new OpenBooksFragment
     */
    public void navigateToOpenBooks() {
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        OpenBooksFragment fragment = new OpenBooksFragment();

        if (currentFragment instanceof JournalFragment) {
            // Let the BooksFragment save any important state
            ((JournalFragment) currentFragment).saveScrollPosition();
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to MyBooksFragment
     */
    public void navigateToMyBooks() {
        // remember the current fragment
        Fragment currentFragment = getChildFragmentManager()
                .findFragmentById(R.id.fragmentContainerView);

        // make the new fragment
        MyBooksFragment fragment = new MyBooksFragment();

        if (currentFragment instanceof OpenBooksFragment) {
            // Let the OpenBooksFragment save any important state
            ((OpenBooksFragment) currentFragment).saveScrollPosition();
        }

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to JournalFragment
    Not currently necessary--JournalFragment is the default and as such is always available on the back stack
     */
    public void navigateToJournal() {
        JournalFragment fragment = new JournalFragment();
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

    /*
    Method to navigate to a BookFragment
     */
    public void navigateToBook(Book book) {
        // make new fragment
        BookDetailFragment fragment = new BookDetailFragment(book);

        // pass a Book argument to the fragment
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        fragment.setArguments(args);

        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }

}