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

/*
This class represents the app's MainActivity.
It has view binding, a tab layout, a view pager, a view pager adapter
 */
public class MainActivity extends AppCompatActivity  {

    private ActivityMainBinding binding;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private VPAdapter adapter;

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

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
            }
        });

    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
    Helper method to get BooksFragment
     */
    public BooksFragment getBooksFragment() {
        return (BooksFragment) adapter.getFragment(1);
    }

    public ViewPager2 getViewPager() {
        return viewPager;
    }

    public VPAdapter getAdapter() {
        return adapter;
    }

    /*
    ALL NAVIGATION METHODS FROM HERE
    All navigation methods follow the same pattern. I'm not commenting all of them so listen up.
    1. create a new instance of the destination fragment
    2. (Optional) Pass data to the fragment using a Bundle
    3. get the current position from the view pager adapter
    4. ask the adapter to replace the current item with the new instance of the destination fragment
    5. save the current position as the previous position
    6. reset the view pager adapter
    7. ask the adapter again to stay in the current position
     */

    // used by BooksFragment
    public void navigateToBookSearchFragment() {
        BookSearchFragment bookSearchFragment = new BookSearchFragment();

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, bookSearchFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by BooksFragment, MatchResultsFragment
    public void navigateToBookFragment(Book book) {
        BookFragment bookFragment = new BookFragment(book);

        Bundle args = new Bundle();
        args.putParcelable("book", book);
        bookFragment.setArguments(args);

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, bookFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by BookSearchFragment
    public void navigateToBookResultsFragment(String query) {

        BookResultsFragment resultsFragment = BookResultsFragment.newInstance(query);

        int currentPosition = viewPager.getCurrentItem();

        // Use post to ensure it's not interrupting an existing transition
        viewPager.post(() -> {
            // Replace current fragment with the results fragment
            adapter.replaceFragment(currentPosition, resultsFragment);

            // Update ViewPager
            viewPager.setAdapter(adapter);
            viewPager.setCurrentItem(currentPosition, false);
        });

    }

    public void navigateToBooksFragment() {
        // For simplicity, we'll just go back to the search fragment
        // If you need to maintain state, you'd need to store the filtered books
        BooksFragment bookFragment = new BooksFragment();

        // Get the current position in the ViewPager
        int currentPosition = getViewPager().getCurrentItem();

        // Replace current fragment
        adapter.replaceFragment(currentPosition, bookFragment);

        // Update ViewPager
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by RecsFragment, MatchOptionsFragment, MatchResultsFragment
    public void navigateToMatchSearchFragment() {
        MatchSearchFragment recsMatchFragment = new MatchSearchFragment();

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, recsMatchFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by MatchSearchFragment
    public void navigateToMatchOptionsFragment(List<Book> books) {
        MatchOptionsFragment fragment = new MatchOptionsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("BOOK_RESULTS", new ArrayList<>(books));
        fragment.setArguments(args);

        // Get current ViewPager position (should be tab 2 for Recs)
        int currentPosition = viewPager.getCurrentItem();

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

    // used by MatchSearchFragment
    public void navigateToRecsFragment() {
        RecsFragment recsFragment = new RecsFragment();

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, recsFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by JournalEntryFragment (back)
    public void navigateToJournalFragment() {
        JournalFragment journalFragment = new JournalFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, journalFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by JournalFragment
    public void navigateToOpenBooksFragment() {
        OpenBooksFragment openBooksFragment = new OpenBooksFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, openBooksFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by OpenBooksFragment
    public void navigateToMyBooksFragment() {
        MyBooksFragment myBooksFragment = new MyBooksFragment();
        int currentPosition = viewPager.getCurrentItem();
        adapter.replaceFragment(currentPosition, myBooksFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

    // used by JournalFragment and NewEntryFragment
    public void navigateToJournalEntry(Entry entry) {
        // Create JournalEntryFragment instance with the selected Entry
        JournalEntryFragment journalEntryFragment = new JournalEntryFragment(entry);

        // pass data to the fragment using Bundle
        Bundle args = new Bundle();
        args.putParcelable("entry", entry);
        journalEntryFragment.setArguments(args);

        int currentPosition = viewPager.getCurrentItem();

        adapter.replaceFragment(currentPosition, journalEntryFragment);

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);

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

        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(currentPosition, false);
    }

}