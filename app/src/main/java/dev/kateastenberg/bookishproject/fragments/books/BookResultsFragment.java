package dev.kateastenberg.bookishproject.fragments.books;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterBooks;
import dev.kateastenberg.bookishproject.databinding.FragmentBookResultsBinding;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This is a class for a BookResultsFragment.
A BookResultsFragment displays a list of books in a recycler view that were returned
either in an API search or a universal Book search
It uses view binding, a recycler view, a recycler adapter, an array list of results, and relevant layout fields and elements
It also has a static final String variable.
 */
public class BookResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    // this variable is the name of the Bundle that contains information about the query that was run.
    // it receives this from BookSearchFragment
    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String API_SEARCH = "api_search";
    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";

    private int selectedPosition = RecyclerView.NO_POSITION;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ProgressBar progressBar;
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private boolean apiSearch;
    private String title;
    private String author;

    public BookResultsFragment() {}

    /*
    Method to create a BookResultsFragment from a certain query
     */
    public static BookResultsFragment newInstance (String title, String author, Boolean apiSearch) {
        // make a new BookResultsFragment
        BookResultsFragment fragment = new BookResultsFragment();
        // make a new Bundle
        Bundle args = new Bundle();
        // give that bundle the query from the BookSearchFragment and also search type
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        args.putBoolean(API_SEARCH, apiSearch);
        // give the fragment this Bundle with the query and type as arguments
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            this.apiSearch = getArguments().getBoolean("api_search", false);
            this.title = getArguments().getString("title");
            this.author = getArguments().getString("author");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentBookResultsBinding binding = FragmentBookResultsBinding.inflate(inflater, container, false);

        // set variables
        rView = binding.recyclerView2;
        noResults = binding.messageNoResults;
        progressBar = binding.progressBar;

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bvm = new ViewModelProvider(this).get(BooksViewModel.class);

        observeViewModel();

        loadBooks(title, author);

        // First check if we have a scroll state saved
        Bundle scrollState = getArguments() != null ?
                getArguments().getBundle("SCROLL_STATE") : null;

        // we do have a saved scroll state
        if (scrollState != null) {
            // make the saved list state into a parcelable
            Parcelable listState = scrollState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // have the layout manager restore that scroll state
                gridLayoutManager.onRestoreInstanceState(listState);
            }
        }
        // Then check saved instance state (for config changes)
        else if (savedInstanceState != null) {
            Parcelable listState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                gridLayoutManager.onRestoreInstanceState(listState);
            }
            selectedPosition = savedInstanceState.getInt(KEY_SELECTED_POSITION,
                    RecyclerView.NO_POSITION);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save RecyclerView state
        if (gridLayoutManager != null) {
            Parcelable listState = gridLayoutManager.onSaveInstanceState();
            outState.putParcelable(KEY_RECYCLER_STATE, listState);
            outState.putInt(KEY_SELECTED_POSITION, selectedPosition);
        }
    }

    @Override
    public void onDestroy() {
        // Dispose to prevent memory leaks
        if (intentDisposable != null && !intentDisposable.isDisposed()) {
            intentDisposable.dispose();
        }

        if (intentSubject != null && !intentSubject.hasComplete()) {
            intentSubject.onComplete();
        }
        super.onDestroy();
    }

    /*
    Method to determine what a long click does.
    This is a method from the RecyclerAdapterBooks.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will add the associated book to the database (the user's collection).
    */
    @Override
    public void onNoteLongClick(int position) {

        Book book = results.get(position);

        if (apiSearch) {
            processIntent(new BooksIntent.AddApiBook(book));
        }
        else {
            processIntent(new BooksIntent.AddBook(book));
        }
    }

    @Override
    public void onNoteClick(int position) {
        processIntent(new BooksIntent.CardFlipped(position));
    }

    // currently unused but may use in future for screen rotation save state handling
    public void saveScrollPosition() {
        if (gridLayoutManager != null) {
            // Save the current position to a persistent field
            Bundle scrollState = new Bundle();
            Parcelable listState = gridLayoutManager.onSaveInstanceState();
            scrollState.putParcelable(KEY_RECYCLER_STATE, listState);

            // Store it with the fragment
            if (getArguments() == null) {
                setArguments(new Bundle());
            }
            getArguments().putBundle("SCROLL_STATE", scrollState);
        }
    }

    /*
    Method to set up the recycler view, its adapter, and its layout manager
     */
    private void setupRecyclerView() {
        // create a new RecyclerAdapterBooks using the results list to populate
        adapter = new RecyclerAdapterBooks(results);
        // give the recycler view this as an OnNoteListener--it will use the event listeners defined here to determine what to do
        adapter.setOnNoteListener(this);
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rView.setLayoutManager(gridLayoutManager);
        rView.setAdapter(adapter);
    }

    public void observeViewModel() {
        bvm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        bvm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        bvm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        bvm.getBooks().observe(getViewLifecycleOwner(), books -> {

            // Save scroll state
            final Parcelable savedState = gridLayoutManager != null ?
                    gridLayoutManager.onSaveInstanceState() : null;

            // update recyclerview UI
            int oldSize = results.size();
            results.clear();
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize);
            }

            results.addAll(books);
            adapter.notifyItemRangeInserted(0, books.size());

            noResults.setVisibility((books.isEmpty() ? View.VISIBLE : View.GONE));

            if (savedState != null) {
                gridLayoutManager.onRestoreInstanceState(savedState);
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof BooksIntent.LoadBooks) {
                loadBooks(title, author);
            }
            else if (intent instanceof BooksIntent.CardFlipped) {
                adapter.toggleFlip(((BooksIntent.CardFlipped) intent).position);
            }
            else {
                bvm.handleIntent(intent);
            }
        });

    }

    public void processIntent(BooksIntent intent) {
        intentSubject.onNext(intent);
    }

    private void loadBooks(String title, String author) {
        if (apiSearch) {
            processIntent(new BooksIntent.SearchApiBooks(title, author));
        }
        else {
            processIntent(new BooksIntent.SearchUniversalBooks(title, author));
        }
    }

}