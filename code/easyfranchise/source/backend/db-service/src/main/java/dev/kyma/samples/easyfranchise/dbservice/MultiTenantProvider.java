package dev.kyma.samples.easyfranchise.dbservice;

import org.hibernate.engine.jdbc.connections.spi.AbstractMultiTenantConnectionProvider;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.spi.ServiceRegistryAwareService;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement Hibernate MultiTenantConnectionProvider
 * This class is highly hibernate dependent and follows hibernate documentation to implement multi-tenant functionality. 
 *
 */
public class MultiTenantProvider extends AbstractMultiTenantConnectionProvider implements ServiceRegistryAwareService {

    private static final Logger logger = LoggerFactory.getLogger(MultiTenantProvider.class);
    private static final long serialVersionUID = 1L;

    private static MultiTenantProvider instance = null;

    /**
     * Here would be good place to initiate connections, but the initializer
     * is called before we know of any tenant.
     */
    public MultiTenantProvider() {
        if (instance != null) {
            logger.warn("unexpected instance allocation - high load during tenant initialization detected");
            return;
        }
        instance = this;
    }

    private static String advanceTenant = null;

    /**
     * We need to set advance tenant for first db connection
     * @param tenant
     */
    public static void assignInitializerSchema(String tenant) {
        if (advanceTenant == null) {
            advanceTenant = tenant;
        }
    }

    /**
     * Signal error condition by resetting all fields and clear connection map.
     */
    public static void connectionError() {
        advanceTenant = null;
        if (instance == null) {
            logger.error("instance not found");
        } else {
            instance.clear();
            instance = null;
        }
        
    }

    /**
     * Clear connection map.
     */
    private void clear() {
        SynchronizedConnectionMap.clear();
        
    }

    @Override
    protected ConnectionProvider getAnyConnectionProvider() {
        if (SynchronizedConnectionMap.isEmpty()) {
            if (advanceTenant == null) {
                logger.error("no advance tenant specified - cannot get initial db connection");
            }
            selectConnectionProvider(advanceTenant);
        }
        return SynchronizedConnectionMap.any();
    }

    @Override
    protected ConnectionProvider selectConnectionProvider(String tenantIdentifier) {
        return SynchronizedConnectionMap.selectConnectionProvider(tenantIdentifier);
    }

    @Override
    public void injectServices(ServiceRegistryImplementor serviceRegistry) {
        SynchronizedConnectionMap.injectServices(serviceRegistry);
    }

}