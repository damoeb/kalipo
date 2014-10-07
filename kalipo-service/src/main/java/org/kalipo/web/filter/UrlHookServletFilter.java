package org.kalipo.web.filter;

import org.apache.commons.lang3.StringUtils;
import org.kalipo.repository.ThreadRepository;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Support Url forwarding to thread e.g. <kalipo-url>/http://www.someurl.com
 * <p>
 * Created by damoeb on 25.09.14.
 */
public class UrlHookServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to initialize
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        final String forwardUri = URLDecoder.decode(StringUtils.trimToEmpty(StringUtils.substring(httpRequest.getRequestURI(), 1)), "UTF-8");
        if (isUrl(forwardUri)) {

            WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(request.getServletContext());

            ThreadRepository threadRepository = context.getBean(ThreadRepository.class);
            // todo find thread by exact string comparison
//            Thread thread = threadRepository.findByUri(forwardUri);
//
//            if (thread == null) {
//                Document document = Jsoup.parse(new URL(forwardUri), 400);
//                httpResponse.sendRedirect(String.format("/#/threads/create?title=%s&uri=%s", URLEncoder.encode(document.title(), "UTF-8"), URLEncoder.encode(forwardUri, "UTF-8")));
//
//            } else {
//                httpResponse.sendRedirect("/#/thread/" + thread.getId());
//            }


        } else {
            chain.doFilter(request, response);
        }

    }

    @Override
    public void destroy() {
        // Nothing to destroy
    }

    // --

    private boolean isUrl(String forwardUri) {

        if (!StringUtils.startsWithIgnoreCase(forwardUri, "http")) {
            return false;
        }

        try {
            // url must be valid, but not necessarily reachable - could have been deleted
            new URL(forwardUri);

        } catch (MalformedURLException e) {
            return false;
        }

        return true;
    }
}
