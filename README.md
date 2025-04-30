# BookishProjectMAD2025

This is a project for the Mobile Application Development course at Northeastern (Roux Campus), Spring 2025.

BookishProject is a mobile app for Android that has three major functions.

(1) Tracking books
This app keeps track of a user's collection of books. A user can add a book to their collection by searching the GoogleBooks API and adding it to their collection, which is a Firebase database. They can change the data of a book by adding a series, number, genre, age level, and tags to the book.

(2) Recommending books
Users can get book recommendations. Currently the only option for this implemented is based on similarity to other books in the user's collection. The user enters a book they liked and the app retrieves other books similar to this book based on the similarity of their attributes.

(3) Tracking reading activity
Users can log their reading activity by creating entries in the reading journal. The a journal entry has attributes date, timestamp, book, activity type, pages read (if applicable), and comments. These entries are stored in the Firebase database, and the Journal screen displays the user's entries by date and time.

FUTURE DIRECTIONS:
- delete book
- filter by genre, age range
- search by title or author
- sort alphabetically or by date added
- improve welcome page
- dedicated landscape layouts
- favorites
- book menu page
- owned books
- update Journal UI
- transitions

VERSIONS:
1.0: initial version
1.1: (don't remember)
1.2: (don't remember)
1.3: added book editing, merged create/view entry fragments, changed colors, updated Journal/Entry UI
