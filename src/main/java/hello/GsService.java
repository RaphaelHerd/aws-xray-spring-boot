package hello;

import hello.Greeting;

public interface GsService {
	public Greeting getGreetings(String name);
	public String getTracing();
	public String getSampleDataFromDatabase();

}
