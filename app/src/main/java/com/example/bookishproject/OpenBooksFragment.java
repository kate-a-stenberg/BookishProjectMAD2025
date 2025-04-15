package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBookResultsBinding;
import com.example.bookishproject.databinding.FragmentBooksBinding;
import com.example.bookishproject.databinding.FragmentOpenBooksBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class OpenBooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    FragmentOpenBooksBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ImageButton backButton;
    private FloatingActionButton addNewBook;
    private BookFirebaseHelper fbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {// Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentOpenBooksBinding.inflate(inflater, container, false);

        rView = binding.recyclerView;
        noResults = binding.messageNoResults;
        backButton = binding.buttonBack;
        addNewBook = binding.fabAddNewCurrentRead;

        setupRecyclerView();

        adapter.setOnNoteListener(this);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadCurrentReads();

        Toast.makeText(getContext(), "Tap and hold a book to create new journal entry", Toast.LENGTH_LONG).show();

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Create a new instance of RecsFragment
                JournalFragment journalFragment = new JournalFragment();

                // Get the current position in the ViewPager (should be 2 for the Recs tab)
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with the RecsFragment
                ((MainActivity) getActivity()).getAdapter().replaceFragment(currentPosition, journalFragment);

                // This will refresh the ViewPager with the new fragment
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

        addNewBook.setOnClickListener(v -> {
            if (getActivity() != null) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity)getActivity()).navigateToMyBooksFragment();
                }
            }
            // findCurrentReads();
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        loadCurrentReads(); // Refresh the list every time the fragment becomes visible
    }

    private void setupRecyclerView() {
        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        rView.setAdapter(adapter);
    }

    protected void loadCurrentReads() {
        // Show loading indicator or placeholder
        noResults.setText("Loading your current reads...");
        noResults.setVisibility(View.VISIBLE);

        fbHelper = new BookFirebaseHelper();

        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {
                // Clear existing results
                results.clear();

                // Filter for currently reading books
                for (Book book : allBooks) {
                    // Fix string comparison by using equals()
                    if (book.getStatus() != null && "Currently reading".equals(book.getStatus())) {
                        results.add(book);
                    }
                }

                // Update UI on the main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (results.isEmpty()) {
                                noResults.setText("You don't have any books marked as currently reading");
                                noResults.setVisibility(View.VISIBLE);
                                rView.setVisibility(View.GONE);
                            } else {
                                noResults.setVisibility(View.GONE);
                                rView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

//    private void findCurrentReads() {
//
//        BookFirebaseHelper fbHelper = new BookFirebaseHelper();
//
//        // Get all books from Firebase and filter locally
//        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
//            @Override
//            public void onCallback(List<Book> allBooks) {
//
//                results.clear();
//
//                // Filter books based on title and author
//                List<Book> filteredBooks = new ArrayList<>();
//
//                for (Book book : allBooks) {
//                    if (book.getStatus() != null && book.getStatus().equals("Currently reading")) {
//                        filteredBooks.add(book);
//                    }
//                }
//
//                // Now pass these filtered results to your RecsMatchResultsFragment
//                if (getActivity() == null) return;
//
//                if (filteredBooks.isEmpty()) {
//                    Toast.makeText(getContext(), "No matching books found in your collection", Toast.LENGTH_SHORT).show();
//                    rView.setVisibility(View.GONE);
//                } else {
//                    // Create a new fragment with the results or update existing one
////                    if (getActivity() instanceof MainActivity) {
////                        ((MainActivity)getActivity()).showRecsMatchResultsFragment(filteredBooks);
////                    }
//                    rView.setVisibility(View.VISIBLE);
//                    adapter.notifyDataSetChanged(); // Update the adapter with new data
//                }
//            }
//        });
//    }

    @Override
    public void onNoteClick(Book book) {
// Show toast for debugging
//        Toast.makeText(getActivity(), "Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();

        // Tell the adapter to expand/collapse this book's card
        // We need to find the position of this book in the list
        int position = results.indexOf(book);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }
    }

    @Override
    public void onNoteLongClick(Book book) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToNewEntryFragment(book);
        }
    }
}