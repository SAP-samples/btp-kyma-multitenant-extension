package dev.kyma.samples.easyfranchise;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * BaseRS provides utility methods that can be used by its subclasses
 */
public class BaseRS {


    protected static final String emptyJson = "{}";

    /**
     * This private method can be used to sanitize all text before returning a response.
     * We do not show a specific sanitizer library here, but it is useful to have one central place for sanitizing.
     * Usually all characters considered malicious are either removed or encoded.
     * 
     * @param json
     * @return
     */
    private static String sanitize(String json) {
        return json;
    }


    /**
     * Return REST service response with SERVER_ERROR return code and http status set
     * 
     * @param responseMessage - REST service response message also used in response
     *                        entity
     * @return REST service response
     */
    public static Response createErrorResponse() {
        String sanitizedJson = sanitize("Unexpected Error");
        return Response.status(Status.INTERNAL_SERVER_ERROR).header("Access-Control-Allow-Origin", "*")
                .header("Cache-control", "no-store").header("Pragma", "no-cache").entity(sanitizedJson).build();
    }

    /**
     * Return REST service response
     * 
     * @param responseMessage - REST service response message also used in response
     *                        entity
     * @param status Status code
     * @return REST service response
     */
    public static Response createErrorResponse(String responseMessageJson, Status status) {
        String sanitizedJson = sanitize(responseMessageJson);
        return Response.status(status).header("Access-Control-Allow-Origin", "*")
                .header("Cache-control", "no-store").header("Pragma", "no-cache").entity(sanitizedJson).build();
    }

    /**
     * Return FORBIDDEN REST service response
     * 
     * @return REST service response
     */
    public static Response createResponse(WebApplicationException e) {
        return Response.status(e.getResponse().getStatus()).header("Access-Control-Allow-Origin", "*")
                .header("Cache-control", "no-store").header("Pragma", "no-cache").entity(e.getMessage()).build();
    }

    
    /**
     * Return REST service response with OK return code, entity object is mapped to JSON
     * 
     */
    public static Response createOkResponse(String entityJson) {
        String sanitizedJson = sanitize(entityJson);
        return Response.status(Status.OK).header("Access-Control-Allow-Origin", "*").header("Cache-control", "no-store")
                .header("Pragma", "no-cache").entity(sanitizedJson).build();
    }


    /**
     * REST service response with OK return code, response contains the message as Json
     * 
     * @param msg - message will be represented as JSon
     * @return REST service response
     */
    public static Response createOkResponseSimpleText(String msg) {
        String sanitizedJson = sanitize(msg);
        return Response.status(Status.OK).header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization")
                .header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD")
                .header("Cache-control", "no-store").header("Pragma", "no-cache").entity(sanitizedJson).build();
    }

}
