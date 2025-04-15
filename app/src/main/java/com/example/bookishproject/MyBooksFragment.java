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
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBooksBinding;
import com.example.bookishproject.databinding.FragmentMyBooksBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MyBooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener{

    private static final String TAG = "MyBooksFragment"; // For logging


    private FragmentMyBooksBinding binding;
    private FragmentManager fragmentManager;
    private Parcelable recyclerViewState;
    private LinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    private RecyclerAdapterBooks adapter;
    private List<Book> bookList = new ArrayList<>();
    private FloatingActionButton searchButton;
    private ImageButton backButton;
    private BookFirebaseHelper fbHelper;

    public MyBooksFragment() {
        // Required empty public constructor
    }

    public static MyBooksFragment newInstance(String param1, String param2) {
        MyBooksFragment fragment = new MyBooksFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView called");

        binding = FragmentMyBooksBinding.inflate(inflater, container, false);

        searchButton = binding.buttonMyBooksSearch;
        backButton = binding.buttonMyBooksBack;
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

        Toast.makeText(getContext(), "Tap and hold a book to add to current reads", Toast.LENGTH_LONG).show();

        // TODO: allow this to filter by title. popup? type in title?
        searchButton.setOnClickListener(v -> {
//            // Show a dialog with category options
//            String[] categories = {"Fiction", "Fantasy", "Romance", "Science Fiction", "Mystery"};
//
//            new AlertDialog.Builder(getContext())
//                    .setTitle("Filter by Category")
//                    .setItems(categories, (dialog, which) -> {
//                        String selectedCategory = categories[which];
//                        filterByCategory(selectedCategory);
//                    })
//                    .setNegativeButton("Clear Filter", (dialog, which) -> {
//                        filterByCategory(null); // Clear the filter
//                    })
//                    .show();
        });

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();
                // Create a new instance of RecsFragment
                OpenBooksFragment openBooksFragment = new OpenBooksFragment();

                // Get the current position in the ViewPager (should be 2 for the Recs tab)
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with the RecsFragment
                ((MainActivity) getActivity()).getAdapter().replaceFragment(currentPosition, openBooksFragment);

                // This will refresh the ViewPager with the new fragment
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
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
        int position = bookList.indexOf(book);
        book.setStatus("Currently reading");

        Entry entry = new Entry();
        entry.setBook(book);
        entry.setType(ActivityType.STARTED);
        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));
        entry.updateDescription();

        JournalFirebaseHelper jFbHelper = new JournalFirebaseHelper();
        jFbHelper.addEntry(entry);


        fbHelper.updateBook(book, new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> books) {
                // This callback will be triggered after the update completes
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), book.getTitle() + " added to your current reads!", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    });
                }
            }
        });

        Toast.makeText(getActivity(), book.getTitle() + " added to your current reads!", Toast.LENGTH_SHORT).show();
        adapter.notifyDataSetChanged();
    }

}