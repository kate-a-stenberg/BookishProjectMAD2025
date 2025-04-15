package com.example.bookishproject;

import java.util.List;

public class VolumeInfo {
    private String title;
    private List<String> authors;
    private String description;
    private List<String> categories;
    private String publishedDate;
    private ImageLinks imageLinks;
    private String maturityRating;

    // constructor?

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

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

}
