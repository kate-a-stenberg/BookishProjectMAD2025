package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.bookishproject.databinding.FragmentBooksBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;

/*
This is a class for a BooksFragment.
A BooksFragment displays information about books in the user's collection using a recycler view.
It uses view binding, a layout manager to manage the recycler view, a recycler view adapter (specific to Books),
an array list of Books, and a BookFirebaseHelper to manage connections with the database.
This Fragment implements the OnNoteListener interface
 */
public class BooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private FragmentBooksBinding binding;
    private BookFirebaseHelper fbHelper;
    private List<Book> bookList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapterBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView noBooks;
    private ExtendedFloatingActionButton addBook;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBooksBinding.inflate(inflater, container, false);

        // setting variables for the Fragment
        addBook = binding.fabAddBook;
        recyclerView = binding.rview;
        progressBar = binding.progressBar;
        noBooks = binding.messageNoBooks;
        bookList = new ArrayList<>();
        fbHelper = new BookFirebaseHelper();

        setupRecyclerView();
        loadBooks();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadBooks();

        // First check if we have a scroll state saved
        Bundle scrollState = getArguments() != null ?
                getArguments().getBundle("SCROLL_STATE") : null;

        // we do have a saved scroll state
        if (scrollState != null) {
            // make the saved list state into a parcelable
            Parcelable listState = scrollState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // have the layout manager restore that scroll state
                gridLayoutManager.onRestoreInstanceState(listState);
            }
        }
        // Then check saved instance state (for config changes)
        else if (savedInstanceState != null) {
            Parcelable listState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                gridLayoutManager.onRestoreInstanceState(listState);
            }
            selectedPosition = savedInstanceState.getInt(KEY_SELECTED_POSITION,
                    RecyclerView.NO_POSITION);
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }

        // if user taps the add button, go to BookSearchFragment
        addBook.setOnClickListener(v -> {
            if (getParentFragment() instanceof BooksHostFragment) {
                ((BooksHostFragment) getParentFragment()).navigateToBookSearch();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }

        loadBooks();

        if (recyclerView != null) {
            // Use post to ensure logic happens after layout completes
            recyclerView.post(() -> {
                if (adapter != null && bookList != null) {
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
        if (gridLayoutManager != null) {
            Parcelable listState = gridLayoutManager.onSaveInstanceState();
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
        int position = bookList.indexOf(book);
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
    A long click will open the BookFragment.
     */
    @Override
    public void onNoteLongClick(Book book) {
        if (getParentFragment() instanceof BooksHostFragment) {
            ((BooksHostFragment) getParentFragment()).navigateToBook(book);
        }
    }

    /*
    Helper method to set up the recycler view
     */
    private void setupRecyclerView() {

        // create a new adapter using this book list for data
        adapter = new RecyclerAdapterBooks(getContext(), bookList);
        // use this as the OnNoteListener for the recycler view adapter
        adapter.setOnNoteListener(this);
        // assign the adapter to the recycler view
        recyclerView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    /*
    Method to load books into the fragment.
     */
    protected void loadBooks() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

        // Save current scroll state before loading
        final Parcelable savedState = gridLayoutManager != null ?
                gridLayoutManager.onSaveInstanceState() : null;

        // get all the books from the database using the BooksFirebaseHelper
        fbHelper.getAllBooks(books -> {

            // check for null activity
            if (getActivity() == null) {
                return;
            }

            // moves operations from a background thread to the UI thread to update the recycler view with Books
            getActivity().runOnUiThread(() -> {

                // begone, progress bar!
                progressBar.setVisibility(View.GONE);

                // Don't clear and re-add if sizes match and content is same
                // This prevents unnecessary adapter refresh that resets position
                if (bookList.size() != books.size() || !bookList.containsAll(books)) {
                    bookList.clear();
                    bookList.addAll(books);
                    adapter.notifyDataSetChanged();
                }

                // Show empty state if needed
                if (bookList.isEmpty()) {
                    noBooks.setVisibility(View.VISIBLE);
                } else {
                    noBooks.setVisibility(View.GONE);
                }

                // Restore scroll position after data loaded
                if (savedState != null) {
                    gridLayoutManager.onRestoreInstanceState(savedState);
                }

            });
        });
    }

    /*
    Method to remove book from collection.
    Uses BookFirebaseHelper for logic
     */
    public void removeBookFromCollection(Book book) {
        // null check
        if (book != null && book.getApiId() != null) {
            // ask the BookFirebaseHelper to delete the book from the database
            fbHelper.deleteBook(book.getApiId());
            Toast.makeText(getContext(), "Book removed from your collection", Toast.LENGTH_SHORT).show();
        }
    }

    // Helper method to be called before navigation
    public void saveScrollPosition() {
        if (gridLayoutManager != null) {
            // Save the current position to a persistent field
            Bundle scrollState = new Bundle();
            Parcelable listState = gridLayoutManager.onSaveInstanceState();
            scrollState.putParcelable(KEY_RECYCLER_STATE, listState);

            // Store it with the fragment
            if (getArguments() == null) {
                setArguments(new Bundle());
            }
            getArguments().putBundle("SCROLL_STATE", scrollState);
        }
    }


    @Override
    public void performSearch(String query) {
        // TODO: write search logic for Books
    }
}