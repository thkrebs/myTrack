package com.tmv.core.service;

import java.io.IOException;

public interface WordPressPostService {

    /**
     * Erstellt einen neuen WordPress-Beitrag.
     *
     * @param title   Der Titel des Beitrags
     * @param content Der Inhalt des Beitrags
     * @param lat     Die geografische Breite des Beitrags
     * @param lng     Die geografische Länge des Beitrags
     * @return
     */
    Integer createPost(String title, String content, double lat, double lng) throws IOException;
}