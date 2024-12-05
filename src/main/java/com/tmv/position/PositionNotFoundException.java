package com.tmv.position;

public class PositionNotFoundException extends RuntimeException {

    PositionNotFoundException(Long id) {
        super("Could not find position " + id);
    }
}
