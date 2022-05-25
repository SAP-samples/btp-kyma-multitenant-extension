package dev.kyma.samples.easyfranchise.dbentities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;

@Entity
@NamedQueries({ @NamedQuery(name = Mentor.QUERY_GETALL_ENTITIES, query = "SELECT u FROM Mentor u") })
public class Mentor {

    public static final String QUERY_GETALL_ENTITIES = "Mentor.getAll";
    
    // define primary key and use DB default key generation strategy
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String email;
    private String phone;
    private String experience;
    private int capacity;
    private LocalDateTime LastUpdate;

    // model relation to Franchise from here: Mentor.id is used as foreign key in Franchise.mentor_id
    @OneToMany
    @JoinColumn(referencedColumnName = "Id", name = "MentorId")
    private List<Franchise> franchises = new ArrayList<>();
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public LocalDateTime getLastUpdate() {
        return LastUpdate;
    }

    public void setLastUpdate(LocalDateTime lastUpdate) {
        this.LastUpdate = lastUpdate;
    }

    public List<Franchise> getFranchises() {
        return franchises;
    }

    public void setFranchises(List<Franchise> franchises) {
        this.franchises = franchises;
    }

    @Override
    public String toString() {
        return "Mentor [id=" + id + ", Name=" + name + ", Email=" + email + ", Phone=" + phone + ", Experience="
                + experience + ", Capacity=" + capacity + ", franchises=" + franchises + "]";
    }

}
