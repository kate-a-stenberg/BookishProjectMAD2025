package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentMatchSearchBinding;

import java.util.ArrayList;
import java.util.List;

/*
This class represents a MatchSearchFragment.
A MatchSearchFragment is for the user to enter a book (by title and/or author) that they would like similar books to.
It has view binding and layout fields and elements.
 */
public class MatchSearchFragment extends Fragment {

    private FragmentMatchSearchBinding binding;
    private EditText inputTitle, inputAuthor;
    private Button buttonSearch;
    private ImageButton buttonBack;

    /*
    Required empty constructor
    */
    public MatchSearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentMatchSearchBinding.inflate(inflater, container, false);

        // assign layout element variables
        inputTitle = binding.textTitleSearch;
        inputAuthor = binding.textAuthorSearch;
        buttonSearch = binding.buttonBookSearch;
        buttonBack = binding.buttonBack;

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // back button will ask MainActivity to go back to RecsFragment
        buttonBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToRecsFragment();
            }
        });

        // search button will perform internal search
        buttonSearch.setOnClickListener(v -> {
            performInternalSearch();
        });

    }

    /*
    Method to search the user's book collection (firebase database) for books matching search terms
    Will send resulting list to MainActivity to populate a MatchOptionsFragment
     */
    private void performInternalSearch() {

        // parse the input fields for search
        String title = inputTitle.getText().toString().trim().toLowerCase();
        String author = inputAuthor.getText().toString().trim().toLowerCase();

        if (title.isEmpty() && author.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }

        // get a new BookFirebaseHelper
        BookFirebaseHelper fbHelper = new BookFirebaseHelper();

        // Get all books from Firebase and filter locally
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {
                // New array list that will hold filtered books
                List<Book> filteredBooks = new ArrayList<>();

                // for each book in the book database
                for (Book book : allBooks) {
                    // if either the title is empty or the book title contains the search title
                    boolean titleMatch = title.isEmpty() || (book.getTitle() != null && book.getTitle().toLowerCase().contains(title));
                    // if either the author is empty or the book author contains the search author
                    boolean authorMatch = author.isEmpty() || (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(author));

                    // Add book if both title and author match (or if respective field was empty)
                    if (titleMatch && authorMatch) {
                        filteredBooks.add(book);
                    }
                }

                if (getActivity() == null) {
                    return;
                }

                if (filteredBooks.isEmpty()) {
                    Toast.makeText(getContext(), "No matching books found in your collection", Toast.LENGTH_SHORT).show();
                }
                else {
                    // ask MainActivity to go to a new MatchOptionsFragment based on the filteredBooks list we made
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity)getActivity()).navigateToMatchOptionsFragment(filteredBooks);
                    }
                }
            }
        });
    }

}