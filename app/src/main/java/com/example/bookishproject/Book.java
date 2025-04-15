package com.example.bookishproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class Book implements Parcelable {

    private String id;
    private String title;
    private String series;
    private int number;
    private String author;
    private int cover;
    private String coverUrl;
    private String genre;
    private String synopsis;
    private List<String> categories;
    private String ageRange;
    private String pubYear;
    private String status;
    private String apiId;

    public Book(){}

    protected Book(Parcel in) {
        apiId = in.readString();
        title = in.readString();
        author = in.readString();
        synopsis = in.readString();
        genre = in.readString();
        ageRange = in.readString();
        status = in.readString();
        coverUrl = in.readString();
        pubYear = in.readString();

        // Read the categories list
        int size = in.readInt();
        if (size > 0) {
            categories = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                categories.add(in.readString());
            }
        }
    }

    public Book (String title, String series, int number, String author, int cover, String genre, String synopsis, String ageRange, String pubYear) {
        this.title = title;
        this.series = series;
        this.number = number;
        this.author = author;
        this.cover = cover;
        this.genre = genre;
        this.synopsis = synopsis;
        this.ageRange = ageRange;
        this.categories = new ArrayList<>();
        this.pubYear = pubYear;
        this.status = "Unread";
    }

    public String getId() {
        return this.id;
    }
    public String getTitle() {
        return title;
    }
    public String getSeries() {
        return this.series;
    }
    public int getNumber() {
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

    public void setId(String id) {
        this.id = id;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void setSeries(String series) {
        this.series = series;
    }
    public void setNumber(int number) {
        this.number = number;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setCover(int cover) {
        this.cover = cover;
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
    public void addTheme(String theme) {
        this.categories.add(theme);
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

    @Override
    public int describeContents() {
        return 0;
    }

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

    public static final Creator<Book> CREATOR = new Creator<Book>() {
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
