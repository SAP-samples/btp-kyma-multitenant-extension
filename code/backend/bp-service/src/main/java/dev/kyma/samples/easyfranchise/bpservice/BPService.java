package dev.kyma.samples.easyfranchise.bpservice;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.BaseRS;
import dev.kyma.samples.easyfranchise.Util;
import dev.kyma.samples.easyfranchise.communication.Connection;
import dev.kyma.samples.easyfranchise.communication.ConnectionParameter;
import dev.kyma.samples.easyfranchise.communication.DestinationUtil;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.HttpHeaders;
/**
 * Rest service for S/4HANA  connectivity
 *
 */
@Path(BPService.webContextPath)
public class BPService extends BaseRS {

    public static final String webContextPath = "/bpservice/v1/{tenantId}";
    private static final Logger logger = LoggerFactory.getLogger(BPService.class);
    
    public static final String BUSINESS_PARTNER_RESOURCE_PATH = "/sap/opu/odata/sap/API_BUSINESS_PARTNER/A_BusinessPartner";    
    public static final String BUSINESS_PARTNER_FILTER_QUERY_OPTION = "$filter=BusinessPartnerCategory%20eq%20%272%27";
    public static final String BUSINESS_PARTNER_EXPAND_QUERY_OPTION = "$expand=to_BusinessPartnerAddress/to_EmailAddress";
    public static final String BUSINESS_PARTNER_ODATA_REQUEST = BUSINESS_PARTNER_RESOURCE_PATH + "?" + BUSINESS_PARTNER_FILTER_QUERY_OPTION + "&" + BUSINESS_PARTNER_EXPAND_QUERY_OPTION;

    private static final String UNEXPECTED_ERROR = "Unexpected error in Business Partner Service";
 
    /**
     * Read BusinessPartners (with BusinessPartnerAddress and EmailAddress).
     * BusinessPartners are filtered.
     * @param tenantId
     * @param uri
     * @param resContext
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("bupa")
    public Response getBusinessPartner(@PathParam("tenantId") String tenantId, @Context UriInfo uri, @Context HttpHeaders headers, @Context ContainerRequestContext resContext) {
    	logger.info(Util.createLogDetails(resContext));
		String subdomain = getSubdomain(tenantId);   	

        //extract authorization header from EFService call
        String authorizationHeader;
        if (headers != null && headers.getHeaderString(HttpHeaders.AUTHORIZATION) != null){
            authorizationHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION).replace("Bearer","");
            logger.info("BPService: header(" + HttpHeaders.AUTHORIZATION + ")= " + authorizationHeader);
        }else{
            authorizationHeader = "";
            logger.info("BPService: No Authorization header found ");
        }
        try {
            ConnectionParameter param = DestinationUtil.getDestinationData(subdomain, Util.getS4HanaDestinationName(), authorizationHeader).setAcceptJsonHeader();
            
            // set subdomain dynamically as search term
            String searchString = BUSINESS_PARTNER_ODATA_REQUEST.replace("<cf-subdomain>", subdomain);
            param.updateUrl(param.getUrl() + searchString);
            Connection.call(param);
            if (param.status != HttpStatus.SC_OK) {
                throw new WebApplicationException("Call to S4Hana failed: " + param.content, param.status);
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
	 * Call db service to read schema name which is equal to subdomain
	 * @param tenantId
	 * @return
	 */
	private String getSubdomain(String tenantId) {
        ConnectionParameter param = new ConnectionParameter(Util.getDBServiceUrl(tenantId) + "config/subdomain");
        param.setAcceptJsonHeader();
        Connection.call(param);
        if (param.status != HttpStatus.SC_OK) {
			throw new WebApplicationException(param.content, param.status);
        }
        return param.content;
	}
}