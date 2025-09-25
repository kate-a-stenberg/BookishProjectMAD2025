package dev.kateastenberg.bookishproject.viewmodels;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import dev.kateastenberg.bookishproject.helpers.ComparerBasic;
import dev.kateastenberg.bookishproject.helpers.firebase.BookFirebaseHelper;
import dev.kateastenberg.bookishproject.helpers.firebase.UserBookFirebaseHelper;
import dev.kateastenberg.bookishproject.intents.BooksIntent;
import dev.kateastenberg.bookishproject.interfaces.BookApi;
import dev.kateastenberg.bookishproject.interfaces.Comparer;
import dev.kateastenberg.bookishproject.interfaces.UseCaseCallback;
import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.models.UserBook;
import dev.kateastenberg.bookishproject.models.api.BookItem;
import dev.kateastenberg.bookishproject.models.api.ReturnedBooks;
import dev.kateastenberg.bookishproject.models.api.VolumeInfo;
import dev.kateastenberg.bookishproject.network.BookApiClient;
import dev.kateastenberg.bookishproject.usecases.LoadBooksUseCase;
import dev.kateastenberg.bookishproject.usecases.SearchBooksUseCase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BooksViewModel extends ViewModel {
    private MutableLiveData<List<UserBook>> userBooks = new MutableLiveData<>();
    private MutableLiveData<List<Book>> books = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> successMessage = new MutableLiveData<>();
    private MutableLiveData<String> query = new MutableLiveData<>();
    private MutableLiveData<Boolean> adminStatus = new MutableLiveData<>();
    private MutableLiveData<UserBook> userBook = new MutableLiveData<>();
    private SearchBooksUseCase searchBooksUseCase;
    private LoadBooksUseCase loadBooksUseCase;
    private UserBookFirebaseHelper ubfbHelper;
    private BookFirebaseHelper bfbHelper;

    public BooksViewModel() {
        searchBooksUseCase = new SearchBooksUseCase();
        loadBooksUseCase = new LoadBooksUseCase();
        ubfbHelper = new UserBookFirebaseHelper();
        bfbHelper = new BookFirebaseHelper();
        adminStatus.setValue(isAdminUser());
    }

    public void loadBooks() {
        isLoading.setValue(true);
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("No current authenticated user");
            isLoading.setValue(false);
            return;
        }

        loadBooksUseCase.execute(new UseCaseCallback<List<UserBook>>() {
            @Override
            public void onSuccess(List<UserBook> result) {
                isLoading.setValue(false);
                userBooks.setValue(result);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    /*
    used by MatchOptionsFragment - needs to allow for a title/author search
     */
    public void searchUserBooks(String title, String author) {
        isLoading.setValue(true);

        searchBooksUseCase.execute(title, author, new UseCaseCallback<List<UserBook>>() {
            @Override
            public void onSuccess(List<UserBook> userBooks) {
                isLoading.setValue(false);
                BooksViewModel.this.userBooks.setValue(userBooks);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });

    }

    public void searchUniversalBooks(String title, String author) {
        isLoading.setValue(true);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("No current authenticated user");
            isLoading.setValue(false);
            return;
        }

        bfbHelper.getBooks(bookList -> {
            isLoading.setValue(false);
            books.setValue(bookList);
            if (bookList.isEmpty()) {
                errorMessage.setValue("No books found");
            }

        }, title, author);
    }

    public void searchApiBooks(String title, String author) {
        if (!isAdminUser()) {
            errorMessage.setValue("You do not have permission to do this");
            return;
        }

        StringBuilder queryBuilder = new StringBuilder();

        if (!title.isEmpty()) {
            queryBuilder.append("intitle:").append(title);
        }
        if (!author.isEmpty()) {
            if (queryBuilder.length() > 0) {
                queryBuilder.append("+");
            }
            queryBuilder.append("inauthor:").append(author);
        }

        String searchQuery = queryBuilder.toString();

        isLoading.setValue(true);
        BookApi bookApi = BookApiClient.getClient();
        bookApi.searchBooks(searchQuery, BookApiClient.getApiKey()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ReturnedBooks> call, @NonNull Response<ReturnedBooks> response) {
                isLoading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().getItems() != null && !response.body().getItems().isEmpty()) {
                        List<Book> newBooks = new ArrayList<>();
                        for (BookItem item : response.body().getItems()) {
                            newBooks.add(convertToBook(item));
                        }
                        books.setValue(newBooks);

                    } else {
                        errorMessage.setValue("No results found");
                    }
                } else {
                    errorMessage.setValue(response.message());
                }
            }
            @Override
            public void onFailure(@NonNull Call<ReturnedBooks> call, @NonNull Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Network error: " + t.getMessage());
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
        book.setId(item.getId());

        return book;

    }

    public void addBook(Book book) {

        isLoading.setValue(true);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            errorMessage.setValue("No current authenticated user");
            isLoading.setValue(false);
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // ask ubfbHelper if book already exists in collection
        ubfbHelper.isBookInUserCollection(userId, book.getId(), isInCollection -> {
            if (!isInCollection) {
                // create a UserBook
                UserBook userBook = new UserBook();
                userBook.setBookId(book.getId());
                userBook.setBook(book);
                userBook.setUserId(userId);
                userBook.setStatus("Want to read");
                String userBookId = FirebaseDatabase.getInstance().getReference().push().getKey();
                userBook.setUserBookId(userBookId);
                // add it to their collection
                ubfbHelper.addUserBook(userBook);
                successMessage.setValue(book.getTitle() + " has been added to your collection!");
            }
            else {
                errorMessage.setValue(book.getTitle() + " is already in your collection!");
            }
        });
    }

    private void addApiBook(Book book) {
        // admin check
        if (!isAdminUser()) {
            adminStatus.setValue(false);
            errorMessage.setValue("You do not have permisson to do this");
            return;
        }

        if (book.getId() == null) {
            errorMessage.setValue("Cannot add a book without an API ID");
            return;
        }

        // create a new BookFirebaseHelper and ask it to add the Book to the database
        // but only if it's not in the collection already
        bfbHelper.isBookInCollection(book.getId(), isInCollection -> {
            if (!isInCollection) {
                bfbHelper.addBook(book);
                successMessage.setValue(book.getTitle() + " added to database");
            }
            else {
                errorMessage.setValue(book.getTitle() + " is already in the universal collection");
            }
        });
    }

    private void findSimilarBooks(UserBook selectedBook) {
        isLoading.setValue(true);

        // Get the BookFirebaseHelper instance
        BookFirebaseHelper fbHelper = new BookFirebaseHelper();

        // Get all books from Firebase to compare with
        fbHelper.getAllBooks(allBooks -> {

            // Create a list to store matching books
            List<Book> matchingBooks = new ArrayList<>();

            // Create a BookComparator instance
            Comparer comparator = new ComparerBasic(selectedBook);

            // Loop through all books and find matches
            for (Book book : allBooks) {
                // Skip the selected book itself
                if (book.getId() != null && book.getId().equals(selectedBook.getBookId())) {
                    continue;
                }

                // Check if the books are similar based on the comparison criteria
                float matchScore = comparator.compareBooks(book);

                // if a book is over 50% similar, add it to the matchingBooks list
                // eventually: allow user to decide if they want only very similar results or a wider search
                if (matchScore >= 0.5) {
                    matchingBooks.add(book);
                }

            }
            books.setValue(allBooks);
            isLoading.setValue(false);
        });
    }

    private void updateUserBook(UserBook userBook) {
        ubfbHelper = new UserBookFirebaseHelper();
        ubfbHelper.updateUserBook(userBook, bookList -> {
            successMessage.setValue("Book updated successfully!");
        });
        this.userBook.setValue(userBook);
    }

    private void updateBook (UserBook userBook) {
        bfbHelper = new BookFirebaseHelper();
        ubfbHelper = new UserBookFirebaseHelper();
        bfbHelper.updateBook(userBook.getBook(), bookList -> {
            ubfbHelper.updateUserBook(userBook, bookList1 -> {
                successMessage.setValue("Book updated successfully");
            });
        });
    }

    private void deleteBook (Book book) {
        bfbHelper = new BookFirebaseHelper();
        bfbHelper.deleteBook(book.getId());
    }

    private void removeUserBook (UserBook userBook) {
        ubfbHelper = new UserBookFirebaseHelper();

        if (FirebaseAuth.getInstance().getCurrentUser().getUid() == null) {
            errorMessage.setValue("No current authenticated user");
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        ubfbHelper.removeUserBook(userId, userBook.getUserBookId());
    }

    /*
    Method to determine if the current user is an admin user or not
    Note: use positive form for clarity despite usually being used in negative
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isAdminUser() {
        Boolean adminCheck;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            adminCheck = "kate.a.stenberg@gmail.com".equals(currentUser.getEmail());
        }
        else {
            adminCheck = false;
        }
        adminStatus.setValue(adminCheck);
        return adminCheck;
    }

    public void handleIntent(BooksIntent intent) {
        if (intent instanceof BooksIntent.LoadBooks) {
            loadBooks();
        }
        else if (intent instanceof BooksIntent.SearchUserBooks) {
            BooksIntent.SearchUserBooks searchIntent = (BooksIntent.SearchUserBooks) intent;
            searchUserBooks(searchIntent.title, searchIntent.author);
        }
        else if (intent instanceof BooksIntent.SearchUniversalBooks) {
            BooksIntent.SearchUniversalBooks searchIntent = (BooksIntent.SearchUniversalBooks) intent;
            searchUniversalBooks(searchIntent.title, searchIntent.author);
        }
        else if (intent instanceof BooksIntent.SearchApiBooks) {
            BooksIntent.SearchApiBooks searchIntent = (BooksIntent.SearchApiBooks) intent;
            searchApiBooks(searchIntent.title, searchIntent.author);
        }
        else if (intent instanceof BooksIntent.AddBook) {
            BooksIntent.AddBook addIntent = (BooksIntent.AddBook) intent;
            addBook(addIntent.book);
        }
        else if (intent instanceof BooksIntent.AddApiBook) {
            BooksIntent.AddApiBook addIntent = (BooksIntent.AddApiBook) intent;
            addApiBook(addIntent.book);
        }
        else if (intent instanceof BooksIntent.FindSimilarBooks) {
            BooksIntent.FindSimilarBooks findIntent = (BooksIntent.FindSimilarBooks) intent;
            findSimilarBooks(findIntent.userBook);
        }
        else if (intent instanceof BooksIntent.AdminCheck) {
            isAdminUser();
        }
        else if (intent instanceof BooksIntent.UpdateUserBook) {
            BooksIntent.UpdateUserBook updateIntent = (BooksIntent.UpdateUserBook) intent;
            updateUserBook(updateIntent.userBook);
        }
        else if (intent instanceof BooksIntent.UpdateBook) {
            BooksIntent.UpdateBook updateIntent = (BooksIntent.UpdateBook) intent;
            updateBook(updateIntent.userBook);
        }
        else if (intent instanceof BooksIntent.DeleteBook) {
            BooksIntent.DeleteBook deleteIntent = (BooksIntent.DeleteBook) intent;
            deleteBook(deleteIntent.book);
        }
        else if (intent instanceof BooksIntent.RemoveUserBook) {
            BooksIntent.RemoveUserBook removeIntent = (BooksIntent.RemoveUserBook) intent;
            removeUserBook(removeIntent.userBook);
        }
    }

    // GETTERS
    public LiveData<List<UserBook>> getUserBooks() {
        return userBooks;
    }
    public LiveData<List<Book>> getBooks() {
        return books;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
    public LiveData<String> getQuery() {
        return query;
    }
    public LiveData<UserBook> getUserBook() {
        return userBook;
    }
    public LiveData<Boolean> getAdminStatus() {
        return adminStatus;
    }
}
