package com.example.bookishproject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface BookApi {
    @GET("volumes")
    Call<BookResponse> searchBooks(
            @Query("q") String query,
            @Query("key") String apiKey
    );
}
