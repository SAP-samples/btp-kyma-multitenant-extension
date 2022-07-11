package dev.kyma.samples.easyfranchise.communication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.security.xsuaa.client.DefaultOAuth2TokenService;
import com.sap.cloud.security.xsuaa.client.XsuaaDefaultEndpoints;
import com.sap.cloud.security.xsuaa.tokenflows.TokenFlowException;
import com.sap.cloud.security.xsuaa.tokenflows.XsuaaTokenFlows;

import dev.kyma.samples.easyfranchise.Util;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.WebApplicationException;
/**
 * Utility methods to work with Destinations and the Destination Service.
 *
 */
public class DestinationUtil {

    // Destination Service
    private static String DESTINATION_SERVICE_INSTANCE_CLIENT_ID = "/etc/secrets/sapcp/destination/destinationserviceinstance/clientid";
    private static String DESTINATION_SERVICE_INSTANCE_CLIENT_SECRET = "/etc/secrets/sapcp/destination/destinationserviceinstance/clientsecret";
    private static String DESTINATION_SERVICE_INSTANCE_DESTINATION_URI = "/etc/secrets/sapcp/destination/destinationserviceinstance/uri";
    private static String XSUAA_SERVICE_INSTANCE_URL = "/etc/secrets/sapcp/xsuaa/xsuaaserviceinstance/url";

    private static final Logger logger = LoggerFactory.getLogger(DestinationUtil.class);

    private static String getDestinationServiceXSUAAToken(String subDomain) {
        // Read XSUAA URI and replace the subdomain
        String xsuaaUri = readFileToString(XSUAA_SERVICE_INSTANCE_URL);
        xsuaaUri = xsuaaUri.replace(
                xsuaaUri.substring(xsuaaUri.indexOf("://") + 3, xsuaaUri.indexOf(".authentication")), subDomain);
        logger.debug("xsuaaUri: ", xsuaaUri);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        DefaultOAuth2TokenService defaultOAuth2TokenService = new DefaultOAuth2TokenService(httpClient);
        OAuth2ServiceConfiguration serviceConfiguration = new OAuth2ServiceConfiguration(
                readFileToString(DESTINATION_SERVICE_INSTANCE_CLIENT_ID), readFileToString(DESTINATION_SERVICE_INSTANCE_CLIENT_SECRET), xsuaaUri);
        XsuaaDefaultEndpoints xsuaaDefaultEndpoints = new XsuaaDefaultEndpoints(serviceConfiguration);

        XsuaaTokenFlows tokenFlows = new XsuaaTokenFlows(defaultOAuth2TokenService, xsuaaDefaultEndpoints,
                serviceConfiguration.getClientIdentity());
        String jwtToken;
        try {
            jwtToken = tokenFlows.clientCredentialsTokenFlow().execute().getAccessToken();
            //logger.info("DestinationUtil clientCredentialsTokenFlow: " + jwtToken);
        } catch (IllegalArgumentException | TokenFlowException e) {
            throw new WebApplicationException("Failed to create credential token for destination service", e, 550);

        }
        return jwtToken;
    }



    private static String getDestinationServiceUserToken(String subDomain, String authorizationHeader) {
        // Read XSUAA URI and replace the subdomain
        String xsuaaUri = readFileToString(XSUAA_SERVICE_INSTANCE_URL);
        xsuaaUri = xsuaaUri.replace(
                xsuaaUri.substring(xsuaaUri.indexOf("://") + 3, xsuaaUri.indexOf(".authentication")), subDomain);
        logger.info("xsuaaUri: " + xsuaaUri);
        logger.info("subDomain:" + subDomain);
        authorizationHeader = authorizationHeader.strip();
        logger.debug("authorizationHeader: "+ authorizationHeader);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        DefaultOAuth2TokenService defaultOAuth2TokenService = new DefaultOAuth2TokenService(httpClient);
        OAuth2ServiceConfiguration serviceConfiguration = new OAuth2ServiceConfiguration(
                readFileToString(DESTINATION_SERVICE_INSTANCE_CLIENT_ID), readFileToString(DESTINATION_SERVICE_INSTANCE_CLIENT_SECRET), xsuaaUri);
        XsuaaDefaultEndpoints xsuaaDefaultEndpoints = new XsuaaDefaultEndpoints(serviceConfiguration);

        XsuaaTokenFlows tokenFlows = new XsuaaTokenFlows(defaultOAuth2TokenService, xsuaaDefaultEndpoints,
                serviceConfiguration.getClientIdentity());
        String jwtToken;
        try {
            jwtToken = tokenFlows.userTokenFlow()
                                 .token(authorizationHeader)
                                 .subdomain(subDomain)
                                 .execute()
                                 .getAccessToken();
            //logger.debug("DestinationUtil userTokenFlow: " + jwtToken);
        } catch (IllegalArgumentException | TokenFlowException e) {
            throw new WebApplicationException("Failed to create user token for destination service", e, 550);

        }
        return jwtToken;
    }

    public static ConnectionParameter getDestinationData(String subDomain, String destinationName,
            String authorizationHeader) throws Exception {
        Destination destination = null;
        if (Util.isLocalDev()) {
            destination = Util.getS4HANADestinationForLocalDev(destinationName);
        } else {
            destination = getDestinationfromDestinationService(subDomain, destinationName, authorizationHeader);
        }

        if (destination == null) {
            throw new WebApplicationException("Could not read destinations details destination name=" + destinationName
                    + ". Please check your destination configuration!", 550);
        }

        if (!destination.isHTTPDestination()) {
            throw new WebApplicationException("Destination name=" + destinationName + "is not of type HTTP", 550);
        }

        if (destination.isNoAuthentication()) {
            ConnectionParameter p = new ConnectionParameter(destination.destinationConfiguration.URL);
            return p;
        } else if (destination.isBasicAuthentication()) {
            logger.info("getting connection Parameter  destination name=\"" + destinationName
                    + "\". (with type=BasicAuthentication");
            ConnectionParameter p = new ConnectionParameter(destination.destinationConfiguration.URL);

            p.user = destination.destinationConfiguration.User;
            p.pass = destination.destinationConfiguration.Password;
            p.authorizationType = ConnectionParameter.AuthorizationType.Basic;

            return p;
        } else if (destination.isOAuth2SAMLBearerAssertion()) {
            // use principal propagation destination
            logger.info("getting connection Parameter  destination name=\"" + destinationName
                    + "\". ( principal propagation with type=OAuth2SAMLBearerAssertion");

            ConnectionParameter p = new ConnectionParameter(destination.destinationConfiguration.URL);

            // authorization Header is only required if destination is using principal
            // propagation
            p.authorizationType = ConnectionParameter.AuthorizationType.BearerToken;
            p.token = destination.authTokens[0].value;

            return p;

        } else {
            throw new WebApplicationException(
                    "Unsupported Destination, only BasicAuthentication, OAuth2SAMLBearerAssertion or NoAuthentication allowed for destination name="
                            + destinationName + ". But found " + destination.destinationConfiguration.Authentication,
                    550);
        }
    }

    private static Destination getDestinationfromDestinationService(String subDomain, String destinationName, String authorizationHeader)
            throws TokenFlowException, Exception {

        // call Destination service
    	// here you can try out the  destination API: https://api.sap.com/api/SAP_CP_CF_Connectivity_Destination/tryout
        // "Find a destination" API will automatically return the token for SAP S/4HANA Cloud 
        ConnectionParameter param = new ConnectionParameter(readFileToString(DESTINATION_SERVICE_INSTANCE_DESTINATION_URI)
                + "/destination-configuration/v1/destinations/" + destinationName);

        if(authorizationHeader == null || authorizationHeader.isEmpty()){
            // no authorization header, create a credentialtoken without userinfo, 
            // in which case Destination with pricipal propagation (OAuth2SAMLBearerAssertion) is not possible, only Basic authentication works

            param.token = getDestinationServiceXSUAAToken(subDomain);

        }else {
            // using pricipal propagation,  exchange user token from EFservice (set by Approuter) to user token using destination credentials
            param.token = getDestinationServiceUserToken(subDomain, authorizationHeader);
        }
        param.authorizationType = ConnectionParameter.AuthorizationType.BearerToken;
        param.setAcceptJsonHeader();

        Connection.call(param);
        if (param.status != 200) {
            if (param.status == HttpStatus.SC_NOT_FOUND)
                throw new WebApplicationException("Failed to get destination " + destinationName, 550);
            else
                throw new WebApplicationException(
                        "Failed to get destination " + destinationName + " with status: " + param.status, 550);
        }

        //logger.info("getDestinationfromDestinationService: " + param.content);

        Destination destination = JsonbBuilder.create().fromJson(param.content, Destination.class);
        logger.debug(destination.toString());
        return destination;
    }

    private static String readFileToString(String filePath) {
        Path fileName = Path.of(filePath);
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
