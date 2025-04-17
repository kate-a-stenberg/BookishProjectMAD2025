package com.example.bookishproject;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/*
Class to connect to an external API
 */
public class BookApiClient {
    // API URL
    private static final String BASE_URL = "https://www.googleapis.com/books/v1/";
    // API key
    private static final String API_KEY = BuildConfig.API_KEY;
    // an API-type object
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
