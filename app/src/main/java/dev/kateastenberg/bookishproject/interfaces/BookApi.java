package dev.kateastenberg.bookishproject.interfaces;

import dev.kateastenberg.bookishproject.models.api.ReturnedBooks;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/*
Interface to specify what an API should do
 */
public interface BookApi {
    @GET("volumes")
    Call<ReturnedBooks> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey
    );
}
