package dev.kyma.samples.easyfranchise.communication;

import java.net.URI;
import java.util.Map;

import com.sap.cloud.security.config.ClientCredentials;
import com.sap.cloud.security.config.Service;

public class OAuth2ServiceConfiguration implements com.sap.cloud.security.config.OAuth2ServiceConfiguration {
	private String clientId;
	private String clientSecret;
	private String url;	

	public OAuth2ServiceConfiguration(String clientId, String clientSecret, String url) {
		super();
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.url = url;		
	}

	@Override
	public String getClientId() {
		return clientId;
	}

	@Override
	public String getClientSecret() {
		return clientSecret;
	}

	@Override
	public URI getUrl() {
		return URI.create(url);
	}

	@Override
	public String getProperty(String name) {
		return null;
	}

	@Override
	public Map<String, String> getProperties() {
		return null;
	}

	@Override
	public boolean hasProperty(String name) {
		return false;
	}

	@Override
	public Service getService() {
		return Service.XSUAA;
	}

	@Override
	public boolean isLegacyMode() {
		return false;
	}
	
	public ClientCredentials getClientIdentity() {
		return new ClientCredentials(clientId, clientSecret);
	}

}
