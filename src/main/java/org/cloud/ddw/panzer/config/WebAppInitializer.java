package org.cloud.ddw.panzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;

/**
 * Created by d05660ddw on 2016/11/2.
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer
        implements ServletContextListener {

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * See {@link AbstractAnnotationConfigDispatcherServletInitializer}.
     */
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    /**
     * Set the application context for the Spring MVC web tier.
     *
     * @See {@link AbstractAnnotationConfigDispatcherServletInitializer}
     */
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { MvcConfiguration.class };
    }

    /**
     * Map the Spring MVC servlet as the root.
     *
     * @See {@link AbstractAnnotationConfigDispatcherServletInitializer}
     */
    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

        /* Let super do its thing... */
        super.onStartup(servletContext);

        /* Add the Spring Security filter.
        servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy())
               .addMappingForUrlPatterns(null, false, "/*"); */

        // Add metrics servlet.
        //ServletRegistration.Dynamic metricsServlet = servletContext.addServlet("metrics", AdminServlet.class);
        //metricsServlet.addMapping("/metrics/*");
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        try {
            onStartup(servletContextEvent.getServletContext());
        } catch (ServletException e) {
            logger.error("Failed to initialize web application", e);
            System.exit(0);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    /**
     * Overrided to squelch a meaningless log message when embedded.
     */
    @Override
    protected void registerContextLoaderListener(ServletContext servletContext) {
    }
}
