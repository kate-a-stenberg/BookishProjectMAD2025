package com.example.bookishproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BookApiClient {
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    private static final String API_KEY = "AIzaSyB_161KreW7JBdBnBjMjjg3t_e8rikJRNE";
    private static BookApi bookApi;

    public static BookApi getClient() {
        if (bookApi == null) {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
            bookApi = retrofit.create(BookApi.class);
        }
        return bookApi;
    }

    public static String getApiKey() {
        return API_KEY;
    }
}
