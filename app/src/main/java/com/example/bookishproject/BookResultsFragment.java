package com.example.bookishproject;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bookishproject.databinding.FragmentBookResultsBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
This is a class for a BookResultsFragment.
A BookResultsFragment displays a list of books in a recycler view that were returned in a search of an API
It uses view binding, a recycler view, a recycler adapter, an array list of results, and relevant layout fields and elements
It also has a static final String variable.
 */
public class BookResultsFragment extends Fragment implements RecyclerAdapterBooks.OnNoteListener {

    // this variable is the name of the Bundle that contains information about the query that was run.
    // it receives this from BookSearchFragment
    private static final String ARG_QUERY = "query";

    private FragmentBookResultsBinding binding;
    private RecyclerView rView;
    private RecyclerAdapterBooks adapter;
    private List<Book> results = new ArrayList<>();
    private TextView noResults;
    private ImageButton backButton;

    /*
    Method to create a BookResultsFragment from a certain query
     */
    public static BookResultsFragment newInstance (String query) {
        // make a new BookResultsFragment
        BookResultsFragment fragment = new BookResultsFragment();
        // make a new Bundle
        Bundle args = new Bundle();
        // give that bundle the query from the BookSearchFragment and also the query
        args.putString(ARG_QUERY, query);
        // give the fragment this Bundle with the query as an argument
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentBookResultsBinding.inflate(inflater, container, false);

        // set variables
        rView = binding.recyclerView2;
        noResults = binding.messageNoResults;
        backButton = binding.buttonBack;

        setupRecyclerView();
        loadQuery();

        return binding.getRoot();
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // instructions for the user
        Toast.makeText(getContext(), "Tap and hold a book to add to your collection", Toast.LENGTH_SHORT).show();

        // set an onClickListener for the back button
        backButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                // ask MainActivity to go to BookSearchFragment
                ((MainActivity)getActivity()).navigateToBookSearchFragment();
            }
        });
    }

    /*
    Method to determine what a click does.
    This is a method from the RecyclerAdapterBooks.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with clicks.
    A click will expand/collapse the card clicked
    */
    @Override
    public void onNoteClick(Book book) {

        // find position in the list
        int position = results.indexOf(book);
        // if it's a valid position
        if (position != -1) {
            // ask the adapter to toggle expansion of the card at that position
            adapter.toggleExpansion(position);
        }

    }

    /*
    Method to determine what a long click does.
    This is a method from the RecyclerAdapterBooks.OnNoteListener interface
    The recycler view will use this as a listener to determine what to do with long clicks.
    A long click will add the associated book to the database (the user's collection).
    */
    @Override
    public void onNoteLongClick(Book book) {
        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();

            // if the Book has no status, set it to "Want to read"
            if (book.getStatus() == null || book.getStatus().isEmpty()) {
                book.setStatus("Want to Read");
            }

            // create a new BookFirebaseHelper and ask it to add the Book to the database
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

    /*
    Method to set up the recycler view
     */
    private void setupRecyclerView() {
        // create a new RecyclerAdapterBooks using the results list to populate
        adapter = new RecyclerAdapterBooks(getContext(), results);
        // give the recycler view this as an OnNoteListener--it will use the event listeners defined here to determine what to do
        adapter.setOnNoteListener(this);
        rView.setLayoutManager(new LinearLayoutManager(getContext()));
        rView.setAdapter(adapter);
    }

    /*
    Method to load a query into the fragment
     */
    private void loadQuery() {
        // if there are arguments
        if (getArguments() != null) {
            // set a query variable to the Bundle received from BookSearchFragment
            String query = getArguments().getString(ARG_QUERY);
            // null check
            if (query != null && !query.isEmpty()) {
                // search books using the query
                searchBooks(query);
            }
        }
    }

    /*
    Method to search the API for books from a given query
     */
    public void searchBooks(String query) {
        // get rid of the "no results" message
        noResults.setVisibility(View.GONE);
        // clear the results list to avoid adding a million things over multiple searches
        results.clear();
        adapter.notifyDataSetChanged();

        // choose your API
        BookApi bookApi = BookApiClient.getClient();
        // ask that API to search books with the given query and API key
        bookApi.searchBooks(query, BookApiClient.getApiKey()).enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ReturnedBooks> call, Response<ReturnedBooks> response) {
                // BookResponse is a list of return BookItems
                if (response.isSuccessful() && response.body() != null) {
                    // if there are results returned
                    if (response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                        // Convert API response to Book objects and add them to this Fragment's results list
                        for (BookItem item : response.body().getItems()) {
                            results.add(convertToBook(item));
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // if no results, display "no results" message
                        noResults.setVisibility(View.VISIBLE);
                    }
                } else {
                    // if unsuccessful, make a toast
                    Toast.makeText(getContext(), "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                    noResults.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<ReturnedBooks> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                noResults.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
    Method to convert a BookItem returned from an API call to a Book object usable by this app.
    Takes a BookItem (Google Books data) as an argument
     */
    private Book convertToBook(BookItem item) {

        // get the volume info from the BookItem (contains attributes for the Google Book)
        VolumeInfo info = item.getVolumeInfo();
        // initialize a new book
        Book book = new Book();

        // use the item's title as the Book's title
        book.setTitle(info.getTitle());

        // if the item has authors, use this as the Book's author
        if (info.getAuthors() != null && !info.getAuthors().isEmpty()) {
            book.setAuthor(TextUtils.join(", ", info.getAuthors()));
        } else {
            book.setAuthor("Unknown Author");
        }

        // if the item has a maturity rating, use this as the Book's age range
        if (info.getMaturityRating() != null) {
            book.setAgeRange(info.getMaturityRating());
        }

        // if the item has categories, use these as the Book's categories
        if (info.getCategories() != null && !info.getCategories().isEmpty()) {
            book.setCategories(new ArrayList<>(info.getCategories()));
        } else {
            book.setCategories(new ArrayList<>());
        }

        // if the item has no publishedDate, maturityRating, or description, set these fields in the Book as "unknown"/"not specified"/"no description available", respectively
        book.setPubYear(info.getPublishedDate() != null ? info.getPublishedDate() : "Unknown");
        book.setAgeRange(info.getMaturityRating() != null ? info.getMaturityRating() : "Not specified");
        book.setSynopsis(info.getDescription() != null ? info.getDescription() : "No description available");

        // if the item has image links, get the thumbnail link and set that as the Book's coverUrl
        if (info.getImageLinks() != null) {
            String imageUrl = info.getImageLinks().getThumbnail();
            if (imageUrl != null && imageUrl.startsWith("http://")) {
                imageUrl = imageUrl.replace("http://", "https://");
            }
            book.setCoverUrl(imageUrl);
        }

        // get the item's id and set this as the Book's ApiId
        book.setApiId(item.getId());

        return book;

    }

}