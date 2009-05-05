/*
 * Copyright 2007-2008 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.impalaframework.web.integration;

import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

/**
 * Implementation of <code>FilterConfig</code> which is designed for use in an internal module filter. Used by 
 * <code>FilterFactoryBean</code>
 * 
 * @see ServletFactoryBean
 * @author Phil Zoio
 */
public class IntegrationFilterConfig extends BaseIntegrationConfig implements FilterConfig {

	public IntegrationFilterConfig(Map<String, String> initParameterMap, ServletContext servletContext, String filterName) {
		super(initParameterMap, servletContext, filterName);
	}
	
	public String getFilterName() {
		return super.getName();
	}

}