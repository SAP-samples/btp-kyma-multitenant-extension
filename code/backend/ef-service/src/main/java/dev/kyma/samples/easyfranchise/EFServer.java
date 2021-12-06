package dev.kyma.samples.easyfranchise;

import java.net.URI;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class to start ef service.
 * Add call to doAfterStartup() for post server start initializations.
 */
public class EFServer {
    
    private static final Logger logger = LoggerFactory.getLogger(EFServer.class);

    public static Server server = null;
    
    public static void main(String[] args) throws Exception {
        logger.warn("Start Server using ef service app " + EFServer.class.getName());
        // Web-Context:
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/easyfranchise");

        // JAX-RS with Jersey:
        ServletHolder jerseyServlet = context.addServlet(org.glassfish.jersey.servlet.ServletContainer.class, "/rest/*");
        jerseyServlet.setInitParameter("jersey.config.server.provider.packages", "dev.kyma.samples.easyfranchise");
        jerseyServlet.setInitOrder(0);

        // Jetty-Server:
        int port = (args != null && args.length > 0) ? Integer.parseInt(args[0]) : 8080;
        Server jettyServer = new Server(port);
        server = jettyServer;
        URI s = jettyServer.getURI();
        logger.info("uri after new: " + s);
        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            logger.info("started");
            s = jettyServer.getURI();
            logger.info("server url: " + s);            
            EFUtil.doAfterStartup();
            jettyServer.join();
        } catch (Exception e) {
            logger.error("unexpected Exception: " + e.getMessage(), e);
        } finally {
            jettyServer.destroy();
        }
    }
}