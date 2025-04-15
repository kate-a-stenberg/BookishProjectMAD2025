package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentMatchOptionsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MatchOptionsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    private static final String ARG_QUERY = "query";
    private FragmentMatchOptionsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ImageButton backButton;
    private ProgressBar progressBar;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_book_results, container, false);
        binding = FragmentMatchOptionsBinding.inflate(inflater, container, false);

        rView = binding.rview;
        backButton = binding.buttonMatchOptionsBack;
        progressBar = binding.progressBar; // Initialize the progress bar

        setupRecyclerView();

        adapter.setOnNoteListener(this);

//                position -> {
//            Book selectedBook = results.get(position);
//            if (getActivity() instanceof MainActivity) {
//                BooksFragment booksFragment = ((MainActivity) getActivity()).getBooksFragment();
//                if (booksFragment != null) {
//                    booksFragment.addBookToDatabase(selectedBook);
//                    Toast.makeText(getContext(), "Book added to your collection", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });

        if (getArguments() != null && getArguments().containsKey("BOOK_RESULTS")) {
            List<Book> passedBooks = getArguments().getParcelableArrayList("BOOK_RESULTS");
            if (passedBooks != null && !passedBooks.isEmpty()) {
                results.clear();
                results.addAll(passedBooks);
                adapter.notifyDataSetChanged();
            } else {
                noResults.setVisibility(View.VISIBLE);
            }
        } else if (getArguments() != null) {
            String query = getArguments().getString(ARG_QUERY);
            if (query != null && !query.isEmpty()) {
                searchBooks(query);
            }
        }

        return binding.getRoot();
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toast.makeText(getContext(), "Tap and hold a book to find matches", Toast.LENGTH_LONG).show();

        backButton.setOnClickListener(v -> {
            // Navigate back to MatchOptionsFragment
            if (getActivity() instanceof MainActivity) {
                MainActivity activity = (MainActivity) getActivity();

                // For simplicity, we'll just go back to the search fragment
                // If you need to maintain state, you'd need to store the filtered books
                MatchSearchFragment searchFragment = new MatchSearchFragment();

                // Get the current position in the ViewPager
                int currentPosition = activity.getViewPager().getCurrentItem();

                // Replace current fragment
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
        Toast.makeText(getContext(), "Finding similar books to: " + book.getTitle(), Toast.LENGTH_SHORT).show();

        progressBar.setVisibility(View.VISIBLE);

        // Start the comparison process
        findSimilarBooks(book);
    }

    private void findSimilarBooks(Book selectedBook) {

        Log.d("BookComparison", "Starting comparison for book: " + selectedBook.getTitle());

        // Get the BookFirebaseHelper instance
        BookFirebaseHelper fbHelper = new BookFirebaseHelper();

        // Get all books from Firebase to compare with
        Log.d("BookComparison", "Fetching books from Firebase...");
        fbHelper.getAllBooks(new BookFirebaseHelper.FirebaseCallback() {
            @Override
            public void onCallback(List<Book> allBooks) {

                Log.d("BookComparison", "Retrieved " + allBooks.size() + " books from Firebase");

                // Create a list to store matching books
                List<Book> matchingBooks = new ArrayList<>();

                // Create a BookComparator instance
                Comparer comparator = new ComparerBasic(selectedBook);

                // Loop through all books and find matches
                for (Book book : allBooks) {
                    // Skip the selected book itself
                    if (book.getApiId() != null && book.getApiId().equals(selectedBook.getApiId())) {
                        continue;
                    }

                    Log.d("BookComparison", "Comparing with: " + book.getTitle());

                    // Check if the books are similar based on your comparison criteria
                    float matchScore = comparator.compareBooks(book);
                    Log.d("BookComparison", "Match score: " + matchScore);

                    if (comparator.compareBooks(book) >= 0.75) {
                        matchingBooks.add(book);
                    }

                }

                Log.d("BookComparison", "Found " + matchingBooks.size() + " similar books");

                // Hide loading indicator
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }

                // Use MainActivity to navigate to results fragment
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToMatchResultsFragment(selectedBook, matchingBooks);

                }
            }
        });
    }

}