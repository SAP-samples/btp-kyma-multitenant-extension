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
    private String notificationEmail;
    private String notificationPassword;

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

    public String getNotificationEmail() {
        return notificationEmail;
    }

    public void setNotificationEmail(String notificationEmail) {
        this.notificationEmail = notificationEmail;
    }

    public String getNotificationPassword() {
        return notificationPassword;
    }

    public void setNotificationPassword(String notificationPassword) {
        this.notificationPassword = notificationPassword;
    }

    @Override
    public String toString() {
        return "Configuration [franchisor=" + franchisor + ", id=" + id + ", logoUrl=" + logoUrl
                + ", notificationEmail=" + notificationEmail + ", notificationPassword=" + notificationPassword
                + ", tenantId=" + tenantId + "]";
    }

    
}
