package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentMatchOptionsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
This class represents a MatchOptionsFragment.
A match options fragment displays the options that the user can match. In MatchSearchFragment the user entered search terms,
and in MatchOptionsFragment they are returned a list of Books that match and choose which one they would like to find matches for.
It has view binding, a recycler view, a recycler view adapter (specific to Books), an array list of search results, and layout fields and elements.
It also has a static final String attribute.
 */
public class MatchOptionsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private FragmentMatchOptionsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private ImageButton backButton;
    private ProgressBar progressBar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentMatchOptionsBinding.inflate(inflater, container, false);

        rView = binding.rview;
        backButton = binding.buttonMatchOptionsBack;
        progressBar = binding.progressBar; // Initialize the progress bar

        setupRecyclerView();

        adapter.setOnNoteListener(this);


        if (getArguments() != null && getArguments().containsKey("BOOK_RESULTS")) {
            // the passedBooks list that this fragment will used can be found by getting the parcelable array labeled "BOOK_RESULTS" from getArguments()
            List<Book> passedBooks = getArguments().getParcelableArrayList("BOOK_RESULTS");
            if (passedBooks != null && !passedBooks.isEmpty()) {
                // clear everything and then add passedBooks back to the view
                results.clear();
                results.addAll(passedBooks);
                adapter.notifyDataSetChanged();
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Tap and hold a book to find matches", Toast.LENGTH_LONG).show();

        // set a listener for the back button - go back to MatchSearchFragment
        backButton.setOnClickListener(v -> {
            // Ask MainActivity to navigate back to MatchOptionsFragment
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToMatchSearchFragment();
            }
        });

    }

    /*
    Method to determine what a click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with clicks.
    A click will expand/collapse the card clicked
    */
    @Override
    public void onNoteClick(Book book) {

        // need to find the position of this book in the list
        int position = results.indexOf(book);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }
    }

    /*
    Method to determine what a long click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will find similar books to the book on the clicked card
     */
    @Override
    public void onNoteLongClick(Book book) {
        Toast.makeText(getContext(), "Finding similar books to: " + book.getTitle(), Toast.LENGTH_SHORT).show();
        findSimilarBooks(book);
    }
    private void setupRecyclerView() {
        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        rView.setAdapter(adapter);
    }

    /*
    Method to find similar books to a selected book
     */
    private void findSimilarBooks(Book selectedBook) {

        // Get the BookFirebaseHelper instance
        BookFirebaseHelper fbHelper = new BookFirebaseHelper();

        // Get all books from Firebase to compare with
        fbHelper.getAllBooks(allBooks -> {

            // Create a list to store matching books
            List<Book> matchingBooks = new ArrayList<>();

            // Create a BookComparator instance
            Comparer comparator = new ComparerBasic(selectedBook);

            // Loop through all books and find matches
            for (Book book : allBooks) {
                // Skip the selected book itself
                if (book.getApiId() != null && book.getApiId().equals(selectedBook.getApiId())) {
                    continue;
                }

                // Check if the books are similar based on the comparison criteria
                float matchScore = comparator.compareBooks(book);

                // if a book is over 50% similar, add it to the matchingBooks list
                // TODO: allow user to decide if they want only very similar results or a wider search
                if (matchScore >= 0.5) {
                    matchingBooks.add(book);
                }

            }

            // Use MainActivity to navigate to results fragment based on the selectedBook and matchingBooks list
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMatchResultsFragment(selectedBook, matchingBooks);

            }
        });
    }

}