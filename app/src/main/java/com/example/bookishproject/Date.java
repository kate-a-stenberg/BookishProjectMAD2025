package com.example.bookishproject;

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

    public int compareTo(Date otherDate) {
        if (this.year != otherDate.getYear()) {
            return this.year - otherDate.getYear();
        }
        if (this.month != otherDate.month) {
            return this.month - otherDate.getMonth();
        }
        return this.day - otherDate.getDay();
    }

    public void setDay(int day) {
        this.day = day;
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public void setYear(int year) {
        this.year = year;
    }

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

    public int getDay() {
        return this.day;
    }
    public int getMonth() {
        return this.month;
    }
    public int getYear() {
        return this.year;
    }

    public String displayDate() {
        return monthString() + " " + this.day + ", " + this.year;
    }

}
