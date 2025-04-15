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

public class MatchSearchFragment extends Fragment {
    private static final String TAG = "Match Search"; // For logging

    public interface OnSearchListener { // interface for sending chosen book back to activity
        void onSearchQuery(String query);
    }

    private FragmentMatchSearchBinding binding;
    private EditText inputTitle, inputAuthor;
    private Button buttonSearch;
    private ImageButton buttonBack;
    private OnSearchListener searchListener;

    public MatchSearchFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BookSearchFragment newInstance(String param1, String param2) {
        BookSearchFragment fragment = new BookSearchFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

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

        binding = FragmentMatchSearchBinding.inflate(inflater, container, false);

        inputTitle = binding.textTitleSearch;
        inputAuthor = binding.textAuthorSearch;
        buttonSearch = binding.buttonBookSearch;
        buttonBack = binding.buttonBack;

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttonBack.setOnClickListener(v -> {
//            if (getActivity() != null) {
////                // Add debug logging
////                System.out.println("BookSearchFragment: Back button clicked");
//
//                // Pop the backstack
//                getActivity().getSupportFragmentManager().popBackStack();
//
//                // Handle visibility changes directly
//                if (getActivity() instanceof MainActivity) {
//                    MainActivity activity = (MainActivity) getActivity();
//                    activity.showViewPager();  // Create this method in MainActivity
//                }
//            }

            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Create a new instance of RecsFragment
                RecsFragment recsFragment = new RecsFragment();

                // Get the current position in the ViewPager (should be 2 for the Recs tab)
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with the RecsFragment
                ((MainActivity) getActivity()).getAdapter().replaceFragment(currentPosition, recsFragment);

                // This will refresh the ViewPager with the new fragment
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
        });

        buttonSearch.setOnClickListener(v -> {
            Log.d(TAG, "Search button pressed. starting performInternalSearch()");
            performInternalSearch();
        });

    }

    private void performInternalSearch() {

        Log.d(TAG, "inside performInternalSearch");
        // start new MatchSearchResultsFragment based on the new list

        String title = inputTitle.getText().toString().trim().toLowerCase();
        String author = inputAuthor.getText().toString().trim().toLowerCase();

        if (title.isEmpty() && author.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }

        BookFirebaseHelper fbHelper = new BookFirebaseHelper();
        Log.d(TAG, "entering fbHelper.getAllBooks()");

        // Get all books from Firebase and filter locally
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {
                // Filter books based on title and author
                List<Book> filteredBooks = new ArrayList<>();
                Log.d(TAG, "created filteredBooks. now about to look for matches");

                for (Book book : allBooks) {
                    boolean titleMatch = title.isEmpty() ||
                            (book.getTitle() != null && book.getTitle().toLowerCase().contains(title));

                    boolean authorMatch = author.isEmpty() ||
                            (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(author));

                    // Add book if both title and author match (or if respective field was empty)
                    if (titleMatch && authorMatch) {
                        filteredBooks.add(book);
                    }
                }
                Log.d(TAG, "looked for matches. about to check for getActivity()");

                // Now pass these filtered results to your MatchSearchResultsFragment
                if (getActivity() == null) {
                    Log.d(TAG, "getActivity() is null");
                    return;
                }
                Log.d(TAG, "getActivity() not null. about to check if filteredBooks is empty");

                if (filteredBooks.isEmpty()) {
                    Log.d(TAG, "filteredBooks is empty");

                    Toast.makeText(getContext(), "No matching books found in your collection", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "filteredBooks not empty. about to check if getActivity() is instanceof MainActivity");
                    // Create a new fragment with the results or update existing one
                    if (getActivity() instanceof MainActivity) {
                        Log.d(TAG, "calling navigateToMatchOptions");
                        ((MainActivity)getActivity()).navigateToMatchOptionsFragment(filteredBooks);
                    }
                }
            }
        });
        Log.d(TAG, "exited fbHelper.getAllBooks()");
    }

    public void setOnSearchListener(MatchSearchFragment.OnSearchListener listener) {
        this.searchListener = listener;
    }
}