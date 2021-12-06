package dev.kyma.samples.easyfranchise;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.communication.Connection;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter.RequestMethod;
import dev.kyma.samples.easyfranchise.dbentities.Coordinator;
import dev.kyma.samples.easyfranchise.dbentities.Franchise;
import dev.kyma.samples.easyfranchise.dbentities.Mentor;
import dev.kyma.samples.easyfranchise.dbentities.Tenant;
import dev.kyma.samples.easyfranchise.s4entities.A_BusinessPartner;
import dev.kyma.samples.easyfranchise.s4entities.BusinessPartner;
import dev.kyma.samples.easyfranchise.s4entities.BusinessPartnerAddress;
import dev.kyma.samples.easyfranchise.s4entities.EmailAddress;
import dev.kyma.samples.easyfranchise.s4entities.S4Util;
import dev.kyma.samples.easyfranchise.uientities.UIFranchise;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.UriBuilder;

/**
 * Support class for EasyFranchise service
 *
 */
public class EFUtil {

    private static final Logger logger = LoggerFactory.getLogger(EFUtil.class);

    private static final String SCHEDULER_PATH = "easyfranchise/rest/efservice/v1/scheduler/on";

    /**
     * Initializations that require a running server/rest api go here.
     */
    public static void doAfterStartup() {
        if (Util.isSchedulerAutoStart()) {
            enableScheduler();
        }
    }

    /**
     * Enable scheduler by calling our own rest service 
     */
    private static void enableScheduler() {
        var server = EFServer.server;
        URI uri = server.getURI();
        if (uri == null) {
            logger.error("Cannot access server URL");
            return;
        }
        // replace path in server url with path to scheduler rest service
        uri = UriBuilder.fromUri(uri).replacePath(SCHEDULER_PATH).build();
        logger.info("Scheduler starting with URI " + uri);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        // call rest api asynchronously
        client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body).thenAccept(System.out::println).join();
    }

    /**
     * Check if there are BusinessPartners in S4 with enabled EasyFranchise access who don't have a Mentor yet and notify Coordinators via email.     * 
     */
    public static void sendMailToCoordinatorForMentorsWithoutFranchisee() {
        logger.info("Start Business Partner Sync");
        List<Tenant> tenants = null;
        try {
            tenants = getTenants();
        } catch (Exception e) {
            logger.error("Error during Business Partner Sync", e);
            return;
        }
        for (Tenant tenant : tenants) {
            try {
                logger.info("Start S4 Sync for tenant: " + tenant.getTenantid());
                List<String> newFranchiseNames = new ArrayList<String>();
                List<UIFranchise> uif = getUiFranchisees(tenant.getTenantid());
                for (UIFranchise franchise : uif) {
                    // If no mentor is assigned send mail
                    if (franchise.mentorId == null) {
                        newFranchiseNames.add(franchise.fullName);
                    }
                }
                logger.info("Number of Business Partners found without mentor: " + newFranchiseNames.size());
                if (newFranchiseNames.size() > 0) {
                    List<Coordinator> coordinators = getCoordinators(tenant.getTenantid());
                    if (coordinators.size() > 0) {
                        logger.info("Sending E-Mail to " + coordinators.size() + " coordinator(s)");
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("newFranchiseCount", newFranchiseNames.size());
                        jsonObject.put("coordinators", coordinators);
                        jsonObject.put("newFranchiseNames", newFranchiseNames);
    
                        ConnectionParameter param = new ConnectionParameter(RequestMethod.PUT, Util.getEmailServiceUrl() + "notifycoordinator");
                        param.payload = jsonObject.toString();
                        param.setAcceptJsonHeader();
                        Connection.call(param);
                        if (param.status != HttpStatus.SC_OK)
                            throw new WebApplicationException("Error while sending notification to coordinators Status: " + param.status + " Content: " + param.content + " Payload: " + param.payload, 550);
                    } else {
                        logger.warn("No coordinators registered for notifications.");
                    }
                }
                logger.info("Finished S4 Sync for tenant: " + tenant.getTenantid());
            } catch (Exception e) {
                logger.error("Error during S4 Sync for tenant: " + tenant.getTenantid(), e);
            }
        }
        logger.info("Finished Business Partner Sync");
    }

    /**
     * Read all Tenants from db service
     * @return
     */
    private static List<Tenant> getTenants() {
        ConnectionParameter param = new ConnectionParameter(Util.getDBAdminServiceUrl() + "config/tenants").setAcceptJsonHeader();
        Connection.call(param);
        if (param.status != HttpStatus.SC_OK) { 
            throw new WebApplicationException("Failed to read teanants from DB, stop sync. Status:  " + param.status + " Content: " + param.content, 550);
        }
        @SuppressWarnings("serial")
        List<Tenant> tenants = JsonbBuilder.create().fromJson(param.content, new ArrayList<Tenant>() {}.getClass().getGenericSuperclass());
        return tenants;
    }

    /**
     * To be deprecated
     * this method should be deprecated if sendMailToCoordinatorForMentorsWithoutFranchisee is no longer required (i.e. scheduler is no longer needed)
     * Read BusinessPartners from S4 and merge with Franchises from DB.
     * @param tenantId
     * @return
     */
    public static List<UIFranchise> getUiFranchisees(String tenantId) {
        A_BusinessPartner aBusinessParnter = getBusinessPartnerFromS4(tenantId, ""); // no authorization header
        List<Franchise> franchises = getFranchisesFromDB(tenantId);        
    
        return assembleFranchiseesForUI(tenantId, aBusinessParnter, franchises);
    }

    /**
     * Read BusinessPartners from S4 and merge with Franchises from DB.
     * @param tenantId
     * @param authorizationHeader -- authorization header containers current user info, which will forworaded to destination service when principal propagaion destination is used for S4 system
     * @return
     */
    public static List<UIFranchise> getUiFranchisees(String tenantId, String authorizationHeader) {
        A_BusinessPartner aBusinessParnter = getBusinessPartnerFromS4(tenantId, authorizationHeader);
        List<Franchise> franchises = getFranchisesFromDB(tenantId);        
    
        return assembleFranchiseesForUI(tenantId, aBusinessParnter, franchises);
    }

    /**
     * Assemble List of UIFranchise by merging BusinessPartner list from S4 with list from db service
     * 
     * @param tenantId
     * @param aBusinessPartner
     * @param franchisees
     * @return
     */
    private static List<UIFranchise> assembleFranchiseesForUI(String tenantId, A_BusinessPartner aBusinessPartner, List<Franchise> franchisees) {
        List<UIFranchise> uiFranchiseList = new ArrayList<>();
        for (BusinessPartner bp : aBusinessPartner.d.results) {
            // copy BusinessPartner fields to UIFranchise object
            BusinessPartnerAddress addr = null;
            if (bp.to_BusinessPartnerAddress.results.size() > 0) {
                addr = bp.to_BusinessPartnerAddress.results.get(0);
            }
            UIFranchise u = new UIFranchise();
            uiFranchiseList.add(u);
            u.businessPartner = bp.BusinessPartner;
            u.fullName = bp.BusinessPartnerFullName;
            u.mentorId = null;
            u.mentorName = null;
            u.creationDate = S4Util.getDateFromS4Date(bp.CreationDate);
            u.businessPartnerGrouping = bp.BusinessPartnerGrouping;

            if (addr != null) {
                // copy BusinessPartnerAddress fields to UIFranchise object
                u.cityCode = addr.CityCode;
                u.cityName = addr.CityName;
                u.postalCode = addr.PostalCode;
                u.streetName = addr.StreetName;
                u.houseNumber = addr.HouseNumber;

                if (addr.to_EmailAddress.results.size() > 0) {
                    // copy BusinessPartner EmailAddress field to UIFranchise object
                    EmailAddress email = addr.to_EmailAddress.results.get(0);
                    u.emailAddress = email.EmailAddress;
                }
            }
            Franchise f = findFranchise(u.businessPartner, franchisees);
            if (f != null && f.getMentorId() != null) {
                // read Mentor for Franchise from db service                
                ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "mentor/" + f.getMentorId()).setAcceptJsonHeader();
                Connection.call(param);
                if (param.status!=HttpStatus.SC_OK) {
                	throw new WebApplicationException(param.content, param.status);                	    				
    			}
                // parse Mentor from db service string result
                Mentor mentor = JsonbBuilder.create().fromJson(param.content, Mentor.class);
                // copy mentor details to UIFranchise object
                u.mentorName = mentor.getName();
                u.mentorId = f.getMentorId();
            }
        }
        return uiFranchiseList;
    }

    private static Franchise findFranchise(String businessPartnerId, List<Franchise> franchises) {
        if (businessPartnerId == null) {
            return null;
        }
        for (Franchise fr : franchises) {
            if (businessPartnerId.equals(fr.getBusinessPartner())) {
                return fr;
            }
        }
        return null;
    }

    private static A_BusinessPartner getBusinessPartnerFromS4(String tenantId, String authorizationHeader) {
        ConnectionParameter param = new ConnectionParameter(Util.getBPServiceUrl(tenantId) + "bupa");
        if(authorizationHeader != null && !authorizationHeader.isEmpty()){
            //authrizatiionHeader is not empty
            param.token = authorizationHeader;
            param.authorizationType = ConnectionParameter.AuthorizationType.BearerToken; // pass in the authorization token, but not used as no authentication is required when calling BPService    
        }else{
            param.authorizationType = ConnectionParameter.AuthorizationType.NO_AUTH;
        }
        param.setAcceptJsonHeader();
        Connection.call(param);

        if (param.status != HttpStatus.SC_OK) {
            throw new WebApplicationException("Failed to read business partner from S4, skip tenant. Status:  " + param.status + " Content: " + param.content, 550);
        }

        A_BusinessPartner businessPartners = JsonbBuilder.create().fromJson(param.content, A_BusinessPartner.class);
        logger.info("BusinessPartners found: " + businessPartners.d.results.size());
        return businessPartners;
    }
    
    private static List<Franchise> getFranchisesFromDB(String tenantId) {
        // call dbservice to read our franchise entities
        ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "franchise").setAcceptJsonHeader();
        Connection.call(param);
        
        if (param.status != HttpStatus.SC_OK)
            throw new WebApplicationException("Failed to merge business partners from S4 with DB, skip tenant. Status:  "
                    + param.status + " Content: " + param.content, 550);
        
        
        @SuppressWarnings("serial")
        List<Franchise> franchises = JsonbBuilder.create().fromJson(param.content, new ArrayList<Franchise>() {
        }.getClass().getGenericSuperclass());
        return franchises;
    }

    @SuppressWarnings("serial")
    private static List<Coordinator> getCoordinators(String tenantId) {
    
        ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "coordinator");
        param.setAcceptJsonHeader();
        Connection.call(param);                        
        
        if (param.status != HttpStatus.SC_OK)
            throw new WebApplicationException("Failed to read Coordinators from DB. Status:  "
                    + param.status + " Content: " + param.content, 550);
        
        Jsonb jsonb = JsonbBuilder.create();
        List<Coordinator> coordinators = jsonb.fromJson(param.content, new ArrayList<Coordinator>() {}.getClass().getGenericSuperclass());
        return coordinators;
    }

}
