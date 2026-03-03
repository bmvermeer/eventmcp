package nl.brianvermeer.mcp.eventmcp.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Validator {

    private Validator() {}

    public static String checkURL(String url) {
        if (url == null) return null;
        if (!url.startsWith("http://") && !url.startsWith("https://")) return null;

        try {
            new URI(url);
            return url;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    public static String checkURL(String url, String defaultValue) {
        return checkURL(url) != null ? url : defaultValue;
    }
}
