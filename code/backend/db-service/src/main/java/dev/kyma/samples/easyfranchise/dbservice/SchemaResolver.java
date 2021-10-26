package dev.kyma.samples.easyfranchise.dbservice;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Implement Hibernate CurrentTenantIdentifierResolver
 * This class is highly hibernate dependent and follows hibernate documentation to implement multi-tenant functionality. 
 *
 */
public class SchemaResolver implements CurrentTenantIdentifierResolver {
    
    // default tenant identifier
    private String tenantIdentifier = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        return tenantIdentifier;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    /**
     * Set the current schema identifier
     * @param tenantIdentifier
     */
    public void setTenantIdentifier(String tenantIdentifier) {
        this.tenantIdentifier = tenantIdentifier;
    }
}
