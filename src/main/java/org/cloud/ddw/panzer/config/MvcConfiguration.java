package org.cloud.ddw.panzer.config;

import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.cloud.ddw.panzer.controller.DefaultController;
import org.cloud.ddw.panzer.utils.DateFormatTag;
import org.cloud.ddw.panzer.utils.PaginationTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Created by d05660ddw on 2016/11/2.
 */
@EnableWebMvc
@Configuration
@ComponentScan(useDefaultFilters = false, basePackages = {"org.cloud.ddw.panzer"},
        includeFilters = {@ComponentScan.Filter(Controller.class)})
public class MvcConfiguration extends WebMvcConfigurerAdapter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public CommonsMultipartResolver configureMultipartResolver() {
        CommonsMultipartResolver resolver = new CommonsMultipartResolver();
        resolver.setMaxUploadSize(1048576000);
        resolver.setMaxInMemorySize(40960);
        resolver.setDefaultEncoding("utf-8");
        return resolver;
    }

    @Bean
    public SimpleUrlHandlerMapping configureSimpleUrlHandlerMapping() {
        SimpleUrlHandlerMapping resolver = new SimpleUrlHandlerMapping();
        Properties mappings = new Properties();
        mappings.put("/*/**", getDefaultController());
        resolver.setOrder(2147483647);
        resolver.setMappings(mappings);
        return resolver;
    }

    @Bean
    public DefaultController getDefaultController() {
        return new DefaultController();
    }

    /**
     * Basic setup for freemarker views.
     */
    @Bean
    public FreeMarkerViewResolver freeMarkerViewResolver() {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        /* Make sure all our views are in /WEB-INF/ftl/views/ and end with *.ftl
         * This helps keep the views in one place
         * Note: This is used in conjuction with
         *       `configuration.setServletContextForTemplateLoading` below
         */
        resolver.setPrefix("/");
        resolver.setSuffix(".ftl");
        /* Disable the cache when doing local development
         * This means that you will see any updates to an FTL file
         * immediately after refreshing the browser
         * (don't disable the cache in production!)
         */
        resolver.setCache(false);

        /* When returning a freemarker view, set the charset to UTF-8
         * and the content-type to text/html
         * Note: This should always be set by the backend. Don't set this in the view layer!
         */
        resolver.setContentType("text/html;charset=UTF-8");

        return resolver;
    }

    @Bean
    public FreeMarkerConfigurer freeMarkerConfigurer(WebApplicationContext applicationContext)
            throws IOException, TemplateException {
        FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();

        Map<String, Object> variables = new HashMap<>();
        variables.put("paginate", new PaginationTag());
        variables.put("dateFormat", new DateFormatTag());
        configurer.setFreemarkerVariables(variables);

        configurer.setServletContext(applicationContext.getServletContext());

        freemarker.template.Configuration configuration = configurer.createConfiguration();

        // Make sure all freemarker files go in /WEB-INF/ftl/
        // This helps keep the code organized
        configuration.setServletContextForTemplateLoading(applicationContext.getServletContext(), "/WEB-INF/ftl/");

        // When starting a new FreeMarker project, always set the incompatible improvements to the version
        // you are using.
        configuration.setIncompatibleImprovements(freemarker.template.Configuration.VERSION_2_3_25);

        // Use this for local development. When a template exception occurs,
        // it will format the error using HTML so it can be easily read
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);

        // Makre sure everything is UTF-8 from the beginning to avoid headaches
        configuration.setDefaultEncoding("UTF-8");
        configuration.setOutputEncoding("UTF-8");
        configuration.setURLEscapingCharset("UTF-8");

        // Apply the configuration settings to the configurer
        configurer.setConfiguration(configuration);

        return configurer;
    }

    @Bean
    SessionLocaleResolver localeResolver() {
        // Enable the SessionLocaleResolver
        // Even if you don't localize your webapp you should still specify this
        // so that things like numbers, dates, and currencies are formatted properly
        SessionLocaleResolver localeResolver = new SessionLocaleResolver();
        localeResolver.setDefaultLocale(Locale.US);

        return localeResolver;
    }
}
