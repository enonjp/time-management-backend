package co.jp.enon.tms.common.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "tms.app")
public class TimeManagementAppConfig {

	private String jwtSecret;
    private String jwtExpirationMs;
    private String frontendResetPasswordUrl;

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}
	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtExpirationMs(String jwtExpirationMs) {
		this.jwtSecret = jwtExpirationMs;
	}
	public String getJwtExpirationMs() {
		return jwtExpirationMs;
	}
	public void setFrontendResetPasswordUrl(String frontendResetPasswordUrl) {
		this.frontendResetPasswordUrl = frontendResetPasswordUrl;
	}
	public String getFrontendResetPasswordUrl() {
		return frontendResetPasswordUrl;
	}
}
