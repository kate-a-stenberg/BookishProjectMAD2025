package com.example.bookishproject;

/*
This class represents a Date.
A Date has a day, a month, and a year
 */
public class Date {

    private int day;
    private int month;
    private int year;

    public Date() {}

    public Date(int day, int month, int year) {
        this.day = day;
        this.month = month;
        this.year = year;
    }

    // GETTERS
    public int getDay() {
        return this.day;
    }
    public int getMonth() {
        return this.month;
    }
    public int getYear() {
        return this.year;
    }

    // SETTERS
    public void setDay(int day) {
        this.day = day;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public void setYear(int year) {
        this.year = year;
    }

    /*
    Method to compare two dates.
    Returns the difference between thisDate and otherDate.
    A positive integer means otherDate is earlier than thisDate; negative integer means otherDate is later than thisDate
     */
    public int compareTo(Date otherDate) {

        // start out with year. the years are different, then one date is clearly earlier
        if (this.year != otherDate.getYear()) {
            return this.year - otherDate.getYear();
        }
        // if the years are the same, but the months are different, one is earlier than the other
        if (this.month != otherDate.month) {
            return this.month - otherDate.getMonth();
        }
        // if the years and months are the same, but the days are different one is earlier than the other
        return this.day - otherDate.getDay();
    }

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

}
