package dev.kyma.samples.easyfranchise;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.communication.Connection;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter.RequestMethod;
import dev.kyma.samples.easyfranchise.dbentities.Coordinator;
import dev.kyma.samples.easyfranchise.dbentities.Mentor;
import dev.kyma.samples.easyfranchise.uientities.MentorNotification;
import dev.kyma.samples.easyfranchise.uientities.UIFranchise;
import dev.kyma.samples.easyfranchise.uientities.NotificationConfig;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.OPTIONS;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Rest service for EasyFranchise operations. For tenant specific calls, the
 * tenantId is expected as header parameter. All calls return a
 * jakarta.ws.rs.core.Response object with a Response.status: 200 on success. If
 * no tenandId is provided or some parameters are not correct 400 (bad request)
 * will be return. For other errors status will be 500 or 550
 */
@Path(EFService.WEB_CONTEXT_PATH)
public class EFService extends BaseRS {

    private static final Logger logger = LoggerFactory.getLogger(EFService.class);

    public static final String WEB_CONTEXT_PATH = "/efservice/v1";
    private static final String UNEXPECTED_ERROR = "Unexpected Error in EasyFranchiseService: ";

    private static ScheduledExecutorService scheduler = null;
    private static ScheduledFuture<?> schedulerHandle = null;

    /**
     * Scheduler - run task every 5 minutes
     * 
     * @param schedulerEnabled: String, "on" means start scheduler service, anything else means stop scheduler
     * @param headers
     * @param resContext
     * @return
     */
    @GET
    @Path("scheduler/{schedulerEnabled}")
    public Response setTimer(@PathParam("schedulerEnabled") String schedulerEnabled, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            boolean isScheduleEnabled = "on".equals(schedulerEnabled) ? true : false;
            logger.warn("scheduler state: " + schedulerEnabled);
            if (isScheduleEnabled) {
                if (scheduler != null) {
                    return createOkResponseSimpleText("scheduler already running");
                }
                scheduler = Executors.newScheduledThreadPool(1);
                Runnable task = () -> EFUtil.sendMailToCoordinatorForMentorsWithoutFranchisee();
                // start with 5 minutes delay and then every 5 minutes
                schedulerHandle = scheduler.scheduleAtFixedRate(task, 5, 5, TimeUnit.MINUTES);
            } else if (isScheduleEnabled == false && scheduler != null) {
                Runnable canceller = () -> {
                    schedulerHandle.cancel(false);
                    scheduler = null;
                };
                scheduler.schedule(canceller, 1, TimeUnit.SECONDS);
                logger.warn("scheduler canceller initiated");
            }
            return createOkResponseSimpleText("scheduler enabled: " + isScheduleEnabled);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * 
     * Reads all BusinessPartners from S4HANA and "merges" each of them with details persisted in the DB.
     * 
     * @param uri
     * @param headers
     * @param resContext - Response with List of Franchise entities as JSON String
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("franchisee")
    public Response getFranchisees(@Context UriInfo uri, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        
        //extract authorization header from AppRouter
        String authorizationHeader;
        if (headers != null && headers.getHeaderString(HttpHeaders.AUTHORIZATION) != null){
            authorizationHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION).replace("Bearer","");
            logger.info("EFService: header(" + HttpHeaders.AUTHORIZATION + ")= " + authorizationHeader);
        }else{
            authorizationHeader = "";
            logger.info("EF Service: No Authorization header found");
        }

        try {
            var tenantId = Util.validateTenantAccess(headers);
            List<UIFranchise> uif = EFUtil.getUiFranchisees(tenantId, authorizationHeader);
            return createOkResponse(JsonbBuilder.create().toJson(uif));
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Creates a new franchisee with the provided businesspartnerId.
     * 
     * @param businesspartnerId - the BusinessPartner id for whom to create a franchise
     * @param headers
     * @param resContext
     * @return
     */
    @PUT
    @Path("franchisee/{businesspartnerId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createFranchise(@PathParam("businesspartnerId") String businesspartnerId, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "franchise/" + businesspartnerId).setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Returns the franchisor name.
     * 
     * @param resContext
     * @param headers
     * @return
     */
    @GET
    @Path("config/franchisor")
    public Response getFranchisor(@Context ContainerRequestContext resContext, @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "config/franchisor").setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Sets the franchisor name.
     * 
     * @param franchisorName
     * @param resContext
     * @param headers
     * @return
     */
    @PUT
    @Path("config/franchisor")
    public Response setFranchisor(String franhisorName, @Context ContainerRequestContext resContext,
            @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "config/franchisor").setAcceptJsonHeader();
            param.payload = franhisorName;
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Returns the LogoUrl from CONFIGURATION table.
     * 
     * @param resContext
     * @param headers
     * @return
     */
    @GET
    @Path("config/logourl")
    public Response getLogourl(@Context ContainerRequestContext resContext, @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "config/logourl").setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.info("confirmation logourl NOT read");
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.info("confirmation logourl NOT read");
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Sets the logourl in CONFIGURATION table.
     * 
     * @param logoUrl
     * @param resContext
     * @param headers
     * @return
     */
    @PUT
    @Path("config/logourl")
    public Response setLogourl(String logoUrl, @Context ContainerRequestContext resContext, @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "config/logourl").setAcceptJsonHeader();
            param.payload = logoUrl;
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Returns the NotificationConfig from CONFIGURATION table.
     * 
     * @param resContext
     * @param headers
     * @return
     */
    @GET
    @Path("config/notificationconfig")
    public Response getNotificationConfig(@Context ContainerRequestContext resContext, @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "config/notificationconfig").setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            Jsonb jsonb = JsonbBuilder.create();
            NotificationConfig config = jsonb.fromJson(param.content, NotificationConfig.class);
            if (config.password != null && config.password.length() > 0) {
                config.password = "Password Set";
            } 
            
            return createOkResponse(jsonb.toJson(config));
        } catch (WebApplicationException e) {
            logger.info("confirmation notificationmail NOT read");
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.info("confirmation notificationmail NOT read");
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Sets the NotificationConfig in CONFIGURATION table.
     * 
     * @param logoUrl
     * @param resContext
     * @param headers
     * @return
     */
    @PUT
    @Path("config/notificationconfig")
    public Response setNotificationConfig(NotificationConfig notificationConfig, @Context ContainerRequestContext resContext, @Context HttpHeaders headers) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "config/notificationconfig").setAcceptJsonHeader();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("email", notificationConfig.email);
            jsonObject.put("password", notificationConfig.password);            
            param.payload = jsonObject.toString();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Notify mentor via email service about newly assigned Franchise.
     * 
     * @param mentorNotification
     * @param headers
     * @param resContext
     * @return
     */
    @PUT
    @Path("mentor/notify")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notifyMentor(MentorNotification mentorNotification, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
        	var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getEmailServiceUrl(tenantId) + "notifymentor").setAcceptJsonHeader();
            // build JSON according to email service requirements:
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mentorName", mentorNotification.mentor.getName());
            jsonObject.put("email", mentorNotification.mentor.getEmail());
            jsonObject.put("franchiseName", mentorNotification.franchise.fullName);
            jsonObject.put("franchiseEmail", mentorNotification.franchise.emailAddress);
            param.payload = jsonObject.toString();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Get all mentors.
     * 
     * @param headers
     * @param resContext
     * @return
     */
    @GET
    @Path("mentor")
    public Response getAllMentors(@Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            // call dbservice to read mentors
            ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "mentor").setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.info("confirmation mentor NOT read");
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Creates or updates a single mentor.
     * 
     * @param mentor
     * @param headers
     * @param resContext
     * @return
     */
    @PUT
    @Path("mentor")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateMentor(Mentor mentor, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "mentor").setAcceptJsonHeader();
            param.payload = JsonbBuilder.create().toJson(mentor);
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Delete mentor.
     * 
     * @param mentorId
     * @param headers
     * @param resContext
     * @return
     */
    @DELETE
    @Path("mentor/{mentorId}")
    public Response deleteMentor(@PathParam("mentorId") String mentorId, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.DELETE, Util.getDBServiceUrl(tenantId) + "mentor/" + mentorId).setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }

    }

    /**
     * Assigns a mentor to a franchise.
     * 
     * @param mentorId
     * @param bpId
     * @param headers
     * @param resContext
     * @return
     */
    @PUT
    @Path("franchisee/{businesspartnerId}/mentor/{mentorId}")
    public Response assignMentor(@PathParam("mentorId") String mentorId, @PathParam("businesspartnerId") String bpId, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "franchise/" + bpId + "/mentor/" + mentorId).setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Gets a coordinator.
     * 
     * @param headers
     * @param resContext
     * @return
     */
    @GET
    @Path("coordinator")
    public Response getCoordinator(@Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);

            ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "coordinator").setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            logger.info("confirmation coordinator read");
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.info("confirmation coordinator NOT read");
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.info("confirmation coordinator NOT read");
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Creates or updates a coordinator.
     * 
     * @param coordinator
     * @param headers
     * @param resContext
     * @return
     */
    @PUT
    @Path("coordinator")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createOrUpdateCoordinator(Coordinator coordinator, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);

            if (coordinator == null || coordinator.getName() == null || coordinator.getName().length() == 0) {
                throw new WebApplicationException("Cant create an empty coordinator. Please provide coordinator details", HttpStatus.SC_BAD_REQUEST);
            }
            ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getDBServiceUrl(tenantId) + "coordinator").setAcceptJsonHeader();
            param.payload = JsonbBuilder.create().toJson(coordinator);
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(JsonbBuilder.create().toJson(param.content));

        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * Delete coordinator.
     * 
     * @param id
     * @param headers
     * @param resContext
     * @return
     */
    @DELETE
    @Path("coordinator/{Id}")
    public Response deleteCoordinator(@PathParam("Id") String id, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext, headers));
        try {
            var tenantId = Util.validateTenantAccess(headers);
            ConnectionParameter param = new ConnectionParameter(RequestMethod.DELETE, Util.getDBServiceUrl(tenantId) + "coordinator/" + id).setAcceptJsonHeader();
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException(param.content, param.status);
            }
            return createOkResponse(param.content);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }

    /**
     * OPTIONS calls for local development.
     * For local develoment UI host/port and backend host/port are not the same. In that case a browser usually issues 
     * on OPTION call to check for backend clearance. If certain headers are available in the response
     * (see createOkResponseSimpleText() implementation) the browser will allow the backend call.
     */

    @OPTIONS
    @Path("franchisee/{businesspartnerId}")
    public Response setOptions01() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("config/franchisor")
    public Response setOptions02() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("config/logourl")
    public Response setOptions03() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("mentor/notify")
    public Response setOptions04() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("mentor")
    public Response setOptions05() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("mentor/{mentorId}")
    public Response setOptions06() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("franchisee/{businesspartnerId}/mentor/{mentorId}")
    public Response setOptions07() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("coordinator")
    public Response setOptions08() {
        return createOkResponseSimpleText("ok");
    }
    
    @OPTIONS
    @Path("coordinator/{Id}")
    public Response setOptions09() {
        return createOkResponseSimpleText("ok");
    }
    
}