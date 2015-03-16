package org.kalipo.service.util;

import org.pegdown.LinkRenderer;
import org.pegdown.ast.AutoLinkNode;
import org.pegdown.ast.ExpLinkNode;
import org.pegdown.ast.RefLinkNode;
import org.pegdown.ast.WikiLinkNode;

/**
 * Created by damoeb on 16.03.15.
 */
public class UrlBoxingLinkRenderer extends LinkRenderer {

    private static String prefix = "somewhere-i-used-to-live"; // todo from properties

    public UrlBoxingLinkRenderer() {
        super();
    }

    private Rendering box(Rendering r) {
        r.href = prefix + r.href;
        return r;
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
