package dev.kyma.samples.easyfranchise.dbentities;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({ @NamedQuery(name = Coordinator.QUERY_GETALL_ENTITIES, query = "SELECT c FROM Coordinator c") })

public class Coordinator {

	public static final String QUERY_GETALL_ENTITIES = "Coordinator.getAll";

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String name;
	private String email;
	private LocalDateTime lastUpdate;

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

	public LocalDateTime getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(LocalDateTime lastUpdate) {
	    this.lastUpdate = lastUpdate;
	}

	@Override
	public String toString() {
		return "Coordinator [Id=" + id + ", Name=" + name + ", Email=" + email + ", LastUpdate=" + lastUpdate + "]";
	}

}
