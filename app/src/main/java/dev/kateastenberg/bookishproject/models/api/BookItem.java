package dev.kateastenberg.bookishproject.models.api;

/*
This is a class for a BookItem
This functions as sort of an adapter class between an API data return and a Book object.
It uses an id and VolumeInfo, which is the information that the API has on its books
 */
public class BookItem {

    private final String id;
    private final VolumeInfo volumeInfo;

    public BookItem(String id, VolumeInfo volumeInfo) {
        this.id = id;
        this.volumeInfo = volumeInfo;
    }

    // just getters

    public String getId() {
        return this.id;
    }

    public VolumeInfo getVolumeInfo() {
        return this.volumeInfo;
    }

}
