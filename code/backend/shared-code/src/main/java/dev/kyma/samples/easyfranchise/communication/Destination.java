package dev.kyma.samples.easyfranchise.communication;

public class Destination {

    public Owner owner;
    public DestinationConfiguration destinationConfiguration = new DestinationConfiguration();
    public AuthTokens[] authTokens; 

    public String toString(){
        return "Destination: subaccountId = " + owner.SubaccountId + "InstanceId = " + owner.InstanceId + 
                " Name = " + destinationConfiguration.Name + " Type = " + destinationConfiguration.Type + 
                " URL = " + destinationConfiguration.URL + " Authentication = " + destinationConfiguration.Authentication + 
                " ProxyType = " + destinationConfiguration.ProxyType + " tokenServiceURLType = " + destinationConfiguration.tokenServiceURLType + 
                " audience = " + destinationConfiguration.audience + " Description = " + destinationConfiguration.Description + 
                " authnContextClassRef = " + destinationConfiguration.authnContextClassRef + "clientKey = " + destinationConfiguration.clientKey + 
                " nameIdFormat = " + destinationConfiguration.nameIdFormat + " tokenServiceUser = " + destinationConfiguration.tokenServiceUser + 
                " tokenServiceURL = " + destinationConfiguration.tokenServiceURL + " tokenServicePassword = " + destinationConfiguration.tokenServicePassword + 
                " User = " + destinationConfiguration.User + " Password =  <redacted>" + 
                " AuthToken.type = " + authTokens[0].type + " AuthToken.value = " + authTokens[0].value + 
                " AuthToken.expires_in = " + authTokens[0].expires_in + " AuthToken.scope = " +  authTokens[0].scope;
    }

    public boolean isHTTPDestination(){
        return destinationConfiguration.Type.equalsIgnoreCase("http");
    }

    public boolean isBasicAuthentication() {
        return destinationConfiguration.Authentication.equalsIgnoreCase("BasicAuthentication");
    }

    public boolean isNoAuthentication() {
        return destinationConfiguration.Authentication.equalsIgnoreCase("NoAuthentication");
    }

    public boolean isOAuth2SAMLBearerAssertion() {
        return destinationConfiguration.Authentication.equalsIgnoreCase("OAuth2SAMLBearerAssertion");
    }
}
