package com.example.bookishproject;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.bookishproject.databinding.FragmentBooksBinding;

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
public class BooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private FragmentBooksBinding binding;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private RecyclerAdapterBooks adapter;
    private List<Book> bookList = new ArrayList<>();
    private BookFirebaseHelper fbHelper;
    private ProgressBar progressBar;
    private TextView noBooks;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBooksBinding.inflate(inflater, container, false);

        // setting variables for the Fragment
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
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar(this);
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
        if (getActivity() instanceof MainActivity) {
            // ask the MainActivity to go to BookFragment
            ((MainActivity) getActivity()).getNavigator().navigateToBookFragment(book);
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

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        // assign this layout manager to the recycler view
        binding.rview.setLayoutManager(layoutManager);
    }

    /*
    Method to load books into the fragment.
     */
    protected void loadBooks() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

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

                // clear the book list to avoid adding everything a million times
                bookList.clear();
                // add all books back
                bookList.addAll(books);

                if (adapter != null) {
                    adapter.notifyDataSetChanged();

                    // Show empty state or content based on results
                    if (bookList.isEmpty()) {
                        noBooks.setVisibility(View.VISIBLE);
                    }
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

}