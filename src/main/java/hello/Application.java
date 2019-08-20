package hello;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.AWSXRayRecorderBuilder;
import com.amazonaws.xray.plugins.ECSPlugin;
import com.amazonaws.xray.strategy.sampling.LocalizedSamplingStrategy;

@SpringBootApplication
public class Application {
    
    /**
	 * Entrypoint
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

    
	/**
	 * Event gets fired as soon as the Spring environment is loaded
     * http://blog.netgloo.com/2014/11/13/run-code-at-spring-boot-startup/
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void initXRayAfterStartup() {
		initializeXRay();
	}
	

	/*
	 * Initializes the AWS X-Ray SDK and prevents to track the health check calls at /<service>/actuator/health endpoint
	 * 
	 */
	private void initializeXRay() {
		try {
			AWSXRayRecorderBuilder builder = AWSXRayRecorderBuilder.standard().withPlugin(new ECSPlugin());
		
			// create the content of the filter in JSON format
			// set /*/actuator/health as endpoint to ignore in tracing
			final String configString = "{ " + "  \"rules\": [ { \"description\": \"Health\", " + "\"service_name\": \"*\", "
					+ "\"http_method\": \"*\", " + "\"url_path\": \"*/actuator/health\", " + "\"fixed_target\": 0, "
					+ "\"rate\": 0.00 } ],\n" + "  \"default\": " + "{ \"fixed_target\": 1," + " \"rate\": 0.1 },\n"
					+ "  \"version\": 1\n" + " }";
			
			// create a temp file at OS temp directory.
			File temp = File.createTempFile("tempfile", ".tmp");
			// write it to the file. In container environments, after a restart this file is gone (immutable environment)
			try (BufferedWriter bw = new BufferedWriter(new FileWriter(temp))) {
				bw.write(configString);
				bw.close();
			} catch(IOException ex) {
				ex.printStackTrace();
			}
			// create url for the SDK initializer
			URL ruleFile = new URL(String.format("file://%s", temp.getPath()));
			// set sampling strategy which as been defined in the JSON file above
			builder.withSamplingStrategy(new LocalizedSamplingStrategy(ruleFile));
			// apply that policy on a global level for that Spring boot instance
			AWSXRay.setGlobalRecorder(builder.build());
			
		} catch (Exception e) {
				e.printStackTrace();
		}
  }
}
