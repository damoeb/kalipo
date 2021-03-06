package org.kalipo.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.MetricsServlet;
import org.atmosphere.cache.UUIDBroadcasterCache;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.cpr.*;
import org.atmosphere.spring.bean.AtmosphereSpringContext;
import org.atmosphere.util.VoidAnnotationProcessor;
import org.kalipo.web.filter.CachingHttpHeadersFilter;
import org.kalipo.web.filter.StaticResourcesProductionFilter;
import org.kalipo.web.filter.UrlHookServletFilter;
import org.kalipo.web.filter.gzip.GZipServletFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.util.ReflectionUtils;

import javax.inject.Inject;
import javax.servlet.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
@Configuration
@AutoConfigureAfter(CacheConfiguration.class)
public class WebConfigurer implements ServletContextInitializer {

    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    @Inject
    private Environment env;

    @Inject
    private MetricRegistry metricRegistry;

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        log.info("Web application configuration, using profiles: {}", Arrays.toString(env.getActiveProfiles()));
        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);
        initMetrics(servletContext, disps);
        try {
            initAtmosphereServlet(servletContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (env.acceptsProfiles(Constants.SPRING_PROFILE_PRODUCTION)) {
            initCachingHttpHeadersFilter(servletContext, disps);
            initStaticResourcesProductionFilter(servletContext, disps);
        }
        initGzipFilter(servletContext, disps);
        initUrlHookFilter(servletContext, disps);
        log.info("Web application fully configured");
    }

    /**
     * Initializes the GZip filter.
     */
    private void initGzipFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Registering GZip Filter");
        FilterRegistration.Dynamic compressingFilter = servletContext.addFilter("gzipFilter", new GZipServletFilter());
        Map<String, String> parameters = new HashMap<>();
        compressingFilter.setInitParameters(parameters);
        compressingFilter.addMappingForUrlPatterns(disps, true, "*.css");
        compressingFilter.addMappingForUrlPatterns(disps, true, "*.json");
        compressingFilter.addMappingForUrlPatterns(disps, true, "*.html");
        compressingFilter.addMappingForUrlPatterns(disps, true, "*.js");
        compressingFilter.addMappingForUrlPatterns(disps, true, "/app/rest/*");
        compressingFilter.addMappingForUrlPatterns(disps, true, "/metrics/*");
        compressingFilter.setAsyncSupported(true);
    }

    /**
     * Initializes the UrlCatcher filter.
     */
    private void initUrlHookFilter(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Registering UrlHook Filter");
        FilterRegistration.Dynamic urlCatcherFilter = servletContext.addFilter("urlHookFilter", new UrlHookServletFilter());
        Map<String, String> parameters = new HashMap<>();
        urlCatcherFilter.setInitParameters(parameters);
        // todo improve pattern to catch only urls starting with http[s]:
        urlCatcherFilter.addMappingForUrlPatterns(disps, true, "/*");
        urlCatcherFilter.setAsyncSupported(false);
    }

    /**
     * Initializes the static resources production Filter.
     */
    private void initStaticResourcesProductionFilter(ServletContext servletContext,
                                                     EnumSet<DispatcherType> disps) {

        log.debug("Registering static resources production Filter");
        FilterRegistration.Dynamic staticResourcesProductionFilter =
                servletContext.addFilter("staticResourcesProductionFilter",
                        new StaticResourcesProductionFilter());

        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/index.html");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/images/*");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/fonts/*");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/scripts/*");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/styles/*");
        staticResourcesProductionFilter.addMappingForUrlPatterns(disps, true, "/views/*");
        staticResourcesProductionFilter.setAsyncSupported(true);
    }

    /**
     * Initializes the cachig HTTP Headers Filter.
     */
    private void initCachingHttpHeadersFilter(ServletContext servletContext,
                                              EnumSet<DispatcherType> disps) {
        log.debug("Registering Cachig HTTP Headers Filter");
        FilterRegistration.Dynamic cachingHttpHeadersFilter =
                servletContext.addFilter("cachingHttpHeadersFilter",
                        new CachingHttpHeadersFilter());

        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/images/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/fonts/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/scripts/*");
        cachingHttpHeadersFilter.addMappingForUrlPatterns(disps, true, "/styles/*");
        cachingHttpHeadersFilter.setAsyncSupported(true);
    }

    /**
     * Initializes Metrics.
     */
    private void initMetrics(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Initializing Metrics registries");
        servletContext.setAttribute(InstrumentedFilter.REGISTRY_ATTRIBUTE,
                metricRegistry);
        servletContext.setAttribute(MetricsServlet.METRICS_REGISTRY,
                metricRegistry);

        log.debug("Registering Metrics Filter");
        FilterRegistration.Dynamic metricsFilter = servletContext.addFilter("webappMetricsFilter",
                new InstrumentedFilter());

        metricsFilter.addMappingForUrlPatterns(disps, true, "/*");
        metricsFilter.setAsyncSupported(true);

        log.debug("Registering Metrics Servlet");
        ServletRegistration.Dynamic metricsAdminServlet =
                servletContext.addServlet("metricsServlet", new MetricsServlet());

        metricsAdminServlet.addMapping("/metrics/metrics/*");
        metricsAdminServlet.setAsyncSupported(true);
        metricsAdminServlet.setLoadOnStartup(2);
    }

    /**
     * Initializes Atmosphere.
     * https://github.com/Atmosphere/atmosphere/wiki/Configuring-Atmosphere-as-a-Spring-Bean
     */
    private void initAtmosphereServlet(ServletContext servletContext) throws ServletException, IllegalAccessException, InstantiationException {
        log.debug("Registering Atmosphere Servlet");

        final String servletName = "atmosphereServlet";

        AtmosphereServlet servlet = new AtmosphereServlet();

        Field initializerField = ReflectionUtils.findField(AtmosphereServlet.class, "initializer");
        ReflectionUtils.makeAccessible(initializerField);
        AtmosphereFrameworkInitializer initializer = (AtmosphereFrameworkInitializer) initializerField.get(servlet);

        Field frameworkField = ReflectionUtils.findField(AtmosphereFrameworkInitializer.class, "framework");
        ReflectionUtils.makeAccessible(frameworkField);
        ReflectionUtils.setField(frameworkField, initializer, atmosphereFramework());

        ServletRegistration.Dynamic atmosphereServlet = servletContext.addServlet(servletName, servlet);

        atmosphereServlet.setInitParameter("org.atmosphere.cpr.packages", "org.kalipo.web.websocket");
        atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcasterCacheClass", UUIDBroadcasterCache.class.getName());
        atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcaster.shareableThreadPool", "true");
        atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcaster.maxProcessingThreads", "10");
        atmosphereServlet.setInitParameter("org.atmosphere.cpr.broadcaster.maxAsyncWriteThreads", "10");
        servletContext.addListener(new org.atmosphere.cpr.SessionSupport());

        atmosphereServlet.addMapping("/websocket/*");
        atmosphereServlet.setLoadOnStartup(3);
        atmosphereServlet.setAsyncSupported(true);
    }

    @Bean
    public AtmosphereFramework atmosphereFramework() throws ServletException, InstantiationException, IllegalAccessException {
        AtmosphereFramework atmosphereFramework = new NoAnalyticsAtmosphereFramework();
        // atmosphereFramework.setBroadcasterCacheClassName(UUIDBroadcasterCache.class.getName());
        atmosphereFramework.init();

//        atmosphereFramework.init(atmosphereSpringContext(), false);
//        atmosphereFramework.removeAllInitParams();
//        atmosphereFramework.allowAllClassesScan(true);
//        List<AtmosphereInterceptor> interceptors = new ArrayList<>();
//        for (Class<? extends AtmosphereInterceptor> a : atmosphereFramework.defaultInterceptors()) {
//            interceptors.add(a.newInstance());
//        }
//        interceptors.addAll(interceptors());
//        atmosphereFramework.addAtmosphereHandler("/webservice/live/*", atmosphereChatHandler(), interceptors);

//        ReflectorServletProcessor r = new ReflectorServletProcessor();
//        r.setServletClassName("com.sun.jersey.spi.spring.container.servlet.SpringServlet");
//
//        atmosphereFramework.addAtmosphereHandler("/websocket/*", r);


        return atmosphereFramework;
    }

    @Bean
    public AtmosphereSpringContext atmosphereSpringContext() {
        AtmosphereSpringContext atmosphereSpringContext = new AtmosphereSpringContext();
        Map<String, String> map = new HashMap<>();
        map.put("org.atmosphere.cpr.broadcasterClass", org.atmosphere.cpr.DefaultBroadcaster.class.getName());
        map.put(AtmosphereInterceptor.class.getName(), TrackMessageSizeInterceptor.class.getName());
        map.put(AnnotationProcessor.class.getName(), VoidAnnotationProcessor.class.getName());
        map.put("org.atmosphere.cpr.broadcasterLifeCyclePolicy", BroadcasterLifeCyclePolicy.ATMOSPHERE_RESOURCE_POLICY.IDLE_DESTROY.toString());
        atmosphereSpringContext.setConfig(map);
        return atmosphereSpringContext;
    }

    /**
     * Atmosphere sends tracking data to Google Analytics, which is a potential security issue.
     * <p>
     * If you want to send this data, please use directly the AtmosphereFramework class.
     * </p>
     */
    public class NoAnalyticsAtmosphereFramework extends AtmosphereFramework {

        public NoAnalyticsAtmosphereFramework() {
            super();
        }

        @Override
        protected void analytics() {
            // noop
        }


    }
}
