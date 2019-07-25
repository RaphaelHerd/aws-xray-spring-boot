package hello;

import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import com.amazonaws.xray.entities.Subsegment;
import com.amazonaws.xray.spring.aop.AbstractXRayInterceptor;

/**
 * @author raphael
 * To activate X-Ray tracing in your application, your code must extend the abstract class AbstractXRayInterceptor by overriding the following methods.
 * 
 * 
 * Your classes must either be annotated with the @XRayEnabled annotation, or implement the XRayTraced interface. 
 * This tells the AOP system to wrap the functions of the affected class for X-Ray instrumentation.
 */
@Aspect
@Component
public class XRayInspector extends AbstractXRayInterceptor {
	/***
	 * This function allows customization of the metadata attached to the current function’s trace. 
	 * By default, the class name of the executing function is recorded in the metadata. You can add more data if you need additional insights.
	 */
	@Override
	protected Map<String, Map<String, Object>> generateMetadata(ProceedingJoinPoint proceedingJoinPoint, Subsegment subsegment) {
		return super.generateMetadata(proceedingJoinPoint, subsegment);
	}

	/**
	 * This function is empty, and should remain so. 
	 * It serves as the host for a pointcut instructing the interceptor about which methods to wrap. 
	 * Define the pointcut by specifying which of the classes that are annotated with @XRayEnabled to trace. 
	 * The following pointcut statement tells the interceptor to wrap all (*) beans annotated with the @XRayEnabled annotation.
	 */
	@Override
	@Pointcut("@within(com.amazonaws.xray.spring.aop.XRayEnabled) && bean(*)")
	public void xrayEnabledClasses() {	}
}