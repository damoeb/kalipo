package org.kalipo.service;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.kalipo.security.Privileges;
import org.kalipo.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for render plaintext to HTML.
 */
@Service
public class MarkupService {

    private final Logger log = LoggerFactory.getLogger(MarkupService.class);

    private static final Pattern REGEX_HASHTAG = Pattern.compile("#(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern REGEX_QUOTE = Pattern.compile("[\n\r\t ]*[>]+([^\n\r]+)[\n\r]?", Pattern.DOTALL);
    private static final Pattern REGEX_LINK = Pattern.compile("\\(?\\bhttp[s]?://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]", Pattern.CASE_INSENSITIVE);

    /**
     * Translates plaintext to HTML. It creates quotes, links and hashtags. URL shortening is resolved if possible, to increase anonymity.
     *
     * @param plainText
     * @return the HTML markup
     */
    public String toHtml(String plainText) {

        StringBuffer htmlBuffer = new StringBuffer(plainText);

        renderQuotes(htmlBuffer);
        renderLinks(htmlBuffer);
        renderHashtags(htmlBuffer);

        return htmlBuffer.toString();
    }

    private void renderLinks(StringBuffer htmlBuffer) {

        Matcher matcher = REGEX_LINK.matcher(htmlBuffer);
        while (matcher.find()) {

            String replacement;
            final String url = matcher.group().trim();
            try {
                final URI targetUri = resolveRedirects(url);

                if (SecurityUtils.hasPrivilege(Privileges.CREATE_COMMENT_WITH_LINK)) {
                    replacement = createHref(targetUri);
                } else {
                    replacement = String.format("%s [%s]", targetUri.toASCIIString(), targetUri.getHost());
                }

            } catch (URISyntaxException e) {
                replacement = url;
            }

            htmlBuffer.replace(matcher.start(), matcher.end(), replacement);
            matcher.region(matcher.start() + replacement.length(), htmlBuffer.length());
        }
    }

    /**
     * Follows all redirects and uses the final location as url
     *
     * @param url
     * @return
     * @throws URISyntaxException
     */
    public URI resolveRedirects(String url) throws URISyntaxException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            HttpGet request = new HttpGet(url);
            HttpContext context = new BasicHttpContext();

            HttpResponse result = httpClient.execute(request, context);
            URI finalUrl = request.getURI();
            RedirectLocations locations = (RedirectLocations) context.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
            if (locations != null) {
                finalUrl = locations.getAll().get(locations.getAll().size() - 1);
            }

            return finalUrl;

        } catch (Exception e) {
            log.error(String.format("Failed to follow url %s", url), e);

        } finally {
            try {
                httpClient.close();
            } catch (IOException e1) {
                // ignore
            }
        }
        return new URI(url);
    }

    private String createHref(URI targetUri) {
        String asciiUrl = targetUri.toASCIIString();
        String label = asciiUrl;
        if (asciiUrl.length() > 35) {
            label = asciiUrl.substring(0, 25) + "…" + asciiUrl.substring(asciiUrl.length() - 10, asciiUrl.length());
        }
        // todo pipe links though a forwarder for log purposes
        return String.format("<a href=\"%s\">%s</a> [%s]", asciiUrl, label, targetUri.getHost());
    }

    private void renderHashtags(StringBuffer htmlBuffer) {

        Matcher matcher = REGEX_HASHTAG.matcher(htmlBuffer);

        while (matcher.find()) {
            String replacement = createHashtag(matcher.group(1).trim());
            htmlBuffer.replace(matcher.start(), matcher.end(), replacement);
            matcher.region(matcher.start() + replacement.length(), htmlBuffer.length());
        }
    }

    private String createHashtag(String hashtag) {
        return String.format("<a href=\"/#/tag/%1$s\">#%1$s</a>", hashtag);
    }

    private void renderQuotes(StringBuffer htmlBuffer) {

        Matcher matcher = REGEX_QUOTE.matcher(htmlBuffer);

        while (matcher.find()) {
            String replacement = createQuote(matcher.group(1).trim());
            htmlBuffer.replace(matcher.start(), matcher.end(), replacement);
            matcher.region(matcher.start() + replacement.length(), htmlBuffer.length());
        }
    }

    private String createQuote(String quote) {
        return String.format("<div class=\"quote\">%s</div>", quote);
    }
}
