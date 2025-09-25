package dev.kateastenberg.bookishproject.fragments.recs;

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

import dev.kateastenberg.bookishproject.adapters.RecyclerAdapterUserBooks;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.databinding.FragmentMatchOptionsBinding;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.activities.MainActivity;
import dev.kateastenberg.bookishproject.fragments.common.HostFragment;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.viewmodels.BooksViewModel;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.subjects.PublishSubject;

/*
This class represents a MatchOptionsFragment.
A match options fragment displays the options that the user can match. In MatchSearchFragment the user entered search terms,
and in MatchOptionsFragment they are returned a list of UserBooks that match and choose which one they would like to find matches for.
It has view binding, a recycler view, a recycler view adapter (specific to Books), an array list of search options, and layout fields and elements.
It also has a static final String attribute.
 */
public class MatchOptionsFragment extends Fragment implements RecyclerAdapterUserBooks.OnNoteListener {

    private static final String ARG_TITLE = "title";
    private static final String ARG_AUTHOR = "author";
    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static final String KEY_SELECTED_POSITION = "selected_position";

    private int selectedPosition = RecyclerView.NO_POSITION;
    private RecyclerView rView;
    private RecyclerAdapterUserBooks adapter;
    private GridLayoutManager gridLayoutManager;
    private List<UserBook> options = new ArrayList<>();
    private ProgressBar progressBar;
    private BooksViewModel bvm;
    private PublishSubject<BooksIntent> intentSubject = PublishSubject.create();
    private Disposable intentDisposable;
    private TextView noBooks;
    private String title;
    private String author;


    public static MatchOptionsFragment newInstance (String title, String author) {
        MatchOptionsFragment fragment = new MatchOptionsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        args.putString(ARG_AUTHOR, author);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            this.title = getArguments().getString("title");
            this.author = getArguments().getString("author");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentMatchOptionsBinding binding = FragmentMatchOptionsBinding.inflate(inflater, container, false);

        rView = binding.rview;
        progressBar = binding.progressBar;
        noBooks = binding.messageNoBooks;

        setupRecyclerView();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
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
    This is a method from RecyclerAdapterBooks.OnNoteListener
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will find similar books to the book on the clicked card
     */
    @Override
    public void onNoteLongClick(int position) {
        UserBook userBook = options.get(position);

        processIntent(new BooksIntent.FindSimilarBooks(userBook));
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

    private void setupRecyclerView() {
        adapter = new RecyclerAdapterUserBooks(options);
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
        bvm.getUserBooks().observe(getViewLifecycleOwner(), books -> {

            // Save scroll state
            final Parcelable savedState = gridLayoutManager != null ?
                    gridLayoutManager.onSaveInstanceState() : null;

            // update recyclerview UI
            int oldSize = options.size();
            options.clear();
            if (oldSize > 0) {
                adapter.notifyItemRangeRemoved(0, oldSize);
            }

            options.addAll(books);
            adapter.notifyItemRangeInserted(0, books.size());

            noBooks.setVisibility((books.isEmpty() ? View.VISIBLE : View.GONE));

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
            else if (intent instanceof BooksIntent.FindSimilarBooks) {
                UserBook userBook = ((BooksIntent.FindSimilarBooks) intent).userBook;
                if (getParentFragment() instanceof RecsHostFragment) {
                    // then ask the MainActivity to go to a BookResultsFragment using this UserBook to match to
                    ((RecsHostFragment) getParentFragment()).navigateToMatchResults(userBook);
                }
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
        processIntent(new BooksIntent.SearchUserBooks(title, author));
    }



}