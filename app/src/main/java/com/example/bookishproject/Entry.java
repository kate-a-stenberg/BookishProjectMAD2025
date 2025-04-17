package com.example.bookishproject;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/*
This class represents an Entry.
An entry represents a reading journal entry, summarizing a user's reading activity on a given day.
Entry implements Parcelable, allowing it to be passed easily between fragments in the form of a Bundle.
Entry has the following attributes:
id: how it's stored in the firebase database
book: the Book associated with the reading activity (what book the user interacted with)
type: whether the reader started a book, read pages, finished the book, or abandoned the book
pagesRead: if the type is EntryType.PAGES_READ, this is the number of pages they read
date: the date of the activity
description: a brief verbal description of the reading activity
comments: any comments the user entered about the reading activity
 */
public class Entry implements Parcelable {

    private String id;
    private Book book;
    private EntryType type;
    private int pagesRead = 0;
    private Date date;
    private String description;
    private String comments;

    /*
    No-argument constructor
     */
    public Entry() {};

    /*
    Constructor using a Book
     */
    public Entry(Book book) {
        this.book = book;
        this.description = entryDescription();
    }

    /*
    Constructor using a Parcel
     */
    protected Entry(Parcel in) {
        // TODO: add other fields
        id = in.readString();
        pagesRead = in.readInt();
        description = in.readString();
        comments = in.readString();
    }

    /*
    Method to create an instance of an Entry from a Parcel
    */
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

    // GETTERS
    public Book getBook() {
        return book;
    }
    public EntryType getType() {
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

    // SETTERS
    public void setBook(Book book) {
        this.book = book;
    }
    public void setType(EntryType type) {
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
    public void setComments(String comments) {
        this.comments = comments;
    }
    public void setId(String id) {
        this.id = id;
    }

    /*
    Method to create a description of the entry based on EntryType.
    For display purposes
     */
    public String entryDescription() {

        String description;

        if (type == EntryType.STARTED) {
            description = "You started reading!";
            book.setStatus("Currently reading");
        }
        else if (type == EntryType.PAGES_READ) {
            description = "You read " + pagesRead + " pages.";
        }
        else if (type == EntryType.FINISHED ) {
            description = "You finished the book!";
            book.setStatus("Read");
        }
        else {
            description = "You did not finish.";
            this.book.setStatus("DNF");
        }
        return description;
    }

    /*
    Method to update the description
     */
    public void updateDescription() {
        this.description = entryDescription();
    }

    /*
    Required method for Parcelable class
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
    Method to write the entry to a Parcel
     */
    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        // TODO: add other fields
        parcel.writeString(id);
        parcel.writeInt(pagesRead);
        parcel.writeString(description);
    }

}