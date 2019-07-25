package hello;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.amazonaws.xray.AWSXRay;
import com.amazonaws.xray.proxies.apache.http.HttpClientBuilder;
import com.amazonaws.xray.spring.aop.XRayEnabled;

/**
 * @author raphael Add the @XRayEnabled for enable automatic tracing of all
 *         methods
 */
@Service
@XRayEnabled
public class GsServiceImpl implements GsService {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();

	@Autowired
	DataSource dataSource;

	/**
	 * Fully automated tracing will happen in this method.
	 */
	@Override
	public Greeting getGreetings(String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	/**
	 * Tracing Calls to Downstream HTTP Web Services with the X-Ray SDK When your
	 * application makes calls to microservices or public HTTP APIs, you can use the
	 * X-Ray SDK for Java’s version of HttpClient to instrument those calls and add
	 * the API to the service graph as a downstream service.
	 * 
	 * The X-Ray SDK for Java includes DefaultHttpClient and HttpClientBuilder
	 * classes that can be used in place of the Apache HttpComponents equivalents to
	 * instrument outgoing HTTP calls.
	 */
	@Override
	public String getTracing() {
		// Create instrumentable HTTP client (HttpClientBuilder is from AWS XRay SDK)
		CloseableHttpResponse response = null;
		CloseableHttpClient httpclient = HttpClientBuilder.create().build();

		// Continue using Java SDK defaults
		HttpGet httpGet = new HttpGet("https://www.google.com");

		String resultMessage = new String();
		try {
			response = httpclient.execute(httpGet);

			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();

			try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
				resultMessage = br.lines().collect(Collectors.joining(System.lineSeparator()));
			}

			resultMessage = "tracing works. Msg is : " + resultMessage;
		} catch (Exception ex) {
			ex.printStackTrace();
			resultMessage = ex.getMessage();
		} finally {
			try {
				if (response != null)
					response.close();
			} catch (Exception ex) {
			}
		}

		return resultMessage;
	}

	/**
	 * Instrumenting Calls to a PostgreSQL Database
	 * 
	 * @throws SQLException
	 */
	@Override
	public String getSampleDataFromDatabase() {

		String resultString = new String();
		
		// begin new SQL tracing request
		AWSXRay.beginSubsegment("database-get-questions");
		
		try (Connection c = dataSource.getConnection()) {
			Statement s = c.createStatement();
			ResultSet rs = s.executeQuery("SELECT * from questions");

			resultString = "did't worked";
			if (rs.next()) {
				resultString = "worked res : " + rs.getInt(1);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			resultString = ex.getMessage();
		} 
		
		// end opened tracing request and measure time and params
		AWSXRay.endSubsegment();
		
		return resultString;
	}
}
