package dev.kateastenberg.bookishproject.intents;

import dev.kateastenberg.bookishproject.models.Book;
import dev.kateastenberg.bookishproject.models.UserBook;

public abstract class BooksIntent {

    public static class LoadBooks extends BooksIntent {}

    public static class CardFlipped extends BooksIntent {
        public final int position;
        public CardFlipped (int position) {
            this.position = position;
        }
    }

    public static class BookDetailView extends BooksIntent {
        public final int position;
        public BookDetailView (int position) {
            this.position = position;
        }
    }

    public static class FindBooks extends BooksIntent {
        public FindBooks() {}
    }

    public static class SearchUserBooks extends BooksIntent {
        public final String title;
        public final String author;
        public SearchUserBooks(String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    public static class SearchUniversalBooks extends BooksIntent {
        public final String title;
        public final String author;
        public SearchUniversalBooks(String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    public static class SearchApiBooks extends BooksIntent {
        public final String title;
        public final String author;
        public SearchApiBooks(String title, String author) {
            this.title = title;
            this.author = author;

        }
    }

    public static class AddBook extends BooksIntent {
        public final Book book;
        public AddBook (Book book) {
            this.book = book;
        }
    }

    public static class AddApiBook extends BooksIntent {
        public final Book book;
        public AddApiBook(Book book) {
            this.book = book;
        }
    }

    public static class FindSimilarBooks extends BooksIntent {
        public final UserBook userBook;
        public FindSimilarBooks (UserBook userBook) {
            this.userBook = userBook;
        }
    }

    public static class GoSearch extends BooksIntent {
        public final String title;
        public final String author;
        public GoSearch(String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    public static class GoApiSearch extends BooksIntent {
        public final String title;
        public final String author;
        public GoApiSearch(String title, String author) {
            this.title = title;
            this.author = author;
        }
    }

    public static class AdminCheck extends BooksIntent {
        public AdminCheck() {}
    }

    public static class UpdateUserBook extends BooksIntent {
        public final UserBook userBook;
        public UpdateUserBook (UserBook userBook) {
            this.userBook = userBook;
        }
    }

    public static class UpdateBook extends BooksIntent {
        public final UserBook userBook;
        public UpdateBook (UserBook userBook) {
            this.userBook = userBook;
        }
    }

    public static class SeeUserDetails extends BooksIntent{
        public SeeUserDetails () {}
    }

    public static class HideUserDetails extends BooksIntent {
        public HideUserDetails() {}
    }

    public static class SetEditable extends BooksIntent {
        public SetEditable() {}
    }

    public static class DeleteBook extends BooksIntent {
        public final Book book;
        public DeleteBook (Book book) {
            this.book = book;
        }
    }

    public static class RemoveUserBook extends BooksIntent {
        public final UserBook userBook;
        public RemoveUserBook (UserBook userBook) {
            this.userBook = userBook;
        }
    }
}
