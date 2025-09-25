package dev.kateastenberg.bookishproject.interfaces;

public interface UseCaseCallback<T> {
    void onSuccess(T result);
    void onError(String error);
}
