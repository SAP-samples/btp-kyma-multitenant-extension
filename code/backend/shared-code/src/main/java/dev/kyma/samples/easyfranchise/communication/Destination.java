package dev.kyma.samples.easyfranchise.communication;

import jakarta.json.bind.annotation.JsonbNillable;
import jakarta.json.bind.annotation.JsonbProperty;

@JsonbNillable
public class Destination {
    
    @JsonbProperty("Name")
    public String name;
    
    @JsonbProperty("Type")
    public String type;
    
    @JsonbProperty("URL")
    public String url;
    
    @JsonbProperty("Authentication")
    public String authentication;
    
    @JsonbProperty("Internet")
    public String internet;
    
    @JsonbProperty("User")    
    public String user;
    
    @JsonbProperty("Password")
    public String password;
}
