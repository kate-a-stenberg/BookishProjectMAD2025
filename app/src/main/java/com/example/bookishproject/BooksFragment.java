package com.example.bookishproject;

import android.app.AlertDialog;
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
import com.example.bookishproject.databinding.FragmentBooksBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;

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
    private FloatingActionButton addButton;
    private BookFirebaseHelper fbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBooksBinding.inflate(inflater, container, false);

        // setting variables for the Fragment
        addButton = binding.buttonBooksAdd;
        recyclerView = binding.rview;
        bookList = new ArrayList<>();
        fbHelper = new BookFirebaseHelper();

        setupRecyclerView();
        loadBooks();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        Log.d(TAG, "onViewCreated called");

        // instructions for this fragment
        Toast.makeText(getContext(), "Tap and hold a book to see more details", Toast.LENGTH_LONG).show();

        // Set up the button click listener here
        addButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                // ask MainActivity to navigate to BookSearchFragment
                ((MainActivity)getActivity()).navigateToBookSearchFragment();
            }
        });

        binding.buttonBooksSearch.setOnClickListener(v -> {
            // TODO: filter by title, author, genre, category, or pub date
        });

    }

    @Override
    public void onResume() {
        super.onResume();

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
            ((MainActivity) getActivity()).navigateToBookFragment(book);
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

        // get all the books from the database using the BooksFirebaseHelper
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
             @Override
             public void onCallback(List<Book> books) {

                 // check for null activity
                 if (getActivity() == null) {
                     return;
                 }

                 // moves operations from a background thread to the UI thread to update the recycler view with Books
                 getActivity().runOnUiThread(() -> {

                     // clear the book list to avoid adding everything a million times
                     bookList.clear();
                     // add all books back
                     bookList.addAll(books);

                     if (adapter != null) {
                         adapter.notifyDataSetChanged();

                         // Show empty state or content based on results
                         if (bookList.isEmpty()) {
                             // TODO: add empty message to user so they don't wait forever
                         }
                     }
                 });
             }
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

    /*
    Method to update a book in the database
    Currently not used
     */
    public void updateBookInCollection(Book book) {
        if (book != null) {
            // going to ask the BookFirebaseHelper to do it but ran out of time to make sure this works properly.
            // Also there's currently no need for it here so whatever
//            fbHelper.updateBook(book);
        }
    }

}