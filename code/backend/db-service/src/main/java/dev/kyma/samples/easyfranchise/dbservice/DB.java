package dev.kyma.samples.easyfranchise.dbservice;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.Util;
import dev.kyma.samples.easyfranchise.communication.TenantInfo;
import dev.kyma.samples.easyfranchise.dbentities.Configuration;
import dev.kyma.samples.easyfranchise.dbentities.Coordinator;
import dev.kyma.samples.easyfranchise.dbentities.Franchise;
import dev.kyma.samples.easyfranchise.dbentities.Mentor;
import dev.kyma.samples.easyfranchise.dbentities.Tenant;
import jakarta.ws.rs.WebApplicationException;

/**
 * Handle multi-tenant calls to HANA DB.
 */
public class DB {

    private static final Logger logger = LoggerFactory.getLogger(DB.class);

    public static final String PERSISTENCE_UNIT_NAME = "efserver";

    private static EntityManagerFactory emf = null;

    /**
     * Create DB connection. Usually only the first connection goes through here.
     * Then all connections go via MutliTenantProvider.
     * If this connection does not succeed all info is deleted and reset for next connection. 
     * @param tenantId
     * @param persistenceUnit
     * @return
     */
    private static EntityManagerFactory getEntityManagerFactory(String tenantId, String persistenceUnit) {
        if (emf == null) {
            logger.info("getEntityManagerFactory");
            MultiTenantProvider.assignInitializerSchema(tenantId);  // needed for very first DB connection
            try {
                Map<String, String> props = new HashMap<String, String>();
                props.put("hibernate.connection.username", tenantId);
                props.put("hibernate.connection.password", getPasswordForSchema(tenantId));
                props.put("hibernate.connection.url", getConnectionUrl());
                logger.info("initiate EntityManagerFactory for persistence unit " + persistenceUnit);
                emf = Persistence.createEntityManagerFactory(persistenceUnit, props);
                logger.info("CONNECTED EntityManagerFactory = " + emf);
            } catch (Exception e) {
                MultiTenantProvider.connectionError();
                emf = null;
                logger.error("Could not initiate EntityManagerFactory for" + tenantId+ ":", e);
            }
        }
        return emf;
    }
    
    /**
     * Get EntityManager for specific schema
     * @param dbschema
     * @return
     */
    @SuppressWarnings("deprecation")
    private static EntityManager getEntityManager(String dbschema) {
        dbschema = dbschema.toUpperCase();
        logger.info("getEntityManager");
        emf = getEntityManagerFactory(dbschema, PERSISTENCE_UNIT_NAME);
        final SessionFactoryImplementor sessionFactory = ((org.hibernate.internal.SessionFactoryImpl) emf).getSessionFactory();
        logger.info("sess = " + sessionFactory);
        var schemaResolver = (SchemaResolver) sessionFactory.getCurrentTenantIdentifierResolver();
        logger.info("resolver = " + schemaResolver);
        schemaResolver.setTenantIdentifier(dbschema);
        EntityManager em = emf.createEntityManager();
        if (em == null) {
            logger.error("Could not initialize EntityManager");
        }
        return em;
    }

    /**
     * Read Password for schema user.
     * Reads all entries from admin-only table Tenant and returns password if schema was found.
     * @param schemaName
     * @return
     */
    static String getPasswordForSchema(String schemaName) {
        if (Util.getDBAdmin().equalsIgnoreCase(schemaName)) {
            return Util.getDBPassword();
        }
        EntityManager em = getEntityManager(Util.getDBAdmin());
        try {
            TypedQuery<Tenant> query = em.createNamedQuery(Tenant.QUERY_GETALL_ENTITIES, Tenant.class);
            List<Tenant> trainings = query.getResultList();
            for (Tenant tenant : trainings) {
                if (schemaName.equalsIgnoreCase(tenant.getSchema())) {
                    return tenant.getPassword();
                }
            }
        } catch (Exception e) {
            // if there is an exception it will occur during DB connection creation in hibernate threads
            // not much to do except for error logging  
            logger.error("error during getPasswordForSchema: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        logger.error("Cannot find password for schema " + schemaName);
        return null;
    }

    /**
     * Build JDBC connection URL.
     * Package visibility to allow access for DB classes in same package. 
     * @return
     */
    static String getConnectionUrl() {
        return "jdbc:sap://" + Util.getDBSqlEndpoint() + "/?encrypt=true";
    }

    /**
     * Get tenant instance for tenantid via admin table TENANT
     * 
     * @param tenenatId
     * @return
     */
    public static Tenant getTenantInfo(String tenantId) {
        if (tenantId == null || tenantId.length() <= 1) {
            throw new WebApplicationException("tenantId invalid: " + tenantId, 550);
        }
        EntityManager em = getEntityManager(Util.getDBAdmin());
        try {
            logger.info("got EntityManager " + em);
            
            // check if tenant already exists
            var tenant = em.find(Tenant.class, tenantId);
            if (tenant != null) {
                logger.info("tenant info: " + tenant);
                return tenant;
            }
            throw new WebApplicationException("unknown tenant: " + tenantId, 550);
        } catch (Exception e) {
            throw new WebApplicationException("Database admin exception: " + e.getMessage(), e, 550);
        } finally {
            em.close();
        }
    }
    
    /**
     * Get all tenant instances from admin table TENANT
     * 
     * @param tenenatId
     * @return
     */
    public static List<Tenant> getAllTenants() {
        EntityManager em = getEntityManager(Util.getDBAdmin());
        try {
            logger.info("got EntityManager " + em);
            
            TypedQuery<Tenant> query = em.createNamedQuery(Tenant.QUERY_GETALL_ENTITIES, Tenant.class);
            List<Tenant> tenants = query.getResultList();
            return tenants;
        } catch (Exception e) {
            logger.error("error during getTenants: " + e.getMessage(), e);
        } finally {
            em.close();
        }
        
        return null;
    }

    /**
     * Get db schema from tenantid via admin table TENANT
     * 
     * @param tenenatId
     * @return
     */
    public static String getDBSchema(String tenantId) {
        Tenant tenant = getTenantInfo(tenantId);
        return tenant != null ? tenant.getSchema() : null;
    }

    /**
     * Get subdomain from tenantid via admin table TENANT
     * 
     * @param tenenatId
     * @return
     */
    public static String getSubdomain(String tenantId) {
        Tenant tenant = getTenantInfo(tenantId);
        return tenant != null ? tenant.getSubdomain() : null;
    }

    /**
     * Onboard subaccount. Fails if this tenant id was already saved or schema/user name already used.
     * Will create new user and schema, then create all needed tables and finally create CONFIGURATION entry with default data.
     * @param tenantId
     * @param subdomain will be used as schema name
     */
    public static String onboard(String tenantId, TenantInfo tenantInfo) {
        // DB schema should be in  upper case
        String schema = tenantInfo.subdomain.toUpperCase();

        // check base DB configuration:
        String db_sqlEndoint = Util.getDBSqlEndpoint();
        String db_name = Util.getDBName();
        String db_admin = Util.getDBAdmin();
        String db_password = Util.getDBPassword();
        if (db_sqlEndoint == null || db_name == null || db_admin == null || db_password == null) {
            String msg = "DB configuration error: sqlEndpoint " + db_sqlEndoint + " name " + db_name + " admin user " + db_admin;
            if (db_password == null) {
                msg += " password null";
            }
            throw new WebApplicationException(msg, 550);
        }

        logger.warn("onboard with tenant " + tenantId + " schema "+ schema + " and subdomain " + tenantInfo.subdomain + " DB admin " + Util.getDBAdmin());
        
        // DB admin creates DB user and schema for the new tenant: 
        EntityManager emAdmin = getEntityManager(Util.getDBAdmin());
        try {
            logger.info("got EntityManager for DB admin: " + emAdmin);
            
            validateUniquenesOfTenant(emAdmin, tenantId);
            validateUniquenesOfSubdomain(emAdmin, tenantInfo.subdomain);
            validateUniquenesOfSchema(emAdmin, schema);
            
            // create new tenant
            Tenant tenant = new Tenant();
            tenant.setTenantid(tenantId);
            tenant.setSchema(schema);
            tenant.setSubdomain(tenantInfo.subdomain);
            tenant.setSubaccountid(tenantInfo.subaccountId);
            tenant.setPassword(Util.getDBPassword());
            
            // create user and schema
            emAdmin.getTransaction().begin();     
            Query q = emAdmin.createNativeQuery("CREATE USER " + schema + " PASSWORD " + tenant.getPassword() + " NO FORCE_FIRST_PASSWORD_CHANGE;");
            q.executeUpdate();
            q = emAdmin.createNativeQuery("ALTER USER " + schema + " DISABLE PASSWORD LIFETIME;");
            q.executeUpdate();

            // persist tenant
            emAdmin.merge(tenant);
            emAdmin.getTransaction().commit();
        } catch (Exception e) {
            throw new WebApplicationException("Database admin exception: " + e.getMessage(), e, 550);
        } finally {
            emAdmin.close();
        }
        
        // DB operations in new schema as new DB user 
        EntityManager emTenant = getEntityManager(schema.toUpperCase());
        try {
            logger.info("got EntityManager for new schema " + emTenant);
           
            // Create Tables for new tenant:

            // read sql file and break up into single create statements:
            String sql_all = null;
            try (InputStream inputStream = Util.class.getResourceAsStream("/create.sql")) {
                InputStreamReader isReader = new InputStreamReader(inputStream);
                BufferedReader reader = new BufferedReader(isReader);
                StringBuffer sb = new StringBuffer();
                String str;
                while((str = reader.readLine()) != null){
                   sb.append(str);
                }
                sql_all = sb.toString();
            }
            // set correct schema for all table creation commands
            sql_all = sql_all.replaceAll("__SCHEMANAME__", schema.toUpperCase());
            var sqls = sql_all.split(";");
            if (sqls == null || sqls.length < 1) {
                throw new WebApplicationException("cannot parse table creation file", 550);
            }
            emTenant.getTransaction().begin();
            for (String create_stmt : sqls) {
                // create tables:
                Query q = emTenant.createNativeQuery(create_stmt);
                q.executeUpdate();
            }
            // set predefined configuration data in config table:
            Configuration c = new Configuration();
            c.setId(1L);
            c.setFranchisor(tenantInfo.subdomain);
            c.setTenantId(tenantId);
            c.setLogoUrl("https://raw.githubusercontent.com/matthieupelatan/easy-franchise-assets/main/images/logo-placeholder.png");
            emTenant.merge(c);
            emTenant.getTransaction().commit();

            var successMsg = "new tenant tables created for " + tenantId; 
            logger.warn(successMsg);
            return successMsg;
        } catch (Exception e) {
            throw new WebApplicationException("tenant user exception: " + e.getMessage(), e, 550);
        } finally {
            emTenant.close();
        }
    }

    private static void validateUniquenesOfTenant(EntityManager emAdmin, String tenantId) {
        var tenant = emAdmin.find(Tenant.class, tenantId);
        if (tenant != null) {
            throw new WebApplicationException("tenant " + tenantId + " was already onboarded", 550);
        }
    }

    private static void validateUniquenesOfSchema(EntityManager emAdmin, String schema) {
        TypedQuery<Tenant> query = emAdmin.createQuery("SELECT t FROM Tenant t WHERE t.schema = :schema", Tenant.class);
        var tenants = query.setParameter("schema", schema).getResultList();
        if (tenants != null && tenants.size() > 0) {
            throw new WebApplicationException("schema " + schema + " is already used by a different tenant");
        }
    }
    
    private static void validateUniquenesOfSubdomain(EntityManager emAdmin, String subdomain) {
        TypedQuery<Tenant> query = emAdmin.createQuery("SELECT t FROM Tenant t WHERE t.subdomain = :subdomain", Tenant.class);
        var tenants = query.setParameter("subdomain", subdomain).getResultList();
        if (tenants != null && tenants.size() > 0) {
            throw new WebApplicationException("subdomain " + subdomain + " is already used by a different tenant");
        }
    }

    /**
     * Offboard subaccount. Delete schema, user and all data.
     * @param tenantId
     * @param subdomain will be used as schema name
     */
    public static String offboard(String tenantId) {
        String db_sqlEndpoint = Util.getDBSqlEndpoint();
        String db_name = Util.getDBName();
        String db_admin = Util.getDBAdmin();
        String db_password = Util.getDBPassword();
        if (db_sqlEndpoint == null || db_name == null || db_admin == null || db_password == null) {
            String msg = "DB configuration error: sqlEndpoint " + db_sqlEndpoint + " name " + db_name + " admin user " + db_admin;
            if (db_password == null) {
                msg += " password null";
            }
            throw new WebApplicationException(msg, 550);
        }
    
        logger.warn("offboard tenant " + tenantId + " DB admin " + Util.getDBAdmin());
        
        // The admin database user remove this tenant from tenant table and delete tenant schema: 
        EntityManager emAdmin = getEntityManager(Util.getDBAdmin());
        try {
            logger.info("got EntityManager for database admin: " + emAdmin);
            
            // check if tenant really exists
            var tenant = emAdmin.find(Tenant.class, tenantId);
            if (tenant == null) {
                throw new WebApplicationException("tenant not existing - no offboard possible for " + tenantId, 550);
            }
            
            emAdmin.getTransaction().begin();
            String subaccountAndUserAndSchemaName = tenant.getSchema();
            // delete schema:
            Query q = emAdmin.createNativeQuery("DROP USER " + subaccountAndUserAndSchemaName.toUpperCase() + " CASCADE;");
            q.executeUpdate();
            // delete tenant entry
            emAdmin.remove(tenant);
    
            emAdmin.getTransaction().commit();
        } catch (Exception e) {
            throw new WebApplicationException("Database admin exception: " + e.getMessage(), e, 550);
        } finally {
            emAdmin.close();
        }
        var successMsg = "offboarding complete for " + tenantId; 
        logger.warn(successMsg);
        return successMsg;
    }

    /**
     * Read single entity from CONFIGURATION table.
     * @param tenantId
     * @return
     */
    public static Configuration getConfig(String tenantId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        Configuration c = null;
        try {
            c = em.find(Configuration.class, 1L);
            if (c == null) {
                em.getTransaction().begin();
                c = new Configuration();
                c.setId(1L);
                em.persist(c);
                em.getTransaction().commit();
            }
        } finally {
            em.close();
        }
        return c;
    }

    /**
     * Write single entity to CONFIGURATION table.
     * @param tenantId
     * @param c
     */
    public static void setConfig(String tenantId, Configuration c) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        try {
            em.getTransaction().begin();
            c.setId(1L);
            em.merge(c);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    /**
     * Read all Franchise entities.
     * @param tenantId
     * @return
     */
    public static List<Franchise> getFranchises(String tenantId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        List<Franchise> franchises = null;
        try {
            TypedQuery<Franchise> query = em.createNamedQuery(Franchise.QUERY_GETALL_ENTITIES, Franchise.class);
            franchises = query.getResultList();
            for (Franchise f : franchises) {
                // remove all entities from EntityManager
                em.detach(f);
            }
        } finally {
            em.close();
        }
        return franchises;
    }

    
    /**
     * Create Franchise entity.
     * @param tenantId
     * @param businessPartnerId
     * @return
     */
    public static String createFranchise(String tenantId, String businessPartnerId) {
        Franchise franchise = new Franchise();
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);

        try {
            Franchise f = em.find(Franchise.class, businessPartnerId);
            if (f != null) {
                throw new WebApplicationException("franchise already exists for " + businessPartnerId, 550);
            }
            em.getTransaction().begin();
            franchise.setBusinessPartner(businessPartnerId);
            franchise.setLastUpdate(LocalDateTime.now());
            em.merge(franchise);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return "franchise created for " + businessPartnerId;        
    }
    
    /**
     * Update Franchise with mentor Id.
     * @param tenantId
     * @param mentorId
     * @param businessPartnerId
     * @return
     */
    public static String assignMentorToFranchise(String tenantId, Long mentorId, String businessPartnerId) {
        Franchise franchise = new Franchise();
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        try {
            Franchise f = em.find(Franchise.class, businessPartnerId);
            if (f == null) {
                throw new WebApplicationException("Could not find franchise " + businessPartnerId, 550);
            }
            Mentor m = em.find(Mentor.class, mentorId);
            if (m == null) {
                throw new WebApplicationException("Could not find mentor " + mentorId, 550);
            }
            em.getTransaction().begin();
            franchise.setBusinessPartner(businessPartnerId);
            franchise.setMentorId(m.getId());
            franchise.setLastUpdate(LocalDateTime.now());
            em.merge(franchise);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return "assigned mentor " + mentorId + " to businesspartner " + businessPartnerId;
    }

    /**
     * Read all Mentor entities.
     * @param tenantId
     * @return
     */
    public static List<Mentor> getMentors(String tenantId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        List<Mentor> mentors = null;
        try {
            TypedQuery<Mentor> query = em.createNamedQuery(Mentor.QUERY_GETALL_ENTITIES, Mentor.class);
            mentors = query.getResultList();
            removeCircularDependenciesMentors(mentors);
        } finally {
            em.close();
        }
        return mentors;
    }

    /**
     * Read Mentor instance with given Id.
     * @param tenantId
     * @param mid
     * @return
     */
    public static Mentor readMentor(String tenantId, Long mid) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        Mentor m = null;
        try {
            m = em.find(Mentor.class, mid);
            if (m != null) {
                removeCircularDependenciesMentor(m);
            }
        } finally {
            em.close();
        }
        return m;
    }

    /**
     * Update Mentor entity if Id is non-zero. Create new entity otherwise
     * @param tenantId
     * @param mentor
     * @return
     */
    public static Mentor createOrUpdateMentor(String tenantId, Mentor mentor) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        try {
            em.getTransaction().begin();
            
            if (mentor.getId() != 0L) {
                // check if entity already exists
                Mentor old = em.find(Mentor.class, mentor.getId());
                if (old == null) {
                    // old entity dose not exist
                    throw new WebApplicationException("Could not find mentor for update " + mentor.getId(), 550);
                }
            }
            mentor.setLastUpdate(LocalDateTime.now());
            mentor = em.merge(mentor);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return mentor;

    }

    /**
     * Delete Mentor entity.
     * @param tenantId
     * @param mentorId
     * @return
     */
    public static Mentor deleteMentor(String tenantId, Long mentorId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        Mentor m = null;
        try {
            em.getTransaction().begin();
            m = em.find(Mentor.class, mentorId);
            if (m == null) {
                throw new WebApplicationException("Cannot delete non-existing mentor " + mentorId, 550);
            }
            em.remove(m);
            em.getTransaction().commit();
            removeCircularDependenciesMentor(m);
        } finally {
            em.close();
        }
        return m;
    }

    
	/**
	 * Read all Coordinators.
	 * @param tenantId
	 * @return
	 */
	public static List<Coordinator> getCoordinators(String tenantId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
		try {
			TypedQuery<Coordinator> query = em.createNamedQuery(Coordinator.QUERY_GETALL_ENTITIES, Coordinator.class);
			List<Coordinator> coordinators = query.getResultList();
			return coordinators;
		} finally {
			em.close();
		}
	}


	/**
     * Update Coordinator entity if Id is non-zero. Create new entity otherwise
	 * @param tenantId
	 * @param coordinator
	 * @return
	 */
	public static Coordinator createOrUpdateCoordinator(String tenantId, Coordinator coordinator) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        try {
            em.getTransaction().begin();
            
            if (coordinator.getId() != 0L) {
                // check if entity already exists
            	Coordinator old = em.find(Coordinator.class, coordinator.getId());
                if (old == null) {
                    // old entity dose not exist
                    throw new WebApplicationException("Could not find coordinator for update " + coordinator.getId(), 550);
                }
            }
            coordinator.setLastUpdate(LocalDateTime.now());
            coordinator = em.merge(coordinator);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return coordinator;
	}

    /**
     * Delete Coordinator entity.
     * @param tenantId
     * @param coordId
     * @return
     */
    public static Coordinator deleteCoordinator(String tenantId, Long coordId) {
        String dbschema = DB.getDBSchema(tenantId);
        EntityManager em = getEntityManager(dbschema);
        Coordinator coordinator = null;
        try {
            em.getTransaction().begin();
            coordinator = em.find(Coordinator.class, coordId);
            if (coordinator == null) {
                throw new WebApplicationException("Cannot delete non-existing coordinator " + coordId, 550);
            }
            em.remove(coordinator);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return coordinator;
    }

    // circular dependency handling

    private static void removeCircularDependenciesMentor(Mentor m) {
        m.setFranchises(null);
    }

    private static void removeCircularDependenciesMentors(List<Mentor> mentors) {
        for (Mentor m : mentors) {
            removeCircularDependenciesMentor(m);
        }
    }

}
