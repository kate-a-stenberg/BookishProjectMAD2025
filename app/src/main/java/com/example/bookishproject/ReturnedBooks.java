package com.example.bookishproject;

import java.util.List;

/*
This is a class for a ReturnedBooks.
A BookResponse is the response received from the Google Books API.
It's just a list of BookItems.
 */
public class ReturnedBooks {
    private List<BookItem> items;

    public List<BookItem> getItems() {
        return items;
    }
    public void setItems(List<BookItem> items) {
        this.items = items;
    }
}
