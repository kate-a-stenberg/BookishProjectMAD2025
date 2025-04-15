package com.example.bookishproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.bookishproject.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    private static final String TAG = "Match Search"; // For logging

    private ActivityMainBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private VPAdapter adapter;
    private OnBackPressedCallback backCallback;
    private int currentTabPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewPager2 and its adapter
        viewPager = binding.viewPager;
        adapter = new VPAdapter(this, 4);
        viewPager.setAdapter(adapter);

        // Set up TabLayout with ViewPager2
        tabLayout = binding.tabLayout;
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(adapter.getPageTitle(position));
        }).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTabPosition = tab.getPosition();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentTabPosition = position;
            }
        });

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Monitor the back dispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                // This won't handle back, but will let us know if others don't handle it
                System.out.println("Monitor: Back press reached lowest priority handler");
            }
        });

        System.out.println("Back press monitor attached");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Make sure back handling is enabled
        if (backCallback != null && !backCallback.isEnabled()) {
            backCallback.setEnabled(true);
            System.out.println("Back callback re-enabled in onResume");
        }
    }

    // helper method to get BooksFragment
    public BooksFragment getBooksFragment() {
        return (BooksFragment) adapter.getFragment(1);
    }

    // used by BooksFragment
    public void navigateToBookSearchFragment() {
        BookSearchFragment bookSearchFragment = new BookSearchFragment();

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, bookSearchFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by BooksFragment, MatchResultsFragment
    public void navigateToBookFragment(Book book) {
        BookFragment bookFragment = new BookFragment(book);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("book", book);
        bookFragment.setArguments(args);

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, bookFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by BookSearchFragment
    public void navigateToBookResultsFragment(String query) {

        BookResultsFragment resultsFragment = BookResultsFragment.newInstance(query);

        int currentPosition = viewPager.getCurrentItem();

        // Use post to ensure we're not interrupting an existing transition
        viewPager.post(() -> {
            // Replace current fragment with the results fragment
            adapter.replaceFragment(currentPosition, resultsFragment);

            // Update ViewPager
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(currentPosition, false);
        });

    }

    // used by RecsFragment
    public void navigateToMatchSearchFragment() {
        MatchSearchFragment recsMatchFragment = new MatchSearchFragment();

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, recsMatchFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by MatchSearchFragment
    public void navigateToMatchOptionsFragment(List<Book> books) {
        Log.d(TAG, "inside navigateToMatchOptionsFragment()");
        MatchOptionsFragment fragment = new MatchOptionsFragment();
        Log.d(TAG, "created new MatchOptionsFragment. setting arguments");

        Bundle args = new Bundle();
        args.putParcelableArrayList("BOOK_RESULTS", new ArrayList<>(books));
        fragment.setArguments(args);

        // Get current ViewPager position (should be tab 2 for Recs)
        int currentPosition = viewPager.getCurrentItem();

        Log.d(TAG, "set arguments. about to replace fragment");

        // Replace current fragment with the new one
        adapter.replaceFragment(currentPosition, fragment);

        // Update ViewPager
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by MatchOptionsFragment
    public void navigateToMatchResultsFragment(Book selectedBook, List<Book> matchingBooks) {
        if (matchingBooks.isEmpty()) {
            Toast.makeText(this, "No similar books found in your collection", Toast.LENGTH_SHORT).show();
            return;
        }

        MatchResultsFragment resultsFragment = new MatchResultsFragment();

        Bundle args = new Bundle();
        args.putParcelable("SELECTED_BOOK", selectedBook);
        args.putParcelableArrayList("MATCHING_BOOKS", new ArrayList<>(matchingBooks));
        resultsFragment.setArguments(args);

        // Get current ViewPager position
        int currentPosition = viewPager.getCurrentItem();

        // Replace current fragment with the results fragment
        adapter.replaceFragment(currentPosition, resultsFragment);

        // Update ViewPager
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

    }

    // used by JournalEntryFragment (back)
    public void navigateToJournalFragment() {
        JournalFragment journalFragment = new JournalFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, journalFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by JournalFragment
    public void navigateToOpenBooksFragment() {
        OpenBooksFragment openBooksFragment = new OpenBooksFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, openBooksFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by OpenBooksFragment
    public void navigateToMyBooksFragment() {
        MyBooksFragment myBooksFragment = new MyBooksFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, myBooksFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    // used by JournalFragment
    public void navigateToJournalEntry(Entry entry) {
        // Create JournalEntryFragment instance with the selected Entry
        JournalEntryFragment journalEntryFragment = new JournalEntryFragment(entry);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("entry", entry);
        journalEntryFragment.setArguments(args);

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, journalEntryFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);

    }

    // used by OpenBooksFragment
    public void navigateToNewEntryFragment(Book book) {
        NewEntryFragment newEntryFragment = new NewEntryFragment();

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("selected_book", book);
        newEntryFragment.setArguments(args);

        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, newEntryFragment);

        int previousPosition = currentPosition;
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(previousPosition, false);
    }

    public void showViewPager() {
        System.out.println("MainActivity: showViewPager called");
        binding.fragmentContainerView.setVisibility(View.GONE);
        binding.viewPager.setVisibility(View.VISIBLE);

        // Force refresh the current tab
        binding.viewPager.post(() -> {
            int currentTab = binding.viewPager.getCurrentItem();
            // Temporarily move to another tab then back
            int tempTab = (currentTab + 1) % 4;
            binding.viewPager.setCurrentItem(tempTab, false);
            binding.viewPager.setCurrentItem(currentTab, false);
            System.out.println("ViewPager refreshed by cycling tabs");
        });
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public VPAdapter getAdapter() {
        return adapter;
    }

}