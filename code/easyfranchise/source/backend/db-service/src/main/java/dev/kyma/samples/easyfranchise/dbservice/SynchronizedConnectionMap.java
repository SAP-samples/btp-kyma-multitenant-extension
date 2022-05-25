package dev.kyma.samples.easyfranchise.dbservice;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Map Connections to Tenant String. Singleton class to collaborate closely with MultiTenantProvider
 * All public methods are synchronized and forward work to private instance methods 
 * Called from hibernate Threads - not much to do on errors except logging
 *
 */
public class SynchronizedConnectionMap {
    
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedConnectionMap.class);

    private static SynchronizedConnectionMap instance = null;
    private Map<String, C3P0ConnectionProvider> connectionProviderMap = new HashMap<>();
    private Map<String, String> originalSettings;
    private ServiceRegistryImplementor serviceRegistry;

    
    /**
     * Create singleton instance.
     * @return
     */
    private static synchronized SynchronizedConnectionMap getInstance() {
        if (instance == null) {
            instance = new SynchronizedConnectionMap();
            logger.info("new instance created: " + instance);
        }
        return instance;
    }

    // public synchronized methods:
    
    /**
     * Clear the connection map.
     */
    public static synchronized void clear() {
        getInstance().clearInstance();
    }

    /**
     * Check if connection map is empty.
     * @return
     */
    public static synchronized boolean isEmpty() {
        return getInstance().isEmptyInstance();
    }

    /**
     * Provide any connection.
     * Will always return the connection belonging to the default schema of the database administrator user, because this is the first to be created.
     * @return
     */
    public static synchronized ConnectionProvider any() {
        return getInstance().anyInstance();
    }

    /**
     * get the ConnectionProvider for given schema / tenant.
     * If this is the first call for a tenant a new ConnectionProvider will be created on-the-fly.
     * @param tenantIdentifier will be made upper case before used
     * @return
     */
    public static synchronized ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return getInstance().selectConnectionProviderInstance(tenantIdentifier);
    }

    /**
     * Link hibernate ServiceRegsitry with ConnectionProvider.
     * If the link was already done nothing happens.
     * @param serviceRegistry
     */
    public static synchronized void injectServices(ServiceRegistryImplementor serviceRegistry) {
        getInstance().injectServicesInstance(serviceRegistry);
    }
    
    // private instance methods:

    private void clearInstance() {
        connectionProviderMap.clear();
    }

    private boolean isEmptyInstance() {
        return connectionProviderMap.isEmpty();
    }

    private ConnectionProvider anyInstance() {
        if (connectionProviderMap.isEmpty()) {
            logger.error("Unexpected Error: no DB connection available");
            return null;
        }
        logger.info("returning unspecified ('any') connection.");
        return connectionProviderMap.values().iterator().next();
    }

    private ConnectionProvider selectConnectionProviderInstance(String tenantIdentifier) {
        if (connectionProviderMap.get(tenantIdentifier) == null) {
            // we have to initialize a new tenant on-the-fly:
            initConnectionProviderForTenant(tenantIdentifier);
        }
        return connectionProviderMap.get(tenantIdentifier);
    }

    private void initConnectionProviderForTenant(String tenantId) {
        tenantId = tenantId.toUpperCase();
        logger.warn("initiate connection for tenant " + tenantId);
        if (originalSettings == null) {
            logger.error("configuration not found - cannot create ConnectionProvider");
        }
        Map<String, String> props = new HashMap<>(originalSettings);
        props.put("hibernate.connection.username", tenantId);
        props.put("hibernate.connection.password", DB.getPasswordForSchema(tenantId));
        props.put("hibernate.connection.url", DB.getConnectionUrl());

        try {
            C3P0ConnectionProvider connectionProvider = new C3P0ConnectionProvider();
            if (serviceRegistry == null) {
                logger.error("uninitialized service registry - cannot create ConnectionProvider");
            }
            connectionProvider.injectServices(serviceRegistry);
            connectionProvider.configure(props);
            connectionProviderMap.put(tenantId, connectionProvider);
            logger.info("added tenant connection: " + tenantId + " total connections: " + connectionProviderMap.size() + " instance: " + this);
        } catch (Exception e) {
            logger.error("error initializing tenant " + tenantId, e);
        }
    }

    @SuppressWarnings("unchecked")
    private void injectServicesInstance(ServiceRegistryImplementor serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
        originalSettings = serviceRegistry.getService(ConfigurationService.class).getSettings();
        logger.debug(originalSettings.toString());
        String tenant = originalSettings.get("hibernate.connection.username").toUpperCase();
        if (connectionProviderMap.get(tenant) != null) {
            // already mapped - nothing more to do
            logger.warn("inject service was already done for tenant " + tenant + " connections mapped: " + connectionProviderMap.size());
            return;
        }
        logger.warn("inject service for tenant " + tenant);
        C3P0ConnectionProvider connectionProvider = new C3P0ConnectionProvider();
        connectionProvider.injectServices(serviceRegistry);
        connectionProvider.configure(originalSettings);
        connectionProviderMap.put(tenant, connectionProvider);
        logger.warn("inject service for tenant " + tenant + " connections mapped: " + connectionProviderMap.size());
    }

}
