package dev.kateastenberg.bookishproject.fragments.recs;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import dev.kateastenberg.bookishproject.databinding.FragmentMatchResultsBinding;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a MatchResultsFragment.
A match results fragment displays the results of a user's Match search in a recycler view.
It uses view binding, a recycler view, a RecyclerAdapterBooks, a list of results, a layout manager, layout fields and elements, and
 */
public class MatchResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";

    private int selectedPosition = RecyclerView.NO_POSITION;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private UserBook userBook;
    private GridLayoutManager gridLayoutManager;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ProgressBar progressBar;
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;

    /*
    Required empty constructor
     */
    public MatchResultsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.userBook = getArguments().getParcelable("user_book");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentMatchResultsBinding binding = FragmentMatchResultsBinding.inflate(inflater, container, false);

        // set layout field variables
        rView = binding.rview;
        adapter = new RecyclerAdapterBooks();
        noResults = binding.messageNoResults;
        progressBar = binding.progressBar;

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bvm = new ViewModelProvider(this). get(BooksViewModel.class);

        observeViewModel();

        loadBooks(userBook);

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
    A long click will add the book to the user's collection
    */
    @Override
    public void onNoteLongClick(int position) {

        Book book = results.get(position);

        processIntent(new BooksIntent.AddBook(book));

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
    Method to set up recycler view
     */
    private void setupRecyclerView() {

        adapter = new RecyclerAdapterBooks(results);
        adapter.setOnNoteListener(this);
        rView.setAdapter(adapter);

        // Set up layout manager as a field to access later
        gridLayoutManager = new GridLayoutManager(getContext(), 3);
        rView.setLayoutManager(gridLayoutManager);
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
                loadBooks(userBook);
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

    private void loadBooks(UserBook userBook) {
        processIntent(new BooksIntent.FindSimilarBooks(userBook));
    }

}