package org.impalaframework.web.integration;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.impalaframework.module.definition.SimpleModuleDefinition;
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.integration.ModuleProxyServlet;
import org.impalaframework.web.integration.ServletFactoryBean;

import static org.easymock.EasyMock.*;

import junit.framework.TestCase;

public class ServletFactoryBeanTest extends TestCase {

	private HttpServletRequest request;
	private ServletContext context;
	private ServletFactoryBean factoryBean;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		request = createMock(HttpServletRequest.class);
		context = createMock(ServletContext.class);
		factoryBean = new ServletFactoryBean();
		factoryBean.setInitParameters(null);
		factoryBean.setServletName("myservlet");
		factoryBean.setServletClass(ModuleProxyServlet.class);
		factoryBean.setServletContext(context);
	}
	
	public void testGetObject() throws Exception {
		
		factoryBean.afterPropertiesSet();
		ModuleProxyServlet servlet = (ModuleProxyServlet) factoryBean.getObject();
		
		expect(request.getServletPath()).andReturn("/somepath/morebits");
		expect(context.getAttribute(WebConstants.SERVLET_MODULE_ATTRIBUTE_PREFIX+"somepath")).andReturn(null);
		
		replay(request);
		replay(context);
		
		servlet.service(request, null);
		
		verify(request);
		verify(context);
	}
	
	public void testNoServletName() throws Exception {
		
		factoryBean.setServletName(null);
		factoryBean.setModuleDefinition(new SimpleModuleDefinition("mymodule"));
		
		factoryBean.afterPropertiesSet();
		ModuleProxyServlet servlet = (ModuleProxyServlet) factoryBean.getObject();
		
		expect(request.getServletPath()).andReturn("/somepath/morebits");
		expect(context.getAttribute(WebConstants.SERVLET_MODULE_ATTRIBUTE_PREFIX+"somepath")).andReturn(null);
		
		replay(request);
		replay(context);
		
		servlet.service(request, null);
		
		verify(request);
		verify(context);
	}
	
	public void testWithPrefix() throws Exception {
		
		factoryBean.setInitParameters(Collections.singletonMap("modulePrefix", "pathprefix-"));
		factoryBean.afterPropertiesSet();
		ModuleProxyServlet servlet = (ModuleProxyServlet) factoryBean.getObject();
		
		expect(request.getServletPath()).andReturn("/somepath/morebits");
		expect(context.getAttribute(WebConstants.SERVLET_MODULE_ATTRIBUTE_PREFIX+"pathprefix-somepath")).andReturn(null);
		
		replay(request);
		replay(context);
		
		servlet.service(request, null);
		
		verify(request);
		verify(context);
	}

}
