package dev.kyma.samples.easyfranchise;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.communication.Destination;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;

public class Util {

    private static final String X_TENANT_ID = "x-tenant-id";

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    private static final String LOCAL_CONFIG_FILE = "/hiddenconfig.properties";

    // Backend Config
    private static String BACKEND_CONFIG_PATH = "/etc/backend-config/backend.properties";
    private static String BP_SERVICE = "bp.service";
    private static String DB_SERVICE = "db.service";
    private static String DB_ADMIN_SERVICE = "db.admin.service";
    private static String EMAIL_SERVICE = "email.service";
    private static String SCHEDULER_SERVICE_AUTO_START = "scheduler.auto.start";
    private static String S4HANA_DESTINATION_PROPERTY_NAME = "s4hana.destination";

    // DB Configuration
    private static String DB_SECRET_PATH = "/etc/secrets/db-config/db.properties";
    private static String DB_NAME = "db.name";
    private static String DB_SQLENDPOINT = "db.sqlendpoint";
    private static String DB_ADMIN = "db.admin";
    private static String DB_PASSWORD = "db.password";

    // Tenant configuration (only for dev mode)
    private static String DEVMODE_TENANTID = "devmode.tenantid";

    /**
     * Read config file. If Kyma deployed take from specified path. If that fails
     * LOCAL_CONFIG_FILE is tried.
     * 
     * @param path if null always read from LOCAL_CONFIG_FILE
     * @return
     */
    private static Properties readProperties(String path) {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            if (isLocalDev()) {
                input = Util.class.getResourceAsStream(LOCAL_CONFIG_FILE);
                if (input == null)
                    throw new WebApplicationException("Could not find the resource file " + LOCAL_CONFIG_FILE
                            + ". Check that it is avlaible. User hiddenconfig-template.properties as template.");
            } else {
                if (path != null) {
                    File configFile = new File(path);
                    if (configFile.exists()) {
                        input = new FileInputStream(configFile);
                    } else {
                        throw new WebApplicationException("Could not find property file at : " + path);
                    }
                }
            }            

            // read and parse properties file
            prop.load(input);

            trimAllProperties(prop);             
            
            return prop;

        } catch (IOException ex) {
            logger.error("File access error: " + path);
            logger.error(ex.getMessage());
            logger.info(ex.getMessage(), ex);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error(e.getMessage());
                    logger.info(e.getMessage(), e);
                }
            }
        }
    }

    private static void trimAllProperties(Properties props) {
        for (Entry<Object, Object> entry : props.entrySet()) {
            if (entry.getValue()!= null) {
                entry.setValue(entry.getValue().toString().trim());
            }
        }
    }

    public static String getDBServiceUrl(String tenantId) {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        return p.getProperty(DB_SERVICE) + tenantId + "/";
    }

    public static String getBPServiceUrl(String tenantId) {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        return p.getProperty(BP_SERVICE) + tenantId + "/";
    }

    public static String getEmailServiceUrl() {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        return p.getProperty(EMAIL_SERVICE);
    }

    public static String getDBAdminServiceUrl() {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        return p.getProperty(DB_ADMIN_SERVICE);
    }

    public static String getDBSqlEndpoint() {
        Properties p = readProperties(DB_SECRET_PATH);
        return p.getProperty(DB_SQLENDPOINT);
    }

    public static String getDBName() {
        Properties p = readProperties(DB_SECRET_PATH);
        return p.getProperty(DB_NAME);
    }

    public static String getDBAdmin() {
        Properties p = readProperties(DB_SECRET_PATH);
        return p.getProperty(DB_ADMIN);
    }

    public static String getDBPassword() {
        Properties p = readProperties(DB_SECRET_PATH);
        return p.getProperty(DB_PASSWORD);
    }

    public static String getS4HanaDestinationName() {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        return p.getProperty(S4HANA_DESTINATION_PROPERTY_NAME);
    }

    private static String getLocalDevTenant() {
        Properties p = readProperties(DEVMODE_TENANTID);
        return p.getProperty(DEVMODE_TENANTID);
    }

    /**
     * Extract tenant id from header in local dev mode tenantid is read from
     * properties file.
     * 
     * @param headers
     * @return null, if no tenant header found
     */
    public static String validateTenantAccess(HttpHeaders headers) {
        if (Util.isLocalDev()) {
            String localTenant = Util.getLocalDevTenant();
            if (localTenant != null) {
                return localTenant;
            }
        }
        if (headers == null) {
            throw new WebApplicationException("header empty, but header with key " + X_TENANT_ID + "expected.",
                    HttpStatus.SC_BAD_REQUEST);
        }
        var tenantId = headers.getHeaderString(X_TENANT_ID);
        if (tenantId == null || tenantId.length() == 0) {
            throw new WebApplicationException("header with key " + X_TENANT_ID + " is missing.",
                    HttpStatus.SC_BAD_REQUEST);
        }
        if (tenantId.indexOf(",") >= 0) {
            throw new WebApplicationException(
                    "header with key " + X_TENANT_ID + " only allowed once, but more values are found: " + tenantId,
                    HttpStatus.SC_BAD_REQUEST);

        }
        return tenantId;
    }

    public static boolean isSchedulerAutoStart() {
        Properties p = readProperties(BACKEND_CONFIG_PATH);
        if ("on".equalsIgnoreCase(p.getProperty(SCHEDULER_SERVICE_AUTO_START))) {
            return true;
        }
        return false;
    }

    public static String createLogDetails(ContainerRequestContext resContext) {
        return createLogDetails(resContext, null);
    }

    public static String createLogDetails(ContainerRequestContext resContext, HttpHeaders headers) {
        String msg = resContext.getMethod() + " " + resContext.getUriInfo().getAbsolutePath();
        if (headers != null && headers.getHeaderString(X_TENANT_ID) != null)
            msg += " header(" + X_TENANT_ID + "): " + headers.getHeaderString(X_TENANT_ID);
        return msg;

    }

    public static boolean isLocalDev() {
        String localDev = System.getProperty("local_dev");
        if ("true".equals(localDev) || "TRUE".equals(localDev)) {
            return true;
        } else {
            return false;
        }
    }

    public static Destination getS4HANADestinationForLocalDev(String destinationName) {
        Properties props = readProperties(BACKEND_CONFIG_PATH);

        Destination destination = new Destination();
        destination.destinationConfiguration.URL = props.getProperty(S4HANA_DESTINATION_PROPERTY_NAME + ".URL");
        destination.destinationConfiguration.User = props.getProperty(S4HANA_DESTINATION_PROPERTY_NAME + ".User");
        destination.destinationConfiguration.Password = props.getProperty(S4HANA_DESTINATION_PROPERTY_NAME + ".Password");
        destination.destinationConfiguration.Authentication = props.getProperty(S4HANA_DESTINATION_PROPERTY_NAME + ".Authentication");
        destination.destinationConfiguration.Type = props.getProperty(S4HANA_DESTINATION_PROPERTY_NAME + ".Type");
        return destination;
    }

}
