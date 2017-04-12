package com.gmail.stefvanschiedev.browser;

import java.io.File;

/**
 * Container for often used values
 */
public class Values {

    public static final File SRCFILE = new File(System.getProperty("user.home") + "/.quec");
    public static final File COOKIESFILE = new File(SRCFILE, "cookies.qbf");
    public static final File BOOKMARKFILE = new File(SRCFILE, "bookmarks.qbf");
    public static final File VISITEDPAGESFILE = new File(SRCFILE, "visited.qbf");
    public static final File USERDATAFILE = new File(SRCFILE, "userdata");
    public static final File EXTENSIONSFILE = new File(SRCFILE, "extensions");

    static final File[] INSTALLABLEFILES = new File[] {SRCFILE, COOKIESFILE, BOOKMARKFILE, VISITEDPAGESFILE, USERDATAFILE, EXTENSIONSFILE};

    static final Double[] ZOOM_LIST = {.25, .5, 1.0, 1.5, 2.0, 4.0};
}