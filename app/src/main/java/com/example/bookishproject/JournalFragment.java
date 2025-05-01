package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.bookishproject.databinding.FragmentJournalBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/*
This class represents a JournalFragment.
A journal fragment displays information about the user's reading journal entries in a recycler view.
It has view binding, a layout manager, a recycler view, a recycler adapter (journal-specific), an array list of journal entries,
a journal firebase helper, and a floating action button.
 */
public class JournalFragment extends Fragment implements RecyclerAdapterJournal.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private FragmentJournalBinding binding;
    private ExtendedFloatingActionButton add;
    private LinearLayoutManager layoutManager;
    private RecyclerView rview;
    private RecyclerAdapterJournal adapter;
    private List<Entry> entryList = new ArrayList<>();
    private JournalFirebaseHelper fbHelper;
    private ProgressBar progressBar;
    private TextView noEntries;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private String currentSearchQuery = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalBinding.inflate(inflater, container, false);

        // setting variables
        rview = binding.rview;
        add = binding.fabAddEntry;
        progressBar = binding.progressBar;
        noEntries = binding.messageNoEntries;
        entryList = new ArrayList<>();
        fbHelper = new JournalFirebaseHelper();

        setupRecyclerView();
        loadEntries();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // First check if we have state in arguments (from navigation)
        Bundle scrollState = getArguments() != null ?
                getArguments().getBundle("SCROLL_STATE") : null;

        if (scrollState != null) {
            Parcelable listState = scrollState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // Restore from navigation
                layoutManager.onRestoreInstanceState(listState);
            }
        }

        // Then check saved instance state (for config changes)
        else if (savedInstanceState != null) {
            Parcelable listState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                layoutManager.onRestoreInstanceState(listState);
            }
            selectedPosition = savedInstanceState.getInt(KEY_SELECTED_POSITION,
                    RecyclerView.NO_POSITION);
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }

        add.setOnClickListener(v -> {
            if (getParentFragment() instanceof JournalHostFragment) {
                ((JournalHostFragment) getParentFragment()).navigateToOpenBooks();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        loadEntries();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar(this);
        }

        if (rview != null) {
            rview.post(() -> {
                // Use post to ensure the operation happens after layout completes
                if (adapter != null && entryList != null) {
                    // Force refresh the adapter when returning to the fragment
                    adapter.resetExpandedState();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save RecyclerView state
        if (layoutManager != null) {
            Parcelable listState = layoutManager.onSaveInstanceState();
            outState.putParcelable(KEY_RECYCLER_STATE, listState);
            outState.putInt(KEY_SELECTED_POSITION, selectedPosition);
            outState.putString(KEY_SEARCH_QUERY, currentSearchQuery);
        }

    }

    /*
    Method to determine what a click does.
    This is a method from the RecyclerAdapterJournal.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with clicks.
    A click will expand/collapse the card clicked
    */
    @Override
    public void onNoteClick(Entry entry) {

        // find position in the list
        int position = entryList.indexOf(entry);
        // if it's a valid position
        if (position != -1) {
            // ask the adapter to toggle expansion of the card at that position
            if (entry.getComments() != null && !entry.getComments().isEmpty()) {
                adapter.toggleExpansion(position);

            }
        }
    }

    /*
    Method to determine what a long click does.
    This is a method from the RecyclerAdapterJournal.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will open a JournalEntryFragment.
     */
    @Override
    public void onNoteLongClick(Entry entry) {
        if (getParentFragment() instanceof JournalHostFragment) {
            ((JournalHostFragment) getParentFragment()).navigateToJournalEntry(entry);
        }
    }

    /*
    Helper method to set up the recycler view
     */
    private void setupRecyclerView() {

        // create a new adapter using this entry list for data
        adapter = new RecyclerAdapterJournal(getContext(), entryList);
        // use this as the OnNoteListener for the recycler view adapter
        adapter.setOnNoteListener(this);
        // assign the adapter to the recycler view
        rview.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        // assign this layout manager to the recycler view
        binding.rview.setLayoutManager(layoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rview.getContext(), LinearLayoutManager.VERTICAL);
        rview.addItemDecoration(dividerItemDecoration);
    }

    /*
    Method to load Entries into the fragment
     */
    protected void loadEntries() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

        // Save current scroll state before loading
        final Parcelable savedState = layoutManager != null ?
                layoutManager.onSaveInstanceState() : null;

        // get all the entris from the database using the JournalFirebaseHelper
        fbHelper.getAllEntries(new JournalFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Entry> entries) {

                // check for null activity
                if (getActivity() == null) {
                    return;
                }

                // moves operations from a background thread to the UI thread to update the recycler view with Books
                getActivity().runOnUiThread(() -> {

                    // set progress bar gone
                    progressBar.setVisibility(View.GONE);

                    // clear the entry list to avoid adding everything a million times
                    if (entryList.size() != entries.size() || !entryList.containsAll(entries)) {
                        entryList.clear();
                        entryList.addAll(entries);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                    }

                    // Show empty state or content based on results
                    if (entryList.isEmpty()) {
                        noEntries.setVisibility(View.VISIBLE);
                    }
                    else {
                        noEntries.setVisibility(View.GONE);
                    }

                    // Restore scroll position after data loaded
                    if (savedState != null) {
                        layoutManager.onRestoreInstanceState(savedState);
                    }

                });
            }
        });
    }

    // Helper method to be called before navigation
    public void saveScrollPosition() {
        if (layoutManager != null) {
            // Save the current position to a persistent field
            Bundle scrollState = new Bundle();
            Parcelable listState = layoutManager.onSaveInstanceState();
            scrollState.putParcelable(KEY_RECYCLER_STATE, listState);

            // Store it with the fragment
            if (getArguments() == null) {
                setArguments(new Bundle());
            }
            getArguments().putBundle("SCROLL_STATE", scrollState);
        }
    }

    public void filterByTitle(String title) {
        // TODO: implement this
    }

    @Override
    public void performSearch(String query) {
        // TODO: write search logic
    }
}