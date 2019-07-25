package hello;

import javax.servlet.Filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;

/**
 * @author raphael
 * This filter definition enables tracing all incoming HTTP requests.
 * The X-Ray SDK for Java creates a segment for each sampled request. 
 * This segment includes timing, method, and disposition of the HTTP request.
 */
@Configuration
public class XRayConfig {
	
	@Autowired
	private Environment env;
	
	@Bean
	public Filter TracingFilter() {
		String contextPathAsServiceName = env.getProperty("server.servlet.contextPath");
		contextPathAsServiceName = contextPathAsServiceName.replace("/", "");
		return new AWSXRayServletFilter(contextPathAsServiceName);
	}
}
