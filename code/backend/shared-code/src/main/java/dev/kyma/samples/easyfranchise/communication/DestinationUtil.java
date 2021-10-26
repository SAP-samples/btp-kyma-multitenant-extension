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
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.WebApplicationException;

/**
 * Utility methods to work with Destinations and the Destination Service.
 *
 */
public class DestinationUtil {

    // Destination Service
    private static String DESTINATION_CLIENT_ID = "/etc/secrets/sapcp/destination/destinationserviceinstance/clientid";
    private static String DESTINATION_CLIENT_SECRET = "/etc/secrets/sapcp/destination/destinationserviceinstance/clientsecret";
    private static String DESTINATION_URI = "/etc/secrets/sapcp/destination/destinationserviceinstance/uri";
    private static String XSUAA_URI = "/etc/secrets/sapcp/xsuaa/xsuaaserviceinstance/url";

    private static final Logger logger = LoggerFactory.getLogger(DestinationUtil.class);

    private static String getDestinationServiceXSUAAToken(String subDomain) {
        // Read XSUAA URI and replace the subdomain
        String xsuaaUri = readFileToString(XSUAA_URI);
        xsuaaUri = xsuaaUri.replace(
                xsuaaUri.substring(xsuaaUri.indexOf("://") + 3, xsuaaUri.indexOf(".authentication")), subDomain);
        logger.debug("xsuaaUri: ", xsuaaUri);

        CloseableHttpClient httpClient = HttpClients.createDefault();
        DefaultOAuth2TokenService defaultOAuth2TokenService = new DefaultOAuth2TokenService(httpClient);
        OAuth2ServiceConfiguration serviceConfiguration = new OAuth2ServiceConfiguration(
                readFileToString(DESTINATION_CLIENT_ID), readFileToString(DESTINATION_CLIENT_SECRET), xsuaaUri);
        XsuaaDefaultEndpoints xsuaaDefaultEndpoints = new XsuaaDefaultEndpoints(serviceConfiguration);

        XsuaaTokenFlows tokenFlows = new XsuaaTokenFlows(defaultOAuth2TokenService, xsuaaDefaultEndpoints,
                serviceConfiguration.getClientIdentity());
        String jwtToken;
        try {
            jwtToken = tokenFlows.clientCredentialsTokenFlow().execute().getAccessToken();
        } catch (IllegalArgumentException | TokenFlowException e) {
            throw new WebApplicationException("Failed to create xsuaa token for destination service", e, 550);

        }
        return jwtToken;
    }

    public static ConnectionParameter getDestinationData(String subDomain, String destinationName) throws Exception {
        Destination destination = null;
        if (Util.isLocalDev()) {
            destination = Util.getS4HANADestinationForLocalDev(destinationName);
        } else {
            destination = getDestinationfromDestinationService(subDomain, destinationName);
        }

        if (destination.type != null && destination.type.equalsIgnoreCase("http") && destination.authentication != null
                && (destination.authentication.equalsIgnoreCase("BasicAuthentication")
                        || destination.authentication.equalsIgnoreCase("NoAuthentication"))) {
            ConnectionParameter p = new ConnectionParameter(destination.url);

            if (destination.authentication.equalsIgnoreCase("BasicAuthentication")) {
                p.user = destination.user;
                p.pass = destination.password;
                p.authorizationType = ConnectionParameter.AuthorizationType.Basic;
            }
            return p;
        } else {
            throw new WebApplicationException(
                    "Unsupported Destination, only tpye=http and authentication=Basic Authentication allowed for destination name="
                            + destinationName + ". But found type=" + destination.type + " and authentication= "
                            + destination.authentication,
                    550);
        }
    }

    private static Destination getDestinationfromDestinationService(String subDomain, String destinationName)
            throws TokenFlowException, Exception {

        ConnectionParameter param = new ConnectionParameter(readFileToString(DESTINATION_URI)
                + "/destination-configuration/v1/subaccountDestinations/" + destinationName);

        param.token = getDestinationServiceXSUAAToken(subDomain);
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

        Jsonb jsonb = JsonbBuilder.create();
        Destination destination = jsonb.fromJson(param.content, Destination.class);
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
