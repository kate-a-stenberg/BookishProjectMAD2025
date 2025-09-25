package dev.kateastenberg.bookishproject.models;

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
@SuppressWarnings({"unused"})
public class Entry implements Parcelable {

    private String id;
    private String userId;
    private UserBook userBook;
    private String type;
    private int pagesRead = 0;
    private Date date;
    private String description;
    private String comments;
    private long timestamp;

    /*
    Constructor using a Book
     */
    public Entry(UserBook userBook) {
        this.userBook = userBook;
        this.userId = userBook.getUserId();
    }

    /*
    Constructor using a Parcel
     */
    protected Entry(Parcel in) {
        id = in.readString();
        userId = in.readString();
        userBook = in.readParcelable(UserBook.class.getClassLoader());
        type = in.readString();
        pagesRead = in.readInt();
        date = in.readParcelable(Date.class.getClassLoader());
        description = in.readString();
        comments = in.readString();
        timestamp = in.readLong();
    }

    /*
    Method to create an instance of an Entry from a Parcel
    */
    public static final Creator<Entry> CREATOR = new Creator<>() {
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
    public UserBook getUserBook() {
        return userBook;
    }
    public String getType() {
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
    public long getTimestamp() {
        return this.timestamp;
    }

    // SETTERS
    public void setUserId(String id) {
        this.userId = id;
    }
    public void setUserBook(UserBook userBook) {
        this.userBook = userBook;
    }
    public void setType(String type) {
        this.type = type;
    }
    public void setPagesRead(int pages) {
        this.pagesRead = pages;
    }
    public void setDate(Date date) {
        this.date = date;
    }
    public void setComments(String comments) {
        this.comments = comments;
    }
    public void setId(String id) {
        this.id = id;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /*
    Method to create a description of the entry based on EntryType.
    For display purposes
     */
    public String entryDescription() {

        String description;

        switch (type) {
            case "Started":
                description = "You started reading!";
                userBook.setStatus("Currently reading");
                break;
            case "Pages read":
                description = "You read " + pagesRead + " pages.";
                break;
            case "Finished":
                description = "You finished the book!";
                userBook.setStatus("Read");
                break;
            default:
                description = "You did not finish.";
                userBook.setStatus("DNF");
                break;
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
        parcel.writeString(id);
        parcel.writeString(userId);
        parcel.writeParcelable(userBook, i);
        parcel.writeString(type);
        parcel.writeInt(pagesRead);
        parcel.writeParcelable(date, i);
        parcel.writeString(description);
        parcel.writeString(comments);
        parcel.writeLong(timestamp);
    }

}