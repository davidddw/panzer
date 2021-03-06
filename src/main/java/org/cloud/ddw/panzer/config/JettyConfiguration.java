package org.cloud.ddw.panzer.config;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ErrorPageErrorHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.IOException;

/**
 * Created by d05660ddw on 2016/11/2.
 */
@Configuration
public class JettyConfiguration {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext applicationContext;

    @Value("${jetty.port:8080}")
    private int jettyPort;

    @Value("${jetty.contextPath:/}")
    private String contextPath;

    @Value("${jetty.threadPool.MaxThreads:100}")
    private int maxThreads;

    @Value("${jetty.threadPool.minThreads:5}")
    private int minThreads;

    @Value("${jetty.threadPool.daemon:false}")
    private boolean isDaemon;

    @Value("${jetty.threadPool.name:JettyServer}")
    private String threadPoolName;

    @Value("${jetty.log.path:/tmp/yyyy_mm_dd.request.log}")
    private String logPath;

    @Value("${ssl.keyStorePath:/etc/keystore}")
    private String keyStorePath;

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public WebAppContext webAppContext() throws IOException {

        WebAppContext ctx = new WebAppContext();
        ctx.setContextPath(contextPath);

        // System.out.println(new
        // ClassPathResource("webapp").getURI().toString());
        String warPath = new ClassPathResource("webapp").getURI().toString();
        logger.info("warPath:{}", warPath);
        ctx.setWar(warPath);
        //ctx.setWar(new ClassPathResource("webapp").getURI().toString());

        // http://www.eclipse.org/jetty/documentation/current/configuring-jsp.html
		/* Disable directory listings if no index.html is found. */
        ctx.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        ctx.setInitParameter("development", "true");
        ctx.setInitParameter("checkInterval", "10");
        ctx.setInitParameter("compilerTargetVM", "1.8");
        ctx.setInitParameter("compilerSourceVM", "1.8");
        ctx.setInitParameter("ttfFile", "/WEB-INF/resources/verdanab.ttf");

		/*
		 * Create the root web application context and set it as a servlet
		 * attribute so the dispatcher servlet can find it.
		 */
        GenericWebApplicationContext webApplicationContext = new GenericWebApplicationContext();
        webApplicationContext.setParent(applicationContext);
        webApplicationContext.refresh();
        ctx.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webApplicationContext);

        ctx.addEventListener(new WebAppInitializer());

        return ctx;
    }

    @Bean
    public QueuedThreadPool queuedThreadPool() {
        // Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMaxThreads(maxThreads);
        threadPool.setDaemon(isDaemon);
        threadPool.setMinThreads(minThreads);
        threadPool.setName(threadPoolName);

        return threadPool;
    }

    // only for jetty9
    @Bean
    public HttpConfiguration httpConfiguration() {

        // HTTP Configuration
        HttpConfiguration http_config = new HttpConfiguration();
        http_config.setSecureScheme("https");
        http_config.setSecurePort(8443);
        http_config.setOutputBufferSize(32768);
        http_config.setRequestHeaderSize(8192);
        http_config.setResponseHeaderSize(8192);
        http_config.setSendServerVersion(true);
        http_config.setSendDateHeader(false);
        // httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        return http_config;
    }

    // only for jetty9
    @Bean
    public HttpConfiguration httpsConfiguration() {

        // SSL HTTP Configuration
        HttpConfiguration https_config = new HttpConfiguration(httpConfiguration());

        /**
         * Referecne:
         * http://download.eclipse.org/jetty/stable-9/xref/org/eclipse/jetty/server/SecureRequestCustomizer.html
         *
         */
        https_config.addCustomizer(new SecureRequestCustomizer());

        return https_config;
    }

    // only for jetty9
    @Bean
    public SslContextFactory sslContextFactory() {

        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(keyStorePath);

        // FIXME
        sslContextFactory.setKeyStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setKeyManagerPassword("OBF:1u2u1wml1z7s1z7a1wnl1u2g");
        sslContextFactory.setTrustStorePath(keyStorePath);
        sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                "SSL_DHE_DSS_WITH_DES_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");

        return sslContextFactory;
    }

    @Bean
    public RequestLogHandler requestLogHandler() {

        // === jetty-requestlog.xml ===
        NCSARequestLog requestLog = new NCSARequestLog();
        requestLog.setFilename(logPath);
        requestLog.setFilenameDateFormat("yyyy_MM_dd");
        requestLog.setRetainDays(90);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(false);
        requestLog.setLogTimeZone("GMT");
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);

        return requestLogHandler;
    }

    @Bean
    public ErrorHandler createErrorHandler() {
        ErrorPageErrorHandler errorHandler = new ErrorPageErrorHandler();
        errorHandler.addErrorPage(404, "/errorpage");
        return errorHandler;
    }

    @Bean
    public HandlerCollection handlerCollection() throws IOException {

        // Handler Structure
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        contexts.addHandler(webAppContext());
        handlers.setHandlers(new Handler[]{contexts, new DefaultHandler(), requestLogHandler()});

        return handlers;
    }

    /**
     * Jetty Server bean.
     * <p/>
     * Instantiate the Jetty server.
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server jettyServer() throws IOException {

		/* Create the server. */
        Server server = new Server(queuedThreadPool());

		/* Create a basic connector. */
        ServerConnector httpConnector = new ServerConnector(server);
        httpConnector.setPort(jettyPort);
        server.addConnector(httpConnector);

		/* Create a SSL connector */
        // ServerConnector sslConnector = new ServerConnector( server,
        // new SslConnectionFactory(sslContextFactory(), "http/1.1"),
        // new HttpConnectionFactory(httpsConfiguration()));
        // sslConnector.setPort(8443);
        // server.addConnector(sslConnector);

        server.setHandler(handlerCollection());

		/*
		 * We can add servlets or here, or we could do it in the
		 * WebAppInitializer.
		 */
        //addMetricsServlet(webAppContext());

        return server;
    }
}
