package com.example.bookishproject;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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

/*
This class represents an OpenBooksFragment.
An Open Books fragment displays the user's current reads in a recycler view.
It uses view binding, a recycler view, a RecyclerBooksAdapter, a list of results, layout elements, and a BookFirebaseHelper.
 */
public class OpenBooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener, ColorUpdatable {

    private FragmentOpenBooksBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private TextView noBooks;
    private ProgressBar progressBar;
    private BookFirebaseHelper fbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {// Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentOpenBooksBinding.inflate(inflater, container, false);

        rView = binding.recyclerView;
        noBooks = binding.messageNoOpenBooks;
        progressBar = binding.progressBar;

        setupRecyclerView();

        adapter.setOnNoteListener(this);

        return binding.getRoot();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadCurrentReads();
        updateColors();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar(this);
            updateColors();
        }
        loadCurrentReads(); // Refresh the list every time the fragment becomes visible
    }

    /*
    Method to determine what a click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
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
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will ask MainActivity to open a NewEntryFragment based on the selected book.
    */
    @Override
    public void onNoteLongClick(Book book) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).getNavigator().navigateToJournalEntry(book);
        }
    }

    private void setupRecyclerView() {
        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        rView.setAdapter(adapter);
    }

    /*
    Method to load the user's current reads
     */
    protected void loadCurrentReads() {

        // set progress bar to visible
        progressBar.setVisibility(View.VISIBLE);

        // get a new BookFirebaseHelper
        fbHelper = new BookFirebaseHelper();

        // ask the firebaseHelper to get ALL the books from the database
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {
                // Clear existing results
                results.clear();

                // go through all the Books in allBooks
                for (Book book : allBooks) {
                    // if the Book status is "Currently reading"
                    if (book.getStatus() != null && "Currently reading".equals(book.getStatus())) {
                        // add it to our results list
                        results.add(book);
                    }
                }

                // Update UI on the main thread
                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            // set progress bar to gone
                            progressBar.setVisibility(View.GONE);

                            if (results.isEmpty()) {
                                noBooks.setVisibility(View.VISIBLE);
                                rView.setVisibility(View.GONE);
                            } else {
                                noBooks.setVisibility(View.GONE);
                                rView.setVisibility(View.VISIBLE);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void updateColors() {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            activity.applyThemeColors(binding.getRoot(), activity.getCurrentSection());

            progressBar.setIndeterminateTintList(ColorStateList.valueOf(activity.currentInterestColor));
            progressBar.setBackgroundColor(Color.TRANSPARENT);
        }
    }

}