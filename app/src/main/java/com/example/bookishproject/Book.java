package com.example.bookishproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/*
This is a class to represent a Book object.
This class object is the central unit of data in this app.
Book class objects are stored in the firebase database.
The Book class implements Parcelable, which enables it to be passed efficiently between fragments in the form of Bundles.
They have the following attributes:
id : this is the label for this object in storage
title : title of the book
series : series the book belongs to. If no series, series is "standalone"
number : the number in the series. If no series, number is 0
author : the author of the book. currently only allows for one author
cover : if cover is stored in the app, this is the address of the cover
coverUrl : if cover is retrieved externally, this is the address of the cover
genre : the genre of the book
synopsis : the synopsis of the book
categories : categories the book might belong to. wide and varied possibilities such as "Juvenile fiction" or "1800s feminist literature"
ageRange : the age group the book is meant for. Books loaded externally are either MATURE or NOT_MATURE
pubYear : the publication date of the book
status : status of the book could be "Want to read", "Currently reading", "Read," or "DNF"
apiId : the id of the corresponding data item from the API
 */
public class Book implements Parcelable {

    private String id;
    private String title;
    private String series;
    private Integer number;
    private String author;
    private String coverUrl;
    private int cover;
    private String genre;
    private String synopsis;
    private List<String> categories;
    private String ageRange;
    private String pubYear;
    private String status;
    private String apiId;
    private float rating;
    private String review;

    public Book(){}

    /*
    constructor using a parcel
     */
    protected Book(Parcel in) {
        id = in.readString();
        title = in.readString();
        series = in.readString();
        number = in.readInt();
        author = in.readString();
        coverUrl = in.readString();
        cover = in.readInt();
        genre = in.readString();
        synopsis = in.readString();
        ageRange = in.readString();
        pubYear = in.readString();
        status = in.readString();
        apiId = in.readString();
        rating = in.readFloat();
        review = in.readString();


        // Read the categories list
        int size = in.readInt();
        if (size > 0) {
            categories = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                categories.add(in.readString());
            }
        }
    }

    /*
    Getters
     */
    public String getId() {
        return this.id;
    }
    public String getTitle() {
        return title;
    }
    public String getSeries() {
        return this.series;
    }
    public Integer getNumber() {
        return this.number;
    }
    public String getAuthor() {
        return author;
    }
    public int getCover() {
        return this.cover;
    }
    public String getCoverUrl() {
        return this.coverUrl;
    }
    public String getGenre() {
        return this.genre;
    }
    public String getSynopsis() {
        return this.synopsis;
    }
    public String getAgeRange() {
        return this.ageRange;
    }
    public List<String> getCategories() {
        return this.categories;
    }
    public String getPubYear() {
        return this.pubYear;
    }
    public String getStatus() {
        return this.status;
    }
    public String getApiId() {
        return this.apiId;
    }
    public Float getRating() {
        return this.rating;
    }
    public String getReview() {
        return this.review;
    }

    /*
    Setters
     */
    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setSeries(String series) {
        this.series = series;
    }
    public void setNumber(Integer number) {
        this.number = number;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setCoverUrl(String url) {
        this.coverUrl = url;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }
    public void setAgeRange(String ageRange) {
        this.ageRange = ageRange;
    }
    public void setCategories(List<String> categories) {
        this.categories = categories;
    }
    public void setPubYear(String year) {
        this.pubYear = year;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setApiId(String id) {
        this.apiId = id;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public void setReview(String review) {
        this.review = review;
    }

    /*
    required method for Parcelable classes
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
    Method to write the Book to a parcel
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(apiId);
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeString(synopsis);
        parcel.writeString(genre);
        parcel.writeString(ageRange);
        parcel.writeString(status);
        parcel.writeString(coverUrl);
        parcel.writeString(pubYear);

        // For the ArrayList of categories, first check if it's null
        if (categories == null) {
            parcel.writeInt(0);
        } else {
            parcel.writeInt(categories.size());
            for (String category : categories) {
                parcel.writeString(category);
            }
        }
    }

    /*
    Method to create an instance of a Book from a Parcel
     */
    public static final Creator<Book> CREATOR = new Creator<>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}
