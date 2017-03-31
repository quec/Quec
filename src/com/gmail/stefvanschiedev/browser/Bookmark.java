package com.gmail.stefvanschiedev.browser;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a bookmark
 */
public class Bookmark {

    private URL baseURL;

    private static Set<Bookmark> bookmarks = new HashSet<>();

    private Bookmark(URL baseURL) {
        this.baseURL = baseURL;
    }

    private URL getBaseURL() {
        return baseURL;
    }

    static void addBookmark(String baseURL) {
        try {
            addBookmark(new URL(baseURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private static void addBookmark(URL baseURL) {
        bookmarks.add(new Bookmark(baseURL));
    }

    static Bookmark getBookmark(String baseURL) {
        try {
            return getBookmark(new URL(baseURL));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Bookmark getBookmark(URL baseURL) {
        for (Bookmark bookmark : bookmarks) {
            if (bookmark.getBaseURL().equals(baseURL))
                return bookmark;
        }

        return null;
    }

    static void load() {
        try {
            for (String line : Files.readAllLines(Values.BOOKMARKFILE.toPath()))
                addBookmark(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void removeBookmark(String bookmark) {
        bookmarks.remove(getBookmark(bookmark));
    }

    static void save() {
        try {
            Files.write(Values.BOOKMARKFILE.toPath(), "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING);

            for (Bookmark bookmark : bookmarks)
                Files.write(Values.BOOKMARKFILE.toPath(), (bookmark.getBaseURL().toString() + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}