package com.gmail.stefvanschiedev.browser.utils;

import java.lang.reflect.Field;
import java.net.HttpCookie;

/**
 * A utility class for cookies
 */
public class CookieUtil {

    //save format:
    //name=value;Comment=value;CommentURL=value;Discard=value;Domain=value;Max-Age=value;Path=value;Port=value;Secure=value;HttpOnly=value;Version=value;

    public static String toString(HttpCookie cookie) {
        try {
            Class<? extends HttpCookie> clazz = cookie.getClass();
            Field whenCreated = clazz.getDeclaredField("whenCreated");
            whenCreated.setAccessible(true);
            Field header = clazz.getDeclaredField("header");
            header.setAccessible(true);

            return cookie.getName() + "|" + cookie.getValue() +
                    ";Comment=" + cookie.getComment() +
                    ";CommentURL=" + cookie.getCommentURL() +
                    ";Discard=" + cookie.getDiscard() +
                    ";Domain=" + cookie.getDomain() +
                    ";Header=" + header.get(cookie) +
                    ";Max-Age=" + cookie.getMaxAge() +
                    ";Path=" + cookie.getPath() +
                    ";Port=" + cookie.getPortlist() +
                    ";Secure=" + cookie.getSecure() +
                    ";HttpOnly=" + cookie.isHttpOnly() +
                    ";Version=" + cookie.getVersion() +
                    ";WhenCreated=" + whenCreated.get(cookie);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static HttpCookie fromString(String string) {
        String[] split = string.split(";");
        String[] nameValue = split[0].split("|");

        if (nameValue.length < 2)
            return null;

        HttpCookie cookie = new HttpCookie(nameValue[1], nameValue[2]);

        for (int i = 1; i < split.length; i++) {
            String[] value = split[i].split("=");

            if (value.length < 2)
                continue;

            if (value[1].equals("null"))
                value[1] = null;

            try {
                switch (value[0]) {
                    case "Comment":
                        cookie.setComment(value[1]);
                        break;
                    case "CommentURL":
                        cookie.setCommentURL(value[1]);
                        break;
                    case "Discard":
                        cookie.setDiscard(Boolean.parseBoolean(value[1]));
                        break;
                    case "Domain":
                        cookie.setDomain(value[1]);
                        break;
                    case "Header":
                        Field header = cookie.getClass().getDeclaredField("header");
                        header.setAccessible(true);
                        header.set(cookie, value[1]);
                        break;
                    case "Max-Age":
                        cookie.setMaxAge(Long.parseLong(value[1] == null ? "0" : value[1]));
                        break;
                    case "Path":
                        cookie.setPath(value[1]);
                        break;
                    case "Port":
                        cookie.setPortlist(value[1]);
                        break;
                    case "Secure":
                        cookie.setSecure(Boolean.parseBoolean(value[1]));
                        break;
                    case "HttpOnly":
                        cookie.setHttpOnly(Boolean.parseBoolean(value[1]));
                        break;
                    case "Version":
                        cookie.setVersion(Integer.parseInt(value[1] == null ? "0" : value[1]));
                        break;
                    case "WhenCreated":
                        Field whenCreated = cookie.getClass().getDeclaredField("whenCreated");
                        whenCreated.setAccessible(true);
                        whenCreated.set(cookie, Long.parseLong(value[1]));
                        break;
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return cookie;
    }
}