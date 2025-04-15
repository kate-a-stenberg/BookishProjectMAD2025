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

public class BookSearchFragment extends Fragment {

    public interface OnSearchListener { // interface for sending chosen book back to activity
        void onSearchQuery(String query);
    }

    private FragmentBookSearchBinding binding;
    private EditText inputTitle, inputAuthor;
    private ImageButton buttonBack;
    private Button buttonSearch;
    private OnSearchListener searchListener;

    public BookSearchFragment() {
        // Required empty public constructor
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

        binding = FragmentBookSearchBinding.inflate(inflater, container, false);

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

            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Create a new instance of RecsFragment
                BooksFragment recsFragment = new BooksFragment();

                // Get the current position in the ViewPager (should be 2 for the Recs tab)
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with the RecsFragment
                ((MainActivity) getActivity()).getAdapter().replaceFragment(currentPosition, recsFragment);

                // This will refresh the ViewPager with the new fragment
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }

//            if (getActivity() != null) {
//                // Add debug logging
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
        });

        buttonSearch.setOnClickListener(v -> {
            System.out.println("Search button pressed");
            performSearch();
        });

    }

    private void performSearch() {
        String title = inputTitle.getText().toString().trim();
        String author = inputAuthor.getText().toString().trim();

        if (title.isEmpty() && author.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuilder queryBuilder = new StringBuilder();
        if (!title.isEmpty()) {
            queryBuilder.append("intitle:").append(title);
        }
        if (!author.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append("+");
            }
            queryBuilder.append("inauthor:").append(author);
        }

        String query = queryBuilder.toString();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity)getActivity()).navigateToBookResultsFragment(query);
        }
    }

    public void setOnSearchListener(OnSearchListener listener) {
        this.searchListener = listener;
    }

}