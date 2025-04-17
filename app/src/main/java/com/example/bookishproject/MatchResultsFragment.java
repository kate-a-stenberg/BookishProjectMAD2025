package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentMatchOptionsBinding;
import com.example.bookishproject.databinding.FragmentMatchResultsBinding;
import com.example.bookishproject.databinding.FragmentMatchSearchBinding;

import java.util.ArrayList;
import java.util.List;

/*
This class represents a MatchResultsFragment.
A match results fragment displays the results of a user's Match search in a recycler view.
It uses view binding, a recycler view, a RecyclerAdapterBooks, a list of results, a layout manager, layout fields and elements, and
 */
public class MatchResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private FragmentMatchResultsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private ImageButton backButton;
    private LinearLayoutManager layoutManager;

    /*
    Required empty constructor
     */
    public MatchResultsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMatchResultsBinding.inflate(inflater, container, false);

        // set layout field variables
        rView = binding.rview;
        adapter = new RecyclerAdapterBooks();
        backButton = binding.buttonMatchResultsBack;

        if (getArguments() != null) {
            // the list of books for this fragment, matchingBooks, can be found in the parcel labeled "MATCHING_BOOKS" in the arguments
            ArrayList<Book> matchingBooks = getArguments().getParcelableArrayList("MATCHING_BOOKS");

            // clear current results and add all the matchingBooks back in
            if (matchingBooks != null && !matchingBooks.isEmpty()) {
                results.clear();
                results.addAll(matchingBooks);
            }
        }

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Tap and hold a book to add to your collection", Toast.LENGTH_LONG).show();

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMatchSearchFragment();
            }
        });

    }

    private void setupRecyclerView() {

        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        rView.setLayoutManager(layoutManager);
    }

    /*
    Method to determine what a click does.
    This is a method from the RecyclerAdapterBooks.OnNoteListener interface
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
    This is a method from the RecyclerAdapterBooks.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will open a book display, similar to a BookFragment.
    */
    @Override
    public void onNoteLongClick(Book book) {
        // TODO: this will navigate to a book display, but it has to be different from BookFragment so that it can have a different back button that goes back to MatchResultsFragment rather than BooksFragment
    }

}