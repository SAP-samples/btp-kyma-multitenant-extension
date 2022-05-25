package dev.kyma.samples.easyfranchise.dbentities;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = Tenant.QUERY_GETALL_ENTITIES, query = "SELECT u FROM Tenant u") })
public class Tenant {
    public static final String QUERY_GETALL_ENTITIES = "Tenant.getAll";
    @Id
    private String tenantid;
    private String schema;
    private String password;
    private String subdomain;
    private String subaccountid;

    public String getTenantid() {
        return tenantid;
    }

    public void setTenantid(String tenantid) {
        this.tenantid = tenantid;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSubdomain() {
        return subdomain;
    }
    
    public void setSubdomain(String subdomain) {
        this.subdomain = subdomain;
    }
    
    @Override
    public String toString() {
        return "Tenant [tenantid=" + tenantid + ", schema=" + schema + ", subdomain=" + subdomain + "]";
    }

    public String getSubaccountid() {
        return subaccountid;
    }

    public void setSubaccountid(String subaccountid) {
        this.subaccountid = subaccountid;
    }

}
