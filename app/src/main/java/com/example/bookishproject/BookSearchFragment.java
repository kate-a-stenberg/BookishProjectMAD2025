package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBookSearchBinding;

/*
This is a class for a BookSearchFragment.
A BookSearchFragment allows the user to enter information to create a query to search through an API.
It has view binding and layout fields and elements.
 */
public class BookSearchFragment extends Fragment {

    private FragmentBookSearchBinding binding;
    private EditText inputTitle, inputAuthor;
    private ImageButton buttonBack;
    private Button buttonSearch;

    /*
    empty constructor
     */
    public BookSearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentBookSearchBinding.inflate(inflater, container, false);

        // set variables for layout fields and elements
        inputTitle = binding.textTitleSearch;
        inputAuthor = binding.textAuthorSearch;
        buttonSearch = binding.buttonBookSearch;
        buttonBack = binding.buttonBack;

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // the back button will ask the MainActivity to go to BooksFragment
        buttonBack.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity)getActivity()).navigateToBooksFragment();
            }
        });

        // the search button will search
        buttonSearch.setOnClickListener(v -> {
            System.out.println("Search button pressed");
            performSearch();
        });

    }

    /*
    Method to create a search query to pass to BookResultsFragment
     */
    private void performSearch() {

        // get title and author strings from user input
        String title = inputTitle.getText().toString().trim();
        String author = inputAuthor.getText().toString().trim();

        // if the user didn't enter anything, roast them
        if (title.isEmpty() && author.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }

        // get ready to make a new StringBuilder! this will be the query
        StringBuilder queryBuilder = new StringBuilder();
        // if there's a title
        if (!title.isEmpty()) {
            // include the title in the query
            queryBuilder.append("intitle:").append(title);
        }
        // if there's an author
        if (!author.isEmpty()) {
            // and there's also a title already in there
            if (queryBuilder.length() > 0) {
                // gotta use a plus sign
                queryBuilder.append("+");
            }
            // otherwise just add the author to the query
            queryBuilder.append("inauthor:").append(author);
        }

        // make a String out of this StringBuilder
        String query = queryBuilder.toString();

        if (getActivity() instanceof MainActivity) {
            // then ask the MainActivity to go to a BookResultsFragment using this query to populate its search
            ((MainActivity)getActivity()).navigateToBookResultsFragment(query);
        }
    }

}