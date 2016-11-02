package org.cloud.ddw.panzer;

import org.cloud.ddw.panzer.config.RootConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.support.GenericWebApplicationContext;


/**
 * Created by d05660ddw on 2016/11/2.
 */
public class Application {
    /**
     * Flag that will be set to true when the web application context
     * (SpringMVC) is refreshed.
     */
    static boolean webApplicationContextInitialized = false;

    public static void jettyServer() {
        final Logger logger = LoggerFactory.getLogger("Application");

        AnnotationConfigApplicationContext applicationContext =
                new AnnotationConfigApplicationContext();
        try {


            /*
             * One problem with SpringMVC is it creates its own application
             * context, and so it can end up failing but our application will
             * keep running.
             *
             * To detect the case where the SpringMVC's web application context
             * fails we'll listen for ContextRefreshEvents and set a flag when
             * we see the web application context refresh.
             */
            applicationContext.addApplicationListener(
                    new ApplicationListener<ContextRefreshedEvent>() {
                        @Override
                        public void onApplicationEvent(
                                ContextRefreshedEvent event) {
                            ApplicationContext ctx = event.getApplicationContext();
                            if (ctx instanceof GenericWebApplicationContext) {
                                webApplicationContextInitialized = true;
                            }
                        }
                    }
            );

            applicationContext.registerShutdownHook();
            applicationContext.register(RootConfiguration.class);
            applicationContext.refresh();

            if (!webApplicationContextInitialized) {
                logger.error("Failed to initialize web application.  Exiting.");
                System.exit(1);
            }

            logger.info("Running.");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            applicationContext.close();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        jettyServer();
    }
}
