package org.kalipo.service.util;

import org.kalipo.domain.Comment;
import org.pegdown.LinkRenderer;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.WikiLinkNode;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by damoeb on 16.03.15.
 */
public class UrlBoxingLinkRenderer extends LinkRenderer {

    private final String prefix;
    private final Set<Comment.Link> links = new HashSet<>();

    public UrlBoxingLinkRenderer(String prefix) {
        super();

        this.prefix = prefix;
    }

    private Rendering box(Rendering r) {
        r.href = prefix + r.href;

        links.add(new Comment.Link(r.href));

        return r;
    }

    public Set<Comment.Link> getLinks() {
        return links;
    }

    @Override
    public Rendering render(AutoLinkNode node) {
        return box(super.render(node));
    }

    @Override
    public Rendering render(ExpLinkNode node, String text) {
        return box(super.render(node, text));
    }

    @Override
    public Rendering render(RefLinkNode node, String url, String title, String text) {
        return box(super.render(node, url, title, text));
    }

    @Override
    public Rendering render(WikiLinkNode node) {
        return box(super.render(node));
    }
}
