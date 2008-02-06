package org.impalaframework.web.loader;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import javax.servlet.ServletContext;

import junit.framework.TestCase;

import org.impalaframework.module.bootstrap.ModuleManagementFactory;
import org.impalaframework.module.builder.SingleStringModuleDefinitionSource;
import org.impalaframework.resolver.LocationConstants;
import org.impalaframework.web.WebConstants;
import org.impalaframework.web.module.WebXmlRootDefinitionBuilder;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

public class ContextLoaderIntegrationTest extends TestCase {

	private ServletContext servletContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		servletContext = createMock(ServletContext.class);
		System.clearProperty(WebConstants.BOOTSTRAP_LOCATIONS_PROPERTY_PARAM);
		System.clearProperty(WebConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM);
		System.clearProperty(WebConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM);
		System.setProperty(LocationConstants.ROOT_PROJECTS_PROPERTY, "impala-core");
	}
	
	public void testWebXmlBasedContextLoader() throws Exception {
		expect(servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM)).andReturn("parentTestContext.xml");
		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("impala-sample-dynamic-plugin1");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFactory.class));
		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		WebXmlBasedContextLoader loader = new WebXmlBasedContextLoader(){
			@Override
			public String[] getBootstrapContextLocations(ServletContext servletContext) {
				String[] locations = new String[] { 
						"META-INF/impala-bootstrap.xml",
						"META-INF/impala-web-bootstrap.xml"};
				return locations;
			}
		};
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}	
	
	
	public void testWebXmlBasedContextLoaderWithListener() throws Exception {
		expect(servletContext.getInitParameter(ContextLoader.CONFIG_LOCATION_PARAM)).andReturn("parentTestContext.xml");
		expect(servletContext.getInitParameter(WebConstants.MODULE_NAMES_PARAM)).andReturn("impala-sample-dynamic-plugin1");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFactory.class));
		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		WebXmlBasedContextLoader loader = new WebXmlBasedContextLoader(){
			@Override
			public String[] getBootstrapContextLocations(ServletContext servletContext) {
				String[] locations = new String[] { 
						"META-INF/impala-bootstrap.xml",
						"META-INF/impala-web-bootstrap.xml",
						"META-INF/impala-web-listener-bootstrap.xml"};
				return locations;
			}
		};
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}	
	
	public void testConfigurableWebXmlBasedContextLoader() throws Exception {
		expect(servletContext.getInitParameter(WebConstants.BOOTSTRAP_LOCATIONS_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/bootstrap_locations.properties");
		expect(servletContext.getInitParameter(WebConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/plugin_locations.properties");
		expect(servletContext.getInitParameter(WebConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM)).andReturn("org/impalaframework/web/module/plugin_locations.properties");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFactory.class));		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(SingleStringModuleDefinitionSource.class));
		
		replay(servletContext);

		ConfigurableWebXmlBasedContextLoader loader = new ConfigurableWebXmlBasedContextLoader();
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}	
	
	public void testExternalXmlBasedContextLoader() throws Exception {
		expect(servletContext.getInitParameter(WebConstants.BOOTSTRAP_MODULES_RESOURCE_PARAM)).andReturn("xmlspec/xmlspec.xml");
		servletContext.setAttribute(eq(WebConstants.IMPALA_FACTORY_ATTRIBUTE), isA(ModuleManagementFactory.class));		
		servletContext.setAttribute(eq(WebConstants.MODULE_DEFINITION_SOURCE_ATTRIBUTE), isA(WebXmlRootDefinitionBuilder.class));
		
		replay(servletContext);

		ExternalXmlBasedImpalaContextLoader loader = new ExternalXmlBasedImpalaContextLoader() {

			@Override
			public String[] getBootstrapContextLocations(ServletContext servletContext) {
				String[] locations = new String[] { 
						"META-INF/impala-bootstrap.xml",
						"META-INF/impala-web-bootstrap.xml"};
				return locations;
			}
			
		};
		WebApplicationContext context = loader.createWebApplicationContext(servletContext, null);
		
		assertNotNull(context);
		assertTrue(context instanceof GenericWebApplicationContext);
		verify(servletContext);
	}
}
