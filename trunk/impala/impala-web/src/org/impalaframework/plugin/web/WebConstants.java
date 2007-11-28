package org.impalaframework.plugin.web;

import org.springframework.web.context.WebApplicationContext;

public interface WebConstants {

	public static final String PLUGIN_NAMES_PARAM = "pluginNames";
	
	public static final String IMPALA_FACTORY_PARAM = WebApplicationContext.class.getName() + ".CONTEXT_HOLDER";
	
	public static final String WEBAPP_LOCATION_PARAM = "webappConfigLocation";
	
	public static final String ROOT_WEB_PLUGIN_PARAM = "rootWebPlugin";
	
	/** Default config location for the root context */
	public static final String DEFAULT_CONFIG_LOCATION = "/WEB-INF/applicationContext.xml";
	
	/** Default prefix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_PREFIX = "/WEB-INF/";
	
	/** Default suffix for building a config location for a namespace */
	public static final String DEFAULT_CONFIG_LOCATION_SUFFIX = ".xml";

}
