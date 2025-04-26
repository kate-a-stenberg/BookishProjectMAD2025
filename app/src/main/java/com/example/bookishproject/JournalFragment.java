package com.example.bookishproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.Entry;
import com.example.bookishproject.MainActivity;
import com.example.bookishproject.RecyclerAdapterJournal;
import com.example.bookishproject.databinding.FragmentJournalBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
This class represents a JournalFragment.
A journal fragment displays information about the user's reading journal entries in a recycler view.
It has view binding, a layout manager, a recycler view, a recycler adapter (journal-specific), an array list of journal entries,
a journal firebase helper, and a floating action button.
 */
public class JournalFragment extends Fragment implements RecyclerAdapterJournal.OnNoteListener, ColorUpdatable {

    private FragmentJournalBinding binding;
    private LinearLayoutManager layoutManager;
    private RecyclerView rview;
    private RecyclerAdapterJournal adapter;
    private List<Entry> entryList = new ArrayList<>();
    private JournalFirebaseHelper fbHelper;
    private ProgressBar progressBar;
    private TextView noEntries;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalBinding.inflate(inflater, container, false);

        // setting variables
        rview = binding.rview;
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
        updateColors();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).setToolbar(this);
            updateColors();
        }

        loadEntries();

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
            adapter.toggleExpansion(position);
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
        if (getActivity() instanceof MainActivity) {
            // ask the MainActivity to go to JournalEntryFragment based on the entry clicked
            ((MainActivity) getActivity()).getNavigator().navigateToJournalEntry(entry);
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
    }

    /*
    Method to load Entries into the fragment
     */
    protected void loadEntries() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

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
                    entryList.clear();
                    entryList.addAll(entries);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();

                        // Show empty state or content based on results
                        if (entryList.isEmpty()) {
                            noEntries.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    public void filterByTitle(String title) {
        // TODO: implement this
    }

    @Override
    public void updateColors() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.applyThemeColors(binding.getRoot(), activity.getCurrentSection());

            progressBar.setIndeterminateTintList(ColorStateList.valueOf(activity.currentInterestColor));
            progressBar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

}