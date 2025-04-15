package com.example.bookishproject;

import android.health.connect.datatypes.units.Volume;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBookResultsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private static final String ARG_QUERY = "query";

    private FragmentBookResultsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ImageButton backButton;

    public static BookResultsFragment newInstance (String query) {
        BookResultsFragment fragment = new BookResultsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUERY, query);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentBookResultsBinding.inflate(inflater, container, false);

        rView = binding.recyclerView2;
        noResults = binding.messageNoResults;
        backButton = binding.buttonBack;

        setupRecyclerView();

        adapter.setOnNoteListener(this);

        loadQuery();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Tap and hold a book to add to your collection", Toast.LENGTH_LONG).show();

        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();

                // Create a new instance of BookSearchFragment
                BookSearchFragment searchFragment = new BookSearchFragment();

                // Get current position in ViewPager
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace the current fragment with BookSearchFragment
                activity.getAdapter().replaceFragment(currentPosition, searchFragment);

                // Update ViewPager
                activity.getViewPager().setAdapter(activity.getAdapter());
                activity.getViewPager().setCurrentItem(currentPosition, false);
            }
        });

    }

    private void setupRecyclerView() {
        adapter = new RecyclerAdapterBooks(getContext(), results);
        adapter.setOnNoteListener(this);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        rView.setAdapter(adapter);
    }

    // Add this method to your BookResultsFragment class
    private void loadQuery() {
        if (getArguments() != null) {
            String query = getArguments().getString(ARG_QUERY);
            if (query != null && !query.isEmpty()) {
                searchBooks(query);
            }
        }
    }

    public void searchBooks(String query) {
        noResults.setVisibility(View.GONE);
        results.clear();
        adapter.notifyDataSetChanged();

        BookApi bookApi = BookApiClient.getClient();
        bookApi.searchBooks(query, BookApiClient.getApiKey()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<BookResponse> call, Response<BookResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                        // Convert API response to your Book objects
                        for (BookItem item : response.body().getItems()) {
                            results.add(convertToBook(item));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        noResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<BookResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }

    private Book convertToBook(BookItem item) {

        VolumeInfo info = item.getVolumeInfo();
        Book book = new Book();

        book.setTitle(info.getTitle());

        if (info.getAuthors() != null && !info.getAuthors().isEmpty()) {
            book.setAuthor(TextUtils.join(", ", info.getAuthors()));
        } else {
            book.setAuthor("Unknown Author");
        }

        if (info.getMaturityRating() != null) {
            book.setAgeRange(info.getMaturityRating());
        }

        if (info.getCategories() != null && !info.getCategories().isEmpty()) {
            // Still set the genre string for display purposes
//            book.setGenre(TextUtils.join(", ", info.getCategories()));

            // Also set the categories list for filtering
            book.setCategories(new ArrayList<>(info.getCategories()));
        } else {
            book.setCategories(new ArrayList<>());
        }

        book.setPubYear(info.getPublishedDate() != null ? info.getPublishedDate() : "Unknown");
        book.setAgeRange(info.getMaturityRating() != null ? info.getMaturityRating() : "Not specified");
        book.setSynopsis(info.getDescription() != null ? info.getDescription() : "No description available");

        if (info.getImageLinks() != null) {
            String imageUrl = info.getImageLinks().getThumbnail();
            if (imageUrl != null && imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://");
            }
            book.setCoverUrl(imageUrl);
        }

        book.setApiId(item.getId());

        return book;

    }

    @Override
    public void onNoteClick(Book book) {
// Show toast for debugging
//        Toast.makeText(getActivity(), "Clicked: " + book.getTitle(), Toast.LENGTH_SHORT).show();

        // Tell the adapter to expand/collapse this book's card
        // We need to find the position of this book in the list
        int position = results.indexOf(book);
        if (position != -1) {
            adapter.toggleExpansion(position);
        }
    }

    @Override
    public void onNoteLongClick(Book book) {
        // Handle the book selection safely
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();

            // Directly add to Firebase without relying on BooksFragment
            if (book.getStatus() == null || book.getStatus().isEmpty()) {
                book.setStatus("Want to Read");
            }

            BookFirebaseHelper fbHelper = new BookFirebaseHelper();
            fbHelper.addBook(book);

            // Show success message
            Toast.makeText(getContext(), "Book added to your collection", Toast.LENGTH_SHORT).show();

            // Optionally try to notify BooksFragment to refresh
            try {
                BooksFragment booksFragment = activity.getBooksFragment();
                if (booksFragment != null && booksFragment.isAdded()) {
                    booksFragment.loadBooks();
                }
            } catch (Exception e) {
                // Ignore errors, book is already saved to Firebase
            }
        }
    }
}