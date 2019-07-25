package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.amazonaws.xray.spring.aop.XRayEnabled;

@RestController
@XRayEnabled
public class GreetingController {

	@Autowired
	GsService gsService;

	@RequestMapping("/greeting")
	public Greeting greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
		return gsService.getGreetings(name);
	}
	
	@RequestMapping("/tracing")
	public String getTracingInformation() {
		return gsService.getTracing();
	}
	
	@RequestMapping("/database")
	public String getDatabaseInformation() {
		return gsService.getSampleDataFromDatabase();
	}
}