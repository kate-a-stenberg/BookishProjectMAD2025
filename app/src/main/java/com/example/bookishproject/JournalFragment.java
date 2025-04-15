package com.example.bookishproject;

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
import android.widget.Toast;

import com.example.bookishproject.Entry;
import com.example.bookishproject.MainActivity;
import com.example.bookishproject.RecyclerAdapterJournal;
import com.example.bookishproject.databinding.FragmentJournalBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JournalFragment extends Fragment implements RecyclerAdapterJournal.OnNoteListener {
    private static final String TAG = "JournalFragment"; // For logging


    FragmentJournalBinding binding;
    FragmentManager fragmentManager;
    private Parcelable recyclerViewState;
    private LinearLayoutManager layoutManager;
    private RecyclerView rview;
    private RecyclerAdapterJournal adapter;
    private List<Entry> entryList = new ArrayList<>();
    private FloatingActionButton addButton;
    JournalFirebaseHelper fbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalBinding.inflate(inflater, container, false);

        addButton = binding.fabAddEntry;
        rview = binding.rview;
        entryList = new ArrayList<>();
        fbHelper = new JournalFirebaseHelper();

        setupRecyclerView();
        loadEntries();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Tap and hold a journal entry for more details", Toast.LENGTH_LONG).show();

        // Set up the button click listener here
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity)getActivity()).navigateToOpenBooksFragment();
                }
            }
        });

        // TODO: add filter functionality for specific books
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume called. Entries count: " + (entryList != null ? entryList.size() : 0));

        loadEntries();

        // Force refresh the adapter when returning to the fragment
        // Use post to ensure it happens after layout completes
        if (rview != null) {
            rview.post(() -> {
                if (adapter != null && entryList != null) {
                    adapter.resetExpandedState();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified of data change after post");
                }
            });
        }
    }

    private void setupRecyclerView() {

        adapter = new RecyclerAdapterJournal(getContext(), entryList);
        adapter.setOnNoteListener(this);
        rview.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        binding.rview.setLayoutManager(layoutManager);
    }

    protected void loadEntries() {

        fbHelper.getAllEntries(new JournalFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Entry> entries) {

                if (getActivity() == null) {
                    return;
                }

                Log.d(TAG, "Received " + entries.size() + " entries from Firebase");
                for (Entry entry : entries) {
                    Log.d(TAG, "Entry: " + entry.getId() +
                            ", Book: " + (entry.getBook() != null ?
                            entry.getBook().getTitle() : "NULL") +
                            ", Type: " + (entry.getType() != null ?
                            entry.getType().toString() : "NULL"));
                }

                getActivity().runOnUiThread(() -> {

                    Collections.sort(entries, (entry1, entry2) -> {
                        // Assuming newer entries should be shown first
                        return entry2.getDate().compareTo(entry1.getDate());
                    });

                    entryList.clear();

                    for (int i = entries.size() - 1; i >= 0; i--) {
                        entryList.add(entries.get(i));
                    }
//                    entryList.addAll(entries);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();

                        // Hide loading indicator if needed
                        // binding.progressBar.setVisibility(View.GONE);

                        // Show empty state or content based on results
                        if (entryList.isEmpty()) {
                            // binding.emptyState.setVisibility(View.VISIBLE);
                            // binding.recyclerView.setVisibility(View.GONE);
                        } else {
                            // binding.emptyState.setVisibility(View.GONE);
                            // binding.recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                    }
                });
            }
        });
    }



    @Override
    public void onNoteClick(Entry entry) {
        int position = entryList.indexOf(entry);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }
    }

    @Override
    public void onNoteLongClick(Entry entry) {
        // Navigate to the entry details fragment
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToJournalEntry(entry);
        }
    }

    public void filterByTitle(String title) {
        // TODO: implement this
    }

}