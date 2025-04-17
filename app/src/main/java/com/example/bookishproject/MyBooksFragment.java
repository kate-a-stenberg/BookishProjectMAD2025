package com.example.bookishproject;

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

import com.example.bookishproject.databinding.FragmentMyBooksBinding;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/*
This class represents a MyBooksFragment.
A MyBooksFragment is basically a mirror of BooksFragment, but in a different flow tree (Journal)
So it has to have a back button that goes to OpenBooksFragment
Also, the long click does something different from in BooksFragment
It uses view binding, a layout manager, a recycler view, a RecyclerAdapterBooks, a book list, layout elements, and a BookFirebaseHelper.
 */
public class MyBooksFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener{

    private FragmentMyBooksBinding binding;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

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

        Toast.makeText(getContext(), "Tap and hold a book to add to current reads", Toast.LENGTH_LONG).show();

        searchButton.setOnClickListener(v -> {
            // TODO: allow this to filter by title. popup? type in title?
        });

        // back button goes back to OpenBooksFragment
        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToOpenBooksFragment();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        loadBooks();

        // Force refresh the adapter when returning to the fragment
        // Use post to ensure it happens after layout completes
        if (recyclerView != null) {
            recyclerView.post(() -> {
                if (adapter != null && bookList != null) {
                    adapter.resetExpandedState();
                    adapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setupRecyclerView() {

        adapter = new RecyclerAdapterBooks(getContext(), bookList);
        adapter.setOnNoteListener(this);
        recyclerView.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        binding.rview.setLayoutManager(layoutManager);
    }

    protected void loadBooks() {

        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> books) {

                if (getActivity() == null) {
                    return;
                }

                getActivity().runOnUiThread(() -> {

                    bookList.clear();
                    bookList.addAll(books);

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                });
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

        // find position in the list
        int position = bookList.indexOf(book);
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
    A long click will open the BookFragment.
     */
    @Override
    public void onNoteLongClick(Book book) {

        // set the Book's status to "Currently reading"
        book.setStatus("Currently reading");

        // create a new Entry with this book
        Entry entry = new Entry(book);
        // set the EntryType to Started
        entry.setType(EntryType.STARTED);
        // get today's date and set the entry date to that
        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));
        // have the entry update its description
        entry.updateDescription();

        // make a new JournalFirebaseHelper and have it add the entry to the database
        JournalFirebaseHelper jFbHelper = new JournalFirebaseHelper();
        jFbHelper.addEntry(entry);

        // have the BookFirebaseHelper update the book (its status is now "Currently reading" rather than whatever it was before)
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