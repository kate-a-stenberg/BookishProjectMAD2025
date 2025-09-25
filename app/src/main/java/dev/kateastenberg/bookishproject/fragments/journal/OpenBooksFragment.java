package dev.kateastenberg.bookishproject.fragments.journal;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import dev.kateastenberg.bookishproject.R;
import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterUserBooks;
import dev.kateastenberg.bookishproject.databinding.FragmentOpenBooksBinding;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.intents.ReadingSessionIntent;
import dev.kateastenberg.bookishproject.interfaces.Searchable;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.ReadingSessionViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents an OpenBooksFragment.
An Open Books fragment displays the user's current reads in a recycler view.
It uses view binding, a recycler view, a RecyclerBooksAdapter, a list of openBooks, layout elements, and a BookFirebaseHelper.
 */
public class OpenBooksFragment extends Fragment implements RecyclerAdapterUserBooks.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private ReadingSessionViewModel rvm;
    private PublishSubject<ReadingSessionIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private final List<UserBook> openBooks = new ArrayList<>();
    private RecyclerView rView;
    private RecyclerAdapterUserBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private ProgressBar progressBar;
    private TextView noBooks;
    private ExtendedFloatingActionButton add;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentOpenBooksBinding binding = FragmentOpenBooksBinding.inflate(inflater, container, false);

        rView = binding.recyclerView;
        add = binding.fabOpenBook;
        noBooks = binding.messageNoOpenBooks;
        progressBar = binding.progressBar;

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

        loadCurrentReads();

        // First check if we have state in arguments (from navigation)
        Bundle scrollState = getArguments() != null ?
                getArguments().getBundle("SCROLL_STATE") : null;

        if (scrollState != null) {
            Parcelable listState = scrollState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // Restore from navigation
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

        add.setOnClickListener(v -> {
            processIntent(new ReadingSessionIntent.AddBook());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setToolbar((HostFragment) this.getParentFragment());
        }

        loadCurrentReads(); // Refresh the list every time the fragment becomes visible

        if (rView != null) {
            // Use post to ensure logic happens after layout completes
            rView.post(() -> {
                if (adapter != null) {
                    // Force refresh the adapter when returning to the fragment
                    adapter.notifyItemRangeChanged(0, openBooks.size());
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
    public void onNoteClick(int position) {
        processIntent(new ReadingSessionIntent.CardFlipped(position));
    }

    /*
    Method to determine what a long click does.
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will ask MainActivity to open a NewEntryFragment based on the selected book.
    */
    @Override
    public void onNoteLongClick(int position) {

        processIntent(new ReadingSessionIntent.GoNewEntry(position));

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
        processIntent(new ReadingSessionIntent.SearchBooks(query, ""));
    }

    @Override
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    @Override
    public void clearSearch() {
        currentSearchQuery = "";
        loadCurrentReads();

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
        rvm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        rvm.getBooks().observe(getViewLifecycleOwner(), books -> {
            // Save scroll state
            final Parcelable savedState = gridLayoutManager != null ?
                    gridLayoutManager.onSaveInstanceState() : null;

            // update recyclerview UI
            int oldSize = openBooks.size();
            openBooks.clear();
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize);
            }

            openBooks.addAll(books);

            // Show empty state if needed
            if (openBooks.isEmpty()) {
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
                adapter.toggleFlip(((ReadingSessionIntent.CardFlipped)intent).position);
            }
            else if (intent instanceof ReadingSessionIntent.AddBook) {
                if (getParentFragment() instanceof JournalHostFragment) {
                    ((JournalHostFragment) getParentFragment()).navigateToMyBooks();
                }
            }
            else if (intent instanceof ReadingSessionIntent.GoNewEntry) {
                handleBookLongClick(((ReadingSessionIntent.GoNewEntry)intent).position);
            }
            else {
                rvm.handleIntent(intent);
            }
        });

    }

    /*
    Method to load the user's current reads
     */
    private void loadCurrentReads() {

        processIntent(new ReadingSessionIntent.LoadBooks("Currently reading"));
    }

    /*
    Method to set up recycler view
    */
    private void setupRecyclerView() {
        adapter = new RecyclerAdapterUserBooks(openBooks);
        adapter.setOnNoteListener(this);
        rView.setAdapter(adapter);

        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rView.setLayoutManager(gridLayoutManager);
    }

    private void processIntent(ReadingSessionIntent intent) {
        intentSubject.onNext(intent);
    }

    private void handleBookLongClick(int position) {

        RecyclerAdapterUserBooks.ViewHolder viewHolder = (RecyclerAdapterUserBooks.ViewHolder) rView.findViewHolderForAdapterPosition(position);

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

            if (getParentFragment() instanceof JournalHostFragment) {
                ((JournalHostFragment) getParentFragment()).navigateToJournalEntry(new Entry(openBooks.get(position)), visibleCover, transitionName, true);
            }

        }

    }

}