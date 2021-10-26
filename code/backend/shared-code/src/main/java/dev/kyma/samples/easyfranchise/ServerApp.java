package dev.kyma.samples.easyfranchise;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerApp {
    private static final Logger logger = LoggerFactory.getLogger(ServerApp.class);
    
    public static void main(String[] args) throws Exception {
        logger.warn("Start Server using class" + ServerApp.class.getName());
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
        jettyServer.setHandler(context);

        try {
            jettyServer.start();
            jettyServer.join();
        } finally {
            jettyServer.destroy();
        }
    }
}