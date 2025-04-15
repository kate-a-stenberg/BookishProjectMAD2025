package com.example.bookishproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Entry implements Parcelable {

    private String id;
    private Book book;
    private ActivityType type;
    private int pagesRead = 0;
    private Date date;
    private String description;
    private String comments;

    public Entry() {};

    public Entry(Book book) {
        this.book = book;
        this.description = entryDescription();
    }

    public Entry(Book book, ActivityType type, int pagesRead, Date date) {
        this.book = book;
        this.type = type;
        if (type == ActivityType.PAGES_READ) {
            this.pagesRead = pagesRead;
        }
        this.date = date;
        if (type == ActivityType.STARTED) {
            this.description = "You started reading!";
            this.book.setStatus("Currently reading");
        }
        else if (type == ActivityType.PAGES_READ) {
            this.description = "You read " + pagesRead + " pages.";
        }
        else if (type == ActivityType.FINISHED ) {
            this.description = "You finished the book!";
            this.book.setStatus("Read");
        }
        else {
            this.description = "You did not finish.";
            this.book.setStatus("DNF");
        }
    }


    public Entry(Book book, ActivityType type, Date date) {
        this.book = book;
        this.type = type;
        if (type == ActivityType.PAGES_READ) {
            this.pagesRead = pagesRead;
        }
        this.date = date;
        if (type == ActivityType.STARTED) {
            this.description = "You started reading!";
        }
        else if (type == ActivityType.PAGES_READ) {
            this.description = "You read " + pagesRead + " pages.";
        }
        else if (type == ActivityType.FINISHED ) {
            this.description = "You finished the book!";
            this.book.setStatus("Read");
        }
        else {
            this.description = "You did not finish.";
            this.book.setStatus("DNF");
        }
    }

    protected Entry(Parcel in) {
        // TODO: add other fields
        id = in.readString();
        pagesRead = in.readInt();
        description = in.readString();
    }

    public static final Creator<Entry> CREATOR = new Creator<Entry>() {
        @Override
        public Entry createFromParcel(Parcel in) {
            return new Entry(in);
        }

        @Override
        public Entry[] newArray(int size) {
            return new Entry[size];
        }
    };

    public Book getBook() {
        return book;
    }
    public ActivityType getType() {
        return type;
    }
    public int getPagesRead() {
        return pagesRead;
    }
    public Date getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }
    public String getComments() {
        return comments;
    }
    public String getId() {return this.id;}

    public void setBook(Book book) {
        this.book = book;
    }
    public void setType(ActivityType type) {
        this.type = type;
    }
    public void setPagesRead(int pages) {
        this.pagesRead = pages;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String entryDescription() {

        String description;

        if (type == ActivityType.STARTED) {
            description = "You started reading!";
            book.setStatus("Currently reading");
        }
        else if (type == ActivityType.PAGES_READ) {
            description = "You read " + pagesRead + " pages.";
        }
        else if (type == ActivityType.FINISHED ) {
            description = "You finished the book!";
            book.setStatus("Read");
        }
        else {
            description = "You did not finish.";
            this.book.setStatus("DNF");
        }
        return description;
    }
    public void updateDescription() {
        this.description = entryDescription();
    }

    public void setComments(String comments) { this.comments = comments;}
    public void setId(String id) { this.id = id;}

    public String displayActivityType() {
        if (type == ActivityType.STARTED) {
            return "Book started";
        }
        else if (type == ActivityType.PAGES_READ) {
            return pagesRead + " pages read";
        }
        else if (type == ActivityType.FINISHED ) {
            return "Book finished";
        }
        else {
            return "Book abandoned";
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeInt(pagesRead);
        parcel.writeString(description);
    }

}