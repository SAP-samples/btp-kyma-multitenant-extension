package dev.kyma.samples.easyfranchise.dbentities;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Configuration {
    @Id
    private long id;
    private String franchisor;
    private String logoUrl;
    private String tenantId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFranchisor() {
        return franchisor;
    }

    public void setFranchisor(String franchisor) {
        this.franchisor = franchisor;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {
        return "Configuration [Id=" + id + ", Franchisor=" + franchisor + ", LogoUrl=" + logoUrl + ", TenantId="
                + tenantId + "]";
    }
}
