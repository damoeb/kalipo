package org.kalipo.domain;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by markus on 15.07.15.
 */
public class Markup {

    private final StringBuffer buffer;
    private final Set<URI> uris = new HashSet<URI>(5);
    private final Set<String> hashtags = new HashSet<String>(5);

    public Markup(String plainText) {
        buffer = new StringBuffer(plainText);
    }

    public StringBuffer buffer() {
        return buffer;
    }

    public Set<URI> uris() {
        return uris;
    }

    public Set<String> hashtags() {
        return hashtags;
    }
}
