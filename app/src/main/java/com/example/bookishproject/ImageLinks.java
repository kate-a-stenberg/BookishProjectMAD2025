package com.example.bookishproject;

/*
This class represents ImageLinks.
An ImageLinks is meant to hold image links for a Google Books API book.
It is accessible through ReturnedBooks > BookItem > VolumeInfo > ImageLinks.
It has attributes for the thumbnail, and small thumbnail, which are urls.
 */
public class ImageLinks {
    private String smallThumbnail;
    private String thumbnail;

    // constructor?

    public String getThumbnail() {
        return this.thumbnail;
    }

}
