package dev.kyma.samples.easyfranchise.dbservice;

import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.BaseRS;
import dev.kyma.samples.easyfranchise.Util;
import dev.kyma.samples.easyfranchise.communication.TenantInfo;
import dev.kyma.samples.easyfranchise.dbentities.Configuration;
import dev.kyma.samples.easyfranchise.dbentities.Coordinator;
import dev.kyma.samples.easyfranchise.dbentities.Franchise;
import dev.kyma.samples.easyfranchise.dbentities.Mentor;
import dev.kyma.samples.easyfranchise.uientities.NotificationConfig;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Rest service for DB operations.
 * serve this kind of URL: /dbservice/v1/3bc4e8e7-ea5a-44a6-b78f-4b7b4f053fd4/config/logourl
 * For every rest call tenantid is specified as path parameter.
 * All calls return a jakarta.ws.rs.core.Response object with a Response.status: 200 on success and status 500 or 550 on failure
 * path part /efbackend/rest comes from ServerApp
 */
@Path(DBService.WEB_CONTEXT_PATH)
public class DBService extends BaseRS {

    private static final Logger logger = LoggerFactory.getLogger(DBService.class);

    public static final String WEB_CONTEXT_PATH = "/dbservice/v1/{tenantId}";

    private static final String UNEXPECTED_ERROR = "Unexpected error in dbservice";

    /**
     * Onboard new subaccount. Called after user subscribes the EasyFranchise service via cockpit.
     * Subdomain name will be used as DB schema name (with upper case letters) 
     * @param tenantId
     * @param tenentInfo 
     * @param resContext
     * @return
     */
    @PUT
    @Path("onboard")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response onboard(@PathParam("tenantId") String tenantId, TenantInfo tenantInfo, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        
        if (tenantInfo == null || tenantInfo.subdomain == null || tenantInfo.subdomain.length() == 0 || tenantInfo.subaccountId == null || tenantInfo.subaccountId.length() == 0) {
            throw new WebApplicationException("Cant create tenant. Please provide tenant details", HttpStatus.SC_BAD_REQUEST);
        }
        
        try {
            var msg = DB.onboard(tenantId, tenantInfo);
            return createOkResponse(msg);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Offboard subaccount. Called after user unsubscribes the EasyFranchise service via cockpit.
     * All DB data including schema and schema user will be immediately deleted.
     * @param tenantId
     * @param resContext
     * @return 
     */
    @PUT
    @Path("offboard")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response offboard(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            var msg = DB.offboard(tenantId);
            return createOkResponse(msg);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read FRANCHISE table and return all entries as JSON String.
     * @param tenantId
     * @param resContext
     * @return Response with List of Franchise entities as JSON String
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("franchise")
    public Response getFranchises(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Jsonb jsonb = JsonbBuilder.create();
            List<Franchise> franchises = DB.getFranchises(tenantId);
            String jsonMsg = jsonb.toJson(franchises);
            return createOkResponse(jsonMsg);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * Create FRANCHISE table entry
     * @param tenantId
     * @param businesspartnerId
     * @param resContext
     * @return
     */
    @PUT
    @Path("franchise/{businesspartnerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFranchise(@PathParam("tenantId") String tenantId, @PathParam("businesspartnerId") String businesspartnerId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String ret = DB.createFranchise(tenantId, businesspartnerId);
            return createOkResponse(ret);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Assign Mentor to Franchise by setting MENTORID field in FRANCHISE table entry.
     * @param tenantId
     * @param mentorId
     * @param bpId
     * @param resContext
     * @return
     */
    @PUT
    @Path("franchise/{businesspartnerId}/mentor/{mentorId}")
    public Response assignOrUpdateMentortoFranchise(@PathParam("tenantId") String tenantId, @PathParam("mentorId") Long mentorId, @PathParam("businesspartnerId") String bpId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String ret = DB.assignMentorToFranchise(tenantId, mentorId, bpId);
            return createOkResponse(ret);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Update MENTOR table entry. Create new entry if a zero Id is given.
     * For Id != 0 the old entry is updated.
     * @param tenantId
     * @param m
     * @param resContext
     * @return
     */
    @PUT
    @Path("mentor")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateMentor(@PathParam("tenantId") String tenantId, Mentor m, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            m = DB.createOrUpdateMentor(tenantId, m);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(m));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read MENTOR table entry.
     * @param tenantId
     * @param mid
     * @param resContext
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("mentor/{mentorId}")
    public Response readMentor(@PathParam("tenantId") String tenantId, @PathParam("mentorId") Long mid, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        Jsonb jsonb = JsonbBuilder.create();
        String jsonMsg = "";
        try {
            Mentor m = DB.readMentor(tenantId, mid);
            if (m != null) {
                jsonMsg = jsonb.toJson(m);
                return createOkResponse(jsonMsg);
            } else {
                throw new WebApplicationException("Could not read mentor " + mid, 550);
            }
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read all entries in MENTOR table.
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("mentor")
    public Response getMentors(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            List<Mentor> mentors = DB.getMentors(tenantId);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(mentors));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Delete entry in MENTOR table with given Id.
     * @param tenantId
     * @param mentorId
     * @param resContext
     * @return
     */
    @DELETE
    @Path("mentor/{mentorId}")
    public Response deleteMentor(@PathParam("tenantId") String tenantId, @PathParam("mentorId") Long mentorId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Mentor mentor = DB.deleteMentor(tenantId, mentorId);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(mentor));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read all COORDINATOR table entries. 
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("coordinator")
    public Response getCoordinators(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            List<Coordinator> coordinators = DB.getCoordinators(tenantId);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(coordinators));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    
    /**
     * Update COORDINATOR table entry. Create new entry if a zero Id is given.
     * For Id != 0 the old entry is updated.
     * @param tenantId
     * @param coordinator
     * @param resContext
     * @return
     */
    @PUT
    @Path("coordinator")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateCoordinator(@PathParam("tenantId") String tenantId, Coordinator coordinator, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            coordinator = DB.createOrUpdateCoordinator(tenantId, coordinator);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(coordinator));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
	/**
     * Delete entry in COORDINATOR table with given Id.
	 * @param tenantId
	 * @param coordinatorId
	 * @param resContext
	 * @return
	 */
	@DELETE
	@Path("coordinator/{Id}")
	public Response deleteCoordinator(@PathParam("tenantId") String tenantId, @PathParam("Id") Long coordinatorId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Coordinator mentor = DB.deleteCoordinator(tenantId, coordinatorId);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(mentor));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
	}

    /**
     * Read franchisor from CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/franchisor")
    public Response getFranchisor(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String franchisor = "";
            Configuration conf = DB.getConfig(tenantId);
            if (conf != null && conf.getFranchisor() != null) {
                franchisor = conf.getFranchisor();
            }
            return createOkResponse(franchisor);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Write franchisor to CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @PUT
    @Path("config/franchisor")
    public Response setFranchisor(@PathParam("tenantId") String tenantId, String fran, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Configuration conf = DB.getConfig(tenantId);
            conf.setFranchisor(fran);
            DB.setConfig(tenantId, conf);
            return createOkResponse(fran);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read logourl from CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/logourl")
    public Response getLogourl(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String logourl = "";
            Configuration conf = DB.getConfig(tenantId);
            if (conf != null && conf.getLogoUrl() != null) {
                logourl = conf.getLogoUrl();
            }
            logger.info("confirmation logourl: " + logourl);
            return createOkResponse(logourl);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Write logourl to CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @PUT
    @Path("config/logourl")
    public Response setLogourl(@PathParam("tenantId") String tenantId, String l, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Configuration conf = DB.getConfig(tenantId);
            conf.setLogoUrl(l);
            logger.info("" + conf.toString());
            DB.setConfig(tenantId, conf);
            return createOkResponse(l);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read logourl from CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/notificationconfig")
    public Response getNotificationMail(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Configuration conf = DB.getConfig(tenantId);
            NotificationConfig config = new NotificationConfig();
            if (conf != null && conf.getNotificationEmail() != null) {
                config.email = conf.getNotificationEmail();
            }
            if (conf != null && conf.getNotificationPassword() != null) {
                config.password = conf.getNotificationPassword();
            }

            logger.info("confirmation notificationMail: " + config.email);
            logger.info("confirmation notificationPassword: " + config.password);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(config));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Write logourl to CONFIGURATION table. This table only contains a single element.
     * @param tenantId
     * @param resContext
     * @return
     */
    @PUT
    @Path("config/notificationconfig")
    public Response setLogourl(@PathParam("tenantId") String tenantId, NotificationConfig notificationConfig, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            Configuration conf = DB.getConfig(tenantId);
            conf.setNotificationEmail(notificationConfig.email);
            conf.setNotificationPassword(notificationConfig.password);
            logger.info("" + conf.toString());
            DB.setConfig(tenantId, conf);
            Jsonb jsonb = JsonbBuilder.create();
            return createOkResponse(jsonb.toJson(notificationConfig));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }
    
    /**
     * Read DB schema name for given tenantid from TENANT table. This table is only available in administration database user schema. 
     * The Table TENANT don't exist in the tenant database schemas.  
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/schema")
    public Response getSchemaId(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String dbschema = DB.getDBSchema(tenantId);
            if (dbschema == null) {
                throw new WebApplicationException("Cannot find db schema for tenant " + tenantId, 550);
            }
            return createOkResponse(dbschema);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Read subdomain name for given tenantid from TENANT table. This table is only available in database admin schema, not in other DB tenants
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/subdomain")
    public Response getSubdomain(@PathParam("tenantId") String tenantId, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            String subdomain = DB.getSubdomain(tenantId);
            if (subdomain == null) {
                throw new WebApplicationException("Cannot find db subdomain for tenant " + tenantId, 550);
            }
            return createOkResponse(subdomain);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

}
