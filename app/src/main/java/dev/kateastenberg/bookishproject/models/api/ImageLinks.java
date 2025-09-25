package dev.kateastenberg.bookishproject.models.api;

/*
This class represents ImageLinks.
An ImageLinks is meant to hold image links for a Google Books API book.
It is accessible through ReturnedBooks > BookItem > VolumeInfo > ImageLinks.
It has attributes for the thumbnail, and small thumbnail, which are urls.
Fields are assigned automatically by Retrofit during JSON deserialization.
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class ImageLinks {
    private String thumbnail;
    private String smallThumbnail;

    // constructor?

    public String getThumbnail() {
        return this.thumbnail;
    }

    public String getSmallThumbnail() {
        return this.smallThumbnail;
    }

}
