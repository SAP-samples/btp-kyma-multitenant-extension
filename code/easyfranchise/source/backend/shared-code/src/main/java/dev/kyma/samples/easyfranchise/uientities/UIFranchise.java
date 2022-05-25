package dev.kyma.samples.easyfranchise.uientities;

import java.time.LocalDate;

/**
 * Compound POJO with data from BuPas and DB entities 
 *
 */
public class UIFranchise {
    // fields from s4 BusinessPartner
    public String businessPartner;
    public String fullName;
    public String businessPartnerGrouping;
    public LocalDate creationDate;
    // fields from s4 BusinessPartner -> BusinessPartnerAddress
    public String cityCode;
    public String cityName;
    public String postalCode;
    public String streetName;
    public String houseNumber;
    // fields from s4 BusinessPartner -> BusinessPartnerAddress -> EmailAddress
    public String emailAddress;
    // fields from DB (Mentor - Franchise)
    public Long mentorId;
    public String mentorName;
    
    @Override
    public String toString() {
        return "UIFranchise [businessPartner=" + businessPartner + ", fullName=" + fullName + ", postalCode="
                + postalCode + ", cityCode=" + cityCode + ", cityName=" + cityName + ", streetName=" + streetName
                + ", houseNumber=" + houseNumber + ", mentorId=" + mentorId + ", mentorName="
                + mentorName + ", creationDate=" + creationDate + ", businessPartnerGrouping=" + businessPartnerGrouping
                + ", emailAddress=" + emailAddress + "]";
    }  
}
