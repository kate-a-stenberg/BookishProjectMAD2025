package dev.kateastenberg.bookishproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/*
This class represents a UserBook.
A UserBook is a record of a book specific to a User, with the user-specific status, rating, and review.
 */
@SuppressWarnings({"unused"})
public class UserBook implements Parcelable {

    private String userId;
    private String userBookId;
    private String bookId;
    private Book book;
    private String status;
    private float rating;
    private String review;

    public UserBook() {}

    public UserBook (Book book) {
        this.book = book;
        this.status = "Want to read";
        this.bookId = book.getId();
    }

    public UserBook(String userBookId, Book book) {
        this.userBookId = userBookId;
        this.book = book;
    }

    /*
    constructor using a parcel
     */
    protected UserBook(Parcel in) {
        userId = in.readString();
        userBookId = in.readString();
        bookId = in.readString();
        status = in.readString();
        rating = in.readFloat();
        review = in.readString();
        book = in.readParcelable(Book.class.getClassLoader());
    }

    public String getUserId() {
        return this.userId;
    }
    public String getUserBookId() {
        return this.userBookId;
    }
    public String getBookId() {
        return this.bookId;
    }
    public Book getBook() {
        return this.book;
    }
    public String getStatus() {
        return this.status;
    }
    public float getRating() {
        return this.rating;
    }
    public String getReview() {
        return this.review;
    }

    public void setUserId(String id) {
        this.userId = id;
    }
    public void setUserBookId(String id) {
        this.userBookId = id;
    }
    public void setBookId(String id) {
        this.bookId = id;
    }
    public void setBook(Book book) {
        this.book = book;
    }
    public void setStatus(String status) {
        this.status = status;
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
        parcel.writeString(userId);
        parcel.writeString(userBookId);
        parcel.writeString(bookId);
        parcel.writeString(status);
        parcel.writeFloat(rating);
        parcel.writeString(review);
        parcel.writeParcelable(book, i);
    }

    /*
    Method to create an instance of a Book from a Parcel
     */
    public static final Creator<UserBook> CREATOR = new Creator<>() {
        @Override
        public UserBook createFromParcel(Parcel in) {
            return new UserBook(in);
        }

        @Override
        public UserBook[] newArray(int size) {
            return new UserBook[size];
        }
    };

}
