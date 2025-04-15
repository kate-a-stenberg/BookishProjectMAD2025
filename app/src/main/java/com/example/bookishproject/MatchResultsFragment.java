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

public class MatchResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private FragmentMatchResultsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private ImageButton backButton;
    private LinearLayoutManager layoutManager;
    private Book selectedBook;

    public MatchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMatchResultsBinding.inflate(inflater, container, false);

        rView = binding.rview;
        adapter = new RecyclerAdapterBooks();
        backButton = binding.buttonMatchResultsBack;

        Log.d("MatchResultsFragment", "onCreateView called");

        if (getArguments() != null) {
            selectedBook = getArguments().getParcelable("SELECTED_BOOK");
            ArrayList<Book> matchingBooks = getArguments().getParcelableArrayList("MATCHING_BOOKS");

            // DEBUGGING
            Log.d("MatchResultsFragment", "Arguments received. Selected book: " +
                    (selectedBook != null ? selectedBook.getTitle() : "null"));
            Log.d("MatchResultsFragment", "Matching books: " +
                    (matchingBooks != null ? matchingBooks.size() : "null"));

//            if (selectedBook != null) {
//                comparisonTitleText.setText("Books similar to: " + selectedBook.getTitle());
//            }

            if (matchingBooks != null && !matchingBooks.isEmpty()) {
                results.clear();
                results.addAll(matchingBooks);
                // Note: We'll notify the adapter after setting it up
            }
        } else {
            Log.d("MatchResultsFragment", "No arguments received");
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
                MainActivity activity = (MainActivity) getActivity();

                // Create a new instance of BookSearchFragment
                MatchSearchFragment searchFragment = new MatchSearchFragment();

                // Get current position in ViewPager
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with BookSearchFragment
                activity.getAdapter().replaceFragment(currentPosition, searchFragment);

                // Update ViewPager
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
//            if (getActivity() != null) {
//                getActivity().getSupportFragmentManager().popBackStack();
//                if (getActivity() instanceof MainActivity) {
//                    ((MainActivity) getActivity()).showViewPager();
//                }
//            }
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

    @Override
    public void onNoteClick(Book book) {

        // Tell the adapter to expand/collapse this book's card
        // We need to find the position of this book in the list
        int position = results.indexOf(book);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }
    }

    @Override
    public void onNoteLongClick(Book book) {
        // TODO: this will navigate to a book display, but it has to be different from BookFragment so that it can have a different back button that goes back to MatchResultsFragment rather than BooksFragment
//        if (getActivity() instanceof MainActivity) {
//            ((MainActivity) getActivity()).navigateToBookFragment(book);
//        }
    }

}