package dev.kyma.samples.easyfranchise.dbservice;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.kyma.samples.easyfranchise.BaseRS;
import dev.kyma.samples.easyfranchise.Util;
import dev.kyma.samples.easyfranchise.dbentities.Tenant;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;

/**
 * Rest service for DB ADMIN operations.
 * serve this kind of URL: /dbadminservice/v1/config/tenants
 * path part /efbackend/rest comes from ServerApp
 */
@Path(DBAdminService.WEB_CONTEXT_PATH)
public class DBAdminService extends BaseRS {

    public static final String WEB_CONTEXT_PATH = "/dbadminservice/v1/";

    private static final Logger logger = LoggerFactory.getLogger(DBAdminService.class);

    private static final String UNEXPECTED_ERROR = "Unexpected Error: ";

    /**
     * Read all entries from TENANT table. This table is only available in database admin schema, not in other DB tenants
     * @param tenantId
     * @param resContext
     * @return
     */
    @GET
    @Path("config/tenants")
    public Response getTenants(@Context ContainerRequestContext resContext) {
        logger.info(Util.createLogDetails(resContext));
        try {
            List<Tenant> allTenants = DB.getAllTenants();
            Jsonb jsonb = JsonbBuilder.create();
            String jsonMsg = jsonb.toJson(allTenants);
            return createOkResponse(jsonMsg);
        } catch (WebApplicationException e) {
            logger.error(e.getMessage(), e);
            return createResponse(e);
        } catch (Exception e) {
            logger.error(UNEXPECTED_ERROR + e.getMessage(), e);
            return createErrorResponse();
        }
    }
}
