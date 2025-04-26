package com.example.bookishproject;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;

public class Navigator {

    private AppToolbarProvider provider;
    private Activity activity;
    private ViewPager2 viewPager;
    private VPAdapter vpAdapter;

    public Navigator(Activity activity) {
        this.activity = activity;
    }

    public Navigator(Activity activity, AppToolbarProvider provider) {
        this.activity = activity;
        this.provider = provider;
        this.viewPager = provider.getViewPager();
        this.vpAdapter = provider.getVPAdapter();
    }

    // GETTERS AND SETTERS

    public AppToolbarProvider getProvider() {
        return this.provider;
    }
    public void setProvider(AppToolbarProvider provider) {
        this.provider = provider;
        this.viewPager = provider.getViewPager();
        this.vpAdapter = provider.getVPAdapter();
    }

    private void vpSwap(Fragment fragment) {
        int currentPosition = viewPager.getCurrentItem();
        vpAdapter.replaceFragment(currentPosition, fragment);
        viewPager.setAdapter(vpAdapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // NAVIGATION METHODS

    public void navigateToBookFragment(Book book) {
        BookFragment fragment = new BookFragment(book);

        Bundle args = new Bundle();
        args.putParcelable("book", book);
        fragment.setArguments(args);

        vpSwap(fragment);
    }

    public void navigateToBooksFragment() {
        BooksFragment booksFragment = new BooksFragment();
        vpSwap(booksFragment);
    }

    public void navigateToBookResultsFragment(String query) {
        BookResultsFragment fragment = BookResultsFragment.newInstance(query);

        // Use post to ensure it's not interrupting an existing transition
        viewPager.post(() -> vpSwap(fragment));

    }

    public void navigateToBookSearchFragment() {
        BookSearchFragment fragment = new BookSearchFragment();
        vpSwap(fragment);
    }

    public void navigateToHabitsFragment() {
        HabitsFragment fragment = new HabitsFragment();
        vpSwap(fragment);
    }

    public void navigateToJournalEntry(Entry entry) {
        JournalEntryFragment fragment = new JournalEntryFragment(entry);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("entry", entry);
        args.putBoolean("edit_mode", false); // Set to view mode
        fragment.setArguments(args);

        vpSwap(fragment);
    }

    public void navigateToJournalEntry(Book book) {
        JournalEntryFragment fragment = new JournalEntryFragment(book);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        args.putBoolean("edit_mode", true); // Set to view mode
        fragment.setArguments(args);

        vpSwap(fragment);
    }

    public void navigateToJournalFragment() {
        JournalFragment fragment = new JournalFragment();
        vpSwap(fragment);
    }

    public void navigateToMatchOptionsFragment(List<Book> books) {
        MatchOptionsFragment fragment = new MatchOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("BOOK_RESULTS", new ArrayList<>(books));
        fragment.setArguments(args);

        // Get current ViewPager position (should be tab 2 for Recs)
        vpSwap(fragment);
    }

    public void navigateToMatchResultsFragment(List<Book> matchingBooks) {

        MatchResultsFragment fragment = new MatchResultsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("MATCHING_BOOKS", new ArrayList<>(matchingBooks));
        fragment.setArguments(args);

        vpSwap(fragment);

    }

    public void navigateToMatchSearchFragment() {
        MatchSearchFragment fragment = new MatchSearchFragment();
        vpSwap(fragment);
    }

    public void navigateToMyBooksFragment() {
        MyBooksFragment fragment = new MyBooksFragment();
        vpSwap(fragment);
    }

    public void navigateToOpenBooksFragment() {
        OpenBooksFragment fragment = new OpenBooksFragment();
        vpSwap(fragment);
    }

    public void navigateToRecsFragment() {
        RecsFragment fragment = new RecsFragment();

        vpSwap(fragment);
    }

}
