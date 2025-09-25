package dev.kateastenberg.bookishproject.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/*
This class represents a Date.
A Date has a day, a month, and a year
 */
public class Date implements Parcelable {

    private int day;
    private int month;
    private int year;

    public Date() {}

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    protected Date(Parcel in) {
        day = in.readInt();
        month = in.readInt();
        year = in.readInt();
    }

    public static final Creator<Date> CREATOR = new Creator<>() {
        @Override
        public Date createFromParcel(Parcel in) {
            return new Date(in);
        }

        @Override
        public Date[] newArray(int size) {
            return new Date[size];
        }
    };

    /*
        Method to convert the numeric month to a String, for display purposes
         */
    public String monthString() {
        String monthWord;
        if (this.month == 1) {
            monthWord = "January";
        }
        else if (this.month == 2) {
            monthWord = "February";
        }
        else if (this.month == 3) {
            monthWord = "March";
        }
        else if (this.month == 4) {
            monthWord = "April";
        }
        else if (this.month == 5) {
            monthWord = "May";
        }
        else if (this.month == 6) {
            monthWord = "June";
        }
        else if (this.month == 7) {
            monthWord = "July";
        }
        else if (this.month == 8) {
            monthWord = "August";
        }
        else if (this.month == 9) {
            monthWord = "September";
        }
        else if (this.month == 10) {
            monthWord = "October";
        }
        else if (this.month == 11) {
            monthWord = "November";
        }
        else if (this.month == 12) {
            monthWord = "December";
        }
        else {
            monthWord = "BadMonth";
        }

        return monthWord;
    }

    /*
    Method to generate a String version of the date for display purposes
     */
    public String displayDate() {
        return monthString() + " " + this.day + ", " + this.year;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeInt(day);
        parcel.writeInt(month);
        parcel.writeInt(year);
    }
}
