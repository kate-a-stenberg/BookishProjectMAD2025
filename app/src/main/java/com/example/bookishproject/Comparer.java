package com.example.bookishproject;

/*
This is an interface for Comparer type classes.
A class that implements Comparer must have a compareBooks method returning a float, which is a similarity score.
 */
public interface Comparer {
    public float compareBooks(Book compareBook);
}
