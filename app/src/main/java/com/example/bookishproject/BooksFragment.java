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

public class BooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {
    private static final String TAG = "BooksFragment"; // For logging

    private FragmentBooksBinding binding;
    private FragmentManager fragmentManager;
    private Parcelable recyclerViewState;
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

        Log.d(TAG, "onViewCreated called");

        Toast.makeText(getContext(), "Tap and hold a book to see more details", Toast.LENGTH_LONG).show();

        // Set up the button click listener here
        addButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToBookSearchFragment();
            }

        });
        // Example: In your BooksFragment, add a filter button or dropdown
        binding.buttonBooksSearch.setOnClickListener(v -> {
            // Show a dialog with category options
            String[] categories = {"Fiction", "Fantasy", "Romance", "Science Fiction", "Mystery"};

            new AlertDialog.Builder(getContext())
                    .setTitle("Filter by Category")
                    .setItems(categories, (dialog, which) -> {
                        String selectedCategory = categories[which];
                        filterByCategory(selectedCategory);
                    })
                    .setNegativeButton("Clear Filter", (dialog, which) -> {
                        filterByCategory(null); // Clear the filter
                    })
                    .show();
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        Log.d(TAG, "onResume called. Books count: " + (bookList != null ? bookList.size() : 0));

        loadBooks();

        // Force refresh the adapter when returning to the fragment
        // Use post to ensure it happens after layout completes
        if (recyclerView != null) {
            recyclerView.post(() -> {
                if (adapter != null && bookList != null) {
                    adapter.resetExpandedState();
                    adapter.notifyDataSetChanged();
                    Log.d(TAG, "Adapter notified of data change after post");
                }
            });
        }
//        recyclerView.setVisibility(View.VISIBLE);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "Setting up RecyclerView");

        adapter = new RecyclerAdapterBooks(getContext(), bookList);
        adapter.setOnNoteListener(this);
        recyclerView.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        binding.rview.setLayoutManager(layoutManager);
    }

    protected void loadBooks() {

        Log.d(TAG, "Loading books from Firebase");

        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
             @Override
             public void onCallback(List<Book> books) {

                 if (getActivity() == null) {
                     Log.w(TAG, "Fragment not attached to activity");
                     return;
                 }

                 getActivity().runOnUiThread(() -> {

                     bookList.clear();
                     bookList.addAll(books);

                     Log.d(TAG, "Book list updated, now contains " + bookList.size() + " books");

                     if (adapter != null) {
                         adapter.notifyDataSetChanged();
                         Log.d(TAG, "Adapter notified of data change");

                         // Hide loading indicator if needed
                         // binding.progressBar.setVisibility(View.GONE);

                         // Show empty state or content based on results
                         if (bookList.isEmpty()) {
                             Log.d(TAG, "Book list is empty");
                             // binding.emptyState.setVisibility(View.VISIBLE);
                             // binding.recyclerView.setVisibility(View.GONE);
                         } else {
                             Log.d(TAG, "Book list has items");
                             // binding.emptyState.setVisibility(View.GONE);
                             // binding.recyclerView.setVisibility(View.VISIBLE);
                         }
                     } else {
                         Log.w(TAG, "Adapter is null");
                     }
                 });
             }
        });
    }

//    public void addBookToCollection (Book book) {
//        Log.d(TAG, "addBookToCollection called for: " + book.getTitle());
//        if (book.getStatus() == null) {
//            book.setStatus("Want to Read");
//            Log.d(TAG, "book status is null");
//            Log.d(TAG, book.getStatus());
//        } else if (book.getStatus().isEmpty()) {
//            Log.d(TAG, "book status is empty");
//            book.setStatus("Want to Read");
//        }
//        fbHelper.addBook(book);
//        Log.d(TAG, "called fbHelper.addBook(book)");
//        Toast.makeText(getContext(), "Book added to your collection", Toast.LENGTH_SHORT).show();
//        Log.d(TAG, "loadBooks called");
//        loadBooks();
//    }

    public void removeBookFromCollection(Book book) {
        if (book != null && book.getApiId() != null) {
            fbHelper.deleteBook(book.getApiId());
            Toast.makeText(getContext(), "Book removed from your collection", Toast.LENGTH_SHORT).show();
        }
    }

    public void updateBookInCollection(Book book) {
        if (book != null) {
//            fbHelper.updateBook(book);
        }
    }

    public void onNoteClick(Book book) {

        // Show toast for debugging
//        Toast.makeText(getActivity(), "Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();

        // Tell the adapter to expand/collapse this book's card
        // We need to find the position of this book in the list
        int position = bookList.indexOf(book);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }

    }

    @Override
    public void onNoteLongClick(Book book) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToBookFragment(book);
        }
    }

    public void refreshData() {
        if (adapter != null && bookList != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void filterByCategory(String category) {
        if (category == null || category.isEmpty()) {
            // No filter, load all books
            loadBooks();
            return;
        }

        fbHelper.getBooksWithCategory(category, new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> books) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    bookList.clear();
                    bookList.addAll(books);
                    adapter.notifyDataSetChanged();

                    if (bookList.isEmpty()) {
                        // Show empty state
                    } else {
                        // Show content
                    }
                });
            }
        });
    }

}