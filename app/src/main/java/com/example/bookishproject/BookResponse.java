package com.example.bookishproject;

import java.util.List;

public class BookResponse {
    private List<BookItem> items;

    public List<BookItem> getItems() {
        return items;
    }
    public void setItems(List<BookItem> items) {
        this.items = items;
    }
}
