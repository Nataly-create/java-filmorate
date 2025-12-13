package ru.yandex.practicum.filmorate.exeption;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {
    private long id;
    private String objectDescription;

    public NotFoundException(long id, String objectDescription) {
        super(objectDescription + " with id = " + id + " not found");
    }
}