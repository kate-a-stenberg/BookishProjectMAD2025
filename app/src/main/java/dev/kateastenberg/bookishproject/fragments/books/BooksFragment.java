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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterUserBooks;
import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.interfaces.Searchable;
import dev.kateastenberg.bookishproject.databinding.FragmentBooksBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import android.widget.TextView;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;

/*
This is a class for a BooksFragment.
A BooksFragment displays information about UserBooks in the user's collection using a recycler view.
It uses view binding, a layout manager to manage the recycler view, a recycler view adapter (specific to UserBooks),
an array list of UserBooks, and a UserBookFirebaseHelper to manage connections with the database.
This Fragment implements the OnNoteListener interface
 */
public class BooksFragment extends Fragment implements RecyclerAdapterUserBooks.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    public static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private List<UserBook> bookList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapterUserBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView noBooks;
    private ExtendedFloatingActionButton addBook;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentBooksBinding binding = FragmentBooksBinding.inflate(inflater, container, false);

        // setting variables for the Fragment
        addBook = binding.fabAddBook;
        recyclerView = binding.rview;
        progressBar = binding.progressBar;
        noBooks = binding.messageNoBooks;
        bookList = new ArrayList<>();

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bvm = new ViewModelProvider(this).get(BooksViewModel.class);

        observeViewModel();

        if (getArguments() != null && getArguments().containsKey(KEY_SEARCH_QUERY)) {
            currentSearchQuery = getArguments().getString(KEY_SEARCH_QUERY, "");
        }

        loadBooks();

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
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }

        // if user taps the add button, go to BookSearchFragment
        addBook.setOnClickListener(v -> {
            processIntent(new BooksIntent.FindBooks());
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {

            MainActivity activity = (MainActivity) getActivity();
            activity.setToolbar((HostFragment) this.getParentFragment());

            // Only try to expand search if we have a valid menu and active search
            if (!currentSearchQuery.isEmpty() && activity.getToolbarBuilder().hasMenu()) {
                MenuItem searchItem = activity.getToolbarBuilder().findMenuItem(R.id.search);
                if (searchItem != null) {
                    // Expand search view with current query
                    androidx.appcompat.widget.SearchView searchView =
                            (androidx.appcompat.widget.SearchView) searchItem.getActionView();

                    searchItem.expandActionView();
                    assert searchView != null;
                    searchView.setQuery(currentSearchQuery, false);

                    // Important: Don't re-execute the search if we already have results
                    // Just re-run if bookList is empty (i.e., first load or config change)
                    if (bookList.isEmpty()) {
                        performSearch(currentSearchQuery);
                    }
                    return; // Skip loading all books if we have a search
                }
            }

            // Only load all books if we don't have a search query
            if (currentSearchQuery.isEmpty() && (bookList == null || bookList.isEmpty())) {
                loadBooks();
            }
        }

        // Check if we're resuming with an active search query
        if (!currentSearchQuery.isEmpty()) {
            // We have an active search, re-execute it
            performSearch(currentSearchQuery);
        } else {
            // No active search, load all books
            loadBooks();
        }

        if (recyclerView != null) {
            // Use post to ensure logic happens after layout completes
            recyclerView.post(() -> {
                if (adapter != null && bookList != null && !bookList.isEmpty()) {
                    // Force refresh the adapter when returning to the fragment
                    adapter.notifyItemRangeChanged(0, bookList.size());
                }
            });
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
            outState.putString(KEY_SEARCH_QUERY, currentSearchQuery);
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
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
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will open the BookFragment.
     */
    @Override
    public void onNoteLongClick(int position) {
        processIntent(new BooksIntent.BookDetailView(position));
    }

    @Override
    public void onNoteClick(int position) {
        processIntent(new BooksIntent.CardFlipped(position));
    }

    // Helper method to be called before navigation
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


    @Override
    public void performSearch(String query) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }
        processIntent(new BooksIntent.SearchUserBooks(query, ""));
    }

    @Override
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    @Override
    public void clearSearch() {
        currentSearchQuery = "";
        loadBooks();

        // If you can access the toolbar/menu from here, collapse the search view
        if (getActivity() instanceof MainActivity) {
            MenuItem searchItem = ((MainActivity) getActivity()).getToolbarBuilder().findMenuItem(R.id.search);
            if (searchItem != null && searchItem.isActionViewExpanded()) {
                searchItem.collapseActionView();
            }
        }
    }

    @Override
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
        // No need to re-execute search since the results are still visible
    }

    public void observeViewModel() {
        // all state observers first, THEN connect intent stream to vm
        // means that when intents trigger state changes in vm, observers are already set up to handle changes
        bvm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        bvm.getUserBooks().observe(getViewLifecycleOwner(), books -> {

            // Save scroll state
            final Parcelable savedState = gridLayoutManager != null ?
                    gridLayoutManager.onSaveInstanceState() : null;

            // update recyclerview UI
            int oldSize = bookList.size();
            bookList.clear();
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize);
            }

            bookList.addAll(books);

            // Show empty state if needed
            if (bookList.isEmpty()) {
                noBooks.setVisibility(View.VISIBLE);
            } else {
                noBooks.setVisibility(View.GONE);
                adapter.notifyItemRangeInserted(0, books.size());
            }

            if (savedState != null) {
                gridLayoutManager.onRestoreInstanceState(savedState);
            }

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

        bvm.getQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                setSearchQuery(query);
            }
        });

        // Subscribe the ViewModel to the intent stream
        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof BooksIntent.CardFlipped) {
                adapter.toggleFlip(((BooksIntent.CardFlipped) intent).position);
            }
            else if (intent instanceof BooksIntent.BookDetailView) {
                handleBookLongClick(((BooksIntent.BookDetailView)intent).position);
            }
            else if (intent instanceof BooksIntent.SearchUserBooks) {
                if (getParentFragment() instanceof BooksHostFragment) {
                    ((BooksHostFragment) getParentFragment()).navigateToBookSearch();
                }
            }
            else {
                bvm.handleIntent(intent);
            }
        });
    }

    /*
    Method to load books into the fragment.
     */
    private void loadBooks() {
        processIntent(new BooksIntent.LoadBooks());
    }

    /*
    Helper method to set up the recycler view
     */
    private void setupRecyclerView() {

        // create a new adapter using this book list for data
        adapter = new RecyclerAdapterUserBooks(bookList);
        // use this as the OnNoteListener for the recycler view adapter
        adapter.setOnNoteListener(this);
        // assign the adapter to the recycler view
        recyclerView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void processIntent(BooksIntent intent) {
        intentSubject.onNext(intent);
    }

    private void handleBookLongClick(int position) {
        RecyclerAdapterUserBooks.ViewHolder viewHolder = (RecyclerAdapterUserBooks.ViewHolder) recyclerView.findViewHolderForAdapterPosition(position);

        if (viewHolder != null) {

            ImageView visibleCover;
            String transitionName;

            if (viewHolder.getCardFront().getVisibility() == View.VISIBLE) {
                visibleCover = viewHolder.getCoverFront();
                transitionName = "front_cover_transition_" + position;
            }
            else {
                visibleCover = viewHolder.getCoverBack();
                transitionName = "back_cover_transition_" + position;
            }

            if (getParentFragment() instanceof BooksHostFragment) {
                ((BooksHostFragment) getParentFragment()).navigateToBook(bookList.get(position), visibleCover, transitionName);
            }

        }
    }

}