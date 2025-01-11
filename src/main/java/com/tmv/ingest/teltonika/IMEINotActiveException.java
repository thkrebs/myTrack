package com.tmv.ingest.teltonika;

public class IMEINotActiveException extends RuntimeException{
    public IMEINotActiveException(String s) {
        super(s);
    }
}
