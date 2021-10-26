package dev.kyma.samples.easyfranchise.communication;


public class ConnectionParameter {


    public ConnectionParameter(RequestMethod requestMethod, String url) {
        this.url = url;
        this.requestMethod=requestMethod;
    }

    public ConnectionParameter(String url) {
        this(RequestMethod.GET, url);
         
    }
    
    public enum AuthorizationType {
        NO_AUTH, Basic, BearerToken
    };
    
    public enum RequestMethod{
        GET, POST, PUT, DELETE
    }

    private  RequestMethod requestMethod;
    private String url = null;
    
    public AuthorizationType authorizationType = AuthorizationType.NO_AUTH;
    

    public String payload = null;
    
    public String user;
    public String pass;
    private boolean acceptJsonHeader = false;
    public String token;
    // if we get an error during reading the response we will also try to read the error stream
    // if we then actually get content from error stream this is signaled here with true and can be evaluated by caller.  
    public boolean contentSetByExceptionHandling = false;

    // call result:
    //TDDO? rename contend to resultcontend + resultSatus?
    public String content = null;
    public int status = 0;
    @Override
    public String toString() {
        return "ConnectionParameter [url=" + getUrl() + ", authorizationType=" + authorizationType + ", requestMethod =" + requestMethod.toString()+", payload=" + payload + ", user=" + user + ", pass=" + pass + ", acceptJsonHeader=" + isAcceptJsonHeader() + ", token=" + token
                + "]";
    }
    
    public String getUrl() {
        return url;
    }
    

    public RequestMethod getRequestMethod() {
        return requestMethod;
    }
    
    public ConnectionParameter updateUrl(String url) {
        this.url = url;
        return this;
    }

    public boolean isAcceptJsonHeader() {
        return acceptJsonHeader;
    }

    public ConnectionParameter setAcceptJsonHeader() {       
        this.acceptJsonHeader = true;
        return this; 
    }


}
