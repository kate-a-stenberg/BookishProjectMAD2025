package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBookResultsBinding;
import com.example.bookishproject.databinding.FragmentBooksBinding;
import com.example.bookishproject.databinding.FragmentOpenBooksBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
This class represents an OpenBooksFragment.
An Open Books fragment displays the user's current reads in a recycler view.
It uses view binding, a recycler view, a RecyclerBooksAdapter, a list of results, layout elements, and a BookFirebaseHelper.
 */
public class OpenBooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private FragmentOpenBooksBinding binding;
    private BookFirebaseHelper fbHelper;
    private List<Book> results = new ArrayList<>();
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private LinearLayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView noBooks;
    private ExtendedFloatingActionButton add;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {// Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentOpenBooksBinding.inflate(inflater, container, false);

        rView = binding.recyclerView;
        add = binding.fabOpenBook;
        noBooks = binding.messageNoOpenBooks;
        progressBar = binding.progressBar;

        setupRecyclerView();

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCurrentReads();

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
                ((JournalHostFragment) getParentFragment()).navigateToMyBooks();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }

        loadCurrentReads(); // Refresh the list every time the fragment becomes visible

        if (rView != null) {
            // Use post to ensure logic happens after layout completes
            rView.post(() -> {
                if (adapter != null && results != null) {
                    // Force refresh the adapter when returning to the fragment
                    adapter.resetExpandedState();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
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
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with clicks.
    A click will expand/collapse the card clicked
    */
    @Override
    public void onNoteClick(Book book) {

        // find position in the list
        int position = results.indexOf(book);
        // if it's a valid position
        if (position != -1) {
            // ask the adapter to toggle expansion of the card at that position
            adapter.toggleExpansion(position);
        }

    }

    /*
    Method to determine what a long click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will ask MainActivity to open a NewEntryFragment based on the selected book.
    */
    @Override
    public void onNoteLongClick(Book book) {
        if (getParentFragment() instanceof JournalHostFragment) {
            ((JournalHostFragment) getParentFragment()).navigateToJournalEntry(book);
        }
    }

    private void setupRecyclerView() {
        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rView.setLayoutManager(gridLayoutManager);
    }

    /*
    Method to load the user's current reads
     */
    protected void loadCurrentReads() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

        // get a new BookFirebaseHelper
        fbHelper = new BookFirebaseHelper();

        // ask the firebaseHelper to get ALL the books from the database
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {
                // Clear existing results
                results.clear();

                // go through all the Books in allBooks
                for (Book book : allBooks) {
                    // if the Book status is "Currently reading"
                    if (book.getStatus() != null && "Currently reading".equals(book.getStatus())) {
                        // add it to our results list
                        results.add(book);
                    }
                }

                // Update UI on the main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // set progress bar to gone
                            progressBar.setVisibility(View.GONE);

                            if (results.isEmpty()) {
                                noBooks.setVisibility(View.VISIBLE);
                                rView.setVisibility(View.GONE);
                            } else {
                                noBooks.setVisibility(View.GONE);
                                rView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void performSearch(String query) {
        // TODO: write search logic
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

}