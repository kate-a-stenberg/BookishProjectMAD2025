package dev.kateastenberg.bookishproject.fragments.journal;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterUserBooks;
import dev.kateastenberg.bookishproject.intents.ReadingSessionIntent;
import dev.kateastenberg.bookishproject.interfaces.Searchable;
import dev.kateastenberg.bookishproject.databinding.FragmentBooksBinding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.Date;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.ReadingSessionViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a MyBooksFragment.
A MyBooksFragment is basically a mirror of BooksFragment, but in a different flow tree (Journal)
So it has to have a back button that goes to OpenBooksFragment
Also, the long click does something different from in BooksFragment
It uses view binding, a layout manager, a recycler view, a RecyclerAdapterBooks, a book list, layout elements, and a BookFirebaseHelper.
 */
public class MyBooksFragment extends Fragment implements RecyclerAdapterUserBooks.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ReadingSessionViewModel rvm;
    private PublishSubject<ReadingSessionIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private List<UserBook> bookList = new ArrayList<>();
    private RecyclerView recyclerView;
    private RecyclerAdapterUserBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView noBooks;
    private ExtendedFloatingActionButton add;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FragmentBooksBinding binding = FragmentBooksBinding.inflate(inflater, container, false);

        recyclerView = binding.rview;
        progressBar = binding.progressBar;
        noBooks = binding.messageNoBooks;
        bookList = new ArrayList<>();
        add = binding.fabAddBook;

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvm = new ViewModelProvider(this).get(ReadingSessionViewModel.class);

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

        add.setVisibility(View.GONE);
        
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

    /*
    Method to determine what a long click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will set the UserBook status to "currently reading".
     */
    @Override
    public void onNoteLongClick(int position) {
        UserBook userBook = bookList.get(position);

        // create new Entry
        Entry entry = new Entry(userBook);
        entry.setType("Started");

        // get today's date and set the entry date to that
        LocalDate today = LocalDate.now();
        entry.setDate(new Date(today.getDayOfMonth(), today.getMonthValue(), today.getYear()));
        long time = System.currentTimeMillis();
        entry.setTimestamp(time);
        // have the entry update its description
        entry.updateDescription();

        processIntent(new ReadingSessionIntent.OpenBook(entry));
    }

    @Override
    public void onNoteClick(int position) {
        processIntent(new ReadingSessionIntent.CardFlipped(position));
    }

    @Override
    public void performSearch(String query) {
        if (query == null || query.isEmpty()) {
            Toast.makeText(getContext(), "You can't search for nothing, dingus", Toast.LENGTH_SHORT).show();
            return;
        }
        processIntent (new ReadingSessionIntent.SearchBooks(query, ""));
    }

    @Override
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    @Override
    public void clearSearch() {
        currentSearchQuery = "";
        loadBooks(); // Reset to show all books

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
        rvm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        rvm.getBooks().observe(getViewLifecycleOwner(), books -> {

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

        rvm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        rvm.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                Toast.makeText(getContext(), success, Toast.LENGTH_SHORT).show();
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof ReadingSessionIntent.CardFlipped) {
                adapter.toggleFlip(((ReadingSessionIntent.CardFlipped) intent).position);
            }
            else {
                rvm.handleIntent(intent);
            }
        });

    }

    /*
    Method to load books into the fragment.
     */
    private void loadBooks() {
        processIntent(new ReadingSessionIntent.LoadBooks());
    }

    /*
    Method to set up recycler view
     */
    private void setupRecyclerView() {

        adapter = new RecyclerAdapterUserBooks(bookList);
        adapter.setOnNoteListener(this);
        recyclerView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(gridLayoutManager);
    }

    private void processIntent (ReadingSessionIntent intent) {
        intentSubject.onNext(intent);
    }

}