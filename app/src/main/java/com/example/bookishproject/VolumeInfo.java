package com.example.bookishproject;

import java.util.List;

/*
This class represents VolumeInfo.
VolumeInfo is the information returned from the GoogleBooks API about a book.
It has just a bunch of attributes and getters and setters.
Very boring.
 */
public class VolumeInfo {
    private String title;
    private List<String> authors;
    private String description;
    private List<String> categories;
    private String publishedDate;
    private ImageLinks imageLinks;
    private String maturityRating;

    // constructor?

    // GETTERS

    public String getTitle() {
        return this.title;
    }
    public List<String> getAuthors() {
        return this.authors;
    }
    public String getDescription() {
        return this.description;
    }
    public List<String> getCategories() {
        return this.categories;
    }
    public String getPublishedDate() {
        return this.publishedDate;
    }
    public ImageLinks getImageLinks() {
        return this.imageLinks;
    }
    public String getMaturityRating() {
        return this.maturityRating;
    }

    // SETTERS

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

}
