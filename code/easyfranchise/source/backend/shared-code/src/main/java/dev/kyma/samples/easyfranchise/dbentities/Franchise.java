package dev.kyma.samples.easyfranchise.dbentities;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = Franchise.QUERY_GETALL_ENTITIES, query = "SELECT u FROM Franchise u") })
public class Franchise {
    public static final String QUERY_GETALL_ENTITIES = "Franchise.getAll";

    @Id // we take the id from s4 system.
    private String businessPartner;

    private LocalDateTime lastUpdate;

    // foreign key needs to be an instance, not standard type (Long instead of long),
    // otherwise we could not have nullable relation
    // we do not model JPA relation on this side, only in Mentor
    private Long MentorId;

    public String getBusinessPartner() {
        return businessPartner;
    }

    public void setBusinessPartner(String businessPartner) {
        this.businessPartner = businessPartner;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Long getMentorId() {
        return MentorId;
    }

    public void setMentorId(Long mentorId) {
        this.MentorId = mentorId;
    }

    @Override
    public String toString() {
        return "Franchise [BusinessPartner=" + businessPartner + ", LastUpdate=" + lastUpdate + ", MentorId=" + MentorId + "]";
    }

}
