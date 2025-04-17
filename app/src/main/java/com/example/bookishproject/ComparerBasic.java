package com.example.bookishproject;

import android.util.Log;

import java.util.List;

/*
This is a class for a ComparerBasic, which implements the Comparer interface
A basic comparer will compare two books using extremely basic methods doable using the book data provided by Google Books
Which is pretty much just author, genre, maturity rating, and categories.
Its only attribute is the Book it is going to compare to another book.
 */
public class ComparerBasic implements Comparer {

    private static final String TAG = "Book Comparison"; // For logging


    private Book book;

    /*
    Basic constructor using the book to compare to another book
     */
    public ComparerBasic(Book book) {
        this.book = book;
    }

    /*
    Method for overall book comparison.
    Compares various attributes and increases the earned score for each similar attribute, and increases the total possible similarity.
    Calculates similarity by percentage--how much of the total possible similarity did the books earn
    Takes a book to compare against as an argument.
    Returns a float which represents how similar two Books are.
     */
    @Override
    public float compareBooks(Book compareBook) {

        // initialize matchScore, earnedScore, and totalScore
        float matchScore = 0;
        float earnedScore = 0;
        float totalScore = 0;

        // add the author similarity to the earned score and increase the raw total by 5
        earnedScore += compareAuthor(compareBook);
        totalScore += 5;

        // add the genre similarity to the earned score and increase the raw total by 5
        earnedScore += compareGenre(compareBook);
        totalScore += 5;

        // add the age range similarity to the earned score and increase the raw total by 5
        earnedScore += compareAgeRange(compareBook);
        totalScore += 5;

        // add the categories similarity to the earned score and increase the raw total by 5
        earnedScore += compareCategories(compareBook);
        totalScore += 5;

        // calculate matchScore by comparing earnedScore to totalScore
        matchScore = earnedScore/totalScore;

        // return matchScore
        return matchScore;
    }

    /*
    Method to quantify the similarity of the authors of two books.
    Returns a full score of 5 if the authors are an exact match and 3 if they share at least one author
    Takes a second book as an arguments.
    Returns a float representing the earned similarity points between the two books.
     */
    private float compareAuthor(Book compareBook) {

        // initialize the score at 0
        float score = 0;

        // null check
        if (book.getAuthor() != null && compareBook.getAuthor() != null) {

            // If they're identical, give return a full score of 5
            if (book.getAuthor().equals(compareBook.getAuthor())) {
                score = 5;
            }

            // If there are multiple authors, check for a partial match
            else {
                // parse the author attribute strings by comma to get separate authors
                String[] authors1 = book.getAuthor().split(",");
                String[] authors2 = compareBook.getAuthor().split(",");

                // Trim whitespace from each author name
                for (int i = 0; i < authors1.length; i++) {
                    authors1[i] = authors1[i].trim();
                }
                for (int i = 0; i < authors2.length; i++) {
                    authors2[i] = authors2[i].trim();
                }

                // we haven't found a match
                boolean foundMatch = false;
                // for each author in the first array of authors
                for (String author1 : authors1) {
                    // and for each author in the second array of authors
                    for (String author2 : authors2) {
                        // if the first author is the same as the second author
                        if (author1.equals(author2)) {
                            // score is 3 (partial credit)
                            score = 3;
                            // we found a match!
                            foundMatch = true;
                            break;
                        }
                    }
                    // stop if we find a match. We don't need to find any more; partial credit has already been given
                    if (foundMatch) break;
                }
            }
        }

        Log.d(TAG, "author match: " + score);

        return score;
    }

    /*
    Method to compare the genres of two books.
    Returns a score of 5 if they match and 0 if they don't.
    Takes a second Book as an argument.
    Returns a float representing the score.
     */
    private float compareGenre(Book compareBook) {

        float score = 0;

        // if Book1's genre is the same as Book2's genre, set score to 5
        if (book.getGenre() != null && compareBook.getGenre() != null && book.getGenre().equals(compareBook.getGenre())) {
            score = 5;
        }
        Log.d(TAG, "genre match: " + score);
        return score;
    }

    /*
    Method to compare the age ranges of two books.
    Returns a score of 5 if they match and 0 if they don't.
    Takes a second Book as an argument.
    Returns a float representing the score.
     */
    private float compareAgeRange(Book compareBook) {

        float score = 0;

        // if age ranges match, return a score of 5
        if (book.getAgeRange() != null && compareBook.getAgeRange() != null && book.getAgeRange().equals(compareBook.getAgeRange())) {
            score = 5;
        }
        Log.d(TAG, "age range match: " + score);
        return score;
    }

    /*
    Method to compare the age ranges of two books.
    Returns 1 point for each category that matches up to 5.
    Takes a second Book as an argument.
    Returns a float representing the score.
    */
    private float compareCategories(Book compareBook) {

        float score = 0;

        // get both category lists
        List<String> catsBook = book.getCategories();
        List<String> catsComp = compareBook.getCategories();

        if (catsBook != null && catsComp != null && !catsBook.isEmpty() && !catsComp.isEmpty()) {
            // for each category in our Book's category list
            for (String cat : catsBook) {
                // if it's in the compBook's category list
                if (catsComp.contains(cat)) {
                    // increase score by 1
                    score += 1; // Add 1 point per shared category
                }
            }
        }

        // if the score is greater than 5, no it's not
        if (score > 5) {
            score = 5;
        }
        Log.d(TAG, "categories match: " + score);
        return score;
    }

}
