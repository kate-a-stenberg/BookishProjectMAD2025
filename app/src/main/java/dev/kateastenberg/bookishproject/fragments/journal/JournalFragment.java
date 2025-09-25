package dev.kateastenberg.bookishproject.fragments.journal;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import dev.kateastenberg.bookishproject.intents.JournalIntent;
import dev.kateastenberg.bookishproject.models.Entry;
import dev.kateastenberg.bookishproject.helpers.firebase.JournalFirebaseHelper;
import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterJournal;
import dev.kateastenberg.bookishproject.interfaces.Searchable;
import dev.kateastenberg.bookishproject.databinding.FragmentJournalBinding;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.viewmodels.JournalViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a JournalFragment.
A journal fragment displays information about the user's reading journal entries in a recycler view.
It has view binding, a layout manager, a recycler view, a recycler adapter (journal-specific), an array list of journal entries,
a journal firebase helper, and a floating action button.
 */
public class JournalFragment extends Fragment implements RecyclerAdapterJournal.OnNoteListener, Searchable {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";
    private static final String KEY_SEARCH_QUERY = "search_query";

    private String currentSearchQuery = "";
    private int selectedPosition = RecyclerView.NO_POSITION;
    private JournalViewModel jvm;
    private PublishSubject<JournalIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private FragmentJournalBinding binding;
    private List<Entry> entryList = new ArrayList<>();
    private RecyclerView rview;
    private RecyclerAdapterJournal adapter;
    private LinearLayoutManager layoutManager;
    private ProgressBar progressBar;
    private TextView noEntries;
    private ExtendedFloatingActionButton add;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentJournalBinding.inflate(inflater, container, false);

        // setting variables
        rview = binding.rview;
        add = binding.fabAddEntry;
        progressBar = binding.progressBar;
        noEntries = binding.messageNoEntries;
        entryList = new ArrayList<>();

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        jvm = new ViewModelProvider(this).get(JournalViewModel.class);

        observeViewModel();

        if (getArguments() != null && getArguments().containsKey(KEY_SEARCH_QUERY)) {
            currentSearchQuery = getArguments().getString(KEY_SEARCH_QUERY, "");
        }

        loadEntries();

        // First check if we have state in arguments (from navigation)
        Bundle scrollState = getArguments() != null ?
                getArguments().getBundle("SCROLL_STATE") : null;

        // if we have a saved scroll state
        if (scrollState != null) {
            Parcelable listState = scrollState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // Restore from navigation
                layoutManager.onRestoreInstanceState(listState);
            }
        }

        // Then check saved instance state (for config changes)
        else if (savedInstanceState != null) {
            // get the state
            Parcelable listState = savedInstanceState.getParcelable(KEY_RECYCLER_STATE);
            if (listState != null) {
                // set the layout to that state
                layoutManager.onRestoreInstanceState(listState);
            }
            // also get whatever position was selected
            selectedPosition = savedInstanceState.getInt(KEY_SELECTED_POSITION,
                    RecyclerView.NO_POSITION);
            // and remember if we had typed in a search query and put that back
            currentSearchQuery = savedInstanceState.getString(KEY_SEARCH_QUERY, "");
        }

        // set operations for add entry
        add.setOnClickListener(v -> {
            processIntent(new JournalIntent.AddEntry());
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        if (getActivity() instanceof MainActivity) {

            MainActivity activity = (MainActivity) getActivity();
            activity.setToolbar((HostFragment) this.getParentFragment());

            // if we have a current search query
            if (!currentSearchQuery.isEmpty() && activity.getToolbarBuilder().hasMenu()) {
                MenuItem searchItem = activity.getToolbarBuilder().findMenuItem(R.id.search);
                if (searchItem != null) {
                    androidx.appcompat.widget.SearchView searchView =
                            (androidx.appcompat.widget.SearchView) searchItem.getActionView();

                    searchItem.expandActionView();
                    if (searchView != null) {
                        searchView.setQuery(currentSearchQuery, false);
                    }

                    if (entryList.isEmpty()) {
                        performSearch(currentSearchQuery);
                    }
                    return;
                }
            }

            if (currentSearchQuery.isEmpty() && (entryList == null || entryList.isEmpty())) {
                loadEntries();
            }
        }

        if (!currentSearchQuery.isEmpty()) {
            performSearch(currentSearchQuery);
        }
        else {
            loadEntries();
        }

        if (rview != null) {
            rview.post(() -> {
                // Use post to ensure the operation happens after layout completes
                if (adapter != null && entryList != null) {
                    // Force refresh the adapter when returning to the fragment
//                    adapter.resetExpandedState();
                    adapter.notifyItemRangeChanged(0, entryList.size());
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save RecyclerView state
        if (layoutManager != null) {
            Parcelable listState = layoutManager.onSaveInstanceState();
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
    Method to determine what a click does.
    This is a method from the RecyclerAdapterJournal.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with clicks.
    A click will expand/collapse the card clicked
    */
    @Override
    public void onNoteClick(int position) {
        processIntent(new JournalIntent.EntryClicked(position));
    }

    /*
    Method to determine what a long click does.
    This is a method from the RecyclerAdapterJournal.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will open a JournalEntryFragment.
     */
    @Override
    public void onNoteLongClick(int position) {

        processIntent(new JournalIntent.EntryLongClicked(position));

    }

    @Override
    public void setSearchQuery(String query) {
        this.currentSearchQuery = query;
    }

    @Override
    public void performSearch(String query) {
        processIntent(new JournalIntent.SearchEntries(query));
    }

    @Override
    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    @Override
    public void clearSearch() {
        currentSearchQuery = "";
        loadEntries();

        if (getActivity() instanceof MainActivity) {
            MenuItem searchItem = ((MainActivity) getActivity()).getToolbarBuilder().findMenuItem(R.id.search);
            if (searchItem != null && searchItem.isActionViewExpanded()) {
                searchItem.collapseActionView();
            }
        }

    }

    // Helper method to be called before navigation
    public void saveScrollPosition() {
        if (layoutManager != null) {
            // Save the current position to a persistent field
            Bundle scrollState = new Bundle();
            Parcelable listState = layoutManager.onSaveInstanceState();
            scrollState.putParcelable(KEY_RECYCLER_STATE, listState);

            // Store it with the fragment
            if (getArguments() == null) {
                setArguments(new Bundle());
            }
            getArguments().putBundle("SCROLL_STATE", scrollState);
        }
    }

    /*
    Method to load Entries into the fragment
     */
    protected void loadEntries() {
        processIntent(new JournalIntent.LoadEntries());
    }

    /*
    Helper method to set up the recycler view
     */
    private void setupRecyclerView() {

        // create a new adapter using this entry list for data
        adapter = new RecyclerAdapterJournal(entryList);
        // use this as the OnNoteListener for the recycler view adapter
        adapter.setOnNoteListener(this);
        // assign the adapter to the recycler view
        rview.setAdapter(adapter);

        // Set up layout manager as a field to access later
        layoutManager = new LinearLayoutManager(getContext());
        // assign this layout manager to the recycler view
        binding.rview.setLayoutManager(layoutManager);

        // add divider between each item
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(rview.getContext(), LinearLayoutManager.VERTICAL);
        rview.addItemDecoration(dividerItemDecoration);
    }

    public void observeViewModel() {

        jvm.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        jvm.getEntries().observe(getViewLifecycleOwner(), entries -> {

            // Save current scroll state before loading
            final Parcelable savedState = layoutManager != null ?
                    layoutManager.onSaveInstanceState() : null;

            int oldSize = entryList.size();
            entryList.clear();
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize);
            }

            entryList.addAll(entries);

            // Show empty state or content based on results
            if (entryList.isEmpty()) {
                noEntries.setVisibility(View.VISIBLE);
            }
            else {
                noEntries.setVisibility(View.GONE);
                adapter.notifyItemRangeInserted(0, entries.size());
            }

            // Restore scroll position after data loaded
            if (savedState != null) {
                layoutManager.onRestoreInstanceState(savedState);
            }

        });
        jvm.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        jvm.getQuery().observe(getViewLifecycleOwner(), query -> {
            if (query != null) {
                setSearchQuery(query);
            }
        });

        intentDisposable = intentSubject.subscribe(intent -> {
            if (intent instanceof JournalIntent.EntryClicked) {
                adapter.toggleExpansion(((JournalIntent.EntryClicked) intent).position);
            }
            else if (intent instanceof JournalIntent.EntryLongClicked) {
                handleEntryLongClick(((JournalIntent.EntryLongClicked)intent).position);
            }
            else if (intent instanceof JournalIntent.AddEntry) {
                if (getParentFragment() instanceof JournalHostFragment) {
                    ((JournalHostFragment) getParentFragment()).navigateToOpenBooks();
                }
            }
            else {
                jvm.handleIntent(intent);
            }
        });

    }

    private void handleEntryLongClick (int position) {
        RecyclerAdapterJournal.ViewHolder viewHolder = (RecyclerAdapterJournal.ViewHolder) rview.findViewHolderForAdapterPosition(position);

        Entry entry = entryList.get(position);

        if (viewHolder != null) {

            ImageView visibleCover = viewHolder.getCover();
            String transitionName = "journal_entry_transition";

            if (getParentFragment() instanceof JournalHostFragment) {
                ((JournalHostFragment) getParentFragment()).navigateToJournalEntry(entry, visibleCover, transitionName, false);
            }

        }
    }

    private void processIntent(JournalIntent intent) {
        intentSubject.onNext(intent);
    }

}